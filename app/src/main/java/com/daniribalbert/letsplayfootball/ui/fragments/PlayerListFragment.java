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
import com.daniribalbert.letsplayfootball.data.database.LeagueDbUtils;
import com.daniribalbert.letsplayfootball.data.database.PlayerDbUtils;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.data.model.SimpleLeague;
import com.daniribalbert.letsplayfootball.ui.adapters.MyLeagueAdapter;
import com.daniribalbert.letsplayfootball.ui.adapters.PlayerListAdapter;
import com.daniribalbert.letsplayfootball.ui.events.FabClickedEvent;
import com.daniribalbert.letsplayfootball.ui.events.OpenPlayerEvent;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of League Items.
 */
public class PlayerListFragment extends BaseFragment {

    public static final String TAG = PlayerListFragment.class.getSimpleName();

    public static final String LEAGUE_ID = "LEAGUE_ID";

    @BindView(R.id.players_recyclerview)
    RecyclerView mRecyclerView;
    private PlayerListAdapter mAdapter;

    private String mLeagueId;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayerListFragment() {
    }

    /**
     * Creates a new instance of PlayerListFragment with a list of players based from the League
     * with the given leagueId.
     * @param leagueId league_id of the League which players will be loaded.
     * @return new instance of this Fragment.
     */
    public static PlayerListFragment newInstance(String leagueId) {
        Bundle args = new Bundle();
        args.putString(LEAGUE_ID, leagueId);

        PlayerListFragment fragment = new PlayerListFragment();
        fragment.setRetainInstance(true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            Bundle args = getArguments();
            mLeagueId = args.getString(LEAGUE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_player_list, container, false);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter == null) {
            mAdapter = new PlayerListAdapter();
            mRecyclerView.setAdapter(mAdapter);
            loadData();
        } else {
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    private void loadData() {
        showProgress(true);
        PlayerDbUtils.getPlayersFromLeague(mLeagueId,
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren()
                                                                                             .iterator();

                                        List<Player> players = new ArrayList<Player>();
                                        while (iterator.hasNext()){
                                            DataSnapshot next = iterator.next();
                                            Player player = next.getValue(Player.class);
                                            if (player != null) {
                                                players.add(player);
                                            }
                                        }

                                        mAdapter.addItems(players);
                                        showProgress(false);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        showProgress(false);
                                    }
                                });
    }

    @Subscribe
    public void onFabClicked(FabClickedEvent event) {
        DialogFragmentEditPlayer dFrag = DialogFragmentEditPlayer.newInstance();
        dFrag.setProgressBar(mProgressBar);
        dFrag.setListener(new DialogFragmentEditPlayer.EditPlayerListener() {
            @Override
            public void onPlayerSaved(Player player) {
                FirebaseUser currentUser = getBaseActivity().getCurrentUser();
                PlayerDbUtils.createUser(player);
                mAdapter.addItem(player);
            }
        });
        dFrag.show(getFragmentManager(), DialogFragmentEditPlayer.TAG);
    }

    @Subscribe
    public void OnPlayerSelectedEvent(OpenPlayerEvent event) {
        DialogFragmentEditPlayer dFrag = DialogFragmentEditPlayer.newInstance(event.playerId);
        dFrag.setProgressBar(mProgressBar);
        dFrag.setListener(new DialogFragmentEditPlayer.EditPlayerListener() {
            @Override
            public void onPlayerSaved(Player player) {
                PlayerDbUtils.updatePlayer(player);
                mAdapter.updateItem(player);
            }
        });
        dFrag.show(getFragmentManager(), DialogFragmentEditPlayer.TAG);
    }

}
