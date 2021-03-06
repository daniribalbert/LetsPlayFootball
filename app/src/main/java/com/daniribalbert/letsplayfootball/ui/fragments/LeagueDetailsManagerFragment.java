package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.PlayersCache;
import com.daniribalbert.letsplayfootball.data.firebase.LeagueDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.PlayerDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.RatingsDbUtils;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.data.model.SimpleLeague;
import com.daniribalbert.letsplayfootball.ui.activities.MatchDetailsManagerActivity;
import com.daniribalbert.letsplayfootball.ui.activities.RulesActivity;
import com.daniribalbert.letsplayfootball.ui.activities.SearchActivity;
import com.daniribalbert.letsplayfootball.ui.constants.IntentConstants;
import com.daniribalbert.letsplayfootball.utils.ToastUtils;

import java.util.HashMap;

import butterknife.OnClick;

/**
 * Fragment with all the league details information.
 */
public class LeagueDetailsManagerFragment extends LeagueDetailsFragment {

    public static final String TAG = LeagueDetailsManagerFragment.class.getSimpleName();

    public static LeagueDetailsManagerFragment newInstance(String leagueId) {
        Bundle args = new Bundle();
        args.putString(IntentConstants.ARGS_LEAGUE_ID, leagueId);

        LeagueDetailsManagerFragment frag = new LeagueDetailsManagerFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override
    protected void setupView() {
        mLeagueCard.setCard(mLeague);

        mManageLeagueOwners.setVisibility(View.VISIBLE);
        mScheduleMatchView.setVisibility(View.VISIBLE);
    }

    @Override
    @OnClick(R.id.league_players_bt)
    public void onShowPlayers() {
        final PlayerListFragment playerListFragment = PlayerListFragment.newInstance(mLeague.id);
        playerListFragment.loadPlayersFromCache();
        playerListFragment.setListener(new PlayerListFragment.OnPlayerSelectedListener() {
            @Override
            public void onPlayerSelected(Player player) {
                final DialogFragmentEditPlayer dFrag = DialogFragmentEditPlayer
                        .newInstance(mLeague.id, player.id);
                dFrag.setListener(new DialogFragmentEditPlayer.EditPlayerListener() {
                    @Override
                    public void onPlayerSaved(final Player player, float rating) {
                        final HashMap<String, Player> leaguePlayers = PlayersCache
                                .getCurrentLeaguePlayers();
                        if (player.isGuest()) {
                            PlayerDbUtils.updatePlayer(player);
                            leaguePlayers.put(player.id, player);
                            PlayersCache.saveLeaguePlayersInfo(leaguePlayers.values());
                            playerListFragment.loadPlayersFromCache();
                        } else {
                            showProgress(true);
                            String userId = getBaseActivity().getCurrentUser().getUid();
                            RatingsDbUtils.savePlayerRating
                                    (player.id, mLeague.id, userId, rating,
                                     new RatingsDbUtils.OnPlayerRateUpdateListener() {
                                         @Override
                                         public void onRateUpdated(
                                                 float rating) {
                                             showProgress(false);
                                             player.setRating(mLeague.id, rating);
                                             leaguePlayers.put(player.id, player);
                                             PlayersCache
                                                     .saveLeaguePlayersInfo(leaguePlayers.values());
                                             playerListFragment.loadPlayersFromCache();
                                         }
                                     });
                        }
                    }
                });
                dFrag.show(getFragmentManager(), DialogFragmentEditPlayer.TAG);
            }
        });
        getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, playerListFragment,
                                     PlayerListFragment.TAG)
                            .addToBackStack(PlayerListFragment.TAG).commit();
    }

    @OnClick(R.id.league_manage_admins)
    public void onSelectManagers() {
        PlayerListFragment playerListFragment = PlayerListFragment.newInstance(mLeague.id);
        playerListFragment.loadPlayersFromCache();
        playerListFragment.setListener(new PlayerListFragment.OnPlayerSelectedListener() {
            @Override
            public void onPlayerSelected(Player player) {
                if (mLeague.isManager(player.id)) {
                    if (mLeague.hasMultipleManagers()) {
                        promptRemoveAdmin(player);
                    } else {
                        ToastUtils.show(R.string.error_cannot_remove_last_league_admin,
                                        Toast.LENGTH_LONG);
                    }
                } else {
                    if (player.isGuest()) {
                        ToastUtils.show(R.string.error_add_guest_as_manager, Toast.LENGTH_SHORT);
                    } else {
                        promptAddAdmin(player);
                    }
                }
            }
        });
        getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, playerListFragment,
                                     PlayerListFragment.TAG)
                            .addToBackStack(PlayerListFragment.TAG).commit();
    }

    private void promptRemoveAdmin(final Player player) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_league_remove_manager_title);
        builder.setMessage(getString(R.string.dialog_league_remove_manager_msg, player.getName(),
                                     mLeague.title));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mLeague.managerIds.put(player.id, false);
                LeagueDbUtils.updateLeagueManagers(mLeague);
                Fragment frag = getFragmentManager().findFragmentByTag(PlayerListFragment.TAG);
                ((PlayerListFragment) frag).loadPlayersFromCache();
                // Check if current user is still admin.
                checkCurrentPlayerAdminPrivileges(player.id);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    /**
     * Checks if current player remains admin of the league after removing one of the player admins.
     * @param latestAdminRemovedId latest
     */
    private void checkCurrentPlayerAdminPrivileges(String latestAdminRemovedId) {
        if (latestAdminRemovedId.equalsIgnoreCase(getBaseActivity().getCurrentUser().getUid())) {
            getActivity().finish();
        }
    }

    private void promptAddAdmin(final Player player) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_league_add_manager_title);
        builder.setMessage(
                getString(R.string.dialog_league_add_manager_msg, player.getName(), mLeague.title));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mLeague.managerIds.put(player.id, true);
                LeagueDbUtils.updateLeagueManagers(mLeague);
                Fragment frag = getFragmentManager().findFragmentByTag(PlayerListFragment.TAG);
                ((PlayerListFragment) frag).loadPlayersFromCache();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    @OnClick(R.id.league_card)
    public void onLeagueSelected() {
        DialogFragmentEditLeague dFrag = DialogFragmentEditLeague.newInstance(mLeague.id);
        dFrag.setListener(new DialogFragmentEditLeague.EditLeagueListener() {
            @Override
            public void onLeagueSaved(League league) {
                mLeagueCard.setCard(league);
                LeagueDbUtils.updateLeague(league);
                mLeague = league;
            }
        });
        dFrag.show(getFragmentManager(), DialogFragmentEditLeague.TAG);
    }

    @OnClick(R.id.league_schedule_match_bt)
    public void onScheduleMatch() {
        DialogFragmentEditMatch dFragMatch = DialogFragmentEditMatch.newInstance(mLeague.id);
        dFragMatch.setListener(new DialogFragmentEditMatch.EditMatchListener() {
            @Override
            public void onMatchSaved(Match match) {
                MatchDbUtils.createMatch(match);
                loadNextMatch();
            }
        });
        dFragMatch.show(getChildFragmentManager(), DialogFragmentEditMatch.TAG);
    }

    @OnClick(R.id.league_add_new_guest_player)
    public void onAddNewGuestPlayer() {
        DialogFragmentEditPlayer dFrag = DialogFragmentEditPlayer.newInstance(mLeague.id);
        dFrag.setListener(new DialogFragmentEditPlayer.EditPlayerListener() {
            @Override
            public void onPlayerSaved(Player player, float rating) {
                player.leagues.put(mLeague.id, new SimpleLeague(mLeague.id));
                player = PlayerDbUtils.createGuestPlayer(player);
                PlayersCache.getCurrentLeaguePlayers().put(player.id, player);
            }
        });
        dFrag.show(getFragmentManager(), DialogFragmentEditPlayer.TAG);
    }

    @OnClick(R.id.league_search_players_bt)
    public void onSearchForPlayers() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra(SearchActivity.ARGS_TAG, PlayerSearchFragment.TAG);
        intent.putExtra(IntentConstants.ARGS_LEAGUE_ID, mLeague.id);
        startActivity(intent);
    }

    @OnClick(R.id.league_rules_bt)
    @Override
    public void showLeagueRules() {
        Intent intent = RulesActivity.newIntent(getActivity(), mLeague.id, mLeague.rules, true);
        startActivity(intent);
    }

    protected Intent getMatchDetailsIntent() {
        return new Intent(getActivity(), MatchDetailsManagerActivity.class);
    }

}
