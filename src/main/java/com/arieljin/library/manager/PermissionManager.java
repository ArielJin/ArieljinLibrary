package com.arieljin.library.manager;

import android.content.pm.PackageManager;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;

/**
 * @time 2018/10/14.
 * @email ariel.jin@tom.com
 */
public class PermissionManager {


    private static SparseArray<OnRequestPermissionsResultListener> onRequestPermissionsResultListenerSparseArray;

    public void checkSelfPermission(@NonNull AppCompatActivity activity, @NonNull String permission, final @IntRange(from = 0) int checkRequestCode, @NonNull final OnPermissionCheckedCallback onPermissionCheckedCallback) throws IllegalAccessException {

        if (onRequestPermissionsResultListenerSparseArray == null)
            throw new IllegalAccessException("you must call onCreate() before this method");
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            onRequestPermissionsResultListenerSparseArray.append(checkRequestCode, new OnRequestPermissionsResultListener() {
                @Override
                public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

                    if (requestCode == checkRequestCode) {

                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                            if (onPermissionCheckedCallback != null)
                                onPermissionCheckedCallback.callbackSucceed();

                        } else {
                            if (onPermissionCheckedCallback != null)
                                onPermissionCheckedCallback.callbackFailed();

                        }


                    }

                }
            });


            ActivityCompat.requestPermissions(activity, new String[]{permission}, checkRequestCode);

        } else {

            onPermissionCheckedCallback.callbackSucceed();

        }


    }

    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, final @IntRange(from = 0) int checkRequestCode) {

        if (onRequestPermissionsResultListenerSparseArray == null)
            return;
        OnRequestPermissionsResultListener listener = onRequestPermissionsResultListenerSparseArray.get(checkRequestCode);
        if (listener != null)
            listener.onRequestPermissionsResult(requestCode, permissions, grantResults);
        else
            new IllegalArgumentException("argument checkRequestCode not right!");


    }

    public static void onDestory() {
        if (onRequestPermissionsResultListenerSparseArray != null)
            onRequestPermissionsResultListenerSparseArray.clear();
        onRequestPermissionsResultListenerSparseArray = null;
    }

    public static void onCreate() {
        onRequestPermissionsResultListenerSparseArray = new SparseArray<>();
    }


    private interface OnRequestPermissionsResultListener {

        void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
    }


    public interface OnPermissionCheckedCallback {

        void callbackSucceed();

        void callbackFailed();

    }

}
