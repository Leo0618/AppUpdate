package com.lzj.appupdate;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

/**
 * function:更新应用下载器
 *
 * <p></p>
 * Created by lzj on 2016/12/16.
 */

@SuppressWarnings("ALL")
public class UpdateDownLoader {
    private Context mContext;
    private DownloadManager mDownloadManager;
    private UpdateDataBean mData;
    private long mIdForDownload = -1;

    private static final AtomicReference<UpdateDownLoader> INSTANCE = new AtomicReference<>();

    public static UpdateDownLoader getInstance(Context context) {
        for (; ; ) {
            UpdateDownLoader manager = INSTANCE.get();
            if (manager != null) return manager;
            manager = new UpdateDownLoader(context);
            if (INSTANCE.compareAndSet(null, manager)) return manager;
        }
    }

    public UpdateDownLoader(Context context) {
        mContext = context;
        mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
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

    /** 取消下载 */
    public void cancle() {
        if (mIdForDownload == -1) return;
        mDownloadManager.remove(mIdForDownload);
    }

    private void addIntoDowloadTask() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mData.getDownload_url()));
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(new File(UpdateUtil.getDownloadApkFilePath(mContext))));
        request.setTitle(UpdateUtil.getAppName(mContext));
        mIdForDownload = mDownloadManager.enqueue(request);
        UpdateUtil.log("start a request to download apk file. url=" + mData.getDownload_url());
    }

    private DownloadReceiver mDownloadReceiver;

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        if (mDownloadReceiver == null) {
            mDownloadReceiver = new DownloadReceiver();
        }
        mContext.registerReceiver(mDownloadReceiver, intentFilter);
        UpdateUtil.log("registerReceiver success.");
    }

    private void unregisterBroadcastReceiver() {
        if (mDownloadReceiver != null) {
            mContext.unregisterReceiver(mDownloadReceiver);
            mDownloadReceiver = null;
            UpdateUtil.log("unregisterReceiver success.");
        }
    }

    private class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE) ||
                    intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
                if (mData == null) return;
                UpdateUtil.log("receiver has received, now check md5 is same with server.");
                File downloadedApkFile = new File(UpdateUtil.getDownloadApkFilePath(mContext));
                String md5Local = UpdateUtil.md5File(downloadedApkFile);
                UpdateUtil.log("downloaded apk file md5 = " + md5Local + " \n server apk file md5 = " + mData.getFile_md5());
                if (!md5Local.equalsIgnoreCase(mData.getFile_md5())) {
                    boolean deleteResult = false;
                    if (downloadedApkFile.exists()) {
                        deleteResult = downloadedApkFile.delete();
                    }
                    UpdateUtil.log("md5 is not fit, delete downloaded apk file = " + deleteResult);
                } else {
                    UpdateUtil.installApkFile(context, downloadedApkFile.getAbsolutePath());
                }
            }
            unregisterBroadcastReceiver();
        }
    }
}
