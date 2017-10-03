package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.database.PlayerDbUtils;
import com.daniribalbert.letsplayfootball.data.database.listeners.BaseUploadListener;
import com.daniribalbert.letsplayfootball.data.database.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.utils.ActivityUtils;
import com.daniribalbert.letsplayfootball.utils.FileUtils;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.daniribalbert.letsplayfootball.utils.ToastUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.UploadTask;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

/**
 * Dialog fragment used to add/edit a new league.
 */
public class DialogFragmentEditPlayer extends DialogFragmentViewPlayer implements View.OnClickListener {

    public static final String TAG = DialogFragmentEditPlayer.class.getSimpleName();

    private EditPlayerListener mListener;
    private Uri mImageUri;

    public static DialogFragmentEditPlayer newInstance(String leagueId) {
        Bundle bundle = new Bundle();
        DialogFragmentEditPlayer dFrag = new DialogFragmentEditPlayer();
        bundle.putString(ARGS_LEAGUE_ID, leagueId);
        dFrag.setArguments(bundle);
        dFrag.setRetainInstance(true);
        return dFrag;
    }

    public static DialogFragmentEditPlayer newInstance(String leagueId, String playerId) {
        Bundle bundle = new Bundle();
        DialogFragmentEditPlayer dFrag = new DialogFragmentEditPlayer();
        bundle.putString(ARGS_LEAGUE_ID, leagueId);
        bundle.putString(ARGS_PLAYER, playerId);
        dFrag.setArguments(bundle);
        dFrag.setRetainInstance(true);
        return dFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mLeagueId = args.getString(ARGS_LEAGUE_ID);
            mPlayerId = args.getString(ARGS_PLAYER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_player, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSavePlayer.setOnClickListener(this);
        mPlayerImage.setOnClickListener(this);
        if (savedInstanceState == null) {
            if (TextUtils.isEmpty(mPlayerId)) {
                mPlayer = new Player();
                setupViewMode();
            } else {
                loadPlayerData(mPlayerId);
            }
        } else {
            if (mImageUri != null) {
                GlideUtils.loadCircularImage(mImageUri, mPlayerImage);
            } else if (mPlayer != null && mPlayer.hasImage()) {
                GlideUtils.loadCircularImage(mPlayer.image, mPlayerImage);
            }
        }
    }

    @Override
    protected void setupViewMode() {
        boolean canEdit = mPlayer.isGuest();

        mPlayerImage.setClickable(canEdit);
        mPlayerName.setEnabled(canEdit);
        mPlayerNickname.setEnabled(canEdit);
        mRating.setEnabled(true);
        mSavePlayer.setText(R.string.save);
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
            case R.id.bt_save_player:
                showProgress(true);

                mPlayer.name = mPlayerName.getText().toString();
                mPlayer.nickname = mPlayerNickname.getText().toString();
                mPlayer.setRating(mLeagueId, mRating.getRating());

                if (mImageUri == null) {
                    save(mPlayer);
                    if (getDialog() != null) {
                        dismiss();
                    }
                } else {
                    uploadImage();
                }
                break;
            case R.id.edit_player_pic:
                promptSelectImage();
        }
    }

    private void save(Player player) {
        if (mListener != null){
            mListener.onPlayerSaved(player);
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
                if (downloadUrl != null) {
                    mPlayer.image = downloadUrl.toString();
                   save(mPlayer);
                    if (getDialog() != null) {
                        dismiss();
                    }
                    ToastUtils.show(R.string.toast_profile_saved, Toast.LENGTH_SHORT);
                } else {
                    ToastUtils.show(R.string.toast_error_generic, Toast.LENGTH_SHORT);
                }
                showProgress(false);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ARGS_IMAGE_SELECT) {
                mImageUri = ActivityUtils.extractImageUri(data, getActivity());
                GlideUtils.loadCircularImage(mImageUri, mPlayerImage);
            }
        }
    }

    public void setListener(EditPlayerListener listener) {
        mListener = listener;
    }

    public interface EditPlayerListener {
        void onPlayerSaved(Player player);
    }
}
