package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.database.PlayerDbUtils;
import com.daniribalbert.letsplayfootball.data.database.StorageUtils;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.utils.FileUtils;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.daniribalbert.letsplayfootball.utils.ToastUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

/**
 * Dialog fragment used to add/edit a new league.
 */
public class DialogFragmentEditPlayer extends DialogFragment implements View.OnClickListener {

    public static final String TAG = DialogFragmentEditPlayer.class.getSimpleName();

    public static final String ARGS_LEAGUE_ID = "ARGS_LEAGUE_ID";
    public static final String ARGS_PLAYER = "ARGS_PLAYER";

    public static final int ARGS_IMAGE_SELECT = 201;

    @BindView(R.id.edit_player_pic)
    ImageView mPlayerImage;

    @BindView(R.id.edit_player_name)
    EditText mPlayerName;

    @BindView(R.id.edit_player_nickname)
    EditText mPlayerNickname;

    @BindView(R.id.player_rating)
    RatingBar mRating;

    @BindView(R.id.bt_save_player)
    View mSavePlayer;

    private EditPlayerListener mListener;
    private Player mPlayer;

    private Uri mImageUri;

    @BindView(R.id.dialog_progress)
    ProgressBar mProgressBar;

    private String mLeagueId;
    private String mPlayerId;

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

    private void loadPlayerData(String playerId) {
        showProgress(true);
        PlayerDbUtils.getPlayer(playerId, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mPlayer = dataSnapshot.getValue(Player.class);

                if (mPlayer != null) {
                    mPlayerName.setText(mPlayer.name);
                    mPlayerNickname.setText(mPlayer.nickname);
                    mRating.setRating(mPlayer.getRating(mLeagueId));
                    if (mPlayer.hasImage()) {
                        GlideUtils.loadCircularImage(mPlayer.image, mPlayerImage);
                    }
                }
                showProgress(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showProgress(false);
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
            case R.id.bt_save_player:
                showProgress(true);

                mPlayer.name = mPlayerName.getText().toString();
                mPlayer.nickname = mPlayerNickname.getText().toString();
                mPlayer.setRating(mLeagueId, mRating.getRating());

                if (mImageUri == null) {
                    mListener.onPlayerSaved(mPlayer);
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

    private void uploadImage() {
        showProgress(true);
        StorageReference ref = StorageUtils.getRef();
        StorageReference fileRef = ref.child(mImageUri.getLastPathSegment());
        UploadTask uploadTask = fileRef.putFile(mImageUri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                ToastUtils.show(R.string.toast_error_generic, Toast.LENGTH_SHORT);
                showProgress(false);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                if (downloadUrl != null) {
                    mPlayer.image = downloadUrl.toString();
                    mListener.onPlayerSaved(mPlayer);
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
            File imageFile = FileUtils.getTempFile(getActivity());
            if (requestCode == ARGS_IMAGE_SELECT) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                if (isCamera) {
                    mImageUri = Uri.fromFile(imageFile);
                } else {
                    mImageUri = data == null ? null : data.getData();
                }

                GlideUtils.loadCircularImage(mImageUri, mPlayerImage);
            }
        }
    }

    public void setListener(EditPlayerListener listener) {
        mListener = listener;
    }

    private void showProgress(boolean show) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void promptSelectImage() {
        // Determine Uri of camera image to save.
        final File tempFile = FileUtils.getTempFile(getActivity());

        final PackageManager pManager = getActivity().getPackageManager();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

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

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    public interface EditPlayerListener {
        void onPlayerSaved(Player player);
    }
}
