package com.daniribalbert.letsplayfootball.application;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.twitter.sdk.android.core.Twitter;

/**
 * The app Application Class.
 */
public class App extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        setupAppContext();
        Twitter.initialize(this);
    }

    public static Context getContext(){
        return sContext;
    }

    private void setupAppContext(){
        sContext = this;
    }
}
