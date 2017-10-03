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
import com.daniribalbert.letsplayfootball.data.database.LeagueDbUtils;
import com.daniribalbert.letsplayfootball.data.database.listeners.BaseUploadListener;
import com.daniribalbert.letsplayfootball.data.database.listeners.BaseValueEventListener;
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
public class DialogFragmentEditLeague extends BaseDialogFragment implements View.OnClickListener {

    public static final String TAG = DialogFragmentEditLeague.class.getSimpleName();

    public static final String ARGS_LEAGUE = "ARGS_LEAGUE";

    @BindView(R.id.edit_league_pic)
    ImageView mLeagueImage;

    @BindView(R.id.edit_league_title)
    EditText mLeagueTitle;

    @BindView(R.id.edit_league_description)
    EditText mLeagueDescription;

    @BindView(R.id.bt_save_league)
    View mSaveLeague;

    private EditLeagueListener mListener;
    private League mLeague;

    private Uri mImageUri;

    @BindView(R.id.dialog_progress)
    ProgressBar mProgressBar;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_league, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
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

    private void loadLeagueData(String leagueId) {
        LeagueDbUtils.getLeague(leagueId, new BaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLeague = dataSnapshot.getValue(League.class);

                if (mLeague != null) {
                    mLeagueTitle.setText(mLeague.title);
                    mLeagueDescription.setText(mLeague.description);
                    if (mLeague.hasImage()) {
                        GlideUtils.loadCircularImage(mLeague.image, mLeagueImage);
                    }
                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow()
                       .setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                                  WindowManager.LayoutParams.MATCH_PARENT);
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
                    if (getDialog() != null) {
                        dismiss();
                    }
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
        FileUtils.uploadImage(mImageUri, new BaseUploadListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                super.onFailure(e);
                showProgress(false);
            }

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
        if (resultCode == RESULT_OK) {
            if (requestCode == ARGS_IMAGE_SELECT) {
                mImageUri = ActivityUtils.extractImageUri(data, getActivity());
                GlideUtils.loadCircularImage(mImageUri, mLeagueImage);
            }
        }
    }

    public void setListener(EditLeagueListener listener) {
        mListener = listener;
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    public interface EditLeagueListener {
        void onLeagueSaved(League league);
    }
}
