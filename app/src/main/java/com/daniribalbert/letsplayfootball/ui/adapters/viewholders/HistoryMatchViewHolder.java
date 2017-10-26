package com.daniribalbert.letsplayfootball.ui.adapters.viewholders;

import android.view.View;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.LeagueCache;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.Match;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Match View Holder
 */
public class HistoryMatchViewHolder extends MatchViewHolder implements View.OnClickListener {

    @BindView(R.id.match_card_league)
    TextView mLeagueName;

    public HistoryMatchViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        view.setOnClickListener(this);
    }

    @Override
    public void setMatch(Match match) {
        super.setMatch(match);
        League league = LeagueCache.getLeagueInfo(mMatch.leagueId);
        if (league != null) {
            mLeagueName.setText(league.title);
        }
    }
}
