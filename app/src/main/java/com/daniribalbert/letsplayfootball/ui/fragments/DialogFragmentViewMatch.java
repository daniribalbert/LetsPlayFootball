package com.daniribalbert.letsplayfootball.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.firebase.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.ui.constants.IntentConstants;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.google.firebase.database.DataSnapshot;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Dialog fragment used to add/edit the next match.
 */
public class DialogFragmentViewMatch extends BaseDialogFragment implements View.OnClickListener {

    public static final String TAG = DialogFragmentViewMatch.class.getSimpleName();

    @BindView(R.id.edit_match_pic)
    ImageView mMatchImage;

    @BindView(R.id.edit_match_time_day)
    TextView mMatchDay;
    @BindView(R.id.edit_match_time_hour)
    TextView mMatchHour;

    @BindView(R.id.edit_match_check_in_start_day)
    TextView mMatchCheckInStartDay;
    @BindView(R.id.edit_match_check_in_start_hour)
    TextView mMatchCheckInStartHour;

    @BindView(R.id.edit_match_check_in_end_day)
    TextView mMatchCheckInEndDay;
    @BindView(R.id.edit_match_check_in_end_hour)
    TextView mMatchCheckInEndHour;

    @BindView(R.id.bt_save_match)
    Button mSaveMatch;

    @BindView(R.id.dialog_progress)
    ProgressBar mProgressBar;

    @BindView(R.id.checkbox_check_them_all)
    CheckBox mCheckThemAllBox;

    @BindView(R.id.match_max_players)
    TextView mMaxPlayersTextView;

    protected Match mMatch;

    protected String mMatchId;
    protected String mLeagueId;
    protected String mPlayerId;

    public static DialogFragmentViewMatch newInstance(String leagueId) {
        Bundle bundle = new Bundle();
        bundle.putString(IntentConstants.ARGS_LEAGUE_ID, leagueId);

        DialogFragmentViewMatch dFrag = new DialogFragmentViewMatch();
        dFrag.setRetainInstance(true);
        dFrag.setArguments(bundle);
        return dFrag;
    }

    public static DialogFragmentViewMatch newInstance(String leagueId, String matchId,
                                                      String playerId) {
        Bundle bundle = new Bundle();
        bundle.putString(IntentConstants.ARGS_LEAGUE_ID, leagueId);
        bundle.putString(IntentConstants.ARGS_MATCH_ID, matchId);
        bundle.putString(IntentConstants.ARGS_PLAYER_ID, playerId);

        DialogFragmentViewMatch dFrag = new DialogFragmentViewMatch();
        dFrag.setArguments(bundle);
        dFrag.setRetainInstance(true);
        return dFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadArgs();
    }

    private void loadArgs() {
        Bundle args = getArguments();
        if (args != null) {
            mMatchId = args.getString(IntentConstants.ARGS_MATCH_ID);
            mLeagueId = args.getString(IntentConstants.ARGS_LEAGUE_ID);
            mPlayerId = args.getString(IntentConstants.ARGS_PLAYER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_match, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewMode();
        if (savedInstanceState == null) {
            if (TextUtils.isEmpty(mMatchId)) {
                mMatch = new Match(mLeagueId);
                updatedTimeText();
            } else {
                loadMatchData(mLeagueId, mMatchId);
            }
        }
        if (mMatch != null) {
            GlideUtils.loadCircularImage(mMatch.getImage(), mMatchImage);
        }
    }

    protected void setupViewMode() {
        mCheckThemAllBox.setVisibility(View.GONE);
        mSaveMatch.setText(R.string.close);
        mSaveMatch.setOnClickListener(this);
    }

    protected void loadMatchData(String leagueId, String matchId) {
        MatchDbUtils.getMatch(leagueId, matchId, new BaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMatch = dataSnapshot.getValue(Match.class);

                if (mMatch != null) {
                    updatedTimeText();
                    if (mMatch.hasImage()) {
                        GlideUtils.loadCircularImage(mMatch.getImage(), mMatchImage);
                    }
                    mMaxPlayersTextView.setText(mMatch.getMaxPlayersText());
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_save_match:
                if (getDialog() != null) {
                    dismiss();
                }
                break;
        }
    }

    protected void updatedTimeText() {
        mMatchDay.setText(mMatch.getDateString(mMatch.time));
        mMatchHour.setText(mMatch.getTimeStr(mMatch.time));

        mMatchCheckInStartDay.setText(mMatch.getDateString(mMatch.checkInStart));
        mMatchCheckInStartHour.setText(mMatch.getTimeStr(mMatch.checkInStart));

        mMatchCheckInEndDay.setText(mMatch.getDateString(mMatch.checkInEnds));
        mMatchCheckInEndHour.setText(mMatch.getTimeStr(mMatch.checkInEnds));
    }

}
