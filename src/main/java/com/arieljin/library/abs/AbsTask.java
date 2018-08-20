package com.arieljin.library.abs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Looper;
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
import com.arieljin.library.utils.MainHandlerUtil;

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
    public volatile Set<OnTaskCompleteListener<T>> onTaskPostCompleteListeners, onTaskCompleteListeners;
    protected WeakReference<Context> weakReference;
//    protected ObjectMapper mObjectMapper = null;

    public int progressY;
    public int circleProgressY = 200;

    protected Dialog dialog;
    protected ProgressBar progressBar;

//    protected Dialog circleDialog;
//    protected CircleProgressBar circleProgressBar;

    public boolean needUploadFile, cancelable = true, needLast, needOnlyLast, needToast, /*needCircle = true, */
            needRestart, isSending, needLastOnce;
    protected int delay = 1, progress;
    public int method_type = HttpManger.POST;
    public String loadingText = "加载中";
//    public ShowDialogMothod showDialogMothod = ShowDialogMothod.CIRCLE_DIALOG;

//    private enum ShowDialogMothod{
//        TOAST_DIALOG,CIRCLE_DIALOG,ALL_DIALOG,NULL_DIALOG;
//    }

    protected HashMap<String, String> headers;

    protected abstract String getApiMethodName();

    protected abstract T parseJson(JSONObject json) throws Throwable;

    public AbsTask(Context context, AbsRequest request) {
        this(context, request, (OnTaskCompleteListener) null);
    }

    public AbsTask(Context context, AbsRequest request, OnTaskCompleteListener<T> completeListener) {
        super();
        this.weakReference = new WeakReference<Context>(context);
//        this.mObjectMapper = new ObjectMapper();
        this.request = request;
        this.headers = addHeaders();
        addListener(completeListener);
        init();
    }

    public AbsTask(Context context, AbsRequest request, boolean needToast, /*boolean needCircle, */OnTaskCompleteListener<T> completeListener) {
        super();
        this.weakReference = new WeakReference<Context>(context);
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
            if (onTaskPostCompleteListeners == null) {
                onTaskPostCompleteListeners = new HashSet<OnTaskCompleteListener<T>>();
            }
            onTaskPostCompleteListeners.add(completeListener);
        }
    }

    public void addListenerWithOutPost(OnTaskCompleteListener<T> completeListener) {
        if (completeListener != null) {
            if (onTaskCompleteListeners == null) {
                onTaskCompleteListeners = new HashSet<OnTaskCompleteListener<T>>();
            }
            onTaskCompleteListeners.add(completeListener);
        }
    }

    public void setContext(Context context) {
        this.weakReference = new WeakReference<Context>(context);
    }

    protected void init() {
//        if(needCircle && !needToast){
//            showDialogMothod = ShowDialogMothod.CIRCLE_DIALOG;
//        }else if(!needCircle && needToast){
//            showDialogMothod = ShowDialogMothod.TOAST_DIALOG;
//        }else if(needToast && needCircle){
//            showDialogMothod = ShowDialogMothod.ALL_DIALOG;
//        }else if(!needCircle && !needToast){
//            showDialogMothod = ShowDialogMothod.NULL_DIALOG;
//        }
    }

//    protected abstract String getRequesturl();

    protected abstract T parseCompleteJson(JSONObject jsonObject);

    protected T doInBackground(MyThread<T> thread) {
        Context context = weakReference.get();
        if (!thread.isCancelled && context != null && !isActivityFinishing(context)) {
            JSONObject json = null;
            try {
                json = doMainInBackground();

                if (json != null && json.length() > 0)

                    return parseCompleteJson(json);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
                if (needLast && needLastOnce) {
                    JSONObject lastJson = null;
                    try {
                        lastJson = loadLastJson();
                        if (lastJson != null) {
                            return parseJson(lastJson);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        failed("读取缓存数据异常");
                    } catch (Throwable throwable1) {
                        throwable1.printStackTrace();
                        failed("解析缓存数据异常");
                    }

                } else {
                    failed("网络异常");
                }
            }

        }
        return null;
    }

    protected JSONObject doMainInBackground() throws Throwable {

        String url = getApiMethodName();
//        if (!getApiMethodName().startsWith("http")) {
//            url = getRequesturl() + getApiMethodName();
//        } else {
//            url = getApiMethodName();
//        }

        Log.i("ArielJin", url);

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
//			dialog = new Dialog(context, R.style.contentOverlay);

//			 ViewGroup view = (ViewGroup)
//			 LayoutInflater.from(weakReference.get()).inflate(R.layout.progress,
//			 null);
//			 TextView textView = (TextView)
//			 view.findViewById(android.R.id.text1);
//			 textView.setText(loadingText);

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

//    protected void initCircleDialog(){
//        Context context = weakReference.get();
//        if(circleDialog == null && context != null){
//
//            circleDialog = new Dialog(context, R.style.transparent_dialog);
//
//            ViewGroup viewGroup = (ViewGroup)LayoutInflater.from(context).inflate(R.layout.circle_progressbar,null);
//            circleProgressBar = (CircleProgressBar)viewGroup.findViewById(R.id.circleProgressBar);
////            circleProgressBar = new CircleProgressBar(context);
////            progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//            circleProgressBar.setClickable(false);
//
//            circleDialog.setContentView(viewGroup);
//            circleDialog.setCanceledOnTouchOutside(false);
//            circleDialog.setCancelable(cancelable);
//            if (cancelable) {
//                circleDialog.setOnCancelListener(new OnCancelListener() {
//
//                    @Override
//                    public void onCancel(DialogInterface dialog) {
//                        thread.cancel(true);
//                    }
//                });
//            }
//
//            Window dialogWindow = circleDialog.getWindow();
//            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//            lp.gravity = Gravity.TOP|Gravity.CENTER_HORIZONTAL;
//            lp.y = circleProgressY;
//            lp.width = LayoutParams.WRAP_CONTENT;
//            lp.height = LayoutParams.WRAP_CONTENT;
//            dialogWindow.setAttributes(lp);
//
//        }
//    }

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

                        if (onTaskPostCompleteListeners != null) {
                            if (context instanceof Activity && !((Activity) context).isFinishing()) {
                                ((Activity) context).runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        for (OnTaskCompleteListener<T> onTaskCompleteListener : onTaskPostCompleteListeners) {
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
                        if (context instanceof Activity) {
                            if (!((Activity) context).isFinishing()) {
                                ((Activity) context).runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        showDialog();
                                    }
                                });
                            }
                        } else {
                            MainHandlerUtil.post(new Runnable() {

                                @Override
                                public void run() {
                                    showDialog();
                                }
                            });
                        }
                    }

                    ThreadPoolManager.httpExecute(thread);
                }
            }
        }.start();
    }

//    protected void showDialog(ShowDialogMothod showDialogMothod) {
//        switch (showDialogMothod){
//            case CIRCLE_DIALOG:
//                initCircleDialog();
//                showDialog(circleDialog);
//                break;
//            case TOAST_DIALOG:
//                initDialog();
//                showDialog(dialog);
//                break;
//            case ALL_DIALOG:
//                initCircleDialog();
//                initDialog();
//                showDialog(circleDialog);
//                showDialog(dialog);
//                break;
//            default:break;
//        }
//    }

    protected void showDialog() {
        initTaskDialog();
        if (isShowToastDialog() || /*isShowCircleDialog()*/ isRefreshBaseTask()) {
            Context context = weakReference.get();
            if (context != null && context instanceof Activity && !((Activity) context).isFinishing()) {
                try {
                    if (isShowToastDialog()) {
                        dialog.show();
                    }
                    if (isRefreshBaseTask()) {
//                        circleDialog.show();
                        ((RefreshBaseTask<T>) this).onTaskStart();

                    }
                } catch (Throwable e) {
                }
                if (isShowToastDialog()) {
                    progressBar.setProgress(progress = 0);
                    setProgress();
                }
            }
        }
    }

    protected boolean isDismissToastDialog() {
        if (needToast && dialog != null && dialog.isShowing()) {
            return true;
        }
        return false;
    }

//    protected boolean isDismissCircleDialog(){
////        if(needCircle && circleDialog != null && circleDialog.isShowing()){
////            return true;
////        }
//        return false;
//    }

    protected boolean isShowToastDialog() {
        if (needToast && dialog != null && !dialog.isShowing()) {
            return true;
        }
        return false;
    }

//    protected boolean isShowCircleDialog(){
////        if(needCircle && circleDialog != null && !circleDialog.isShowing()){
////            return true;
////        }
//        return false;
//    }

//    protected boolean isRefreshBaseActivity() {
//        return weakReference != null && weakReference.get() != null && weakReference.get() instanceof RefreshBaseActivity;
//    }

    protected void initTaskDialog() {
        if (needToast && dialog == null) {
            initDialog();
        }
//        if(needCircle && circleDialog == null){
//            initCircleDialog();
//        }
    }

    protected void setProgress() {
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

    protected JSONObject loadLastJson() throws JSONException {
        if (weakReference.get() != null) {
            String s = loadLastString();
            if (!TextUtils.isEmpty(s)) {

                return new JSONObject(s);
            }
        }
        return null;
    }

    protected String loadLastString() {
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

    protected static boolean isRunOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    protected void completed(final T result, final boolean isLoadMore, boolean isLast) {
        if (!isLast) {
            isSending = false;
        }

        delay = 1;

        if (onTaskCompleteListeners != null) {
            for (OnTaskCompleteListener<T> onTaskCompleteListener : onTaskCompleteListeners) {
                if (isLoadMore) {
                    onTaskCompleteListener.onTaskLoadMoreComplete(result);
                } else {
                    onTaskCompleteListener.onTaskComplete(result);
                }
            }
        }

        if (!thread.isCancelled) {
            Context context = weakReference.get();
            if (context != null) {
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    if (!activity.isFinishing()) {
                        if (isRunOnMainThread()) {
                            handleComplete(result, isLoadMore);
                        } else {
                            activity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Activity activity = (Activity) weakReference.get();
                                    if (activity != null && !activity.isFinishing()) {
                                        handleComplete(result, isLoadMore);
                                    }
                                }
                            });
                        }
                    }
                } else {
                    handleComplete(result, isLoadMore);
                }
            }
        }
    }

    protected void handleComplete(final T result, boolean isLoadMore) {
        if (isDismissToastDialog() || isRefreshBaseTask()) {
            if (isDismissToastDialog() && progressBar != null) {
                if (isRunOnMainThread())
                    progressBar.setProgress(progress = 100);
            }
            new Thread() {

                @Override
                public void run() {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    final Activity activity = (Activity) weakReference.get();
                    if (activity != null && !activity.isFinishing()) {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    if (isDismissToastDialog()) {
                                        dialog.dismiss();
                                    }
                                    if (isRefreshBaseTask()) {
//                                        circleDialog.dismiss();
                                        ((RefreshBaseTask<T>) AbsTask.this).onTaskComplete();
                                    }
                                } catch (Exception e) {
                                }
                            }
                        });
                    }
                }
            }.start();
        }

        if (result != null && onTaskPostCompleteListeners != null) {
            for (OnTaskCompleteListener<T> onTaskCompleteListener : onTaskPostCompleteListeners) {
                if (isLoadMore) {
                    onTaskCompleteListener.onTaskLoadMoreComplete(result);
                } else {
                    onTaskCompleteListener.onTaskComplete(result);
                }
            }
        }
    }

    protected void failed(String error) {
        isSending = false;

        if (error == null || error.isEmpty()) {
            error = "数据异常";
        }

        if (!thread.isCancelled) {
            if (onTaskCompleteListeners != null) {
                for (OnTaskCompleteListener<T> onTaskCompleteListener : onTaskCompleteListeners) {
                    onTaskCompleteListener.onTaskFailed(error);
                }
            }

            Context context = weakReference.get();
            if (context != null) {
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    if (!activity.isFinishing()) {
                        if (isRunOnMainThread()) {
                            handleFail(error);
                        } else {
                            final String temp = error;

                            activity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Activity activity = (Activity) weakReference.get();
                                    if (activity != null && !activity.isFinishing()) {
                                        handleFail(temp);
                                    }
                                }
                            });
                        }
                    }
                } else {
//					handleFail(error);
                }
            }
        }
    }

    protected void handleFail(String error) {
        if (needToast/*||needCircle*/) {
            if (isDismissToastDialog()) {
                try {
                    if (isDismissToastDialog()) {
                        dialog.dismiss();
                    }

                } catch (Exception e) {
                }
            }

//			ToastUtil.showErrorToast(error);
        }

        if (isRefreshBaseTask()) {

            try {

                if (((RefreshBaseTask<T>) this).onTaskSending()) {
//                        circleDialog.dismiss();
                    ((RefreshBaseTask<T>) this).onTaskFailed();
                }

            } catch (Exception e) {

            }


        }

        if (onTaskPostCompleteListeners != null) {
            for (OnTaskCompleteListener<T> onTaskCompleteListener : onTaskPostCompleteListeners) {
                onTaskCompleteListener.onTaskFailed(error);
            }
        }
    }

    public void onCancel() {
        if (onTaskCompleteListeners != null) {
            for (OnTaskCompleteListener<T> onTaskCompleteListener : onTaskCompleteListeners) {
                onTaskCompleteListener.onTaskCancel();
            }
        }

        Context context = weakReference.get();
        if (context != null) {
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                if (!activity.isFinishing()) {
                    if (isRunOnMainThread()) {
                        handleCancel();
                    } else {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Activity activity = (Activity) weakReference.get();
                                if (activity != null && !activity.isFinishing()) {
                                    handleCancel();
                                }
                            }
                        });
                    }
                }
            } else {
                handleCancel();
            }
        }
    }

    public void handleCancel() {
        if (onTaskPostCompleteListeners != null) {
            for (OnTaskCompleteListener<T> onTaskCompleteListener : onTaskPostCompleteListeners) {
                onTaskCompleteListener.onTaskCancel();
            }
        }
    }

    protected void error(final String error) {
        isSending = false;

        if (!thread.isCancelled) {
            if (needRestart) {
                if (delay < 60 && !thread.isCancelled) {
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
                return;
            } finally {
                absTask.clear();
            }
        }

        @Override
        public T call() throws Exception {
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

        public void cancel(boolean doListener) {
            AbsTask<T> task = absTask.get();
            if (task == null) {
                return;
            }

            if (!isCancelled && task.isSending) {
                interrupt();
                HttpManger.cancel(task.hashCode());

                task.isSending = false;
                isCancelled = true;

                if (task.needToast /*|| task.needCircle*/ && task.isDismissToastDialog()) {
                    try {
                        if (task.isDismissToastDialog()) {
                            task.dialog.dismiss();
                        }
//                        if(task.isDismissCircleDialog()){
////                            task.circleDialog.dismiss();
//                        }
                    } catch (Exception e) {
                    }
                }

                if (task.isRefreshBaseTask()) {
                    try {

                        ((RefreshBaseTask<T>) task).onTaskCancel();

                    } catch (Exception e) {

                    }


                }

                if (doListener) {
                    task.onCancel();
                }

                absTask.clear();
            }
        }
    }
}