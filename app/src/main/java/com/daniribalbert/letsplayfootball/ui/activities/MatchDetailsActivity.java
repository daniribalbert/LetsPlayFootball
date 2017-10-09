package com.daniribalbert.letsplayfootball.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.firebase.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.ui.fragments.DialogFragmentEditMatch;
import com.daniribalbert.letsplayfootball.ui.fragments.DialogFragmentEditPlayer;
import com.daniribalbert.letsplayfootball.ui.fragments.DialogFragmentViewMatch;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.daniribalbert.letsplayfootball.utils.ToastUtils;
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
        loadArgs();
        if (savedInstanceState == null) {
            loadMatchData();
        }
    }

    private void loadArgs() {
        Intent intent = getIntent();
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
                }
            }
        });
    }

    private void updateMatchLayout() {
        GlideUtils.loadCircularImage(mMatch.image, mMatchImageView);
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
                ToastUtils.show("will show team options", Toast.LENGTH_SHORT);
                break;
        }
    }

    protected void showMatchDialog() {
        String userId = getCurrentUser().getUid();
        DialogFragmentViewMatch dFrag = DialogFragmentViewMatch
                .newInstance(mLeagueId, mMatchId, userId);
        dFrag.show(getFragmentManager(), DialogFragmentViewMatch.TAG);
    }
}
