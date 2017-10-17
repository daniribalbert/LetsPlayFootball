package com.daniribalbert.letsplayfootball.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ProgressBar;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.LeagueCache;
import com.daniribalbert.letsplayfootball.data.firebase.LeagueDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.ui.fragments.BaseFragment;
import com.daniribalbert.letsplayfootball.ui.fragments.LeagueDetailsFragment;
import com.daniribalbert.letsplayfootball.ui.fragments.LeagueDetailsManagerFragment;
import com.google.firebase.database.DataSnapshot;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity which handles League viewing and edition feature.
 */
public class LeagueActivity extends BaseActivity {

    public static final String LEAGUE_TITLE = "LEAGUE_TITLE";

    @BindView(R.id.app_progress)
    ProgressBar mProgressBar;


    private String mLeagueId;
    private League mLeague;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league);
        ButterKnife.bind(this);

        loadArgs(getIntent());

        if (savedInstanceState == null) {
            loadLeague();
        }
    }

    private void loadArgs(final Intent intent) {
        if (intent == null) {
            return;
        }

        setTitle(intent.getStringExtra(LEAGUE_TITLE));
        mLeagueId = intent.getStringExtra(ARGS_LEAGUE_ID);
    }

    private void loadLeague() {
        LeagueDbUtils.getLeague(mLeagueId, new BaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLeague = dataSnapshot.getValue(League.class);
                LeagueCache.saveLeagueInfo(mLeague);
                String userId = mAuth.getCurrentUser().getUid();
                boolean viewMode = !mLeague.isOwner(userId);
                BaseFragment frag;
                String tag;
                if (viewMode) {
                    frag = LeagueDetailsFragment.newInstance(mLeague);
                    tag = LeagueDetailsFragment.TAG;
                } else {
                    frag = LeagueDetailsManagerFragment.newInstance(mLeague);
                    tag = LeagueDetailsManagerFragment.TAG;
                }
                frag.setProgress(mProgressBar);
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, frag, tag)
                                    .commit();
            }
        });
    }
}
