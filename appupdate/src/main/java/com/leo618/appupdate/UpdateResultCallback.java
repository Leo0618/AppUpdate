package com.leo618.appupdate;

import android.support.annotation.NonNull;

/**
 * function:更新检测回调
 *
 * <p></p>
 * Created by lzj on 2016/12/13.
 */

@SuppressWarnings("ALL")
public abstract class UpdateResultCallback {
    public void onStart() {
    }

    public void onError(UpdateException exception) {

    }

    public abstract void onResult(@NonNull UpdateDataBean data);
}
