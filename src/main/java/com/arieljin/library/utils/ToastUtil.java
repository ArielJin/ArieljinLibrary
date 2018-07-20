package com.arieljin.library.utils;

import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arieljin.library.R;
import com.arieljin.library.abs.AbsApplication;

public final class ToastUtil {
	public static final int TOAST_TYPE_ERROR = 1, TOAST_TYPE_OK = 2 ,TOAST_TYPE_MESSAGE = 3;

	private static Toast t;
	private static TextView textView;
	private static ImageView image;

	private static Handler handler;
	private static View v = null;


	private synchronized static void getToast(final CharSequence text, final int duration,
			final int type) {
		if (handler == null) {
			t = null;
			new Thread() {

				@Override
				public void run() {
//					Looper.prepare();
					String str = (String) text;
//					if(str.contains("DOCTYPE")){
//						str = "网络不给力";
//						return;
//					}
//					handler = new Handler(Looper.myLooper());
					handler = MainHandlerUtil.handler;//getMainLoop 防止内存泄露
					getToast(str, duration, type);
//					Looper.loop();
				}
			}.start();
		} else {
			new Thread() {

				@Override
				public void run() {
//					Looper.prepare();
					handler.post(new Runnable() {

						@Override
						public void run() {
							String str = (String) text;
//							if(str.contains("DOCTYPE")){
//								str = "网络不给力";
//								return;
//							}
							makeToast(str, type);
						}
					});
//					Looper.loop();
				}
			}.start();
		}
	}

	private synchronized static void makeToast(final CharSequence text, final int type) {
			if (type == TOAST_TYPE_MESSAGE) {
				v = View.inflate(AbsApplication.getSuperApplication(), R.layout.toast_message,
						null);
			} else if (type == TOAST_TYPE_OK || type == TOAST_TYPE_ERROR) {
				v = View.inflate(AbsApplication.getSuperApplication(), R.layout.toast,
						null);
			}
			if (v != null) {
				textView = (TextView) v.findViewById(R.id.toast_text);
				image = (ImageView) v.findViewById(R.id.toast_image);

				if(t != null) {
					t.cancel();
				}

				t = Toast.makeText(AbsApplication.getSuperApplication(), "",
						Toast.LENGTH_LONG);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.setView(v);

				textView.setText(text);

				if (type == TOAST_TYPE_OK) {
					image.setImageResource(R.mipmap.icon_success);
				} else if (type == TOAST_TYPE_ERROR) {
					image.setImageResource(R.mipmap.icon_fail);
				} else if (type == TOAST_TYPE_MESSAGE) {
					image.setImageResource(R.mipmap.icon_message);
				}
				t.show();
			}
	}

	public synchronized static void showToast(CharSequence text, int duration, int type) {
		getToast(text, duration, type);
	}

	public synchronized static void showToast(CharSequence text, int type) {
		getToast(text, Toast.LENGTH_LONG, type);
	}

	public synchronized static void showErrorToast(CharSequence text) {
		getToast(text, Toast.LENGTH_LONG, TOAST_TYPE_ERROR);
	}

	public synchronized static void showOkToast(CharSequence text) {
		getToast(text, Toast.LENGTH_LONG, TOAST_TYPE_OK);
	}
	public synchronized static void showMessageToast(CharSequence text){
		getToast(text, Toast.LENGTH_LONG, TOAST_TYPE_MESSAGE);
	}
}