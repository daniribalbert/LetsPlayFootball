package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.firebase.LeagueDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseUploadListener;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.utils.ActivityUtils;
import com.daniribalbert.letsplayfootball.utils.FileUtils;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.daniribalbert.letsplayfootball.utils.ToastUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.UploadTask;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

/**
 * Dialog fragment used to add/edit a new league.
 */
public class DialogFragmentEditLeague extends DialogFragmentViewLeague implements View.OnClickListener {

    private EditLeagueListener mListener;

    public static DialogFragmentEditLeague newInstance() {
        DialogFragmentEditLeague dFrag = new DialogFragmentEditLeague();
        dFrag.setRetainInstance(true);
        return dFrag;
    }

    public static DialogFragmentEditLeague newInstance(String leagueId) {
        Bundle bundle = new Bundle();
        DialogFragmentEditLeague dFrag = new DialogFragmentEditLeague();
        bundle.putString(ARGS_LEAGUE, leagueId);
        dFrag.setArguments(bundle);
        dFrag.setRetainInstance(true);
        return dFrag;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSaveLeague.setOnClickListener(this);
        mLeagueImage.setOnClickListener(this);
        Bundle args = getArguments();
        if (savedInstanceState == null) {
            if (args != null) {
                if (!args.containsKey(ARGS_LEAGUE)) {
                    return;
                }
                String leagueId = args.getString(ARGS_LEAGUE);
                loadLeagueData(leagueId);
            } else {
                mLeague = new League();
            }
        } else {
            if (mImageUri != null) {
                GlideUtils.loadCircularImage(mImageUri, mLeagueImage);
            } else if (mLeague != null && mLeague.hasImage()) {
                GlideUtils.loadCircularImage(mLeague.image, mLeagueImage);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_save_league:
                mLeague.title = mLeagueTitle.getText().toString();
                mLeague.description = mLeagueDescription.getText().toString();

                if (mImageUri == null) {
                    mListener.onLeagueSaved(mLeague);
                    tryAndCloseDialog();
                } else {
                    uploadImage();
                }
                break;
            case R.id.edit_league_pic:
                promptSelectImage();
        }
    }

    private void uploadImage() {
        showProgress(true);
        FileUtils.uploadImage(mImageUri, new BaseUploadListener(mProgressBar) {

            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                mLeague.image = downloadUrl.toString();
                mListener.onLeagueSaved(mLeague);
                if (getDialog() != null) {
                    dismiss();
                }
                showProgress(false);
                ToastUtils.show(R.string.toast_generic_saved, Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (handleImageSelectionActivityResult(requestCode,resultCode,data)) {
            GlideUtils.loadCircularImage(mImageUri, mLeagueImage);
        }
    }



    public void setListener(EditLeagueListener listener) {
        mListener = listener;
    }

    public interface EditLeagueListener {
        void onLeagueSaved(League league);
    }
}
