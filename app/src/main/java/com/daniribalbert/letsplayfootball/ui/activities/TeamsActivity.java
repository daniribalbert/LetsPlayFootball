package com.daniribalbert.letsplayfootball.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.PlayersCache;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.constants.IntentConstants;
import com.daniribalbert.letsplayfootball.ui.fragments.PlayerListFragment;
import com.daniribalbert.letsplayfootball.utils.GsonUtils;
import com.daniribalbert.letsplayfootball.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TeamsActivity extends BaseActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    protected SectionsPagerAdapter mSectionsPagerAdapter;

    protected List<Player> mAllPlayersList = new ArrayList<>();

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @BindView(R.id.container)
    ViewPager mViewPager;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.teams_fab_layout)
    View mFabLayout;

    @BindView(R.id.tabs)
    TabLayout mTabLayout;
    protected String mLeagueId;

    protected Match mMatch;

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teams);
        ButterKnife.bind(this);

        loadArgs(getIntent());
        setSupportActionBar(mToolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        loadPlayers();
        setupViewMode();
    }

    protected void setupViewMode() {
        mFabLayout.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.share_menu, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        updateShareIntent();
        // Return true to display menu
        return true;
    }

    protected void updateShareIntent() {
        LogUtils.d("TEAMS TO STRING: \n" + mMatch.getTeamsString());
        if (mShareActionProvider != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, mMatch.getTeamsString());
            shareIntent.setType("text/plain");
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }


    protected void loadPlayers() {
        HashMap<String, Player> allPlayersMap = new HashMap<>(
                PlayersCache.getCurrentLeaguePlayers());

        // Filter for player which have checked-in.
        List<String> toRemove = new ArrayList<>();
        for (String playerId : allPlayersMap.keySet()) {
            if (!mMatch.isCheckedIn(playerId)) {
                toRemove.add(playerId);
            }
        }
        for (String removeThis : toRemove) {
            allPlayersMap.remove(removeThis);
        }

        if (mMatch.teams.size() > 0) {
            for (List<String> team : mMatch.teams.values()) {
                for (String player : team) {
                    allPlayersMap.remove(player);
                }
            }
        }

        // Sort and remove players which exceed Match players limit.
        LinkedList<Player> sortedList = mMatch.sortPlayersByCheckIn(allPlayersMap.values());
        while (sortedList.size() > mMatch.getMaxPlayers()
                && mMatch.getMaxPlayers() != Match.NUMBER_OF_PLAYERS_UNDEFINED) {
            sortedList.removeLast();
        }
        mAllPlayersList.addAll(sortedList);
    }

    protected void loadArgs(Intent intent) {
        if (intent != null) {
            mLeagueId = intent.getStringExtra(IntentConstants.ARGS_LEAGUE_ID);
            String matchJsonStr = intent.getStringExtra(IntentConstants.ARGS_MATCH_JSON);
            mMatch = GsonUtils.fromJson(matchJsonStr, Match.class);
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
                mCurrentFragment.setPlayerSelectionEnabled(false);
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

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            updateCurrentItem();
        }

        public void updateCurrentItem() {
            if (mCurrentFragment != null) {
                mCurrentFragment.setPlayers(getPlayerListForPosition(mViewPager.getCurrentItem()));
            }
        }

        private List<Player> getPlayerListForPosition(int position) {
            List<Player> playerList = new ArrayList<>();
            if (position == 0) {
                playerList.addAll(mMatch.getPlayersWithNoTeam());
            } else {
                List<String> playersIdsInPosition = mMatch.teams.get(getPageTitle(position));
                for (String id : playersIdsInPosition) {
                    boolean isCheckedIn = mMatch.isCheckedIn(id);
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
