package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.firebase.LeagueDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.RequestsDbUtils;
import com.daniribalbert.letsplayfootball.data.model.JoinLeagueRequest;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.data.model.SimpleLeague;
import com.daniribalbert.letsplayfootball.ui.events.OpenLeagueEvent;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.firebase.auth.FirebaseUser;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A fragment where the user can search for a list of Leagues.
 */
public class LeagueSearchFragment extends MyLeaguesFragment
        implements TextView.OnEditorActionListener {

    public static final String TAG = LeagueSearchFragment.class.getSimpleName();

    @BindView(R.id.search_edit_text)
    EditText mSearchText;

    @BindView(R.id.app_progress)
    protected ProgressBar mProgressBar;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LeagueSearchFragment() {
    }

    /**
     * Creates a new instance of LeagueSearchFragment.
     * *
     *
     * @return new instance of this Fragment.
     */
    public static LeagueSearchFragment newInstance() {
        Bundle args = new Bundle();
        LeagueSearchFragment fragment = new LeagueSearchFragment();
        fragment.setRetainInstance(true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search_league, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSearchText.setOnEditorActionListener(this);
        setProgress(mProgressBar);
    }

    private void searchLeague() {
        showProgress(true);

        String searchQuery = mSearchText.getText().toString();
        mAdapter.clear();
        LogUtils.i("Searching for Leagues with name: " + searchQuery);
        LeagueDbUtils.searchLeague(searchQuery, new LeagueDbUtils.LeagueSearchListener() {
            @Override
            public void onLeagueSearchResult(List<SimpleLeague> leagues) {
                mAdapter.addItems(leagues);
                showProgress(false);
            }
        });
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        switch (actionId) {
            case EditorInfo.IME_ACTION_SEARCH:
                searchLeague();
                break;
        }
        return true;
    }

    @Override
    protected void loadData() {
        //DO NOTHING
    }

    @OnClick(R.id.ic_search)
    public void searchIconClicked(){
        searchLeague();
    }

    @Override
    @Subscribe
    public void OnLeagueSelectedEvent(final OpenLeagueEvent event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_join_league_title);
        builder.setMessage(R.string.dialog_join_league_msg);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseUser currentUser = getBaseActivity().getCurrentUser();
                Player currentPlayer = Player.fromFirebase(currentUser);
                JoinLeagueRequest request = new JoinLeagueRequest(event.getLeague(), currentPlayer, currentPlayer.id);
                RequestsDbUtils.sendRequestToJoinLeague(request);
                mAdapter.removeItem(event.getLeague());
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }
}
