package com.daniribalbert.letsplayfootball.data.firebase.listeners;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.daniribalbert.letsplayfootball.utils.ToastUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;

/**
 * Base Listener for uploading images to Firebase
 */
public abstract class BaseUploadListener
        implements OnFailureListener, OnSuccessListener<UploadTask.TaskSnapshot> {

    private final ProgressBar mProgress;

    public BaseUploadListener(ProgressBar progressBar){
        mProgress = progressBar;
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        LogUtils.e("Failed to upload image: " + e.getLocalizedMessage());
        ToastUtils.show(R.string.toast_error_generic, Toast.LENGTH_SHORT);
        if (mProgress != null){
            mProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot){
        if (mProgress != null){
            mProgress.setVisibility(View.GONE);
        }
    }
}
