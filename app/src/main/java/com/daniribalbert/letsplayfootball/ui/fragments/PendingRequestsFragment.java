package com.daniribalbert.letsplayfootball.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.LeagueCache;
import com.daniribalbert.letsplayfootball.data.cache.PlayersCache;
import com.daniribalbert.letsplayfootball.data.firebase.RequestsDbUtils;
import com.daniribalbert.letsplayfootball.data.model.JoinLeagueRequest;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.adapters.PendingRequestsAdapter;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment which lists the player pending requests.
 */
public class PendingRequestsFragment extends BaseFragment {

    public static final String TAG = PendingRequestsFragment.class.getSimpleName();

    @BindView(R.id.requests_recyclerview)
    RecyclerView mRecyclerView;

    PendingRequestsAdapter mAdapter;

    public static PendingRequestsFragment newInstance() {
        Bundle args = new Bundle();

        PendingRequestsFragment fragment = new PendingRequestsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pending_requests_list, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Context context = view.getContext();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter == null) {
            mAdapter = new PendingRequestsAdapter();
            mRecyclerView.setAdapter(mAdapter);
            loadData();
        } else {
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    private void loadData() {
        showProgress(true);
        String playerId = getBaseActivity().getCurrentUser().getUid();
        Player player = PlayersCache.getPlayerInfo(playerId);
        List<String> myManagedLeagues = new LinkedList<>();
        for (String leagueId : player.leagues.keySet()) {
            League league = LeagueCache.getLeagueInfo(leagueId);
            if (league.isOwner(playerId)) {
                myManagedLeagues.add(leagueId);
            }
        }
        RequestsDbUtils.loadMyRequests(playerId, myManagedLeagues, new RequestsDbUtils.Listener() {
            @Override
            public void onLoadFinished(List<JoinLeagueRequest> pendingRequests) {
                mAdapter.clear();
                mAdapter.addItems(pendingRequests);
                showProgress(false);
            }
        });
    }

}
