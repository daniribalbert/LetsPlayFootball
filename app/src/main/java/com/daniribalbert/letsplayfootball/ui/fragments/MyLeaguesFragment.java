package com.daniribalbert.letsplayfootball.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.PlayersCache;
import com.daniribalbert.letsplayfootball.data.firebase.LeagueDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.PlayerDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.data.model.SimpleLeague;
import com.daniribalbert.letsplayfootball.ui.activities.LeagueActivity;
import com.daniribalbert.letsplayfootball.ui.adapters.MyLeagueAdapter;
import com.daniribalbert.letsplayfootball.ui.constants.IntentConstants;
import com.daniribalbert.letsplayfootball.ui.events.EditLeagueEvent;
import com.daniribalbert.letsplayfootball.ui.events.FabClickedEvent;
import com.daniribalbert.letsplayfootball.ui.events.OpenLeagueEvent;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of League Items.
 */
public class MyLeaguesFragment extends BaseFragment {

    public static final String TAG = MyLeaguesFragment.class.getSimpleName();

    @BindView(R.id.my_league_empty_msg)
    View mEmptyView;

    @BindView(R.id.my_league_recyclerview)
    RecyclerView mRecyclerView;
    protected MyLeagueAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MyLeaguesFragment() {
    }

    @SuppressWarnings("unused")
    public static MyLeaguesFragment newInstance() {
        MyLeaguesFragment fragment = new MyLeaguesFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new MyLeagueAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_league_list, container, false);
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
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mRecyclerView.setAdapter(mAdapter);
        loadData();
    }

    protected void loadData() {
        loadPlayerLeagueInfo();
    }


    @Subscribe
    public void onFabClicked(FabClickedEvent event) {
        DialogFragmentEditLeague dFrag = DialogFragmentEditLeague.newInstance();
        dFrag.setListener(new DialogFragmentEditLeague.EditLeagueListener() {
            @Override
            public void onLeagueSaved(League league) {
                FirebaseUser currentUser = getBaseActivity().getCurrentUser();
                LeagueDbUtils.createLeague(league, currentUser.getUid());
                mAdapter.addItem(new SimpleLeague(league));
            }
        });
        dFrag.show(getFragmentManager(), DialogFragmentEditLeague.TAG);
    }

    @Subscribe
    public void OnLeagueSelectedEvent(final OpenLeagueEvent event) {
        SimpleLeague currentLeague = event.getLeague();

        Intent intent = new Intent(getActivity(), LeagueActivity.class);
        intent.putExtra(IntentConstants.ARGS_LEAGUE_ID, currentLeague.league_id);
        intent.putExtra(IntentConstants.ARGS_LEAGUE_TITLE, currentLeague.title);
        startActivity(intent);
    }

    @Subscribe
    public void editLeagueInfo(EditLeagueEvent event) {
        DialogFragmentEditLeague dFrag = DialogFragmentEditLeague
                .newInstance(event.getLeague().league_id);
        dFrag.setListener(new DialogFragmentEditLeague.EditLeagueListener() {
            @Override
            public void onLeagueSaved(League league) {
                LeagueDbUtils.updateLeague(league);
            }
        });
        dFrag.show(getFragmentManager(), DialogFragmentEditLeague.TAG);
    }

    public void loadPlayerLeagueInfo() {
        showProgress(true);
        Player player = PlayersCache.getCurrentPlayerInfo();
        if (player != null) {
            updateAdapter(player);
        }
        showProgress(false);
    }

    private void updateAdapter(@NonNull Player player) {
        mAdapter.clear();
        ArrayList<SimpleLeague> myLeagues = new ArrayList<>();
        HashMap<String, SimpleLeague> leagues = player.leagues;

        if (leagues != null) {
            myLeagues.addAll(leagues.values());
        }
        mAdapter.addItems(myLeagues);

        updateAdapterView(mAdapter.getItemCount() == 0);
    }

    private void updateAdapterView(boolean isEmpty) {
        if (isEmpty) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    private void loadPlayerLeagues() {
        final String userId = getBaseActivity().getCurrentUser().getUid();
        PlayerDbUtils.getPlayer(userId,
                                new BaseValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        showProgress(false);
                                        Player currentPlayer = dataSnapshot.getValue(Player.class);
                                        if (currentPlayer == null) {
                                            LogUtils.e("Failed to load user data! " + userId);
                                            return;
                                        }
                                        PlayersCache.saveCurrentPlayerInfo(currentPlayer);
                                        getBaseActivity().checkPlayerPushToken(currentPlayer);
                                        updateAdapter(currentPlayer);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        super.onCancelled(databaseError);
                                        showProgress(false);
                                    }
                                });
    }
}
