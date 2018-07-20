package com.arieljin.library.utils;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class DestoryUtil {

    public static void onDestory(Object object) {
        Class<?> clz = object.getClass();

        setNull(clz.getFields(), object);
        setNull(clz.getDeclaredFields(), object);

        if (object instanceof Activity) {
            Window window = ((Activity) object).getWindow();
            if (window != null) {
                window.setBackgroundDrawable(null);
                destoryView((ViewGroup) window.getDecorView());
            }
        } else if (object instanceof Fragment) {
            destoryView((ViewGroup) ((Fragment) object).getView());
        }
    }

    private static void setNull(Field[] fields, Object object) {
        for (Field field : fields) {
            try {
                if (!field.getType().isPrimitive() && !Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);

                    if (ViewGroup.class.isAssignableFrom(field.getType())) {
                        destoryView((ViewGroup) field.get(object));
                    }

                    field.set(object, null);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static void destoryView(ViewGroup viewGroup) {
        if (viewGroup != null) {
            viewGroup.setBackgroundDrawable(null);
            if (viewGroup instanceof ExpandableListView) {
                ((ExpandableListView) viewGroup).setAdapter((ExpandableListAdapter) null);
            } else if (viewGroup instanceof AdapterView<?>) {
                ((AdapterView<?>) viewGroup).setAdapter(null);
            } else {
                for (int i = 0, size = viewGroup.getChildCount(); i < size; i++) {
                    View view = viewGroup.getChildAt(i);
                    if (view instanceof ViewGroup) {
                        destoryView((ViewGroup) view);
                    } else {
//						viewGroup.setBackgroundDrawable(null);
                        if (view instanceof ImageView) {
                            ((ImageView) view).setImageDrawable(null);
//                            view.setBackgroundDrawable(null);
                        }
                    }
                }
                viewGroup.removeAllViews();
            }
        }
    }
}