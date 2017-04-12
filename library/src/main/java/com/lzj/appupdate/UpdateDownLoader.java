package com.lzj.appupdate;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * function:更新应用下载器
 *
 * <p></p>
 * Created by lzj on 2016/12/16.
 */

@SuppressWarnings("ALL")
public class UpdateDownLoader {
    private DownloadManager mDownloadManager;
    private UpdateDataBean mData;
    private long mIdForDownload = -1;
    private AtomicBoolean mIsDowning;
    private IDownloadCallback mIDownloadCallback;

    private static final AtomicReference<UpdateDownLoader> INSTANCE = new AtomicReference<>();

    public static UpdateDownLoader getInstance() {
        for (; ; ) {
            UpdateDownLoader manager = INSTANCE.get();
            if (manager != null) return manager;
            manager = new UpdateDownLoader();
            if (INSTANCE.compareAndSet(null, manager)) return manager;
        }
    }

    private UpdateDownLoader() {
        if (UpdateConfigs.context == null) {
            throw new IllegalArgumentException("请设置上下文参数，建议使用applicationContext， 调用UpdateManager.config()设置");
        }
        mDownloadManager = (DownloadManager) UpdateConfigs.context.getSystemService(Context.DOWNLOAD_SERVICE);
        mIsDowning = new AtomicBoolean(false);
    }

    /** 开启下载 */
    public void download(UpdateDataBean data) {
        this.mData = data;
        if (mData == null) {
            throw new IllegalArgumentException("UpdateDataBean is null.");
        }
        UpdateUtil.log("download : " + mData.toString());
        registerBroadcastReceiver();
        addIntoDowloadTask();
    }

    /** 开启下载 */
    public void download(UpdateDataBean data, IDownloadCallback callback) {
        this.mIDownloadCallback = callback;
        download(data);
    }

    /** 取消下载 */
    public void cancle() {
        if (mIdForDownload == -1) return;
        mDownloadManager.remove(mIdForDownload);
    }

    /**
     * 当前是否正在下载中
     */
    public boolean isDownloading() {
        if (mIsDowning != null) return mIsDowning.get();
        return false;
    }

    /**
     * 当前是否处于强制更新的下载中
     */
    public boolean isForceDownloading() {
        if (mData != null && mData.getIs_forced() == 1 && mIsDowning != null) {
            return mIsDowning.get();
        }
        return false;
    }

    private static final int WHAT_DOWNLOAD_START = 100;
    private static final int WHAT_DOWNLOAD_ING = 101;
    private static final int WHAT_DOWNLOAD_FINISHED = 102;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private android.os.Handler mHandler = new Handler(android.os.Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (mIDownloadCallback == null) return;
            switch (msg.what) {
                case WHAT_DOWNLOAD_START:
                    mIDownloadCallback.onStart();
                    UpdateUtil.log("download start... url=" + mData.getDownload_url());
                    break;
                case WHAT_DOWNLOAD_ING:
                    mIDownloadCallback.onProgress(msg.arg1, msg.arg2);
                    UpdateUtil.log("downloading progress: total=" + msg.arg1 + " , downloaded=" + msg.arg2 + " , " + (msg.arg2 * 100 / msg.arg1));
                    break;
                case WHAT_DOWNLOAD_FINISHED:
                    mIDownloadCallback.onFinished((File) msg.obj);
                    UpdateUtil.log("download finished apkFile=" + (((File) msg.obj).getAbsolutePath()));
                    break;
            }
        }
    };

    private void addIntoDowloadTask() {
        final File apkFile = new File(UpdateUtil.getDownloadApkFilePath(UpdateConfigs.context));
        if (apkFile.exists() && apkFile.length() > 0) apkFile.delete();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mData.getDownload_url()));
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(apkFile));
        request.setTitle(UpdateUtil.getAppName(UpdateConfigs.context));
        request.setMimeType("application/vnd.android.package-archive");
        mIdForDownload = mDownloadManager.enqueue(request);
        if (mIsDowning != null) mIsDowning.getAndSet(true);
        UpdateUtil.log("start a request to download apk file. url=" + mData.getDownload_url());
        if (mIDownloadCallback == null) return;
        Message msg = Message.obtain();
        msg.what = WHAT_DOWNLOAD_START;
        mHandler.sendMessage(msg);
        final DownloadManager.Query query = new DownloadManager.Query();
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                Cursor cursor = mDownloadManager.query(query.setFilterById(mIdForDownload));
                if (cursor != null && cursor.moveToFirst()) {
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        mTimerTask.cancel();
                        mTimer.purge();
                        Message msg = Message.obtain();
                        msg.what = WHAT_DOWNLOAD_FINISHED;
                        msg.obj = apkFile;
                        mHandler.sendMessage(msg);
                        return;
                    }
                    int downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    Message msg = Message.obtain();
                    msg.what = WHAT_DOWNLOAD_ING;
                    msg.arg1 = total;
                    msg.arg2 = downloaded;
                    mHandler.sendMessage(msg);
                }
                cursor.close();
            }
        };
        mTimer.schedule(mTimerTask, 0, 1000);
    }

    private DownloadReceiver mDownloadReceiver;

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        if (mDownloadReceiver == null) {
            mDownloadReceiver = new DownloadReceiver();
        }
        UpdateConfigs.context.registerReceiver(mDownloadReceiver, intentFilter);
        UpdateUtil.log("registerReceiver success.");
    }

    private void unregisterBroadcastReceiver() {
        if (mDownloadReceiver != null) {
            UpdateConfigs.context.unregisterReceiver(mDownloadReceiver);
            mDownloadReceiver = null;
            UpdateUtil.log("unregisterReceiver success.");
        }
    }

    private class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE) ||
                    intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
                if (mIsDowning != null) mIsDowning.getAndSet(false);
                if (mData == null) return;
                UpdateUtil.log("receiver has received, now check md5 is same with server.");
                File downloadedApkFile = new File(UpdateUtil.getDownloadApkFilePath(UpdateConfigs.context));
                String md5Local = UpdateUtil.md5File(downloadedApkFile);
                UpdateUtil.log("downloaded apk file md5 = " + md5Local + " \n server apk file md5 = " + mData.getFile_md5());
                if (!md5Local.equalsIgnoreCase(mData.getFile_md5())) {
                    boolean deleteResult = false;
                    if (downloadedApkFile.exists()) {
                        deleteResult = downloadedApkFile.delete();
                    }
                    UpdateUtil.log("md5 is not fit, delete downloaded apk file = " + deleteResult);
                } else {
                    UpdateUtil.installApkFile(downloadedApkFile.getAbsolutePath());
                }
            }
            unregisterBroadcastReceiver();
        }
    }

    public interface IDownloadCallback {
        void onStart();

        void onProgress(int total, int downloaded);

        void onFinished(File apkFile);
    }
}
