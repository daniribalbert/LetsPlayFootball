package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.database.LeagueDbUtils;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Dialog fragment used to add/edit a new league.
 */
public class DialogFragmentEditLeague extends DialogFragment implements View.OnClickListener {

    public static final String TAG = DialogFragmentEditLeague.class.getSimpleName();

    public static final String ARGS_LEAGUE = "ARGS_LEAGUE";

    @BindView(R.id.edit_league_pic)
    ImageView mLeagueImage;

    @BindView(R.id.edit_league_title)
    EditText mLeagueTitle;

    @BindView(R.id.edit_league_description)
    EditText mLeagueDescription;

    @BindView(R.id.bt_save_league)
    View mSaveLeague;

    private EditLeagueListener mListener;
    private League mLeague;

    public static DialogFragmentEditLeague newInstance() {
        DialogFragmentEditLeague dFrag = new DialogFragmentEditLeague();
        dFrag.setRetainInstance(true);
        return dFrag;
    }

    public static DialogFragmentEditLeague newInstance(String leagueId) {
        Bundle bundle = new Bundle();
        DialogFragmentEditLeague dFrag = new DialogFragmentEditLeague();
        bundle.putString(ARGS_LEAGUE, leagueId);
        dFrag.setArguments(bundle);
        dFrag.setRetainInstance(true);
        return dFrag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_league, container, true);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSaveLeague.setOnClickListener(this);
        Bundle args = getArguments();
        if (savedInstanceState == null) {
            if (args != null) {
                if (!args.containsKey(ARGS_LEAGUE)) {
                    return;
                }
                String leagueId = args.getString(ARGS_LEAGUE);
                loadLeagueData(leagueId);
            } else {
                mLeague = new League();
            }
        }
    }

    private void loadLeagueData(String leagueId) {
        LeagueDbUtils.getLeague(leagueId, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLeague = dataSnapshot.getValue(League.class);

                if (mLeague != null) {
                    mLeagueTitle.setText(mLeague.title);
                    mLeagueDescription.setText(mLeague.description);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow()
                   .setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                              WindowManager.LayoutParams.MATCH_PARENT);
    }

    public void setListener(EditLeagueListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View view) {
        mLeague.title = mLeagueTitle.getText().toString();
        mLeague.description = mLeagueDescription.getText().toString();

        mListener.onLeagueSaved(mLeague);
        dismiss();
    }

    public interface EditLeagueListener {
        void onLeagueSaved(League league);
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
}
