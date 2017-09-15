package com.daniribalbert.letsplayfootball.utils;

import android.util.Log;

/**
 * Extension of the Android Log class.
 */
public class LogUtils {
    private static final String TAG = "LetsPlayFootball";

    public static void v(String text){
        Log.v(TAG, text);
    }

    public static void d(String text){
        Log.d(TAG, text);
    }

    public static void i(String text){
        Log.i(TAG, text);
    }

    public static void w(String text){
        Log.w(TAG, text);
    }

    public static void w(String text, Throwable throwable){
        Log.w(TAG, text, throwable);
    }

    public static void e(String text){
        Log.e(TAG, text);
    }

    public static void e(String text, Throwable throwable){
        Log.e(TAG, text, throwable);
    }

}
