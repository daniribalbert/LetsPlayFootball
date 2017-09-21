package com.daniribalbert.letsplayfootball.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Base Activity
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected FirebaseAuth mAuth;

    protected FirebaseAuth.AuthStateListener mAuthListener;

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
                    LogUtils.d("onAuthStateChanged:signed_out");
                }
            }
        };
    }

    public FirebaseUser getCurrentUser(){
        return mAuth.getCurrentUser();
    }
}
