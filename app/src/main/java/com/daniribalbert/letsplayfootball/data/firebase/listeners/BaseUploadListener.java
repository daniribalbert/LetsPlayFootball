package com.daniribalbert.letsplayfootball.data.firebase.listeners;

import android.support.annotation.NonNull;
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

    @Override
    public void onFailure(@NonNull Exception e) {
        LogUtils.e("Failed to upload image: " + e.getLocalizedMessage());
        ToastUtils.show(R.string.toast_error_generic, Toast.LENGTH_SHORT);
    }

    @Override
    public abstract void onSuccess(UploadTask.TaskSnapshot taskSnapshot);
}
