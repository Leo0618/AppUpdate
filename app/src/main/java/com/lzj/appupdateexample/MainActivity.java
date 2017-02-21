package com.lzj.appupdateexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.leo618.utils.LogUtil;
import com.leo618.utils.UIUtil;
import com.lzj.appupdate.UpdateDownLoader;
import com.lzj.appupdate.UpdateManager;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void checkUpdate(View view) {
        UpdateManager.config(getApplication(), true, true);
        UpdateManager.checkUpdate("http://ppwapp.simuwang.com/Other/getAndroidVersion?", null);

        UIUtil.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean downloading = UpdateDownLoader.getInstance(MainActivity.this).isDownloading();
                LogUtil.e("leo", "downloading=" + downloading);
                UIUtil.postDelayed(this, 3000);
            }
        }, 1000);
    }
}
