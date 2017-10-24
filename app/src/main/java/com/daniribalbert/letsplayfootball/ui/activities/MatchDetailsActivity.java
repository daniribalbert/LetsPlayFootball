package com.daniribalbert.letsplayfootball.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.ProgressBar;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.ui.constants.IntentConstants;
import com.daniribalbert.letsplayfootball.ui.fragments.MatchDetailsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity to show details of an Match.
 */
public class MatchDetailsActivity extends BaseActivity  {

    @BindView(R.id.app_progress)
    ProgressBar mProgressBar;

    protected String mLeagueId;
    protected String mMatchId;
    protected String mPlayerId;
    protected Match mMatch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league);
        ButterKnife.bind(this);
        loadArgs(getIntent());
        if (savedInstanceState == null) {
            loadFragment();
        }
    }

    protected void loadFragment() {
        Fragment frag = MatchDetailsFragment.newInstance(mMatchId, mLeagueId, mPlayerId);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, frag,
                                                               MatchDetailsFragment.TAG).commit();
    }

    private void loadArgs(final Intent intent) {
        if (intent != null) {
            mMatchId = intent.getStringExtra(IntentConstants.ARGS_MATCH_ID);
            mLeagueId = intent.getStringExtra(IntentConstants.ARGS_LEAGUE_ID);
            mPlayerId = intent.getStringExtra(IntentConstants.ARGS_PLAYER_ID);
        }
    }

}
