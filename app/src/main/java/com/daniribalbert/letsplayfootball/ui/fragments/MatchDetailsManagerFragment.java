package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.PlayersCache;
import com.daniribalbert.letsplayfootball.data.firebase.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.activities.BaseActivity;
import com.daniribalbert.letsplayfootball.ui.activities.TeamsActivity;
import com.daniribalbert.letsplayfootball.ui.activities.TeamsManagerActivity;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.daniribalbert.letsplayfootball.utils.GsonUtils;
import com.google.firebase.database.DataSnapshot;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment to show a Match details options.
 */
public class MatchDetailsManagerFragment extends MatchDetailsFragment {
    public static final String TAG = MatchDetailsManagerFragment.class.getSimpleName();

    public static MatchDetailsManagerFragment newInstance(String matchId, String leagueId,
                                                          String playerId) {
        Bundle args = new Bundle();
        args.putString(BaseActivity.ARGS_MATCH_ID, matchId);
        args.putString(BaseActivity.ARGS_LEAGUE_ID, leagueId);
        args.putString(BaseActivity.ARGS_PLAYER_ID, playerId);

        MatchDetailsManagerFragment fragment = new MatchDetailsManagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupListeners() {
        super.setupListeners();
        mMatchManageGuestsBt.setOnClickListener(this);
        mMatchManageGuestsBt.setVisibility(View.VISIBLE);
    }

    @Override
    protected void showMatchDialog() {
        String userId = getBaseActivity().getCurrentUser().getUid();
        DialogFragmentEditMatch dFrag = DialogFragmentEditMatch
                .newInstance(mLeagueId, mMatchId, userId);
        dFrag.setListener(new DialogFragmentEditMatch.EditMatchListener() {
            @Override
            public void onMatchSaved(Match match) {
                MatchDbUtils.updateMatch(match);
            }
        });
        dFrag.show(getFragmentManager(), DialogFragmentEditPlayer.TAG);
    }

    @Override
    protected Intent getTeamsActivityIntent() {
        Intent intent = new Intent(getActivity(), TeamsManagerActivity.class);
        intent.putExtra(BaseActivity.ARGS_LEAGUE_ID, mLeagueId);
        intent.putExtra(TeamsActivity.ARG_MATCH, GsonUtils.toJson(mMatch));
        return intent;
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.match_manager_guest_check_in_bt:
                showGuestCheckIn();
                break;
        }
    }

    private void showGuestCheckIn() {
        final PlayerListFragment frag = PlayerListFragment.newInstance(mLeagueId);
        frag.setPlayerSelectionEnabled(false);
        Collection<Player> allPlayers = PlayersCache.getCurrentLeaguePlayers().values();
        List<Player> guestPlayers = new LinkedList<>();
        for (Player player : allPlayers) {
            if (player.isGuest()) {
                guestPlayers.add(player);
            }
        }
        frag.setPlayers(guestPlayers);
        frag.setShowCheckInIcons(true, mMatch);
        frag.setListener(new PlayerListFragment.OnPlayerSelectedListener() {
            @Override
            public void onPlayerSelected(Player player) {
                promptConfirmCheckIn(player, frag);
            }
        });
        getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, frag, PlayerListFragment.TAG)
                            .addToBackStack(PlayerListFragment.TAG).commit();
    }

    private void promptConfirmCheckIn(final Player player, final PlayerListFragment frag) {
        final boolean checkedIn = mMatch.isCheckedIn(player.id);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int msgResId = checkedIn
                       ? R.string.dialog_confirm_check_out
                       : R.string.dialog_confirm_check_in;
        builder.setMessage(getString(msgResId, player.getName()));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (checkedIn) {
                    mMatch = MatchDbUtils.markCheckOut(mMatch, player.id);
                    frag.updateMatch(mMatch);
                } else {
                    mMatch = MatchDbUtils.markCheckIn(mMatch, player.id);
                    frag.updateMatch(mMatch);
                }
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.no, null);
        builder.show();
    }
}
