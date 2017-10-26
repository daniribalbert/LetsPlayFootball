package com.daniribalbert.letsplayfootball.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.ui.events.FabClickedEvent;
import com.daniribalbert.letsplayfootball.ui.fragments.BaseFragment;
import com.daniribalbert.letsplayfootball.ui.fragments.LeagueSearchFragment;
import com.daniribalbert.letsplayfootball.ui.fragments.MatchHistoryFragment;
import com.daniribalbert.letsplayfootball.ui.fragments.MatchUpcomingFragment;
import com.daniribalbert.letsplayfootball.ui.fragments.MyLeaguesFragment;
import com.daniribalbert.letsplayfootball.ui.fragments.PendingRequestsFragment;
import com.daniribalbert.letsplayfootball.ui.fragments.ProfileFragment;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String DRAWER_ITEM = "DRAWER_ITEM";


    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @BindView(R.id.fab_menu_1)
    FloatingActionButton mFab1;

    @BindView(R.id.fab_menu_2)
    FloatingActionButton mFab2;

    @BindView(R.id.fab_menu_layout)
    View mFabMenuLayout;

    @BindView(R.id.app_progress)
    ProgressBar mProgressBar;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private int mSelectedDrawerItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
        mSelectedDrawerItemId = R.id.nav_home;

        // Add first fragment.
        if (savedInstanceState == null) {
            MyLeaguesFragment frag = MyLeaguesFragment.newInstance();
            frag.setProgress(mProgressBar);
            getSupportFragmentManager().beginTransaction()
                                       .add(R.id.fragment_container, frag, MyLeaguesFragment.TAG)
                                       .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DRAWER_ITEM, mSelectedDrawerItemId);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectedDrawerItemId = savedInstanceState.getInt(DRAWER_ITEM);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        mSelectedDrawerItemId = item.getItemId();

        BaseFragment frag = null;
        String tag = "";

        switch (mSelectedDrawerItemId) {
            case R.id.nav_home:
                frag = MyLeaguesFragment.newInstance();
                ((BaseFragment) frag).setProgress(mProgressBar);
                tag = MyLeaguesFragment.TAG;
                mFab.setVisibility(View.VISIBLE);
                break;
            case R.id.nav_profile:
                frag = ProfileFragment.newInstance(getCurrentUser());
                ((BaseFragment) frag).setProgress(mProgressBar);
                tag = ProfileFragment.TAG;
                mFab.setImageResource(android.R.drawable.ic_menu_edit);
                mFab.setVisibility(View.VISIBLE);
                break;
            case R.id.nav_pending_requests:
                frag = PendingRequestsFragment.newInstance();
                ((BaseFragment) frag).setProgress(mProgressBar);
                tag = PendingRequestsFragment.TAG;
                mFab.setVisibility(View.GONE);
                break;
            case R.id.nav_upcoming_matches:
                frag = MatchUpcomingFragment.newInstance();
                ((BaseFragment) frag).setProgress(mProgressBar);
                tag = MatchUpcomingFragment.TAG;
                mFab.setVisibility(View.GONE);
                break;
            case R.id.nav_history:
                frag = MatchHistoryFragment.newInstance();
                ((BaseFragment) frag).setProgress(mProgressBar);
                tag = MatchHistoryFragment.TAG;
                mFab.setVisibility(View.GONE);
                break;
            case R.id.nav_settings:
                //frag = SettingsFragment.newIntent();
                //tag = SettingsFragment.TAG;
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                final Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }

        if (frag != null) {
            final FragmentManager fManager = getSupportFragmentManager();
            FragmentTransaction fTransaction = fManager.beginTransaction();
            fTransaction.replace(R.id.fragment_container, frag, tag);
            fTransaction.commit();
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @OnClick(R.id.fab)
    public void toggleFabMenu() {
        if (mFabMenuLayout.getVisibility() == View.VISIBLE){
            mFabMenuLayout.setVisibility(View.GONE);
            mFab.setImageResource(R.drawable.ic_add);
        } else {
            mFabMenuLayout.setVisibility(View.VISIBLE);
            mFab.setImageResource(R.drawable.ic_close);
        }
    }

    @OnClick(R.id.fab_menu_1)
    public void searchForLeague() {
        toggleFabMenu();
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra(SearchActivity.ARGS_TAG, LeagueSearchFragment.TAG);
        startActivity(intent);
    }

    @OnClick(R.id.fab_menu_2)
    public void addNewLeague() {
        toggleFabMenu();
        EventBus.getDefault().post(new FabClickedEvent(mFab));
    }

}
