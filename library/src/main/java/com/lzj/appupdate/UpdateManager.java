package com.lzj.appupdate;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONObject;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * function:更新管理器
 *
 * <p></p>
 * Created by lzj on 2016/12/13.
 */

public class UpdateManager {
    private static final AtomicReference<UpdateManager> INSTANCE = new AtomicReference<>();
    private Handler uiHandler;
    private static String urlForCheck;
    private static UpdateResultCallback updateResultCallback;
    private static AtomicBoolean mRequesting = new AtomicBoolean(false);

    private static UpdateManager getInstance() {
        for (; ; ) {
            UpdateManager manager = INSTANCE.get();
            if (manager != null) return manager;
            manager = new UpdateManager();
            if (INSTANCE.compareAndSet(null, manager)) return manager;
        }
    }

    private UpdateManager() {
        if (UpdateConfigs.context == null) {
            throw new IllegalArgumentException("must set UpdateConfigs Context, Application Context is better");
        }
        uiHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 配置必要的信息
     *
     * @param context                 调用者应用的上下文，建议使用 Application
     * @param debug                   是否开启调试信息打印,默认开启
     * @param useDefaultDialogDisplay 是否使用默认的更新信息显示对话框,默认开启
     */
    public static void config(Context context, boolean debug, boolean useDefaultDialogDisplay) {
        UpdateConfigs.context = context;
        UpdateConfigs.DEBUG = debug;
        UpdateConfigs.useDefaultDialogDisplay = useDefaultDialogDisplay;
    }

    /**
     * 查询更新信息
     *
     * @param url      查询接口
     * @param callback 回调，调用者可以自定义处理,使用默认时可传入null
     */
    public static void checkUpdate(String url, UpdateResultCallback callback) {
        if (TextUtils.isEmpty(url)) return;
        if (mRequesting.get()) return;
        mRequesting.getAndSet(true);

        urlForCheck = url;
        updateResultCallback = callback;

        getInstance().checkUpdateInternal();
    }

    private void checkUpdateInternal() {
        if (updateResultCallback != null) {
            updateResultCallback.onStart();
            UpdateUtil.log("onStart.");
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    deleteLocalExpiredFile();
                    URL url = new URL(urlForCheck);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        String result = UpdateUtil.readStream(conn.getInputStream());
                        UpdateUtil.log("result: " + result);
                        if (TextUtils.isEmpty(result)) {
                            sendErrorToUI(new UpdateException("result is null.", UpdateConst.STATE_RESULT_NULL));
                        } else {
                            JSONObject jsonObject = new JSONObject(result);
                            int status = jsonObject.getInt("status");
                            String msg = jsonObject.getString("msg");
                            if (status != 0) {
                                sendErrorToUI(new UpdateException("server response result status param is not ok. msg:" + msg, UpdateConst.STATE_RESULT_SERVER_RESP_ERROR));
                            } else {
                                int version_id = jsonObject.getInt("version_id");
                                String version_name = jsonObject.getString("version_name");
                                String download_url = jsonObject.getString("download_url");
                                String file_md5 = jsonObject.getString("file_md5");
                                String remark = jsonObject.getString("remark");
                                int is_forced = jsonObject.getInt("is_forced");

                                String apkLocalExistFilePath = null;
                                File apkLocalFile = new File(UpdateUtil.getDownloadApkFilePath(UpdateConfigs.context));
                                if (apkLocalFile.exists() && apkLocalFile.length() > 0) {
                                    PackageInfo packageinfo = UpdateConfigs.context.getPackageManager().getPackageArchiveInfo(apkLocalFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
                                    if (packageinfo != null && packageinfo.versionCode > UpdateUtil.getPackageVersionCode(UpdateConfigs.context) && packageinfo.versionCode >= version_id) {
                                        apkLocalExistFilePath = apkLocalFile.getAbsolutePath();
                                        version_id = packageinfo.versionCode;
                                    }
                                }
                                final UpdateDataBean data = new UpdateDataBean(version_id, version_name, download_url, file_md5, remark, is_forced, apkLocalExistFilePath);
                                sendSuccessToUI(data);
                                sendShowDialogToUI(data);
                            }
                        }
                    }
                } catch (Exception e) {
                    sendErrorToUI(new UpdateException(e.getMessage(), UpdateConst.STATE_RESULT_ERROR));
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteLocalExpiredFile() {
        File apkLocalFile = new File(UpdateUtil.getDownloadApkFilePath(UpdateConfigs.context));
        if (apkLocalFile.exists() && apkLocalFile.length() > 0) {
            PackageInfo packageinfo = UpdateConfigs.context.getPackageManager().getPackageArchiveInfo(apkLocalFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
            if (packageinfo != null && packageinfo.versionCode <= UpdateUtil.getPackageVersionCode(UpdateConfigs.context)) {
                apkLocalFile.delete();
            }
        }
    }

    private void sendErrorToUI(final UpdateException exception) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (updateResultCallback != null) {
                    updateResultCallback.onError(exception);
                }
                UpdateUtil.log("onError. ex:" + (exception == null ? "null" : exception.toString()));
                mRequesting.getAndSet(false);
            }
        });
    }

    private void sendSuccessToUI(final UpdateDataBean data) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (updateResultCallback != null) {
                    updateResultCallback.onResult(data);
                }
                UpdateUtil.log("onResult. UpdateData: " + data.toString());
                mRequesting.getAndSet(false);
            }
        });
    }

    private void sendShowDialogToUI(final UpdateDataBean data) {
        if (!UpdateConfigs.useDefaultDialogDisplay) return;
        int currVerCode = UpdateUtil.getPackageVersionCode(UpdateConfigs.context);
        if (currVerCode == -1 || currVerCode >= data.getVersion_code()) return;
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(UpdateConfigs.context, UpdateDefaultDisplayDialogUI.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("data", data);
                UpdateConfigs.context.startActivity(intent);
                UpdateUtil.log("launch default dialog show update info.");
            }
        });
    }
}
