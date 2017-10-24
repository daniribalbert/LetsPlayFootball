package com.daniribalbert.letsplayfootball.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.PlayersCache;
import com.daniribalbert.letsplayfootball.data.firebase.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.events.PlayerLongClickEvent;
import com.daniribalbert.letsplayfootball.ui.fragments.PlayerListFragment;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.daniribalbert.letsplayfootball.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class TeamsManagerActivity extends TeamsActivity {

    private boolean mSelectionMode;

    @BindView(R.id.teams_fab_toggle)
    FloatingActionButton mFabMain;

    @BindView(R.id.teams_fab_random)
    FloatingActionButton mFabRandom;

    @BindView(R.id.teams_fab_new_team)
    FloatingActionButton mFabNewTeam;

    @Override
    protected void setupViewMode() {
        mFabLayout.setVisibility(View.VISIBLE);
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

    @OnClick(R.id.teams_fab_toggle)
    public void onFabMainButton() {
        if (mSelectionMode) {
            PlayerListFragment currentFragment = mSectionsPagerAdapter.getCurrentFragment();
            List<Integer> selectedPlayersIndexes = currentFragment.getSelectedPlayers();
            if (selectedPlayersIndexes.size() > 0) {
                promptSelectTeamToAddPlayers(selectedPlayersIndexes);
            } else {
                updateSelectionMode(false);
            }
        } else {
            toggleFabMenu(!(mFabNewTeam.getVisibility() == View.VISIBLE));
        }
    }

    private void toggleFabMenu(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        mFabNewTeam.setVisibility(visibility);
        mFabRandom.setVisibility(visibility);

        int drawable = visible ? R.drawable.ic_close : R.drawable.ic_add;
        mFabMain.setImageResource(drawable);
    }


    @OnClick(R.id.teams_fab_random)
    protected void promptRandomTeams() {
        toggleFabMenu(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_sort_teams_title)
                .setMessage(R.string.dialog_sort_teams_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sortTeams(true);
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sortTeams(false);
                    }
                }).setNeutralButton(android.R.string.cancel, null);
        builder.show();
    }

    private void sortTeams(boolean includeGoalkeepers) {
        mMatch.sortTeams(includeGoalkeepers);
        mSectionsPagerAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(1);
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

    @OnClick(R.id.teams_fab_new_team)
    protected void promptAddTeam() {
        toggleFabMenu(false);
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

    private void addSelectionToTeam(String teamName, List<Integer> selectedPlayersIndexes) {
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

        PlayerListFragment currentFragment = mSectionsPagerAdapter.getCurrentFragment();
        currentFragment.setPlayers(mAllPlayersList);
        currentFragment.teamSelected();
    }

    private void promptSelectTeamToAddPlayers(final List<Integer> selectedPlayersIndexes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                                                                     android.R.layout.select_dialog_singlechoice);
        final List<String> teamNames = new ArrayList<>(mMatch.teams.keySet());
        arrayAdapter.addAll(teamNames);
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int selection) {
                String selectedTeamName = teamNames.get(selection);
                addSelectionToTeam(selectedTeamName, selectedPlayersIndexes);
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
        toggleFabMenu(false);
        mSelectionMode = isInSelectionMode;
        if (isInSelectionMode) {
            mFabMain.setImageResource(R.drawable.ic_check);
        } else {
            mFabMain.setImageResource(R.drawable.ic_add);
            mSectionsPagerAdapter.getCurrentFragment().teamSelected();
        }
    }
}
