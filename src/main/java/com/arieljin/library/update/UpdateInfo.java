package com.arieljin.library.update;

import android.text.TextUtils;

import com.arieljin.library.abs.AbsModel;

import org.json.JSONObject;

public class UpdateInfo extends AbsModel {

    private String appName;

    private String appVersion;

    private String description;

    private String downloadUrl;

    private boolean isForceUpdate;

    public UpdateInfo(String appName, String appVersion, String description, String downloadUrl, boolean isForceUpdate) {
        this.appName = appName;
        this.appVersion = appVersion;
        this.description = description;
        this.downloadUrl = downloadUrl;
        this.isForceUpdate = isForceUpdate;
    }

    public UpdateInfo() {
    }

    @Override
    protected void parse(JSONObject jsonObject) {
        super.parse(jsonObject);
    }

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getDescription() {
        return description;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public boolean canCheckUp() {
        return !TextUtils.isEmpty(appVersion) && !TextUtils.isEmpty(downloadUrl);
    }

    public boolean isForceUpdate() {
        return isForceUpdate;
    }
}
