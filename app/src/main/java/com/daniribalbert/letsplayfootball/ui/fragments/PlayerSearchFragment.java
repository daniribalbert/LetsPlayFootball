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
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.firebase.PlayerDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.SearchListener;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.events.PlayerClickedEvent;
import com.daniribalbert.letsplayfootball.utils.LogUtils;

import org.greenrobot.eventbus.Subscribe;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment where the user can search for a list of Player.
 */
public class PlayerSearchFragment extends PlayerListFragment
        implements TextView.OnEditorActionListener {

    public static final String TAG = PlayerSearchFragment.class.getSimpleName();

    @BindView(R.id.search_edit_text)
    EditText mSearchText;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayerSearchFragment() {
    }

    /**
     * Creates a new instance of PlayerListFragment with a list of players based from the League
     * with the given leagueId.
     *
     * @param leagueId league_id of the League which players will be loaded.
     *
     * @return new instance of this Fragment.
     */
    public static PlayerSearchFragment newInstance(String leagueId) {
        Bundle args = new Bundle();
        args.putString(LEAGUE_ID, leagueId);
        PlayerSearchFragment fragment = new PlayerSearchFragment();
        fragment.setRetainInstance(true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search_player, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSearchText.setOnEditorActionListener(this);
    }

    private void searchPlayer() {
        showProgress(true);

        String searchQuery = mSearchText.getText().toString();
        mAdapter.clear();
        LogUtils.i("Searching for players with name/nickname: " + searchQuery);
        PlayerDbUtils.searchPlayers(searchQuery, new SearchListener<Player>() {
            @Override
            public void onDataReceived(Set<Player> results) {
                for (Player player : results) {
                    if (player.leagues.containsKey(mLeagueId)){
                        results.remove(player);
                    }
                }
                mAdapter.addItems(results);
                showProgress(false);
            }

        });
    }

    @Subscribe
    @Override
    public void OnPlayerSelectedEvent(final PlayerClickedEvent event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_add_player_title);
        final Player player = event.player;
        builder.setMessage(getString(R.string.dialog_add_player_message, player.name));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                PlayerDbUtils.addLeague(player.id, mLeagueId);
                mAdapter.removeItem(player);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        builder.show();
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        switch (actionId) {
            case EditorInfo.IME_ACTION_SEARCH:
                searchPlayer();
                break;
        }
        return true;
    }
}
