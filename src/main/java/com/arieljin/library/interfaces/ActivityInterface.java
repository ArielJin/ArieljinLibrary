package com.arieljin.library.interfaces;

import android.app.Activity;

import java.lang.ref.WeakReference;

public interface ActivityInterface {

    void setHasFinishAnimation(boolean hasFinishAnimation);

    void onFinishAnimation(boolean isCreate);

    public static class MyThread extends Thread {
        private WeakReference<ActivityInterface> weakReference;
        private boolean isCreate;

        public MyThread(ActivityInterface activity, boolean isCreate) {
            super();
            this.weakReference = new WeakReference<ActivityInterface>(activity);
            this.isCreate = isCreate;
        }

        @Override
        public void run() {
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            final ActivityInterface activityInterface = weakReference.get();
            if (activityInterface != null) {
                activityInterface.setHasFinishAnimation(true);
                ((Activity) activityInterface).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        activityInterface.onFinishAnimation(isCreate);
                    }
                });
            }
        }
    }








}
