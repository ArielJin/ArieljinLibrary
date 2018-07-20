package com.arieljin.library.utils;

import android.os.Handler;
import android.os.Looper;

public final class MainHandlerUtil {

	public static final Handler handler = new Handler(Looper.getMainLooper());

	public static void post(Runnable r) {
		handler.post(r);
	}

	public static void postDelayed(Runnable r, long delayMillis) {
		handler.postDelayed(r, delayMillis);
	}
}