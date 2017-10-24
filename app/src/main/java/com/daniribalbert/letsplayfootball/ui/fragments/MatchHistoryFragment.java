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
import com.daniribalbert.letsplayfootball.ui.activities.MatchDetailsActivity;
import com.daniribalbert.letsplayfootball.ui.adapters.MatchListAdapter;
import com.daniribalbert.letsplayfootball.ui.constants.IntentConstants;
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
public class MatchHistoryFragment extends BaseFragment {

    public static final String TAG = MatchHistoryFragment.class.getSimpleName();

    @BindView(R.id.my_matches_recyclerview)
    RecyclerView mRecyclerView;

    protected MatchListAdapter mAdapter = new MatchListAdapter();

    public static MatchHistoryFragment newInstance() {
        Bundle args = new Bundle();

        MatchHistoryFragment fragment = new MatchHistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadArgs();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_match_list, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        mRecyclerView.setAdapter(mAdapter);
        loadMatches();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    protected void loadMatches() {
        Player player = PlayersCache.getPlayerInfo(getBaseActivity().getCurrentUser().getUid());
        if (player == null) {
            return;
        }
        showProgress(true);
        Set<String> leagueIds = player.leagues.keySet();
        for (String leagueId : leagueIds) {
            MatchDbUtils.getPastMatches(leagueId, new BaseValueEventListener() {
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

    @Subscribe
    public void onMatchSelected(OpenMatchEvent event){
        String playerId = getBaseActivity().getCurrentUser().getUid();
        Intent intent = new Intent(getActivity(), MatchDetailsActivity.class);
        intent.putExtra(IntentConstants.ARGS_MATCH_ID, event.matchId);
        intent.putExtra(IntentConstants.ARGS_PLAYER_ID, playerId);
        intent.putExtra(IntentConstants.ARGS_LEAGUE_ID, event.leagueId);
        startActivity(intent);
    }

}
