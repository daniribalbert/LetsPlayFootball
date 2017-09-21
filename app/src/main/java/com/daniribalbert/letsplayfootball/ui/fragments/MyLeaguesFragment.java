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
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.ui.adapters.MyLeagueAdapter;
import com.daniribalbert.letsplayfootball.ui.events.FabClickedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of League Items.
 */
public class MyLeaguesFragment extends BaseFragment {

    public static final String TAG = MyLeaguesFragment.class.getSimpleName();

    @BindView(R.id.my_league_recyclerview)
    RecyclerView mRecyclerView;
    private MyLeagueAdapter mAdapter;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MyLeaguesFragment() {
    }

    @SuppressWarnings("unused")
    public static MyLeaguesFragment newInstance() {
        MyLeaguesFragment fragment = new MyLeaguesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_league_list, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Context context = view.getContext();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        if (mAdapter == null) {
            mAdapter = new MyLeagueAdapter(new ArrayList<League>());
        }
        mRecyclerView.setAdapter(mAdapter);
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

    @Subscribe
    public void onFabClicked(FabClickedEvent event) {
        mAdapter.addItem(new League());
    }
}
