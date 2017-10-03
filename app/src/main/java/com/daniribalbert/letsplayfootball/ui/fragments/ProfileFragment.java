package com.daniribalbert.letsplayfootball.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.database.PlayerDbUtils;
import com.daniribalbert.letsplayfootball.data.database.listeners.BaseUploadListener;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.events.FabClickedEvent;
import com.daniribalbert.letsplayfootball.utils.ActivityUtils;
import com.daniribalbert.letsplayfootball.utils.FileUtils;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.daniribalbert.letsplayfootball.utils.GsonUtils;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.daniribalbert.letsplayfootball.utils.ToastUtils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static android.app.Activity.RESULT_OK;

/**
 * User profile fragment class.
 * Provides extra management functionality to the ProfileViewFragment.
 */
public class ProfileFragment extends ProfileViewFragment
        implements TextWatcher, ValueEventListener, View.OnClickListener {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private Uri mImageUri;

    /**
     * Indicates whether or not the user has changed his profile info.
     */
    private boolean mDataChanged;

    public static ProfileFragment newInstance(@NonNull FirebaseUser user) {
        Bundle args = new Bundle();

        String playerJson = GsonUtils.toJson(Player.fromFirebase(user));
        args.putString(ARGS_PLAYER, playerJson);

        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            String playerJsonStr = args.getString(ARGS_PLAYER);
            this.mPlayer = GsonUtils.fromJson(playerJsonStr, Player.class);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPlayerName.addTextChangedListener(this);
        mPlayerNickname.addTextChangedListener(this);
        mProfilePic.setOnClickListener(this);
        mProfilePic.setClickable(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        PlayerDbUtils.getPlayer(mPlayer.id, this);
    }

    @Subscribe
    public void onFabClicked(FabClickedEvent event) {
        toggleEditMode();
        if (mPlayerName.isEnabled()) {
            event.fab.setImageResource(R.drawable.ic_check);
            mDataChanged = false;
        } else {
            event.fab.setImageResource(android.R.drawable.ic_menu_edit);
            if (mDataChanged) {
                if (mImageUri != null) {
                    uploadImage();
                } else {
                    savePlayerInfo();
                }
            }
        }
    }

    private void savePlayerInfo() {
        mPlayer.nickname = mPlayerNickname.getText().toString();
        mPlayer.name = mPlayerName.getText().toString();
        LogUtils.i("Player info updated! " + mPlayer);
        PlayerDbUtils.updatePlayer(mPlayer);
    }

    public void toggleEditMode() {
        boolean toggle = !mPlayerName.isEnabled();

        mPlayerName.setEnabled(toggle);
        mPlayerNickname.setEnabled(toggle);
        mProfilePic.setClickable(toggle);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        mDataChanged = true;
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        mPlayer = dataSnapshot.getValue(Player.class);
        if (mPlayer != null) {
            mPlayerName.setText(mPlayer.getName());
            mPlayerNickname.setText(mPlayer.getDisplayName());
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    public void onClick(View view) {
        promptSelectImage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ARGS_IMAGE_SELECT) {
                mImageUri = ActivityUtils.extractImageUri(data, getActivity());
                GlideUtils.loadCircularImage(mImageUri, mProfilePic);
            }
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
                    savePlayerInfo();
                    ToastUtils.show(R.string.toast_profile_saved, Toast.LENGTH_SHORT);
                } else {
                    ToastUtils.show(R.string.toast_error_generic, Toast.LENGTH_SHORT);
                }
                showProgress(false);
            }
        });
    }
}
