package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.firebase.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.PlayerDbUtils;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.data.model.SimpleLeague;
import com.daniribalbert.letsplayfootball.ui.events.FabClickedEvent;
import com.daniribalbert.letsplayfootball.ui.events.OpenMatchEvent;
import com.daniribalbert.letsplayfootball.ui.events.OpenPlayerEvent;
import com.daniribalbert.letsplayfootball.ui.events.RemoveMatchEvent;
import com.daniribalbert.letsplayfootball.ui.events.RemovePlayerEvent;

import org.greenrobot.eventbus.Subscribe;

/**
 * A fragment representing the core league items.
 */
public class LeagueInfoOwnerModeFragment extends LeagueInfoFragment {

    public static final String TAG = LeagueInfoOwnerModeFragment.class.getSimpleName();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LeagueInfoOwnerModeFragment() {
    }

    /**
     * Creates a new instance of PlayerListFragment with a list of players based from the League
     * with the given leagueId.
     *
     * @param leagueId league_id of the League which players will be loaded.
     *
     * @return new instance of this Fragment.
     */
    public static LeagueInfoOwnerModeFragment newInstance(String leagueId) {
        Bundle args = new Bundle();
        args.putString(LEAGUE_ID, leagueId);
        LeagueInfoOwnerModeFragment fragment = new LeagueInfoOwnerModeFragment();
        fragment.setRetainInstance(true);
        fragment.setArguments(args);
        return fragment;
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
    @Override
    public void OnPlayerSelectedEvent(OpenPlayerEvent event) {
        final Player player = event.player;

        DialogFragmentEditPlayer dFrag = DialogFragmentEditPlayer
                .newInstance(mLeagueId, player.id);
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
        String currentUserId = getBaseActivity().getCurrentUser().getUid();
        DialogFragmentEditMatch dFrag = DialogFragmentEditMatch
                .newInstance(mLeagueId, event.matchId, currentUserId);
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
