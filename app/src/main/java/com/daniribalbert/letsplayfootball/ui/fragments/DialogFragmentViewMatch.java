package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
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
import com.daniribalbert.letsplayfootball.data.database.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.database.listeners.BaseValueEventListener;
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

    @BindView(R.id.edit_match_pic)
    ImageView mMatchImage;

    @BindView(R.id.edit_match_time_day)
    TextView mMatchDay;

    @BindView(R.id.edit_match_time_hour)
    TextView mMatchHour;

    @BindView(R.id.edit_match_time_layout)
    View mTimeLayout;

    @BindView(R.id.bt_save_match)
    Button mSaveMatch;

    @BindView(R.id.dialog_progress)
    ProgressBar mProgressBar;

    protected Match mMatch;

    protected String mMatchId;
    protected String mLeagueId;

    public static DialogFragmentViewMatch newInstance(String leagueId) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGS_LEAGUE_ID, leagueId);

        DialogFragmentViewMatch dFrag = new DialogFragmentViewMatch();
        dFrag.setRetainInstance(true);
        dFrag.setArguments(bundle);
        return dFrag;
    }

    public static DialogFragmentViewMatch newInstance(String leagueId, String matchId) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGS_LEAGUE_ID, leagueId);
        bundle.putString(ARGS_MATCH_ID, matchId);

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

    protected void setupViewMode(){
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
                        GlideUtils.loadCircularImage(mMatch.image, mMatchImage);
                    }
                }
            }
        });

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
        mMatchDay.setText(mMatch.getDate());
        mMatchHour.setText(mMatch.getTime());
    }

}
