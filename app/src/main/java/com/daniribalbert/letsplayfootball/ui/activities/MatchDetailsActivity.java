package com.daniribalbert.letsplayfootball.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.firebase.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.ui.fragments.DialogFragmentPostMatch;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.daniribalbert.letsplayfootball.utils.GsonUtils;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.firebase.database.DataSnapshot;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity to show details of an Match.
 */
public class MatchDetailsActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.match_card_view)
    View mMatchCardView;

    @BindView(R.id.match_card_image)
    ImageView mMatchImageView;

    @BindView(R.id.match_card_time)
    TextView mMatchCardTime;

    @BindView(R.id.match_card_day)
    TextView mMatchCardDay;

    @BindView(R.id.match_teams_bt)
    View mTeamsBt;

    @BindView(R.id.app_progress)
    ProgressBar mProgressBar;


    protected String mLeagueId;
    protected String mMatchId;
    protected String mPlayerId;
    protected Match mMatch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_details);
        ButterKnife.bind(this);
        setupListeners();
        loadArgs(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.VISIBLE);
        loadMatchData();
    }

    private void loadArgs(final Intent intent) {
        if (intent != null) {
            mMatchId = intent.getStringExtra(ARGS_MATCH_ID);
            mLeagueId = intent.getStringExtra(ARGS_LEAGUE_ID);
            mPlayerId = intent.getStringExtra(ARGS_PLAYER_ID);
        }
    }

    protected void setupListeners() {
        mMatchCardView.setOnClickListener(this);
        mTeamsBt.setOnClickListener(this);
    }

    protected void loadMatchData() {
        MatchDbUtils.getMatch(mLeagueId, mMatchId, new BaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMatch = dataSnapshot.getValue(Match.class);

                if (mMatch != null) {
                    updateMatchLayout();
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void updateMatchLayout() {
        GlideUtils.loadCircularImage(mMatch.getImage(), mMatchImageView);
        mMatchCardDay.setText(mMatch.getDateString(mMatch.time));
        mMatchCardTime.setText(mMatch.getTimeStr(mMatch.time));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.match_card_view:
                showMatchDialog();
                break;
            case R.id.match_teams_bt:
                Intent intent = new Intent(this, TeamsActivity.class);
                intent.putExtra(ARGS_LEAGUE_ID, mLeagueId);
                intent.putExtra(TeamsActivity.ARG_MATCH, GsonUtils.toJson(mMatch));
                startActivity(intent);
                break;
        }
    }

    protected void showMatchDialog() {
        String userId = getCurrentUser().getUid();
        DialogFragmentPostMatch dFrag = DialogFragmentPostMatch
                .newInstance(mLeagueId, mMatchId, userId);
        dFrag.show(getSupportFragmentManager(), DialogFragmentPostMatch.TAG);
    }
}
