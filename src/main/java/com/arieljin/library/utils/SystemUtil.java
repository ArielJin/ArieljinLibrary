package com.arieljin.library.utils;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

/**
 * @time 2018/10/24.
 * @email ariel.jin@tom.com
 */
public class SystemUtil {


    public static void hideKeyboard(Activity activity) {

        if (activity != null && !activity.isFinishing())
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }

}
