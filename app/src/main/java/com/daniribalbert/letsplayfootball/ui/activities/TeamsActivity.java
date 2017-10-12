package com.daniribalbert.letsplayfootball.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;

import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.PlayersCache;
import com.daniribalbert.letsplayfootball.data.firebase.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.events.PlayerLongClickEvent;
import com.daniribalbert.letsplayfootball.ui.fragments.PlayerListFragment;
import com.daniribalbert.letsplayfootball.utils.GsonUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TeamsActivity extends BaseActivity implements View.OnClickListener {

    public static final String ARG_MATCH = "ARG_MATCH";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private List<Player> mAllPlayersList = new ArrayList<>();

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @BindView(R.id.container)
    ViewPager mViewPager;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @BindView(R.id.tabs)
    TabLayout mTabLayout;
    private String mLeagueId;

    private Match mMatch;

    private boolean mSelectionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teams);
        ButterKnife.bind(this);

        loadArgs(getIntent());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mFab.setOnClickListener(this);
        loadPlayers();
    }

    private void loadPlayers() {
        HashMap<String, Player> allPlayersMap = new HashMap<>(
                PlayersCache.getCurrentLeaguePlayers());

        // Filter for player which have checked-in.
        for (String playerId : allPlayersMap.keySet()){
            if (!mMatch.players.containsKey(playerId) || !mMatch.players.get(playerId)){
                allPlayersMap.remove(playerId);
            }
        }

        if (mMatch.teams.size() > 0) {
            for (List<String> team : mMatch.teams.values()) {
                for (String player : team) {
                    allPlayersMap.remove(player);
                }
            }
        }
        mAllPlayersList.addAll(allPlayersMap.values());
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    private void loadArgs(Intent intent) {
        if (intent != null) {
            mLeagueId = intent.getStringExtra(ARGS_LEAGUE_ID);
            String matchJsonStr = intent.getStringExtra(ARG_MATCH);
            mMatch = GsonUtils.fromJson(matchJsonStr, Match.class);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                if (mSelectionMode) {
                    promptSelectTeamToAddPlayers();
                } else {
                    promptAddTeam();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mSelectionMode) {
            updateSelectionMode(false);
        } else {
            promptSaveTeams();
        }
    }

    private void promptSaveTeams() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_team_save_on_leave_message);
        builder.setIcon(android.R.drawable.ic_menu_save);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MatchDbUtils.saveTeam(mMatch);
                finish();
            }
        });
        builder.setNegativeButton(R.string.leave, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.show();
    }

    private void promptAddTeam() {
        final EditText teamNameInput = new EditText(this);
        teamNameInput.setLines(1);
        teamNameInput.setMaxLines(1);
        teamNameInput.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
        teamNameInput.setHint(R.string.hint_team_name);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_add_team_title)
                .setMessage(R.string.dialog_add_team_message)
                .setView(teamNameInput)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String teamName = teamNameInput.getText().toString();
                        createTeam(teamName);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void createTeam(String teamName) {
        mMatch.teams.put(teamName, new ArrayList<String>());
        mSectionsPagerAdapter.notifyDataSetChanged();
        mSectionsPagerAdapter.getCurrentFragment().setPlayerSelectionEnabled(true);
    }

    private void addSelectionToTeam(String teamName) {
        PlayerListFragment currentFragment = mSectionsPagerAdapter.getCurrentFragment();
        List<Integer> selectedPlayersIndexes = currentFragment.getSelectedPlayers();
        List<Player> selectedPlayersList = new ArrayList<>();
        Collections.sort(selectedPlayersIndexes);
        for (int i = selectedPlayersIndexes.size() - 1; i >= 0; i--) {
            int index = selectedPlayersIndexes.get(i);
            selectedPlayersList.add(mAllPlayersList.remove(index));
        }

        for (Player player : selectedPlayersList) {
            mMatch.teams.get(teamName).add(player.id);
        }
        updateSelectionMode(false);
        currentFragment.setPlayers(mAllPlayersList);
        currentFragment.teamSelected();
    }

    private void promptSelectTeamToAddPlayers() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                                                                     android.R.layout.select_dialog_singlechoice);
        final List<String> teamNames = new ArrayList<>(mMatch.teams.keySet());
        arrayAdapter.addAll(teamNames);
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int selection) {
                String selectedTeamName = teamNames.get(selection);
                addSelectionToTeam(selectedTeamName);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    @Subscribe
    public void OnPlayerSelectionActivated(PlayerLongClickEvent event) {
        updateSelectionMode(true);
    }

    private void updateSelectionMode(boolean isInSelectionMode) {
        mSelectionMode = isInSelectionMode;
        if (isInSelectionMode) {
            mFab.setImageResource(R.drawable.ic_check);
        } else {
            mFab.setImageResource(R.drawable.ic_add);
            mSectionsPagerAdapter.getCurrentFragment().teamSelected();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private PlayerListFragment mCurrentFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public PlayerListFragment getCurrentFragment() {
            return mCurrentFragment;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (getCurrentFragment() != object) {
                mCurrentFragment = ((PlayerListFragment) object);
                mCurrentFragment.setPlayers(getPlayerListForPosition(position));
                boolean canSelectPlayers = position == 0 && getCount() > 1;
                mCurrentFragment.setPlayerSelectionEnabled(canSelectPlayers);
            }
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            PlayerListFragment playerListFragment = PlayerListFragment.newInstance(mLeagueId);
            playerListFragment.setPlayers(getPlayerListForPosition(position));
            return playerListFragment;
        }

        private List<Player> getPlayerListForPosition(int position) {
            List<Player> playerList = new ArrayList<>();
            if (position == 0) {
                playerList.addAll(mAllPlayersList);
            } else {
                List<String> playersIdsInPosition = mMatch.teams.get(getPageTitle(position));
                for (String id : playersIdsInPosition) {
                    boolean isCheckedIn = mMatch.players.containsKey(id) && mMatch.players.get(id);
                    if (isCheckedIn) { // Avoid adding players who didn't check-in.
                        playerList.add(PlayersCache.getPlayerInfo(id));
                    }
                }
            }
            return playerList;
        }

        @Override
        public int getCount() {
            return 1 + mMatch.teams.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String teamName = getString(R.string.all_players);
            if (position == 0) {
                return teamName;
            }

            List<String> teamNames = new ArrayList<String>(mMatch.teams.keySet());
            return teamNames.get(position - 1);
        }
    }
}
