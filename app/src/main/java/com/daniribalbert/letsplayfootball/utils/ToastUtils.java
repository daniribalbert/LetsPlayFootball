package com.daniribalbert.letsplayfootball.utils;

import android.widget.Toast;

import com.daniribalbert.letsplayfootball.application.App;

/**
 * Toast utility class.
 */
public class ToastUtils {

    public static void show(String text, int length) {
        Toast.makeText(App.getContext(), text, length).show();
    }

    public static void show(int resId, int length) {
        Toast.makeText(App.getContext(), resId, length).show();
    }
}
