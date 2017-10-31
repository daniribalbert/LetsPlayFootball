package com.daniribalbert.letsplayfootball.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.LeagueCache;
import com.daniribalbert.letsplayfootball.data.cache.PlayersCache;
import com.daniribalbert.letsplayfootball.data.firebase.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.PlayerDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.activities.MatchDetailsActivity;
import com.daniribalbert.letsplayfootball.ui.activities.RulesActivity;
import com.daniribalbert.letsplayfootball.ui.constants.IntentConstants;
import com.daniribalbert.letsplayfootball.ui.views.LeagueCardView;
import com.daniribalbert.letsplayfootball.ui.views.MatchCardView;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment with all the league details information.
 */
public class LeagueDetailsFragment extends BaseFragment {

    public static final String TAG = LeagueDetailsFragment.class.getSimpleName();

    @BindView(R.id.league_upcoming_match_layout)
    View mUpcomingMatchLayout;

    @BindView(R.id.league_match_card)
    MatchCardView mLeagueNextMatchView;

    @BindView(R.id.league_card)
    LeagueCardView mLeagueCard;

    @BindView(R.id.app_progress)
    ProgressBar mProgressBar;

    @BindView(R.id.league_manage_owners)
    View mManageLeagueOwners;
    @BindView(R.id.league_schedule_match_bt)
    View mScheduleMatchView;
    @BindView(R.id.league_search_players_bt)
    View mSearchPlayers;
    @BindView(R.id.league_add_new_guest_player)
    View mAddNewGuestPlayers;
    @BindView(R.id.league_rules_bt)
    View mRulesBt;


    protected String mLeagueId;
    protected League mLeague;
    protected String mMatchId;
    protected String mPlayerId;
    protected Match mMatch;

    public static LeagueDetailsFragment newInstance(String leagueId) {
        Bundle args = new Bundle();
        args.putString(IntentConstants.ARGS_LEAGUE_ID, leagueId);

        LeagueDetailsFragment frag = new LeagueDetailsFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadArgs();
    }

    private void loadArgs() {
        Bundle args = getArguments();
        mLeagueId = args.getString(IntentConstants.ARGS_LEAGUE_ID);
        mLeague = LeagueCache.getLeagueInfo(mLeagueId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_league_details, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupView();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    protected void loadData() {
        loadNextMatch();
        loadPlayers();
    }

    protected void setupView() {
        mLeagueCard.setCard(mLeague);
        mScheduleMatchView.setVisibility(View.GONE);
        mSearchPlayers.setVisibility(View.GONE);
        mAddNewGuestPlayers.setVisibility(View.GONE);
    }

    protected void loadNextMatch() {
        MatchDbUtils.getUpcomingMatch(mLeague.id, new BaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!isAdded()) {
                    return;
                }
                for (DataSnapshot next : dataSnapshot.getChildren()) {
                    mMatch = next.getValue(Match.class);
                    if (mMatch != null) {
                        mUpcomingMatchLayout.setVisibility(View.VISIBLE);
                        mLeagueNextMatchView.setCard(mMatch);
                        return;
                    } else {
                        mUpcomingMatchLayout.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    protected void loadPlayers() {
        PlayerDbUtils
                .getPlayersFromLeague(mLeague.id,
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

                                              PlayersCache.saveLeaguePlayersInfo(players);
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

    @OnClick(R.id.league_players_bt)
    public void onShowPlayers() {
        PlayerListFragment playerListFragment = PlayerListFragment.newInstance(mLeague.id);
        playerListFragment.loadPlayersFromCache();
        playerListFragment.setListener(new PlayerListFragment.OnPlayerSelectedListener() {
            @Override
            public void onPlayerSelected(Player player) {
                DialogFragmentViewPlayer dFrag = DialogFragmentViewPlayer
                        .newInstance(mLeague.id, player.id);
                dFrag.show(getFragmentManager(), DialogFragmentViewPlayer.TAG);
            }
        });
        getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, playerListFragment,
                                     PlayerListFragment.TAG)
                            .addToBackStack(PlayerListFragment.TAG).commit();
    }

    @OnClick(R.id.league_card)
    public void onLeagueSelected() {
        DialogFragmentViewLeague dFrag = DialogFragmentViewLeague.newInstance(mLeague.id);
        dFrag.show(getFragmentManager(), DialogFragmentViewLeague.TAG);
    }

    @OnClick(R.id.league_match_card)
    public void onNextMatchSelected() {
        String currentUserId = getBaseActivity().getCurrentUser().getUid();

        Intent intent = getMatchDetailsIntent();
        intent.putExtra(IntentConstants.ARGS_LEAGUE_ID, mLeague.id);
        intent.putExtra(IntentConstants.ARGS_MATCH_ID, mMatch.id);
        intent.putExtra(IntentConstants.ARGS_PLAYER_ID, currentUserId);
        startActivity(intent);
    }

    @OnClick(R.id.league_manage_owners)
    public void onSelectedOwners() {
        PlayerListFragment playerListFragment = PlayerListFragment.newInstance(mLeague.id);
        HashMap<String, Boolean> ownersId = mLeague.managerIds;
        List<Player> players = new ArrayList<>();
        HashMap<String, Player> currentLeaguePlayers = PlayersCache.getCurrentLeaguePlayers();
        for (String playerId : ownersId.keySet()) {
            players.add(currentLeaguePlayers.get(playerId));
        }
        playerListFragment.setPlayerSelectionEnabled(false);
        playerListFragment.setPlayers(players);
        getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, playerListFragment,
                                     PlayerListFragment.TAG)
                            .addToBackStack(PlayerListFragment.TAG).commit();
    }

    @OnClick(R.id.league_rules_bt)
    public void showLeagueRules() {
        Intent intent = RulesActivity.newIntent(getActivity(), mLeague.id, mLeague.rules, false);
        startActivity(intent);
    }

    protected Intent getMatchDetailsIntent() {
        return new Intent(getActivity(), MatchDetailsActivity.class);
    }
}
