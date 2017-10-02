package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.database.LeagueDbUtils;
import com.daniribalbert.letsplayfootball.data.database.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.database.PlayerDbUtils;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.data.model.SimpleLeague;
import com.daniribalbert.letsplayfootball.ui.adapters.LeagueItemListAdapter;
import com.daniribalbert.letsplayfootball.ui.events.FabClickedEvent;
import com.daniribalbert.letsplayfootball.ui.events.OpenMatchEvent;
import com.daniribalbert.letsplayfootball.ui.events.OpenPlayerEvent;
import com.daniribalbert.letsplayfootball.ui.events.RemoveMatchEvent;
import com.daniribalbert.letsplayfootball.ui.events.RemovePlayerEvent;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

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

    private void loadNextMatch() {
        MatchDbUtils.getUpcomingMatch(mLeagueId, new ValueEventListener() {
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadPlayers() {
        PlayerDbUtils
                .getPlayersFromLeague(mLeagueId,
                                      new ValueEventListener() {
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
                                              showProgress(false);
                                          }
                                      });
    }

    @Subscribe
    public void onFabClicked(FabClickedEvent event) {
        switch (event.fab.getId()) {
            case R.id.fab_menu_2: // Add new player to league.
                DialogFragmentEditPlayer dFrag = DialogFragmentEditPlayer.newInstance(mLeagueId);
                dFrag.setListener(new DialogFragmentEditPlayer.EditPlayerListener() {
                    @Override
                    public void onPlayerSaved(Player player) {
                        player.leagues.put(mLeagueId, new SimpleLeague(mLeagueId));
                        PlayerDbUtils.createGuestPlayer(player);
                        mAdapter.addPlayer(player);
                    }
                });
                dFrag.show(getFragmentManager(), DialogFragmentEditPlayer.TAG);
                break;
            case R.id.fab_menu_3: // Add new match to the League.
                DialogFragmentEditMatch dFragMatch = DialogFragmentEditMatch.newInstance(mLeagueId);
                dFragMatch.setListener(new DialogFragmentEditMatch.EditMatchListener() {
                    @Override
                    public void onMatchSaved(Match match) {
                        MatchDbUtils.createMatch(match);
                        loadNextMatch();
                    }
                });
                dFragMatch.show(getChildFragmentManager(), DialogFragmentEditMatch.TAG);
                break;
        }

    }

    @Subscribe
    public void OnPlayerSelectedEvent(OpenPlayerEvent event) {
        DialogFragmentEditPlayer dFrag = DialogFragmentEditPlayer
                .newInstance(mLeagueId, event.playerId);
        dFrag.setListener(new DialogFragmentEditPlayer.EditPlayerListener() {
            @Override
            public void onPlayerSaved(Player player) {
                PlayerDbUtils.updatePlayer(player);
                mAdapter.updatePlayer(player);
            }
        });
        dFrag.show(getFragmentManager(), DialogFragmentEditPlayer.TAG);
    }

    @Subscribe
    public void OnPlayerRemoveEvent(final RemovePlayerEvent event) {
        String name = event.player.getDisplayName();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_remove_player_title);
        builder.setMessage(getString(R.string.dialog_remove_player_message, name));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAdapter.removePlayer(event.player);
                PlayerDbUtils.removePlayer(event.player, mLeagueId);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    @Subscribe
    public void OnMatchSelectedEvent(OpenMatchEvent event) {
        DialogFragmentEditMatch dFrag = DialogFragmentEditMatch
                .newInstance(mLeagueId, event.matchId);
        dFrag.setListener(new DialogFragmentEditMatch.EditMatchListener() {
            @Override
            public void onMatchSaved(Match match) {
                MatchDbUtils.updateMatch(match);
                loadNextMatch();
            }
        });
        dFrag.show(getFragmentManager(), DialogFragmentEditPlayer.TAG);
    }

    @Subscribe
    public void OnMatchRemoveEvent(final RemoveMatchEvent event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_remove_match_title);
        builder.setMessage(R.string.dialog_remove_match_message);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MatchDbUtils.removeMatch(event.match);
                mAdapter.removeMatch(event.match);
                loadNextMatch();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

}
