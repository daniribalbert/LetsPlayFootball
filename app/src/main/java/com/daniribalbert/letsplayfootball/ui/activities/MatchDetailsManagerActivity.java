package com.daniribalbert.letsplayfootball.ui.activities;

import android.support.v4.app.Fragment;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.ui.fragments.MatchDetailsManagerFragment;

/**
 * Activity to show details of an Match.
 */
public class MatchDetailsManagerActivity extends MatchDetailsActivity {

    @Override
    protected void loadFragment() {
        Fragment frag = MatchDetailsManagerFragment.newInstance(mMatchId, mLeagueId, mPlayerId);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, frag, MatchDetailsManagerFragment.TAG)
                .commit();
    }
}
