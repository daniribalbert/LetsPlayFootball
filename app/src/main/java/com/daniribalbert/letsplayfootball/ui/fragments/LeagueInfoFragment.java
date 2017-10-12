package com.daniribalbert.letsplayfootball.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.PlayersCache;
import com.daniribalbert.letsplayfootball.data.firebase.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.PlayerDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.activities.BaseActivity;
import com.daniribalbert.letsplayfootball.ui.activities.MatchDetailsActivity;
import com.daniribalbert.letsplayfootball.ui.adapters.LeagueItemListAdapter;
import com.daniribalbert.letsplayfootball.ui.events.OpenMatchEvent;
import com.daniribalbert.letsplayfootball.ui.events.PlayerClickedEvent;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing the core league items.
 */
public class LeagueInfoFragment extends BaseFragment {

    public static final String TAG = LeagueInfoFragment.class.getSimpleName();

    public static final String LEAGUE_ID = "LEAGUE_ID";
    public static final String ARGS_LEAGUE_VIEW_MODE = "ARGS_LEAGUE_VIEW_MODE";

    @BindView(R.id.league_items_recyclerview)
    RecyclerView mRecyclerView;
    LeagueItemListAdapter mAdapter;

    String mLeagueId;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LeagueInfoFragment() {
    }

    /**
     * Creates a new instance of PlayerListFragment with a list of players based from the League
     * with the given leagueId.
     *
     * @param leagueId league_id of the League which players will be loaded.
     *
     * @return new instance of this Fragment.
     */
    public static LeagueInfoFragment newInstance(String leagueId) {
        Bundle args = new Bundle();
        args.putString(LEAGUE_ID, leagueId);
        LeagueInfoFragment fragment = new LeagueInfoFragment();
        fragment.setRetainInstance(true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mLeagueId = args.getString(LEAGUE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_league_info, container, false);
        ButterKnife.bind(this, view);

        return view;
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
        EventBus.getDefault().register(this);
        if (mRecyclerView.getAdapter() == null) {
            if (mAdapter == null) {
                mAdapter = new LeagueItemListAdapter(mLeagueId);
            }
            mRecyclerView.setAdapter(mAdapter);
            loadData();
        } else {
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRecyclerView.setAdapter(null);
    }

    protected void loadData() {
        showProgress(true);
        loadNextMatch();
        loadPlayers();
    }

    protected void loadNextMatch() {
        MatchDbUtils.getUpcomingMatch(mLeagueId, new BaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogUtils.w("DATA: " + dataSnapshot);
                if (mAdapter != null) {
                    Iterator<DataSnapshot> iterator = dataSnapshot
                            .getChildren()
                            .iterator();

                    List<Match> upcomingMatches = new ArrayList<Match>();
                    mAdapter.clearUpcomingMatches();
                    while (iterator.hasNext()) {
                        DataSnapshot next = iterator.next();
                        Match match = next.getValue(Match.class);
                        if (match != null) {
                            upcomingMatches.add(match);
                            mAdapter.addMatch(match);
                        }
                    }

                }
            }
        });
    }

    protected void loadPlayers() {
        PlayerDbUtils
                .getPlayersFromLeague(mLeagueId,
                                      new BaseValueEventListener() {
                                          @Override
                                          public void onDataChange(DataSnapshot dataSnapshot) {
                                              LogUtils.w(dataSnapshot.toString());
                                              Iterator<DataSnapshot> iterator = dataSnapshot
                                                      .getChildren()
                                                      .iterator();

                                              List<Player> players = new ArrayList<Player>();
                                              while (iterator.hasNext()) {
                                                  DataSnapshot next = iterator.next();
                                                  Player player = next.getValue(Player.class);
                                                  if (player != null) {
                                                      players.add(player);
                                                  }
                                              }

                                              if (mAdapter != null) {
                                                  mAdapter.clearPlayers();
                                                  mAdapter.addPlayers(players);
                                              }
                                              showProgress(false);
                                          }

                                          @Override
                                          public void onCancelled(
                                                  DatabaseError databaseError) {
                                              super.onCancelled(databaseError);
                                              showProgress(false);
                                          }
                                      });
    }

    @Subscribe
    public void OnPlayerSelectedEvent(PlayerClickedEvent event) {
        final Player player = event.player;

        DialogFragmentViewPlayer dFrag = DialogFragmentViewPlayer
                .newInstance(mLeagueId, player.id);
        dFrag.show(getFragmentManager(), DialogFragmentViewPlayer.TAG);
    }

    @Subscribe
    public void OnMatchSelectedEvent(OpenMatchEvent event) {
        String currentUserId = getBaseActivity().getCurrentUser().getUid();
        PlayersCache.saveLeaguePlayersInfo(mAdapter.getPlayers());

        Intent intent = getMatchDetailsIntent();
        intent.putExtra(BaseActivity.ARGS_LEAGUE_ID, mLeagueId);
        intent.putExtra(BaseActivity.ARGS_MATCH_ID, event.matchId);
        intent.putExtra(BaseActivity.ARGS_PLAYER_ID, currentUserId);
        startActivity(intent);
    }

    @Subscribe
    public void onPlayerUpdatedEvent(Player player){
        mAdapter.updatePlayer(player);
    }

    protected Intent getMatchDetailsIntent() {
        return new Intent(getActivity(), MatchDetailsActivity.class);
    }

}
