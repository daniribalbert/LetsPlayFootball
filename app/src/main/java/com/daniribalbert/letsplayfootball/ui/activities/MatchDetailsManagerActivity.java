package com.daniribalbert.letsplayfootball.ui.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.firebase.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.ui.fragments.DialogFragmentEditMatch;
import com.daniribalbert.letsplayfootball.ui.fragments.DialogFragmentEditPlayer;
import com.daniribalbert.letsplayfootball.ui.fragments.MatchDetailsFragment;
import com.daniribalbert.letsplayfootball.ui.fragments.MatchDetailsManagerFragment;
import com.daniribalbert.letsplayfootball.utils.GsonUtils;

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
