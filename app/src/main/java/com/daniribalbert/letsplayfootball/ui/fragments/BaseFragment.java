package com.daniribalbert.letsplayfootball.ui.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ProgressBar;

import com.daniribalbert.letsplayfootball.ui.activities.BaseActivity;
import com.daniribalbert.letsplayfootball.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Unbinder;

/**
 * That BaseFragment that starts every other Fragment.
 */
public abstract class BaseFragment extends Fragment {

    public static final int ARGS_IMAGE_SELECT = 201;

    protected ProgressBar mProgressBar;
    protected Unbinder mUnbinder;

    public BaseActivity getBaseActivity(){
        return (BaseActivity) getActivity();
    }

    public void setProgress(ProgressBar progressBar){ mProgressBar = progressBar; }

    public void showProgress(boolean show){
        if (mProgressBar != null) {
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);

        }
    }

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

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }
}
