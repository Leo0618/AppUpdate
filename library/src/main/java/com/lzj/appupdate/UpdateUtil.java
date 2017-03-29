package com.lzj.appupdate;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * function:
 *
 * <p></p>
 * Created by lzj on 2016/12/13.
 */

@SuppressWarnings("ALL")
public final class UpdateUtil {

    public static String readStream(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            is.close();
            return baos.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void log(String msg) {
        if (!UpdateConfigs.DEBUG) return;
        Log.d("lzj/Update", msg);
    }

    public static int getPackageVersionCode(Context context) {
        if (context == null) return -1;
        int verCode = -1;
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            if (null != pi.versionName) {
                verCode = pi.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verCode;
    }

    /** 获取默认的外部存储目录 */
    public static File getExternalStorageDirectory() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory();
        }
        return null;
    }

    /**
     * 获取APP根目录
     *
     * @return File("/mnt/storage0/Android/data/packagename") if the phone has SD card,else return File("data/data/packagename/cache")
     */
    public static File getAppRootDir(Context context) {
        File externalCacheDir = context.getExternalCacheDir();
        if (null == getExternalStorageDirectory() || externalCacheDir == null) {
            return context.getCacheDir();
        } else {
            File externalAppRootDir = externalCacheDir.getParentFile();
            if (!externalAppRootDir.exists()) {
                externalAppRootDir.mkdirs();
            }
            return externalAppRootDir;
        }
    }

    /**
     * 获取APP根目录路径
     *
     * @return path("/mnt/storage0/Android/data/packagename/") if the phone has SD card,else return path("data/data/packagename/")
     */
    public static String getAppRootDirPath(Context context) {
        String path = getAppRootDir(context).getAbsolutePath();
        if (!TextUtils.isEmpty(path) && !path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        return path;
    }

    /**
     * 获取应用名称
     *
     * @param context 应用的上下文
     */
    public static String getAppName(Context context) {
        PackageManager pm = context.getPackageManager();
        String name = "";
        try {
            name = pm.getApplicationLabel(pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public static String getDownloadApkFilePath(Context context) {
        return getAppRootDirPath(context) + getAppName(context) + ".apk";
    }

    public static String md5File(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            byte[] result = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                String str = Integer.toHexString(b & 0xff);
                if (str.length() == 1) {
                    sb.append("0");
                }
                sb.append(str);
            }
            fis.close();
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 安装apk文件
     *
     * @param apkFilePath apk文件路径
     */
    public static void installApkFile(String apkFilePath) {
        if (UpdateConfigs.context == null) {
            throw new IllegalArgumentException("请设置上下文参数，建议使用applicationContext， 调用UpdateManager.config()设置");
        }
        log("launch install app UI");
        Intent intentForInstall = new Intent();
        intentForInstall.setAction(Intent.ACTION_VIEW);
        intentForInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        File apkFile = new File(apkFilePath);
        Uri apkUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apkUri = FileProvider.getUriForFile(UpdateConfigs.context, UpdateConfigs.context.getPackageName() + ".fileprovider", apkFile);
            intentForInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            apkUri = Uri.fromFile(apkFile);
        }
        intentForInstall.setDataAndType(apkUri, "application/vnd.android.package-archive");
        UpdateConfigs.context.startActivity(intentForInstall);
    }
}
