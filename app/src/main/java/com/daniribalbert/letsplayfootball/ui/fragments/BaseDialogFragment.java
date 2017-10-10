package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseUploadListener;
import com.daniribalbert.letsplayfootball.utils.ActivityUtils;
import com.daniribalbert.letsplayfootball.utils.FileUtils;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.daniribalbert.letsplayfootball.utils.ToastUtils;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Base Dialog fragment class.
 */
public class BaseDialogFragment extends DialogFragment {

    public static final int ARGS_IMAGE_SELECT = 201;

    ProgressBar mProgressBar;
    protected Uri mImageUri;

    protected void promptSelectImage() {
        // Determine Uri of camera image to save.
        final File tempFile = FileUtils.getTempFile(getActivity());

        final PackageManager pManager = getActivity().getPackageManager();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        final List<Intent> cameraIntents = new ArrayList<Intent>();
        List<ResolveInfo> listCam = pManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(
                    new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
            cameraIntents.add(intent);
        }

        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                               cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        startActivityForResult(chooserIntent, ARGS_IMAGE_SELECT);
    }

    protected void showProgress(boolean show) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    protected boolean handleImageSelectionActivityResult(int requestCode, int resultCode,
                                                         Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ARGS_IMAGE_SELECT) {
                mImageUri = ActivityUtils.extractImageUri(data, getActivity());
                return mImageUri != null;
            }
        }
        return false;
    }

    protected void tryAndCloseDialog(){
        if (getDialog() != null){
            getDialog().dismiss();
        }
    }

}
