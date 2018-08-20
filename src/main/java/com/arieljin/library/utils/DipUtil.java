package com.arieljin.library.utils;

import android.content.Context;

import com.arieljin.library.abs.AbsApplication;

public final class DipUtil {

	public static float getDip() {
		return AbsApplication.getInstance().getResources().getDisplayMetrics().density;
	}

	public static int getIntDip(float i) {
		return (int) getFloatDip(i);
	}

	public static float getFloatDip(float i) {
		return AbsApplication.getInstance().getResources().getDisplayMetrics().density * i;
	}

	public static int getScreenWidth() {
		return AbsApplication.getInstance().getResources().getDisplayMetrics().widthPixels;
	}

	public static int getScreenHeight() {
		return AbsApplication.getInstance().getResources().getDisplayMetrics().heightPixels;
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static float limitValue(float a, float b) {
		float valve = 0;
		final float min = Math.min(a, b);
		final float max = Math.max(a, b);
		valve = valve > min ? valve : min;
		valve = valve < max ? valve : max;
		return valve;
	}
}