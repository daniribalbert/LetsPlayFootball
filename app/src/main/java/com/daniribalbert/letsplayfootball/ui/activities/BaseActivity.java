package com.daniribalbert.letsplayfootball.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.daniribalbert.letsplayfootball.BuildConfig;
import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.firebase.PlayerDbUtils;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Base Activity
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected FirebaseAuth mAuth;

    protected FirebaseAuth.AuthStateListener mAuthListener;

    /**
     * AdMove Interstitial Ad.
     */
    protected InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFirebase();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    LogUtils.d("onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    LogUtils.w("onAuthStateChanged:signed_out");
                }
            }
        };
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void checkPlayerPushToken(Player currentUser) {
        if (TextUtils.isEmpty(currentUser.pushToken)) {
            String token = FirebaseInstanceId.getInstance().getToken();
            if (!TextUtils.isEmpty(token)) {
                PlayerDbUtils.updateCurrentPlayerPushToken(token);
            }
        }
    }

    protected void initBannerAd(ViewGroup adContainerLayout) {
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        String adUnitId = BuildConfig.DEBUG
                          ? getString(R.string.ad_mob_test_banner_id)
                          : getString(R.string.ad_mob_main_banner_id);
        adView.setAdUnitId(adUnitId);

        adContainerLayout.addView(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        LogUtils.i("Setup ad view");
    }

    protected void loadInterstitialAd() {
        mInterstitialAd = new InterstitialAd(this);
        String id = BuildConfig.DEBUG
                    ? getString(R.string.ad_mob_test_interstitial_id)
                    : getString(R.string.ad_mob_main_interstitial_id);
        mInterstitialAd.setAdUnitId(id);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
            }
        });
    }
}
