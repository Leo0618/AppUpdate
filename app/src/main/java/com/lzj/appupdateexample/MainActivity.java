package com.lzj.appupdateexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.leo618.appupdate.UpdateManager;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void checkUpdate(View view) {
        UpdateManager.config(getApplication(), true, true);
        UpdateManager.checkUpdate("http://ppwapp.simuwang.com/Other/getAndroidVersion?", null);
    }
}
