package com.lzj.appupdateexample;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * function:启动页权限检查
 *
 * <p></p>
 * Created by lzj on 2017/5/16.
 */
public class SplashActivity extends AppCompatActivity {
    private SplashPermissionHelper mSplashPermissionHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageView iv = new ImageView(this);
        iv.setBackgroundColor(Color.TRANSPARENT);
        setContentView(iv);
        initPass();
    }

    private void initPass() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            handleAfterPermissions();
        } else {
            mSplashPermissionHelper = new SplashPermissionHelper(this);
            String[] permissions = new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
            mSplashPermissionHelper.run(permissions, new SplashPermissionHelper.IPermissionCallback() {
                @Override
                public void onPassed() {
                    handleAfterPermissions();
                }
            });
        }
    }

    private void handleAfterPermissions() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mSplashPermissionHelper != null) {
            mSplashPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mSplashPermissionHelper != null) {
            mSplashPermissionHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        //can not go back
    }
}
