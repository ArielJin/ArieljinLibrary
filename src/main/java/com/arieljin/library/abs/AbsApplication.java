package com.arieljin.library.abs;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;


public abstract class AbsApplication extends Application {
    protected static AbsApplication instance;

    public static String VERSION = "";
    public static int VERSION_CODE = 1;


    public static String PROCESS_NAME;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        setVersionInfo();
        PROCESS_NAME = getCurProcessName(this);

    }


    public static AbsApplication getInstance() {
        return instance;
    }

    private void setVersionInfo() {
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            VERSION = info.versionName;
            VERSION_CODE = info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }

    public String getAppChannelMetaDataForTask() {
        try {
            String appChannelMetaDataKey = getAppChannelMetaDataKey();
            if (TextUtils.isEmpty(appChannelMetaDataKey))
                return "";
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo != null) {
                Bundle bundle = applicationInfo.metaData;
                if (bundle != null) {
                    Object applicationMeta = applicationInfo.metaData.get(getAppChannelMetaDataKey());
                    if (applicationMeta != null)
                        return applicationMeta.toString();
                }

            }
        } catch (NameNotFoundException e) {
            Log.e(AbsApplication.class.getName(), "can not found the meta in the application attributes:" + getAppChannelMetaDataKey());
        }
        return "";
    }

    public static boolean isOnMainProcess() {
        return PROCESS_NAME.equals("com.bingdian.kaqu");
    }

    protected abstract String getAppChannelMetaDataKey();

    public abstract String getDB_NAME();

}