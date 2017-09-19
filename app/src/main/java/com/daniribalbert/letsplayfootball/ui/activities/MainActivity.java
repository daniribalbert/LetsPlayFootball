package com.daniribalbert.letsplayfootball.ui.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.model.Player;
import com.daniribalbert.letsplayfootball.ui.fragments.MyLeaguesFragment;
import com.daniribalbert.letsplayfootball.ui.fragments.ProfileFragment;
import com.daniribalbert.letsplayfootball.ui.fragments.SettingsFragment;
import com.facebook.login.LoginManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String DRAWER_ITEM = "DRAWER_ITEM";


    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    private int mSelectedDrawerItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
        mSelectedDrawerItemId = R.id.nav_home;

        // Add first fragment.
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, MyLeaguesFragment.newInstance(), MyLeaguesFragment.TAG)
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
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        mSelectedDrawerItemId = item.getItemId();

        Fragment frag = null;
        String tag = "";

        switch (mSelectedDrawerItemId) {
            case R.id.nav_home:
                frag = MyLeaguesFragment.newInstance();
                tag = MyLeaguesFragment.TAG;

                break;
            case R.id.nav_profile:
                frag = ProfileFragment.newInstance(Player.fromFirebase(mAuth.getCurrentUser()));
                tag = ProfileFragment.TAG;
                break;
            case R.id.nav_settings:
                frag = SettingsFragment.newInstance();
                tag = SettingsFragment.TAG;
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                final Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }

        final FragmentManager fManager = getFragmentManager();
        FragmentTransaction fTransaction = fManager.beginTransaction();
        fTransaction.replace(R.id.fragment_container, frag, tag);
        fTransaction.commit();

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}