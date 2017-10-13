package com.daniribalbert.letsplayfootball.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.PlayersCache;
import com.daniribalbert.letsplayfootball.data.firebase.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.activities.BaseActivity;
import com.daniribalbert.letsplayfootball.ui.activities.MatchDetailsActivity;
import com.daniribalbert.letsplayfootball.ui.adapters.MatchListAdapter;
import com.daniribalbert.letsplayfootball.ui.events.OpenMatchEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Match history fragment class.
 */
public class MatchUpcomingFragment extends MatchHistoryFragment {

    public static final String TAG = MatchUpcomingFragment.class.getSimpleName();

    public static MatchUpcomingFragment newInstance() {
        Bundle args = new Bundle();

        MatchUpcomingFragment fragment = new MatchUpcomingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void loadMatches() {
        Player player = PlayersCache.getPlayerInfo(getBaseActivity().getCurrentUser().getUid());
        if (player == null) {
            return;
        }
        showProgress(true);
        Set<String> leagueIds = player.leagues.keySet();
        for (String leagueId : leagueIds) {
            MatchDbUtils.getUpcomingMatches(leagueId, new BaseValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    showProgress(false);
                    mAdapter.clear();
                    for (DataSnapshot next : dataSnapshot.getChildren()) {
                        Match match = next.getValue(Match.class);
                        mAdapter.addMatch(match);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    super.onCancelled(databaseError);
                    showProgress(false);
                }
            });
        }
    }

    private void loadArgs() {
    }

}
