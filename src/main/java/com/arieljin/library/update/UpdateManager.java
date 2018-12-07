package com.arieljin.library.update;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.arieljin.library.manager.PermissionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @time 2018/12/6.
 * @email ariel.jin@tom.com
 */
public class UpdateManager {

    private Context context;
    private Handler handler;
    private ProgressDialog progressDialog;
    private String downloadPath;

    //    private String cachePath;
    private OnAppCheckUploadListener onAppCheckUploadListener;


    public void checkServerAppVersion(final UpdateInfo updateInfo) {

        if (!(updateInfo != null && updateInfo.canCheckUp())) {

            if (onAppCheckUploadListener != null)
                onAppCheckUploadListener.updateErroe(true, "未获取到新版本信息和下载地址！");
            return;
        }
//        String serResponse = "";

        float versionCode = 0;

        try {
            versionCode = Float.valueOf(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
        } catch (Exception e) {
            e.printStackTrace();
            if (onAppCheckUploadListener != null)
                onAppCheckUploadListener.updateErroe(true, "获取到异常版本号！");
            return;
        }

        if (onAppCheckUploadListener != null && onAppCheckUploadListener.needUpdate(updateInfo.getAppVersion(), versionCode)) {

            upload(updateInfo.getAppName(), updateInfo.getAppVersion(), updateInfo.getDescription(), updateInfo.getDownloadUrl());


        }


    }


    public UpdateManager(final Context context, final OnAppCheckUploadListener onAppCheckUploadListener) {
        this.context = context;
        this.onAppCheckUploadListener = onAppCheckUploadListener;
        this.handler = new Handler(context.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        apkInstall();
                        break;
                    case 2:
                        showProgress((Integer) msg.obj);
                        break;
                    case 3:
                        if (onAppCheckUploadListener != null)
                            onAppCheckUploadListener.updateErroe(false, "您的网络可能有问题,请检查网络配置");
                        break;
                }
            }
        };
    }

    private void upload(String appName, float appVersion, String description, final String downloadUrl) {
//        initLocalCachePath(appName, appVersion);
//        appName + "_" + String.valueOf(appVersion)
        showUploadDialog(description, downloadUrl, appName, appVersion);
    }

    private void showUploadDialog(String description, final String downloadUrl, final String appName, final float appVersion) {

        new AlertDialog.Builder(context).setTitle("检测到新版本！").setMessage(!TextUtils.isEmpty(description) ? description : "暂未获取到更新内容！").setNegativeButton("下次再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        }).setPositiveButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (onAppCheckUploadListener != null) {

                    if (!hasSdcard()) {
                        onAppCheckUploadListener.updateErroe(false, "没有外部存储，请到应用市场或者官网自行更新！");
                        return;
                    }

                    try {
                        PermissionManager.onCreate();
                        onAppCheckUploadListener.checkSelfPermission(new PermissionManager.OnPermissionCheckedCallback() {
                            @Override
                            public void callbackSucceed() {

                                downloadPath = Environment.getExternalStorageDirectory() + File.separator + appName + File.separator + appName + "_" + String.valueOf(appVersion) + ".apk";
                                new WeakReference<>(new DownloadThread(handler, downloadUrl, appName, downloadPath)).get().start();
                                PermissionManager.onDestory();
                            }

                            @Override
                            public void callbackFailed() {
                                onAppCheckUploadListener.updateErroe(false, "请开启存储权限！");
                                PermissionManager.onDestory();

                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        onAppCheckUploadListener.updateErroe(false, "获取存储权限异常！");
                        PermissionManager.onDestory();
                    }
                }


            }
        }).create().show();
    }

//    private void initLocalCachePath(String appName, String appVersion) {
//
//        cachePath = context.getFilesDir()
//    }


    private void showProgress(int value) {

        if (value == 100) {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            apkInstall();
            return;
        }
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("正在下载...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
        progressDialog.setProgress(value);
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private synchronized void apkInstall() {

        chmod("777", downloadPath);
        Uri uri = Uri.parse("file://" + downloadPath);
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(installIntent);
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void chmod(String permission, String path) {
        try {
            String command = "chmod " + permission + " " + path;
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static final class DownloadThread extends Thread {

        private String urlPath, downloadPath, appName;
        private Handler handler;

        public DownloadThread(Handler handler, String urlPath, String appName, String downloadPath) {
            this.urlPath = urlPath;
            this.handler = handler;
            this.downloadPath = downloadPath;
            this.appName = appName;
        }

        @Override
        public void run() {
            super.run();
            Process.setThreadPriority(10);


            int connectTimeout = 30 * 1000;
            int readTimeout = 30 * 1000;
            int currentDownload = 0;

            HttpURLConnection conn = null;
            FileOutputStream fos = null;
            InputStream is = null;


            try {

                if (conn == null) {
                    URL url = new URL(urlPath);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(connectTimeout);
                    conn.setReadTimeout(readTimeout);
                    conn.setDoInput(true);

                    conn.connect();
                }
                is = conn.getInputStream();
                // 文件大小
                Integer fileSize = conn.getContentLength();
                if (fileSize.intValue() <= 0) {
                    throw new RuntimeException("未能获知文件大小");
                }

                File file = new File(downloadPath);
                if (file.exists() && file.length() == fileSize) {
                    handler.sendEmptyMessage(1);
                    return;
//				file.delete();
                } else {

                    File oldFile = new File(Environment.getExternalStorageDirectory(), appName);
                    if (oldFile.exists()) {
                        File[] oldFiles = oldFile.listFiles();
                        for (int i = 0; i < oldFiles.length; i++) {
                            if (oldFiles[i].getAbsolutePath().endsWith(".apk")) {
                                oldFiles[i].delete();
                            }
                        }
                    } else {
//                        file.mkdirs();
                        oldFile.mkdirs();
                    }
                }

//                file.createNewFile();

                fos = new FileOutputStream(file);

                byte[] temp = new byte[2048 * 1024];
                int i = 0;
                float oldPercent = 0;
                while ((i = is.read(temp)) > 0) {
                    fos.write(temp, 0, i);
                    currentDownload += i;
                    float percent = (Float.valueOf(currentDownload) / Float.valueOf(fileSize)) * 100.0f;

                    if (percent - oldPercent >= 1 && percent < 100) {
                        oldPercent = percent;
                        handler.obtainMessage(2, (int) percent).sendToTarget();
                    }


                    Log.e(DownloadThread.class.getName(), "download apk percent : " + percent);

                }
                handler.obtainMessage(2, 100).sendToTarget();
                // download fin
                Log.e(DownloadThread.class.getName(), "download apk percent : 下载完成");
                handler.sendEmptyMessage(1);

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(UpdateManager.class.getName(), "-------------- " + e.toString());
                handler.sendEmptyMessage(3);
            } finally {
                try {
                    if (null != fos) {
                        fos.close();
                    }
                    if (null != is) {
                        is.close();
                    }
                } catch (IOException e) {
                    handler.sendEmptyMessage(3);
                    e.printStackTrace();
                }
            }

        }
    }

    private boolean hasSdcard() {

        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

    }


}
