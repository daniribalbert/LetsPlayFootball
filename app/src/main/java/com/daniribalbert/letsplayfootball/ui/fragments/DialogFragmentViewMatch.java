package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.firebase.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.google.firebase.database.DataSnapshot;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Dialog fragment used to add/edit the next match.
 */
public class DialogFragmentViewMatch extends BaseDialogFragment implements View.OnClickListener {

    public static final String TAG = DialogFragmentViewMatch.class.getSimpleName();

    public static final String ARGS_MATCH_ID = "ARGS_MATCH_ID";
    public static final String ARGS_LEAGUE_ID = "ARGS_LEAGUE_ID";
    public static final String ARGS_PLAYER_ID = "ARGS_PLAYER_ID";

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

    @BindView(R.id.tv_check_in_closed)
    TextView mTvCheckinClosed;

    @BindView(R.id.bt_check_in)
    Button mBtCheckIn;

    @BindView(R.id.bt_not_going)
    Button mBtNotGoing;

    @BindView(R.id.match_user_check_in_layout)
    View mMatchCheckInLayout;

    @BindView(R.id.bt_save_match)
    Button mSaveMatch;

    @BindView(R.id.dialog_progress)
    ProgressBar mProgressBar;

    protected Match mMatch;

    protected String mMatchId;
    protected String mLeagueId;
    protected String mPlayerId;

    public static DialogFragmentViewMatch newInstance(String leagueId) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGS_LEAGUE_ID, leagueId);

        DialogFragmentViewMatch dFrag = new DialogFragmentViewMatch();
        dFrag.setRetainInstance(true);
        dFrag.setArguments(bundle);
        return dFrag;
    }

    public static DialogFragmentViewMatch newInstance(String leagueId, String matchId,
                                                      String playerId) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGS_LEAGUE_ID, leagueId);
        bundle.putString(ARGS_MATCH_ID, matchId);
        bundle.putString(ARGS_PLAYER_ID, playerId);

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
            mMatchId = args.getString(ARGS_MATCH_ID);
            mLeagueId = args.getString(ARGS_LEAGUE_ID);
            mPlayerId = args.getString(ARGS_PLAYER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_match, container, false);
        ButterKnife.bind(this, rootView);
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
        } else {
            if (mMatch != null) {
                GlideUtils.loadCircularImage(mMatch.image, mMatchImage);
            }
        }
    }

    protected void setupViewMode() {
        mSaveMatch.setText(R.string.close);
        mSaveMatch.setOnClickListener(this);
        mBtCheckIn.setOnClickListener(this);
        mBtNotGoing.setOnClickListener(this);
    }

    protected void loadMatchData(String leagueId, String matchId) {
        MatchDbUtils.getMatch(leagueId, matchId, new BaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMatch = dataSnapshot.getValue(Match.class);

                if (mMatch != null) {
                    updatedTimeText();
                    updateCheckInLayout();
                    if (mMatch.hasImage()) {
                        GlideUtils.loadCircularImage(mMatch.image, mMatchImage);
                    }
                }
            }
        });

    }

    protected void updateCheckInLayout() {
        if (mMatch.isCheckInOpen()) {
            mBtCheckIn.setVisibility(View.VISIBLE);
            mBtNotGoing.setVisibility(View.VISIBLE);
            mTvCheckinClosed.setVisibility(View.GONE);

            boolean isCheckedIn =
                    mMatch.players.containsKey(mPlayerId) && mMatch.players.get(mPlayerId);
            if (isCheckedIn) {
                mBtCheckIn.setEnabled(false);
                mBtNotGoing.setEnabled(true);
            } else {
                mBtCheckIn.setEnabled(true);
                mBtNotGoing.setEnabled(false);
            }
        } else {
            mBtCheckIn.setVisibility(View.GONE);
            mBtNotGoing.setVisibility(View.GONE);
            mTvCheckinClosed.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow()
                       .setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                                  WindowManager.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_save_match:
                if (getDialog() != null) {
                    dismiss();
                }
                break;
            case R.id.bt_check_in:
                MatchDbUtils.markCheckIn(mMatch, mPlayerId);
                updateCheckInLayout();
                break;
            case R.id.bt_not_going:
                MatchDbUtils.markCheckOut(mMatch, mPlayerId);
                updateCheckInLayout();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
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
