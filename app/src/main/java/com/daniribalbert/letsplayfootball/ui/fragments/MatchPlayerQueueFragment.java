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
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.adapters.MatchPlayerQueueAdapter;
import com.daniribalbert.letsplayfootball.ui.constants.IntentConstants;
import com.daniribalbert.letsplayfootball.ui.events.PlayerClickedEvent;
import com.daniribalbert.letsplayfootball.utils.GsonUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of Player Items in the Match queue.
 */
public class MatchPlayerQueueFragment extends BaseFragment {

    public static final String TAG = MatchPlayerQueueFragment.class.getSimpleName();

    @BindView(R.id.players_recyclerview)
    RecyclerView mRecyclerView;

    MatchPlayerQueueAdapter mAdapter;

    private OnPlayerSelectedListener mListener;
    private Match mMatch;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MatchPlayerQueueFragment() {
    }

    /**
     * Creates a new instance of MatchPlayerQueueFragment based on the given Match.
     *
     * @param match league_id of the League which players will be loaded.
     *
     * @return new instance of this Fragment.
     */
    public static MatchPlayerQueueFragment newInstance(Match match) {
        Bundle args = new Bundle();
        args.putString(IntentConstants.ARGS_MATCH_JSON, GsonUtils.toJson(match));
        MatchPlayerQueueFragment fragment = new MatchPlayerQueueFragment();
        fragment.setRetainInstance(true);
        fragment.setArguments(args);
        fragment.createAdapter(match);
        return fragment;
    }

    private void createAdapter(Match match) {
        mAdapter = new MatchPlayerQueueAdapter(match);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            String matchJson = args.getString(IntentConstants.ARGS_MATCH_JSON);
            mMatch = GsonUtils.fromJson(matchJson, Match.class);
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
            mAdapter = new MatchPlayerQueueAdapter(mMatch);
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
        }
    }

    public void setListener(
            OnPlayerSelectedListener mListener) {
        this.mListener = mListener;
    }

    public interface OnPlayerSelectedListener {
        void onPlayerSelected(Player player);
    }
}
