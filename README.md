# AppUpdate
an independent util library for checking app update info and hint message by dialog,download latest version with notification progress.


----------


功能说明：

1. 支持Android版本号9+，不依赖第三方库；
2. 支持更新信息查询、线上新版apk文件下载；
3. 支持默认更新提示对话框，同时支持自定义处理；

app更新接口json数据格式:

	{
	    "app_name": "apk更新demo",
	    "download_url": "http://www.xxx/apk-v2.3.0.apk",
	    "file_md5": "9d4522b59a92742124110a7abf440267",
	    "file_size": "31227937",
	    "is_forced": "0",
	    "msg": "",
	    "package": "com.lzj.appupdate",
	    "remark": "1、全新首页，新视觉 新体验\n2、修复若干bug",
	    "status": 0,
	    "version_id": "102",
	    "version_name": "v2.0.0"
	}


**version_id** : 应用版本号码，必填；

**download_url** : 线上最新版apk文件下载地址，必填；

**file_md5** : 线上最新版apk文件的md5值，必填；

**is_forced** : 是否为强制更新，0：不强制更新，1：强制更新， 默认为0；

app_name : 应用名称，可选；

file_size : 线上最新版apk文件的大小，可选；

version_name : 应用版本名称，可选；

package : 应用包名，可选；

remark : 应用升级提示语，可选；

status : 接口请求状态码，可选；

msg : 接口请求附加消息，可选；



# How to Use #

----------
Step1: Add the dependency

    dependencies {
           compile 'com.lzj.appupdate:library:0.0.1'
    }


Step2: use in code

	UpdateManager.config(getApplication(), true, true);
    UpdateManager.checkUpdate("http://ppwapp.simuwang.com/Other/getAndroidVersion?", null);

需要使用到的权限：


    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>



如果使用默认dialog显示更新信息需要在project/app/AndroidManifest.xml中加入UpdateDefaultDisplayDialogUI：

	  <application >
		...
        <activity
            android:name="com.lzj.appupdate.UpdateDefaultDisplayDialogUI"
            android:configChanges="keyboardHidden|orientation|screenSize"
            android:theme="@style/UpdateDefaultDisplayDialog"/>

    </application>
