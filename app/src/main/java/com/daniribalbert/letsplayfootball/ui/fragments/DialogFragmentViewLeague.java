package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.firebase.LeagueDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseUploadListener;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.utils.FileUtils;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.daniribalbert.letsplayfootball.utils.ToastUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.UploadTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Dialog fragment used to view a league.
 */
public class DialogFragmentViewLeague extends BaseDialogFragment implements View.OnClickListener {

    public static final String TAG = DialogFragmentViewLeague.class.getSimpleName();

    public static final String ARGS_LEAGUE = "ARGS_LEAGUE";

    @BindView(R.id.edit_league_pic)
    ImageView mLeagueImage;

    @BindView(R.id.edit_league_title)
    EditText mLeagueTitle;

    @BindView(R.id.edit_league_description)
    EditText mLeagueDescription;

    @BindView(R.id.bt_save_league)
    Button mSaveLeague;

    protected League mLeague;

    @BindView(R.id.dialog_progress)
    ProgressBar mProgressBar;

    public static DialogFragmentViewLeague newInstance() {
        DialogFragmentViewLeague dFrag = new DialogFragmentViewLeague();
        dFrag.setRetainInstance(true);
        return dFrag;
    }

    public static DialogFragmentViewLeague newInstance(String leagueId) {
        Bundle bundle = new Bundle();
        DialogFragmentViewLeague dFrag = new DialogFragmentViewLeague();
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
        mUnbinder =  ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSaveLeague.setOnClickListener(this);
        mSaveLeague.setText(R.string.close);
        Bundle args = getArguments();
        if (savedInstanceState == null) {
            if (args != null) {
                if (!args.containsKey(ARGS_LEAGUE)) {
                    tryAndCloseDialog();
                    return;
                }
                String leagueId = args.getString(ARGS_LEAGUE);
                loadLeagueData(leagueId);
            }
        } else {
            if (mImageUri != null) {
                GlideUtils.loadCircularImage(mImageUri, mLeagueImage);
            } else if (mLeague != null && mLeague.hasImage()) {
                GlideUtils.loadCircularImage(mLeague.image, mLeagueImage);
            }
        }
    }

    protected void loadLeagueData(String leagueId) {
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_save_league:
                tryAndCloseDialog();
                break;
        }
    }

}
