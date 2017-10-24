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
import com.daniribalbert.letsplayfootball.data.cache.PlayersCache;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.adapters.PlayerListAdapter;
import com.daniribalbert.letsplayfootball.ui.constants.IntentConstants;
import com.daniribalbert.letsplayfootball.ui.events.PlayerClickedEvent;
import com.daniribalbert.letsplayfootball.ui.events.PlayerLongClickEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of Player Items.
 */
public class PlayerListFragment extends BaseFragment {

    public static final String TAG = PlayerListFragment.class.getSimpleName();

    @BindView(R.id.players_recyclerview)
    RecyclerView mRecyclerView;

    PlayerListAdapter mAdapter;
    String mLeagueId;

    private OnPlayerSelectedListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayerListFragment() {
    }

    /**
     * Creates a new instance of PlayerListFragment with a list of players based from the League
     * with the given leagueId.
     *
     * @param leagueId league_id of the League which players will be loaded.
     *
     * @return new instance of this Fragment.
     */
    public static PlayerListFragment newInstance(String leagueId) {
        Bundle args = new Bundle();
        args.putString(IntentConstants.ARGS_LEAGUE_ID, leagueId);
        PlayerListFragment fragment = new PlayerListFragment();
        fragment.setRetainInstance(true);
        fragment.setArguments(args);
        fragment.createAdapter(leagueId);
        return fragment;
    }

    /**
     * Creates a new instance of PlayerListFragment with a list of players based from the League
     * with the given leagueId.
     *
     * @param leagueId league_id of the League which players will be loaded.
     *
     * @return new instance of this Fragment.
     */
    public static PlayerListFragment newInstance(String leagueId, String matchId) {
        Bundle args = new Bundle();
        args.putString(IntentConstants.ARGS_LEAGUE_ID, leagueId);
        PlayerListFragment fragment = new PlayerListFragment();
        fragment.setRetainInstance(true);
        fragment.setArguments(args);
        fragment.createAdapter(leagueId);
        return fragment;
    }

    private void createAdapter(String leagueId) {
        mAdapter = new PlayerListAdapter(leagueId);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mLeagueId = args.getString(IntentConstants.ARGS_LEAGUE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_player_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);

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
        if (mAdapter == null) {
            mAdapter = new PlayerListAdapter(mLeagueId);
            mRecyclerView.setAdapter(mAdapter);
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
    public void onStop() {
        super.onStop();
        mRecyclerView.setAdapter(null);
    }

    @Subscribe
    public void OnPlayerSelectedEvent(final PlayerClickedEvent event) {
        if (mListener != null) {
            mListener.onPlayerSelected(event.player);
            mAdapter.updatePlayer(event.player);
        }
    }

    public List<Integer> getSelectedPlayers() {
        return mAdapter.getSelectedPlayersIndexes();
    }

    @Subscribe
    public void OnPlayerSelectionActivated(PlayerLongClickEvent event) {
        mAdapter.setSelectionMode(true);
    }

    public void setPlayers(List<Player> players) {
        mAdapter.clear();
        mAdapter.addItems(players);
    }

    public void loadPlayersFromCache() {
        mAdapter.clear();
        HashMap<String, Player> currentLeaguePlayers = PlayersCache.getCurrentLeaguePlayers();
        mAdapter.addItems(currentLeaguePlayers.values());
    }

    public void teamSelected() {
        mAdapter.clearSelection();
        mAdapter.notifyDataSetChanged();
        mAdapter.setSelectionMode(false);
    }

    public void setPlayerSelectionEnabled(boolean enabled) {
        mAdapter.setPlayerSelectionEnabled(enabled);
    }

    @Subscribe
    public void onPlayerUpdatedEvent(Player player) {
        mAdapter.updatePlayer(player);
    }

    public void setListener(
            OnPlayerSelectedListener mListener) {
        this.mListener = mListener;
    }

    public void setShowCheckInIcons(boolean showCheckInIcons, Match match) {
        mAdapter.enableCheckInIcons(showCheckInIcons, match);
    }

    public void updateMatch(Match match) {
        mAdapter.enableCheckInIcons(true, match);
    }

    public interface OnPlayerSelectedListener {
        void onPlayerSelected(Player player);
    }
}
