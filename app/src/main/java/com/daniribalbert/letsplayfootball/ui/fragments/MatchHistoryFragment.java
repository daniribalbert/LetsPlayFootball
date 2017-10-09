package com.daniribalbert.letsplayfootball.ui.fragments;

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
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.adapters.MatchListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mRecyclerView.setAdapter(mAdapter);
        loadMatchHistory();
    }

    private void loadMatchHistory() {
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
                    Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                    while (it.hasNext()) {
                        DataSnapshot next = it.next();
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
