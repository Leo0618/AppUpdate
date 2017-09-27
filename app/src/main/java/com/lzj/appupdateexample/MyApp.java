package com.lzj.appupdateexample;

import android.app.Application;

import com.leo618.utils.AndroidUtilsCore;

/**
 * function:
 *
 * <p></p>
 * Created by lzj on 2017/2/21.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidUtilsCore.install(this);
    }

}
