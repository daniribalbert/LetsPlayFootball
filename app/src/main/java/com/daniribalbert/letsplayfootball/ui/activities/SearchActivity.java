package com.daniribalbert.letsplayfootball.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.ui.constants.IntentConstants;
import com.daniribalbert.letsplayfootball.ui.fragments.LeagueSearchFragment;
import com.daniribalbert.letsplayfootball.ui.fragments.PlayerSearchFragment;

/**
 * Search Activity.
 */
public class SearchActivity extends BaseActivity {

    public static final String ARGS_TAG = "ARGS_TAG";

    private String mFragTag;

    private String mLeagueId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        if (savedInstanceState == null) {
            loadArgs(getIntent());
        }
        setupSearchFragment();
    }

    private void loadArgs(Intent intent) {
        if (intent == null) {
            return;
        }
        mFragTag = intent.getStringExtra(ARGS_TAG);
        mLeagueId = intent.getStringExtra(IntentConstants.ARGS_LEAGUE_ID);
    }

    private void setupSearchFragment() {
        if (mFragTag.equalsIgnoreCase(PlayerSearchFragment.TAG)) {
            PlayerSearchFragment frag = PlayerSearchFragment.newInstance(mLeagueId);
            getSupportFragmentManager().beginTransaction()
                                       .replace(R.id.search_fragment, frag, mFragTag)
                                       .commit();
        } else if (mFragTag.equalsIgnoreCase(LeagueSearchFragment.TAG)) {
            LeagueSearchFragment frag = LeagueSearchFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                                       .replace(R.id.search_fragment, frag, mFragTag)
                                       .commit();
        }
    }
}
