package com.daniribalbert.letsplayfootball.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.database.LeagueDbUtils;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.ui.events.FabClickedEvent;
import com.daniribalbert.letsplayfootball.ui.fragments.DialogFragmentEditLeague;
import com.daniribalbert.letsplayfootball.ui.fragments.PlayerListFragment;
import com.daniribalbert.letsplayfootball.ui.fragments.PlayerSearchFragment;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity which handles League viewing and edition feature.
 */
public class LeagueActivity extends BaseActivity
        implements DialogFragmentEditLeague.EditLeagueListener, View.OnClickListener {

    public static final String LEAGUE_ID = "LEAGUE_ID";
    public static final String LEAGUE_TITLE = "LEAGUE_TITLE";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @BindView(R.id.fab_menu_layout)
    View mFabLayout;

    @BindView(R.id.fab_menu_1)
    FloatingActionButton mFabMenu1;

    @BindView(R.id.fab_menu_2)
    FloatingActionButton mFabMenu2;

    @BindView(R.id.fab_menu_3)
    FloatingActionButton mFabMenu3;

    private String mLeagueId;

    @BindView(R.id.app_progress)
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league);
        ButterKnife.bind(this);

        loadArgs(getIntent());
        setSupportActionBar(mToolbar);

        mFab.setOnClickListener(this);
        mFabMenu1.setOnClickListener(this);
        mFabMenu2.setOnClickListener(this);
        mFabMenu3.setOnClickListener(this);

        if (savedInstanceState == null) {
            PlayerListFragment frag = PlayerListFragment.newInstance(mLeagueId);
            frag.setProgress(mProgressBar);
            getFragmentManager().beginTransaction()
                                .add(R.id.fragment_container, frag, PlayerListFragment.TAG)
                                .commit();
        }
    }

    private void loadArgs(final Intent intent) {
        if (intent == null) {
            return;
        }

        setTitle(intent.getStringExtra(LEAGUE_TITLE));
        mLeagueId = intent.getStringExtra(LEAGUE_ID);
    }

    @Override
    public void onLeagueSaved(League league) {
        LeagueDbUtils.updateLeague(league);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                toggleFabMenu();
                break;
            case R.id.fab_menu_1:
                mFab.setImageResource(R.drawable.ic_add);
                mFab.setVisibility(View.GONE);
                mFabLayout.setVisibility(View.GONE);
                PlayerSearchFragment playerSearchFragment = PlayerSearchFragment
                        .newInstance(mLeagueId);
                playerSearchFragment.setProgress(mProgressBar);
                getFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, playerSearchFragment,
                                             PlayerSearchFragment.TAG)
                                    .addToBackStack(PlayerSearchFragment.TAG).commit();
                break;
            case R.id.fab_menu_2:
                EventBus.getDefault().post(new FabClickedEvent(mFab));
                break;
            case R.id.fab_menu_3:
                // TODO: Add Match
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mFabLayout.getVisibility() == View.VISIBLE) {
            toggleFabMenu();
        } else {
            mFab.setVisibility(View.VISIBLE);
            super.onBackPressed();
        }
    }

    private void toggleFabMenu() {
        boolean isVisible = mFabLayout.getVisibility() == View.VISIBLE;
        if (isVisible) {
            mFabLayout.setVisibility(View.GONE);
            mFab.setImageResource(R.drawable.ic_add);
        } else {
            mFabLayout.setVisibility(View.VISIBLE);
            mFab.setImageResource(R.drawable.ic_close);
        }
    }
}
