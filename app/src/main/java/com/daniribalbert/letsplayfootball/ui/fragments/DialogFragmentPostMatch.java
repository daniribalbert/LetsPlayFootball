package com.daniribalbert.letsplayfootball.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.LeagueCache;
import com.daniribalbert.letsplayfootball.data.firebase.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseUploadListener;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.ui.constants.IntentConstants;
import com.daniribalbert.letsplayfootball.utils.FileUtils;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.daniribalbert.letsplayfootball.utils.ToastUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.UploadTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Dialog fragment used to add/edit the next match.
 */
public class DialogFragmentPostMatch extends BaseDialogFragment implements View.OnClickListener {

    public static final String TAG = DialogFragmentPostMatch.class.getSimpleName();

    @BindView(R.id.edit_match_pic)
    ImageView mMatchImage;

    @BindView(R.id.edit_match_time_day)
    TextView mMatchDay;
    @BindView(R.id.edit_match_time_hour)
    TextView mMatchHour;

    @BindView(R.id.match_description_et)
    EditText mMatchDescription;

    @BindView(R.id.bt_save_match)
    Button mSaveMatch;

    @BindView(R.id.dialog_progress)
    ProgressBar mProgressBar;

    protected Match mMatch;

    protected String mMatchId;
    protected String mLeagueId;
    protected String mPlayerId;

    public static DialogFragmentPostMatch newInstance(String leagueId) {
        Bundle bundle = new Bundle();
        bundle.putString(IntentConstants.ARGS_LEAGUE_ID, leagueId);

        DialogFragmentPostMatch dFrag = new DialogFragmentPostMatch();
        dFrag.setRetainInstance(true);
        dFrag.setArguments(bundle);
        return dFrag;
    }

    public static DialogFragmentPostMatch newInstance(String leagueId, String matchId,
                                                      String playerId) {
        Bundle bundle = new Bundle();
        bundle.putString(IntentConstants.ARGS_LEAGUE_ID, leagueId);
        bundle.putString(IntentConstants.ARGS_MATCH_ID, matchId);
        bundle.putString(IntentConstants.ARGS_PLAYER_ID, playerId);

        DialogFragmentPostMatch dFrag = new DialogFragmentPostMatch();
        dFrag.setArguments(bundle);
        dFrag.setRetainInstance(true);
        return dFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadArgs();
    }

    private void loadArgs() {
        Bundle args = getArguments();
        if (args != null) {
            mMatchId = args.getString(IntentConstants.ARGS_MATCH_ID);
            mLeagueId = args.getString(IntentConstants.ARGS_LEAGUE_ID);
            mPlayerId = args.getString(IntentConstants.ARGS_PLAYER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post_match, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            loadMatchData(mLeagueId, mMatchId);
        }
    }

    protected void loadMatchData(String leagueId, final String matchId) {
        MatchDbUtils.getMatch(leagueId, matchId, new BaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMatch = dataSnapshot.getValue(Match.class);

                if (mMatch != null) {
                    updateLayout();
                    updatedTimeText();
                    updateMatchImage();
                }
            }
        });
    }

    private void updateMatchImage() {
        if (mMatch.hasImage()) {
            GlideUtils.loadCircularImage(mMatch.getImage(), mMatchImage);
        }
    }

    private void updateLayout() {
        League league = LeagueCache.getLeagueInfo(mMatch.leagueId);
        mSaveMatch.setOnClickListener(this);
        mMatchDescription.setText(mMatch.description);
        if (league != null && league.isOwner(mPlayerId)) {
            mMatchDescription.setEnabled(true);
            mMatchImage.setOnClickListener(this);
            mSaveMatch.setText(R.string.save);
        } else {
            mMatchDescription.setEnabled(false);
            mSaveMatch.setText(R.string.close);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_save_match:
                if (mImageUri == null) {
                    saveMatch();
                    tryAndCloseDialog();
                } else {
                    uploadImage();
                }
                break;
            case R.id.edit_match_pic:
                promptSelectImage();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (handleImageSelectionActivityResult(requestCode, resultCode, data)) {
            GlideUtils.loadCircularImage(mImageUri, mMatchImage);
        }
    }

    protected void updatedTimeText() {
        mMatchDay.setText(mMatch.getDateString(mMatch.time));
        mMatchHour.setText(mMatch.getTimeStr(mMatch.time));
    }

    private void uploadImage() {
        showProgress(true);
        FileUtils.uploadImage(mImageUri, new BaseUploadListener(mProgressBar) {

            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                if (downloadUrl != null) {
                    mMatch.image = downloadUrl.toString();
                    saveMatch();
                    tryAndCloseDialog();
                    ToastUtils.show(R.string.toast_generic_saved, Toast.LENGTH_SHORT);
                } else {
                    ToastUtils.show(R.string.toast_error_generic, Toast.LENGTH_SHORT);
                }
                showProgress(false);
            }
        });
    }

    private void saveMatch() {
        if (hasContentChanged()) {
            mMatch.description = mMatchDescription.getText().toString();
            MatchDbUtils.updatePostMatch(mMatch);
        }
    }

    private boolean hasContentChanged() {
        return mImageUri != null || !mMatchDescription.getText().toString()
                                                      .equalsIgnoreCase(mMatch.description);
    }

}
