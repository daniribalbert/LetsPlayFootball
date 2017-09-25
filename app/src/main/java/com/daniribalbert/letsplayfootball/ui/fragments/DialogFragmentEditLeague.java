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
import com.daniribalbert.letsplayfootball.data.database.StorageUtils;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.utils.FileUtils;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
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
public class DialogFragmentEditLeague extends DialogFragment implements View.OnClickListener {

    public static final String TAG = DialogFragmentEditLeague.class.getSimpleName();

    public static final String ARGS_LEAGUE = "ARGS_LEAGUE";

    public static final int ARGS_IMAGE_SELECT = 201;

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

    private ProgressBar mProgressBar;

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
        View rootView = inflater.inflate(R.layout.fragment_add_league, container, false);
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
        LeagueDbUtils.getLeague(leagueId, new ValueEventListener() {
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                mLeague.image = downloadUrl.toString();
                mListener.onLeagueSaved(mLeague);
                if (getDialog() != null) {
                    dismiss();
                }
                showProgress(false);
                ToastUtils.show(R.string.toast_profile_saved, Toast.LENGTH_SHORT);
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
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                if (isCamera) {
                    mImageUri = Uri.fromFile(imageFile);
                } else {
                    mImageUri = data == null ? null : data.getData();
                }

                GlideUtils.loadCircularImage(mImageUri, mLeagueImage);
            }
        }
    }

    public void setListener(EditLeagueListener listener) {
        mListener = listener;
    }

    public void setProgressBar(ProgressBar progress) { mProgressBar = progress; }

    private void showProgress(boolean show){
        if (mProgressBar != null) {
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void promptSelectImage() {
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
