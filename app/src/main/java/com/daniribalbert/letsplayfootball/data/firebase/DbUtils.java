package com.daniribalbert.letsplayfootball.data.firebase;

import com.daniribalbert.letsplayfootball.BuildConfig;

/**
 * DatabaseUtils.
 */
public class DbUtils {

    private static final String DEBUG = "Debug";
    private static final String RELEASE = "Production";

    public static String getRoot() {
        if (BuildConfig.DEBUG) {
            return DEBUG;
        }
        return RELEASE;
    }
}
