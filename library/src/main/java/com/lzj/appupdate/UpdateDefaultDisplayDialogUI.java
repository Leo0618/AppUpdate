package com.lzj.appupdate;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

/**
 * function:默认显示更新信息的对话框
 *
 * <p></p>
 * Created by lzj on 2016/12/15.
 */

public class UpdateDefaultDisplayDialogUI extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.overridePendingTransition(0, android.R.anim.fade_in);
        final UpdateDataBean data = getIntent().getParcelableExtra("data");
        if (data == null) {
            finish();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("更新提示")
                .setMessage(data.getRemark())
                .setNegativeButton("下次再说", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        if (!TextUtils.isEmpty(data.getApkFileLocalPath())) {
            builder.setPositiveButton("马上安装", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                    UpdateUtil.installApkFile(data.getApkFileLocalPath());
                }
            });
        } else {
            builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                    UpdateDownLoader.getInstance().download(data);
                }
            });
        }
        builder.create().show();
    }


    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, android.R.anim.fade_out);
    }
}
