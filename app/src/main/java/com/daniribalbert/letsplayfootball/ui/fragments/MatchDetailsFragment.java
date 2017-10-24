package com.daniribalbert.letsplayfootball.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.firebase.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.ui.activities.TeamsActivity;
import com.daniribalbert.letsplayfootball.ui.constants.IntentConstants;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.daniribalbert.letsplayfootball.utils.GsonUtils;
import com.google.firebase.database.DataSnapshot;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment to show a Match details options.
 */
public class MatchDetailsFragment extends BaseFragment implements View.OnClickListener {

    public static final String TAG = MatchDetailsFragment.class.getSimpleName();

    @BindView(R.id.match_card_view)
    View mMatchCardView;

    @BindView(R.id.match_card_image)
    ImageView mMatchImageView;

    @BindView(R.id.match_card_time)
    TextView mMatchCardTime;

    @BindView(R.id.match_card_day)
    TextView mMatchCardDay;

    @BindView(R.id.match_teams_bt)
    View mTeamsBt;

    @BindView(R.id.app_progress)
    ProgressBar mProgressBar;

    @BindView(R.id.tv_check_in_closed)
    TextView mTvCheckInClosed;

    @BindView(R.id.bt_check_in)
    Button mBtCheckIn;

    @BindView(R.id.bt_not_going)
    Button mBtNotGoing;

    @BindView(R.id.match_user_check_in_layout)
    View mMatchCheckInLayout;

    @BindView(R.id.match_players_bt)
    View mMatchPlayersBt;

    @BindView(R.id.match_manager_guest_check_in_bt)
    View mMatchManageGuestsBt;

    protected String mLeagueId;
    protected String mMatchId;
    protected String mPlayerId;
    protected Match mMatch;

    public static MatchDetailsFragment newInstance(String matchId, String leagueId,
                                                   String playerId) {
        Bundle args = new Bundle();
        args.putString(IntentConstants.ARGS_MATCH_ID, matchId);
        args.putString(IntentConstants.ARGS_LEAGUE_ID, leagueId);
        args.putString(IntentConstants.ARGS_PLAYER_ID, playerId);

        MatchDetailsFragment fragment = new MatchDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_match_details, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadArgs();
    }

    @Override
    public void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.VISIBLE);
        loadMatchData();
    }

    private void loadArgs() {
        Bundle args = getArguments();
        if (args != null) {
            mMatchId = args.getString(IntentConstants.ARGS_MATCH_ID);
            mLeagueId = args.getString(IntentConstants.ARGS_LEAGUE_ID);
            mPlayerId = args.getString(IntentConstants.ARGS_PLAYER_ID);
        }
    }

    protected void setupListeners() {
        mMatchCardView.setOnClickListener(this);
        mBtCheckIn.setOnClickListener(this);
        mBtNotGoing.setOnClickListener(this);
        mTeamsBt.setOnClickListener(this);
        mMatchPlayersBt.setOnClickListener(this);
    }

    protected void loadMatchData() {
        MatchDbUtils.getMatch(mLeagueId, mMatchId, new BaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMatch = dataSnapshot.getValue(Match.class);

                if (mMatch != null && isAdded()) {
                    updateMatchLayout();
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void updateMatchLayout() {
        GlideUtils.loadCircularImage(mMatch.getImage(), mMatchImageView);
        mMatchCardDay.setText(mMatch.getDateString(mMatch.time));
        mMatchCardTime.setText(mMatch.getTimeStr(mMatch.time));
        updateCheckInLayout();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.match_card_view:
                showMatchDialog();
                break;
            case R.id.match_teams_bt:
                startActivity(getTeamsActivityIntent());
                break;
            case R.id.bt_check_in:
                MatchDbUtils.markCheckIn(mMatch, mPlayerId);
                updateCheckInLayout();
                break;
            case R.id.bt_not_going:
                MatchDbUtils.markCheckOut(mMatch, mPlayerId);
                updateCheckInLayout();
                break;
            case R.id.match_players_bt:
                showMatchQueue();
                break;
        }
    }

    private void showMatchQueue() {
        MatchPlayerQueueFragment frag = MatchPlayerQueueFragment.newInstance(mMatch);
        getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, frag, MatchPlayerQueueFragment.TAG)
                            .addToBackStack(MatchPlayerQueueFragment.TAG).commit();
    }

    protected Intent getTeamsActivityIntent() {
        Intent intent = new Intent(getActivity(), TeamsActivity.class);
        intent.putExtra(IntentConstants.ARGS_LEAGUE_ID, mLeagueId);
        intent.putExtra(IntentConstants.ARGS_MATCH_JSON, GsonUtils.toJson(mMatch));
        return intent;
    }

    protected void showMatchDialog() {
        String userId = getBaseActivity().getCurrentUser().getUid();
        if (mMatch.isPastMatch()) {
            DialogFragmentPostMatch dFrag = DialogFragmentPostMatch
                    .newInstance(mLeagueId, mMatchId, userId);
            dFrag.show(getFragmentManager(), DialogFragmentPostMatch.TAG);
        } else {
            DialogFragmentViewMatch dFrag = DialogFragmentViewMatch
                    .newInstance(mLeagueId, mMatchId, userId);
            dFrag.show(getFragmentManager(), DialogFragmentViewMatch.TAG);
        }
    }

    protected void updateCheckInLayout() {
        if (mMatch.isCheckInOpen()) {
            mBtCheckIn.setVisibility(View.VISIBLE);
            mBtNotGoing.setVisibility(View.VISIBLE);
            mTvCheckInClosed.setVisibility(View.GONE);

            boolean isCheckedIn = mMatch.isCheckedIn(mPlayerId);
            ;
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
            if (mMatch.isPastMatch()) {
                mTvCheckInClosed.setVisibility(View.GONE);
            } else {
                mTvCheckInClosed.setVisibility(View.VISIBLE);
            }
        }
    }
}
