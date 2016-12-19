package com.lzj.appupdate;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * function:
 *
 * <p></p>
 * Created by lzj on 2016/12/13.
 */

@SuppressWarnings("ALL")
public final class UpdateDataBean implements Parcelable {
    private int version_code;
    private String version_name;
    private String download_url;
    private String file_md5;
    private String remark;
    private int is_forced;
    private String apkFileLocalPath;

    UpdateDataBean(int version_code, String version_name, String download_url, String file_md5, String remark, int is_forced, String apkFileLocalPath) {
        this.version_code = version_code;
        this.version_name = version_name;
        this.download_url = download_url;
        this.file_md5 = file_md5;
        this.remark = remark;
        this.is_forced = is_forced;
        this.apkFileLocalPath = apkFileLocalPath;
    }

    public int getVersion_code() {
        return version_code;
    }

    public void setVersion_code(int version_code) {
        this.version_code = version_code;
    }

    public String getVersion_name() {
        return version_name;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getFile_md5() {
        return file_md5;
    }

    public void setFile_md5(String file_md5) {
        this.file_md5 = file_md5;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getIs_forced() {
        return is_forced;
    }

    public void setIs_forced(int is_forced) {
        this.is_forced = is_forced;
    }

    public String getApkFileLocalPath() {
        return apkFileLocalPath;
    }

    public void setApkFileLocalPath(String apkFileLocalPath) {
        this.apkFileLocalPath = apkFileLocalPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.version_code);
        dest.writeString(this.version_name);
        dest.writeString(this.download_url);
        dest.writeString(this.file_md5);
        dest.writeString(this.remark);
        dest.writeInt(this.is_forced);
        dest.writeString(this.apkFileLocalPath);
    }


    private UpdateDataBean(Parcel in) {
        this.version_code = in.readInt();
        this.version_name = in.readString();
        this.download_url = in.readString();
        this.file_md5 = in.readString();
        this.remark = in.readString();
        this.is_forced = in.readInt();
        this.apkFileLocalPath = in.readString();
    }

    public static final Parcelable.Creator<UpdateDataBean> CREATOR = new Parcelable.Creator<UpdateDataBean>() {
        @Override
        public UpdateDataBean createFromParcel(Parcel source) {
            return new UpdateDataBean(source);
        }

        @Override
        public UpdateDataBean[] newArray(int size) {
            return new UpdateDataBean[size];
        }
    };

    @Override
    public String toString() {
        return "UpdateDataBean{" +
                "version_code=" + version_code +
                ", version_name='" + version_name + '\'' +
                ", download_url='" + download_url + '\'' +
                ", file_md5='" + file_md5 + '\'' +
                ", remark='" + remark + '\'' +
                ", is_forced=" + is_forced +
                ", apkFileLocalPath='" + apkFileLocalPath + '\'' +
                '}';
    }
}
