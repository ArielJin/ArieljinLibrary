package com.arieljin.library.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.arieljin.library.abs.AbsApplication;

/**
 * @time 2018/7/24.
 * @email ariel.jin@tom.com
 */
public final class PreferencesUtil {

    private static SharedPreferences preferences;

    public static SharedPreferences getPreferences() {
        if (preferences == null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(AbsApplication.getSuperApplication());
        }
        return preferences;
    }

    public static void clear() {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.clear();
        editor.commit();
    }

    public static boolean deletePreferences(String name) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.remove(name);
        return editor.commit();
    }

    public static String readStringPreferences(String name) {
        return getPreferences().getString(name, "");
    }

    public static long readLongPreferences(String name) {
        return getPreferences().getLong(name, 0);
    }

    public static int readIntPreferences(String name, int defaultval) {
        return getPreferences().getInt(name, defaultval);
    }

    public static String readStringPreferences(String name, String defaultVal) {
        return getPreferences().getString(name, defaultVal);
    }

    public static boolean readBooleanPreferences(String name, boolean defaultVal) {
        return getPreferences().getBoolean(name, defaultVal);
    }

    public static boolean writeStringPreferences(String name, String value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(name, value);
        return editor.commit();
    }

    public static boolean writeLongPreferences(String name, long value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putLong(name, value);
        return editor.commit();
    }

    public static boolean writeIntPreferences(String name, int value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(name, value);
        return editor.commit();
    }

    public static boolean writeBooleanPreferences(String name, boolean value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putBoolean(name, value);
        return editor.commit();
    }

}
