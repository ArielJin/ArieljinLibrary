package com.arieljin.library.abs;

import android.content.Context;
import android.util.Log;

import com.arieljin.library.utils.MyWeakHashMap;

public class AbsActivityManager {
	protected final static MyWeakHashMap<String, AbsActivity> mActivities = new MyWeakHashMap<String, AbsActivity>();
	protected static AbsActivity mCurrentActivity;

	public static void finishActivities() {
		for (AbsActivity activity : mActivities.values()) {
			activity.setHasFinishAnimation(true);

			Log.i("arieljin", "finish:" + activity.getClass().getSimpleName());

			activity.finish();
		}
		mActivities.clear();
	}

	public static void onDestroy(AbsActivity activity) {
		mActivities.remove(activity.getClass().getName());
	}

	public static boolean onResume(AbsActivity activity) {
		if (mCurrentActivity == null && activity != null) {
			Context context = AbsApplication.getInstance();
			synchronized (context) {
				context.notifyAll();
			}
		}
		mCurrentActivity = activity;
		return true;
	}

	public static boolean onPause(AbsActivity activity) {
		if (activity == mCurrentActivity) {
			mCurrentActivity = null;
		}
		return true;
	}

	public static AbsActivity getCurrentActivity() {
		return mCurrentActivity;
	}

	public static int getActivityCount() {
		return mActivities == null ? 0 : mActivities.size();
	}

	public static AbsActivity getAcitivity(String className) {
		return mActivities == null ? null : mActivities.get(className);
	}
}