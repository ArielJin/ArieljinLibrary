package com.arieljin.library.abs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.arieljin.library.R;
import com.arieljin.library.listener.OnTaskCompleteListener;
import com.arieljin.library.manager.HttpManger;
import com.arieljin.library.manager.ThreadPoolManager;
import com.arieljin.library.task.RefreshBaseTask;
import com.arieljin.library.utils.DipUtil;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

//import org.codehaus.jackson.map.ObjectMapper;


public abstract class AbsTask<T extends Serializable> implements Runnable {
    public AbsRequest request;
    protected MyThread<T> thread;
    private volatile Set<OnTaskCompleteListener<T>> /*onTaskPostCompleteListeners, */onTaskCompleteListeners;
    protected WeakReference<Context> weakReference;
//    protected ObjectMapper mObjectMapper = null;

    private int progressY;
//    public int circleProgressY = 200;

    private Dialog dialog;
    private ProgressBar progressBar;

//    protected Dialog circleDialog;
//    protected CircleProgressBar circleProgressBar;

    public boolean needUploadFile, cancelable = true, needLast, needOnlyLast, needToast, /*needCircle = true, */
            needRestart, isSending, needLastOnce, isDebug = false;
    private int delay = 1, progress;
    public int method_type = HttpManger.POST;
//    public String loadingText = "加载中";
//    public ShowDialogMothod showDialogMothod = ShowDialogMothod.CIRCLE_DIALOG;

//    private enum ShowDialogMothod{
//        TOAST_DIALOG,CIRCLE_DIALOG,ALL_DIALOG,NULL_DIALOG;
//    }

    protected HashMap<String, String> headers;

    protected abstract String getApiMethodName();

    protected abstract T parseJson(JSONObject json) throws Throwable;

    public AbsTask(Context context, AbsRequest request) {
        this(context, request, null);
    }

    public AbsTask(Context context, AbsRequest request, OnTaskCompleteListener<T> completeListener) {
        super();
        this.weakReference = new WeakReference<>(context);
        this.request = request;
        this.headers = addHeaders();
        addListener(completeListener);
        init();
    }

    public AbsTask(Context context, AbsRequest request, boolean needToast, /*boolean needCircle, */OnTaskCompleteListener<T> completeListener) {
        super();
        this.weakReference = new WeakReference<>(context);
//        this.mObjectMapper = new ObjectMapper();
        this.request = request;
        this.headers = addHeaders();
        this.needToast = needToast;
//        this.needCircle = needCircle;
        addListener(completeListener);
        init();
    }

    public void addListener(OnTaskCompleteListener<T> completeListener) {
        if (completeListener != null) {
            if (onTaskCompleteListeners == null) {
                onTaskCompleteListeners = new HashSet<>();
            }
            onTaskCompleteListeners.add(completeListener);
        }
    }


    public void setContext(Context context) {
        this.weakReference = new WeakReference<>(context);
    }

    protected void init() {

    }

    protected abstract T parseCompleteJson(JSONObject jsonObject);

    protected T doInBackground(MyThread<T> thread) {
        Context context = weakReference.get();
        if (!thread.isCancelled && context != null && !isActivityFinishing(context)) {
            JSONObject json = null;
            try {
                json = doMainInBackground();

                if (json != null && json.length() > 0) {

                    return parseCompleteJson(json);
                }


            } catch (Throwable throwable) {
                throwable.printStackTrace();
                if (isDebug)
                    Log.e(getClass().getName(), "\n" + "response:" + (request != null ? request.getBody(weakReference.get()).toString() : "null") + "\n");
                if (needLast && needLastOnce) {
                    JSONObject lastJson = null;
                    try {
                        lastJson = loadLastJson();
                        if (lastJson != null) {
                            return parseJson(lastJson);
                        } else {
                            failed(!TextUtils.isEmpty(throwable.getMessage())?throwable.getMessage() + ",未获取到缓存数据":"网络异常, 未获取到缓存数据");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        failed(!TextUtils.isEmpty(throwable.getMessage())?throwable.getMessage() + ",读取缓存数据异常":"网络异常, 读取缓存数据异常");
                    } catch (Throwable throwable1) {
                        throwable1.printStackTrace();
                        failed(!TextUtils.isEmpty(throwable.getMessage())?throwable.getMessage() + ",解析缓存数据异常":"网络异常, 解析缓存数据异常");
                    }

                } else {
                    failed(!TextUtils.isEmpty(throwable.getMessage())?throwable.getMessage():"网络异常");
                }
            }

        }
        return null;
    }

    protected JSONObject doMainInBackground() throws Throwable {

        String url = getApiMethodName();

//        Log.i("ArielJin", url);

        HttpResponse response;
        // if (request != null) {
        // Log.i("apis", request.getBody(context.get()).toString());
        // }

        if (needUploadFile && request != null) {
            response = HttpManger.getResponse(hashCode(), url, headers, request.getMultipartEntity(weakReference.get(), this));
            request.finish();
        } else {
            response = HttpManger.getResponse(hashCode(), url, headers, request == null ? null : request.getBody(weakReference.get()), method_type);
        }

        if (thread.isCancelled || response == null) {
            return null;
        }

        String s = EntityUtils.toString(response.getEntity(), "UTF-8").trim();

        if (isDebug)
            Log.e(getClass().getName(), "   请求成功：" + "\n" + "url:" + url + "\n" + "response:" + request.getBody(weakReference.get()).toString() + "\n" + "header:" + (headers != null?headers.toString():"null") + "\n" + "result:" + s);

        return new JSONObject(s);
    }

    protected static boolean isActivityFinishing(Context context) {
        return context instanceof Activity && ((Activity) context).isFinishing();
    }

    protected boolean isRefreshBaseTask() {
        return this instanceof RefreshBaseTask;
    }

    protected abstract HashMap<String, String> addHeaders();

    protected void initDialog() {
        if (dialog == null && weakReference.get() != null) {
            Context context = weakReference.get();

            dialog = new Dialog(context, R.style.transparent_dialog);

            progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.progressbar_task));
            progressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, DipUtil.getIntDip(3)));
            progressBar.setMax(100);

            dialog.setContentView(progressBar);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(cancelable);

            if (cancelable) {
                dialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        thread.cancel(true);
                    }
                });
            }

            Window dialogWindow = dialog.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.gravity = Gravity.TOP;
            lp.y = progressY;
            lp.width = LayoutParams.MATCH_PARENT;
            lp.height = DipUtil.getIntDip(3);
            dialogWindow.setAttributes(lp);
        }
    }


    public T get() {
        if (thread != null) {
            thread.cancel();
        }

        isSending = true;

        try {
            T t = ThreadPoolManager.httpSubmit(thread = new MyThread<T>(this)).get();
            isSending = false;
            return t;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void start() {
        start(false, false);
    }

    public void start(boolean isLoadMore) {
        start(isLoadMore, false);
    }

    public void start(final boolean isLoadMore, final boolean isRestart) {
        isSending = true;

        new Thread() {

            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                Context context = weakReference.get();
                if (context != null) {
                    if (thread == null) {
                        thread = new MyThread<T>(AbsTask.this);
                        if (needLast && !isLoadMore && !needLastOnce) {
                            T result = loadLast();
                            if (result != null) {
                                completed(result, isLoadMore, true);
                                if (needOnlyLast) {
                                    isSending = true;
                                    return;
                                }
                            }
                        }
                    } else {
                        thread.cancel();
                        thread = new MyThread<T>(AbsTask.this);
                        thread.isRestart = isRestart;
                    }

                    thread.isLoadMore = isLoadMore;

                    if (!HttpManger.isNetworkAvailable() && (!needLast || needLastOnce)) {

//                        if(needToast || needCircle) {
//                            ToastUtil.showErrorToast("网络异常");
//                        }

                        if (onTaskCompleteListeners != null) {
                            if (context instanceof Activity && isCanCallbackToUi()) {
                                ((Activity) context).runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        for (OnTaskCompleteListener<T> onTaskCompleteListener : onTaskCompleteListeners) {
//                                            onTaskCompleteListener.onTaskFailed("网络异常");
                                            if (!isLoadMore && needLastOnce) {
                                                T result = loadLast();
                                                if (result != null) {
                                                    completed(result, isLoadMore, true);
                                                    if (needOnlyLast) {
                                                        isSending = true;
                                                        return;
                                                    }
                                                } else {
                                                    onTaskCompleteListener.onTaskFailed("网络异常");
                                                }
                                            } else {
                                                onTaskCompleteListener.onTaskFailed("网络异常");
                                            }
                                        }
                                    }
                                });
                            }
                        }
                        return;
                    }

                    if ((needToast ||/*needCircle*/ isRefreshBaseTask()) && !thread.isRestart && !isLoadMore) {
                        if (context instanceof Activity && !((Activity) context).isFinishing()) {
                            ((Activity) context).runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    showDialog();
                                }
                            });
                        }
//                        else {
//                            MainHandlerUtil.post(new Runnable() {
//
//                                @Override
//                                public void run() {
//                                    showDialog();
//                                }
//                            });
//                        }
                    }

                    ThreadPoolManager.httpExecute(thread);
                }
            }
        }.start();
    }

    private void showDialog() {
        initTaskDialog();
        if (isShowToastDialog() || /*isShowCircleDialog()*/ isRefreshBaseTask()) {
            Context context = weakReference.get();
            if (context != null && context instanceof Activity && !((Activity) context).isFinishing()) {
                try {
                    if (isShowToastDialog()) {
                        dialog.show();
                        progressBar.setProgress(progress = 0);
                        setProgress();
                    }
                    if (isRefreshBaseTask()) {
//                        circleDialog.show();
                        ((RefreshBaseTask<T>) this).onTaskStart();

                    }
                } catch (Throwable e) {

                }

            }
        }
    }

    private boolean isDismissToastDialog() {
        if (needToast && dialog != null && dialog.isShowing()) {
            return true;
        }
        return false;
    }


    private boolean isShowToastDialog() {
        if (needToast && dialog != null && !dialog.isShowing()) {
            return true;
        }
        return false;
    }


    private void initTaskDialog() {
        if (needToast && dialog == null) {
            initDialog();
        }

    }

    private void setProgress() {
        new Thread() {

            @Override
            public void run() {
                while (dialog.isShowing() && progress < 90) {
                    progress += 3;

                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Activity activity = (Activity) weakReference.get();
                    if (activity != null && !activity.isFinishing()) {
                        activity.runOnUiThread(AbsTask.this);
                    }
                }
            }
        }.start();
    }

    @Override
    public void run() {
        progressBar.setProgress(progress);
    }

    protected T loadLast() {
        if (weakReference.get() != null) {
            String s = loadLastString();
            if (!TextUtils.isEmpty(s)) {
                try {
                    return parseJson(new JSONObject(s));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
        return null;
    }

    private JSONObject loadLastJson() throws JSONException {
        if (weakReference.get() != null) {
            String s = loadLastString();
            if (!TextUtils.isEmpty(s)) {

                return new JSONObject(s);
            }
        }
        return null;
    }

    private String loadLastString() {
        FileInputStream fis = null;
        try {
            fis = weakReference.get().openFileInput(getClass().getSimpleName() + getLastTag());
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] data = new byte[512];
            int count = -1;
            while ((count = fis.read(data, 0, data.length)) != -1) {
                outStream.write(data, 0, count);
            }
            return new String(outStream.toByteArray(), "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable e) {

        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    protected void saveLast(String s) {
        if (weakReference.get() != null) {
            FileOutputStream fos = null;
            try {
                fos = weakReference.get().openFileOutput(getClass().getSimpleName() + getLastTag(), Context.MODE_PRIVATE);
                fos.write(s.getBytes());
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    protected String getLastTag() {
        return "";
    }

    public void cancel() {
        if (thread != null) {
            thread.cancel(true);
        }
    }

//    private static boolean isRunOnMainThread() {
//        return Looper.myLooper() == Looper.getMainLooper();
//    }

    protected void completed(final T result, final boolean isLoadMore, final boolean isLast) {


        delay = 1;

        if (thread.isCancelled) {
            if (!isLast) {
                isSending = false;
            }
            return;
        }


        if (weakReference.get() != null && weakReference.get() instanceof Activity) {


            if (isCanCallbackToUi())
                ((Activity) weakReference.get()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isDismissToastDialog()) {
                            if (progressBar != null)
                                progressBar.setProgress(progress = 100);
                            dialog.dismiss();
                        }

                        if (isRefreshBaseTask()) {
                            ((RefreshBaseTask<T>) AbsTask.this).onTaskComplete();
                        }

                        if (!isLast) {
                            isSending = false;
                        }

                        if (onTaskCompleteListeners != null) {
                            for (OnTaskCompleteListener<T> onTaskCompleteListener : onTaskCompleteListeners) {
                                if (isLoadMore) {
                                    onTaskCompleteListener.onTaskLoadMoreComplete(result);
                                } else {
                                    if (result != null)
                                        onTaskCompleteListener.onTaskComplete(result);
//                    else
//                        onTaskCompleteListener.onTaskComplete();
                                }
                            }
                        }


                    }
                });
            else if (!isLast) {
                isSending = false;
            }


        } else {
            if (!isLast) {
                isSending = false;
            }

            if (onTaskCompleteListeners != null) {
                for (OnTaskCompleteListener<T> onTaskCompleteListener : onTaskCompleteListeners) {
                    if (isLoadMore) {
                        onTaskCompleteListener.onTaskLoadMoreComplete(result);
                    } else {
                        if (result != null)
                            onTaskCompleteListener.onTaskComplete(result);
//                    else
//                        onTaskCompleteListener.onTaskComplete();
                    }
                }
            }
        }


    }

//    protected void handleComplete(final T result, boolean isLoadMore) {
//        if (isDismissToastDialog() || isRefreshBaseTask()) {
//            if (isDismissToastDialog() && progressBar != null) {
//                if (isRunOnMainThread())
//                    progressBar.setProgress(progress = 100);
//            }
//            new Thread() {
//
//                @Override
//                public void run() {
//                    try {
//                        sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    final Activity activity = (Activity) weakReference.get();
//                    if (activity != null && !activity.isFinishing()) {
//                        activity.runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                try {
//                                    if (isDismissToastDialog()) {
//                                        dialog.dismiss();
//                                    }
//                                    if (isRefreshBaseTask()) {
////                                        circleDialog.dismiss();
//                                        ((RefreshBaseTask<T>) AbsTask.this).onTaskComplete();
//                                    }
//                                } catch (Exception e) {
//                                }
//                            }
//                        });
//                    }
//                }
//            }.start();
//        }
//
//        if (onTaskPostCompleteListeners != null) {
//            for (OnTaskCompleteListener<T> onTaskCompleteListener : onTaskPostCompleteListeners) {
//                if (isLoadMore) {
//                    onTaskCompleteListener.onTaskLoadMoreComplete(result);
//                } else {
//                    if (result != null)
//                        onTaskCompleteListener.onTaskComplete(result);
////                    else
////                        onTaskCompleteListener.onTaskComplete();
//                }
//            }
//        }
//    }

    protected void failed(String error) {
//        isSending = false;

        if (!(error != null && !TextUtils.isEmpty(error))) {
            error = "数据异常";
        }

        if (!thread.isCancelled) {


            if (weakReference.get() != null && weakReference.get() instanceof Activity) {

                if (isCanCallbackToUi()) {
                    final String finalError = error;
                    ((Activity) weakReference.get()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (isDismissToastDialog())
                                dialog.dismiss();
                            if (isRefreshBaseTask() && ((RefreshBaseTask<T>) AbsTask.this).onTaskSending()) {

                                ((RefreshBaseTask<T>) AbsTask.this).onTaskFailed();


                            }
                            isSending = false;

                            if (onTaskCompleteListeners != null) {
                                for (OnTaskCompleteListener<T> onTaskCompleteListener : onTaskCompleteListeners) {
                                    onTaskCompleteListener.onTaskFailed(finalError);
                                }
                            }

                        }
                    });
                } else {
                    isSending = false;
                }

            } else {
                isSending = false;
                if (onTaskCompleteListeners != null) {
                    for (OnTaskCompleteListener<T> onTaskCompleteListener : onTaskCompleteListeners) {
                        onTaskCompleteListener.onTaskFailed(error);
                    }
                }
            }


        } else {
            isSending = false;
        }
    }

    protected boolean isCanCallbackToUi() {
        return !((Activity) weakReference.get()).isFinishing();
    }

//    protected void handleFail(String error) {
//        if (needToast/*||needCircle*/) {
//            if (isDismissToastDialog()) {
//                try {
//                    if (isDismissToastDialog()) {
//                        dialog.dismiss();
//                    }
//
//                } catch (Exception e) {
//                }
//            }
//
////			ToastUtil.showErrorToast(error);
//        }
//
//        if (isRefreshBaseTask()) {
//
//            try {
//
//                if (((RefreshBaseTask<T>) this).onTaskSending()) {
////                        circleDialog.dismiss();
//                    ((RefreshBaseTask<T>) this).onTaskFailed();
//                }
//
//            } catch (Exception e) {
//
//            }
//
//
//        }
//
//        if (onTaskPostCompleteListeners != null) {
//            for (OnTaskCompleteListener<T> onTaskCompleteListener : onTaskPostCompleteListeners) {
//                onTaskCompleteListener.onTaskFailed(error);
//            }
//        }
//    }

    public void onCancel() {
        if (onTaskCompleteListeners != null) {
            for (OnTaskCompleteListener<T> onTaskCompleteListener : onTaskCompleteListeners) {
                onTaskCompleteListener.onTaskCancel();
            }
        }

//        Context context = weakReference.get();
//        if (context != null) {
//            if (context instanceof Activity) {
//                Activity activity = (Activity) context;
//                if (!activity.isFinishing()) {
//                    if (isRunOnMainThread()) {
//                        handleCancel();
//                    } else {
//                        activity.runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                Activity activity = (Activity) weakReference.get();
//                                if (activity != null && !activity.isFinishing()) {
//                                    handleCancel();
//                                }
//                            }
//                        });
//                    }
//                }
//            } else {
//                handleCancel();
//            }
//        }
    }

//    public void handleCancel() {
//        if (onTaskPostCompleteListeners != null) {
//            for (OnTaskCompleteListener<T> onTaskCompleteListener : onTaskPostCompleteListeners) {
//                onTaskCompleteListener.onTaskCancel();
//            }
//        }
//    }

    protected void error(final String error) {

        if (!thread.isCancelled) {
            if (needRestart) {
                if (delay < 60) {
                    isSending = false;
                    new Thread() {

                        @Override
                        public void run() {
                            try {
                                sleep((delay = delay * 2) * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (needRestart && !thread.isCancelled) {
                                AbsTask.this.start(thread.isLoadMore, true);
                            }
                        }

                    }.start();
                } else {
                    delay = 1;
                    failed(error);
                }
            } else {
                failed(error);
            }
        } else {
            isSending = false;
        }
    }

    protected static final class MyThread<T extends Serializable> extends Thread implements Callable<T> {
        public boolean isCancelled, isRestart, isLoadMore;
        public WeakReference<AbsTask<T>> absTask;

        public MyThread(AbsTask<T> absTask) {
            this.absTask = new WeakReference<AbsTask<T>>(absTask);
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            AbsTask<T> task = absTask.get();

            if (task == null) {
                return;
            }

//            if (task.needLocation) {
//				waitLocation();
//            }

            if (isCancelled) {
                return;
            }

            try {
                T result = task.doInBackground(this);

                if (!isCancelled && result != null) {
                    task.completed(result, isLoadMore, false);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                if (task.needUploadFile && task.request != null) {
                    task.request.finish();
                }
                task.error(e.getMessage());
            } finally {
                absTask.clear();
            }
        }

        @Override
        public T call() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            AbsTask<T> task = absTask.get();

//			if (task.needLocation && !waitLocation()) {
//				return null;
//			}

            if (isCancelled) {
                return null;
            }

            try {
                return task.doInBackground(this);
            } catch (Throwable e) {
                e.printStackTrace();
                if (task.needUploadFile && task.request != null) {
                    task.request.finish();
                }
            } finally {
                absTask.clear();
            }

            return null;
        }

//		protected boolean waitLocation() {
//			if (MyLocationManager.isLocationOpen() && MyLocationManager.getLocation() == null) {
//				int i = 0;
//				while (MyLocationManager.getLocation() == null) {
//					if (isCancelled) {
//						return false;
//					}
//
//					if (i > 100) {
//						return false;
//					}
//
//					i++;
//
//					try {
//						sleep(100);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//
//					// Log.i("nero", MyLocationManager.getLocation() + "");
//				}
//			}
//			return true;
//		}

        protected void cancel() {
            cancel(false);
        }

        public void cancel(final boolean doListener) {
            final AbsTask<T> task = absTask.get();
            if (task == null) {
                return;
            }

            if (!isCancelled && task.isSending) {
                interrupt();
                HttpManger.cancel(task.hashCode());

                isCancelled = true;

                if (task.weakReference != null && task.weakReference.get() instanceof Activity) {
                    if (!((Activity) task.weakReference.get()).isFinishing())
                        ((Activity) task.weakReference.get()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (task.isDismissToastDialog()) {

                                    task.dialog.dismiss();

                                }

                                if (task.isRefreshBaseTask() && ((RefreshBaseTask<T>) task).isSending) {

                                    ((RefreshBaseTask<T>) task).onTaskCancel();


                                }
                                task.isSending = false;

                                if (doListener) {
                                    task.onCancel();
                                }


                            }
                        });
                    else
                        task.isSending = false;
                } else {
                    task.isSending = false;
                    if (doListener) {
                        task.onCancel();
                    }
                }

                absTask.clear();
            }
        }
    }
}