package com.daniribalbert.letsplayfootball.ui.activities;

import android.view.View;

import com.daniribalbert.letsplayfootball.data.firebase.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.ui.fragments.DialogFragmentEditMatch;
import com.daniribalbert.letsplayfootball.ui.fragments.DialogFragmentEditPlayer;

/**
 * Activity to show details of an Match.
 */
public class MatchDetailsManagerActivity extends MatchDetailsActivity implements View.OnClickListener {

    @Override
    protected void showMatchDialog() {
        String userId = getCurrentUser().getUid();
        DialogFragmentEditMatch dFrag = DialogFragmentEditMatch
                .newInstance(mLeagueId, mMatchId, userId);
        dFrag.setListener(new DialogFragmentEditMatch.EditMatchListener() {
            @Override
            public void onMatchSaved(Match match) {
                MatchDbUtils.updateMatch(match);
            }
        });
        dFrag.show(getFragmentManager(), DialogFragmentEditPlayer.TAG);
    }
}
