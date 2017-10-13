package com.daniribalbert.letsplayfootball.ui.fragments;

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
import android.widget.Toast;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseUploadListener;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.utils.ActivityUtils;
import com.daniribalbert.letsplayfootball.utils.FileUtils;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.daniribalbert.letsplayfootball.utils.ToastUtils;
import com.google.firebase.storage.UploadTask;

import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

/**
 * Dialog fragment used to add/edit a new league.
 */
public class DialogFragmentEditPlayer extends DialogFragmentViewPlayer implements View.OnClickListener {

    public static final String TAG = DialogFragmentEditPlayer.class.getSimpleName();

    private EditPlayerListener mListener;

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
                if (mPlayer.isGuest()){
                    mPlayer.rating.put(mLeagueId, mRating.getRating());
                }

                if (mImageUri == null) {
                    save(mPlayer);
                    tryAndCloseDialog();

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
            mListener.onPlayerSaved(player, mRating.getRating());
        }
    }

    private void uploadImage() {
        showProgress(true);
        FileUtils.uploadImage(mImageUri, new BaseUploadListener(mProgressBar) {

            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                if (downloadUrl != null) {
                    mPlayer.image = downloadUrl.toString();
                   save(mPlayer);
                    tryAndCloseDialog();

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
        if (handleImageSelectionActivityResult(requestCode, resultCode, data)){
            GlideUtils.loadCircularImage(mImageUri, mPlayerImage);
        }
    }

    public void setListener(EditPlayerListener listener) {
        mListener = listener;
    }

    public interface EditPlayerListener {
        void onPlayerSaved(Player player, float rating);
    }
}
