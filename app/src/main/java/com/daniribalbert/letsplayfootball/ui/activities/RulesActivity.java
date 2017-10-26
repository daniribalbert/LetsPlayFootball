package com.daniribalbert.letsplayfootball.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.LeagueCache;
import com.daniribalbert.letsplayfootball.data.firebase.LeagueDbUtils;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.ui.constants.IntentConstants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment that holds information about the league rules.
 */
public class RulesActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {

    public static final String TAG = RulesActivity.class.getSimpleName();

    private static final String ARGS_RULES = "ARGS_RULES";
    private static final String ARGS_EDITABLE = "ARGS_EDITABLE";

    @BindView(R.id.rules_tv)
    TextView mRulesTv;

    @BindView(R.id.rules_edit)
    EditText mRulesEdit;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private String mRulesText;
    private String mLeagueId;
    private boolean mCanEdit;

    private boolean mEditMode;

    public static Intent newIntent(Context context, String leagueId, String leagueRules,
                                   boolean canEdit) {
        Intent intent = new Intent(context, RulesActivity.class);
        intent.putExtra(IntentConstants.ARGS_LEAGUE_ID, leagueId);
        intent.putExtra(ARGS_RULES, leagueRules);
        intent.putExtra(ARGS_EDITABLE, canEdit);

        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);
        ButterKnife.bind(this);
        loadArgs(getIntent());

        setSupportActionBar(mToolbar);
        setupView();
    }

    private void loadArgs(Intent intent) {
        mLeagueId = intent.getStringExtra(IntentConstants.ARGS_LEAGUE_ID);
        mRulesText = intent.getStringExtra(ARGS_RULES);
        mCanEdit = intent.getBooleanExtra(ARGS_EDITABLE, false);
    }

    private void setupView() {
        if (TextUtils.isEmpty(mRulesText)) {
            int resId = mCanEdit ? R.string.rules_empty_text_edit : R.string.rules_empty_text;
            mRulesTv.setText(resId);
        } else {
            mRulesTv.setText(mRulesText);
        }
        if (mCanEdit) {
            mToolbar.setOnMenuItemClickListener(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mCanEdit) {
            mToolbar.inflateMenu(R.menu.edit_confirm_menu);

            if (mEditMode) {
                mToolbar.getMenu().findItem(R.id.menu_item_edit).setVisible(false);
                mToolbar.getMenu().findItem(R.id.menu_item_confirm).setVisible(true);
            } else {
                mToolbar.getMenu().findItem(R.id.menu_item_edit).setVisible(true);
                mToolbar.getMenu().findItem(R.id.menu_item_confirm).setVisible(false);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @OnClick(R.id.rules_tv)
    public void onRulesClicked() {
        if (mCanEdit) {
            toggleEditMode(true);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_edit:
                toggleEditMode(true);
                break;
            case R.id.menu_item_confirm:
                String newRules = mRulesEdit.getText().toString();
                // Avoid saving the same old rules.
                if (!newRules.equalsIgnoreCase(newRules)) {
                    mRulesText = newRules;
                    LeagueDbUtils.saveLeagueRules(mLeagueId, mRulesText);
                    League currentLeague = LeagueCache.getLeagueInfo(mLeagueId);
                    if (currentLeague != null) {
                        currentLeague.rules = mRulesText;
                        LeagueCache.saveLeagueInfo(currentLeague);
                    }
                    if (!TextUtils.isEmpty(mRulesText)) {
                        mRulesTv.setText(mRulesText);
                    }
                }
                toggleEditMode(false);
                break;
        }
        return true;
    }

    private void toggleEditMode(boolean editing) {
        mEditMode = editing;
        if (editing) {
            mRulesEdit.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(mRulesText)) {
                mRulesEdit.setText(mRulesText);
            }
        } else {
            mRulesEdit.setVisibility(View.GONE);
        }
        invalidateOptionsMenu();
    }
}
