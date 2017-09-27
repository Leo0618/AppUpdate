package com.lzj.appupdateexample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.leo618.appupdate.UpdateDataBean;
import com.leo618.appupdate.UpdateException;
import com.leo618.appupdate.UpdateManager;
import com.leo618.appupdate.UpdateResultCallback;
import com.leo618.appupdate.UpdateUtil;
import com.leo618.utils.AndroidUtilsCore;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void checkUpdate(View view) {
        UpdateManager.config(getApplication(), true, true);
        UpdateManager.checkUpdate("http://ppwapp.simuwang.com/Other/getAndroidVersion?", null);
    }

    //for issue 1
    public void checkUpdateForToast(View view) {
        //Step1: disable default dialog for update info show
        UpdateManager.config(getApplication(), true, false);

        //Step2: checkUpdate, we handle check result by ourself
        UpdateManager.checkUpdate("http://ppwapp.simuwang.com/Other/getAndroidVersion?", new UpdateResultCallback() {
            @Override
            public void onStart() {
                Log.d(TAG, "checkUpdate onStart.");
            }

            @Override
            public void onError(UpdateException exception) {
                //error. for friendly,we can hint toast message for nothing to update
                toastNothingUpdate();
            }

            @Override
            public void onResult(@NonNull UpdateDataBean data) {
                Log.d(TAG, "checkUpdate onResult. data=" + data.toString());

                int currVerCode = UpdateUtil.getPackageVersionCode(AndroidUtilsCore.getContext());

                //for test, we force to set nothing to update.
                data.setVersion_code(currVerCode);

                // current installed app versioncode is latest,no need update,we hint toast
                if (currVerCode == -1 || currVerCode >= data.getVersion_code()) {
                    toastNothingUpdate();
                }
                // there is no new latest version available to download.
                // if data.getApkFileLocalPath() is not empty,the downloaded
                // apk file exists in local filesystem,you can install right now
                // otherwise,you can use UpdateDownLoader to download apk.
                else {
                    Log.d(TAG, "There is a new latest version available to download.");
                    //show dialog for user to choose update or not.
                }
            }
        });
    }

    private void toastNothingUpdate() {
        Toast.makeText(this, "Currently no new latest version available to download", Toast.LENGTH_SHORT).show();
    }
}
