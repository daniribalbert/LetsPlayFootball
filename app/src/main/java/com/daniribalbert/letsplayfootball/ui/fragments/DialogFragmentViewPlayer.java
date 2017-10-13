package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.firebase.PlayerDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.RatingsDbUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Dialog fragment used to add/edit a new league.
 */
public class DialogFragmentViewPlayer extends BaseDialogFragment implements View.OnClickListener {

    public static final String TAG = DialogFragmentViewPlayer.class.getSimpleName();

    public static final String ARGS_LEAGUE_ID = "ARGS_LEAGUE_ID";
    public static final String ARGS_PLAYER = "ARGS_PLAYER";

    @BindView(R.id.edit_player_pic)
    ImageView mPlayerImage;

    @BindView(R.id.edit_player_name)
    EditText mPlayerName;

    @BindView(R.id.edit_player_nickname)
    EditText mPlayerNickname;

    @BindView(R.id.player_rating)
    RatingBar mRating;

    @BindView(R.id.bt_save_player)
    Button mSavePlayer;

    @BindView(R.id.dialog_progress)
    ProgressBar mProgressBar;

    protected Player mPlayer;
    protected String mLeagueId;
    protected String mPlayerId;

    protected float mPreviousRating = 0f;

    public static DialogFragmentViewPlayer newInstance(String leagueId) {
        Bundle bundle = new Bundle();
        DialogFragmentViewPlayer dFrag = new DialogFragmentViewPlayer();
        bundle.putString(ARGS_LEAGUE_ID, leagueId);
        dFrag.setArguments(bundle);
        dFrag.setRetainInstance(true);
        return dFrag;
    }

    public static DialogFragmentViewPlayer newInstance(String leagueId, String playerId) {
        Bundle bundle = new Bundle();
        DialogFragmentViewPlayer dFrag = new DialogFragmentViewPlayer();
        bundle.putString(ARGS_LEAGUE_ID, leagueId);
        bundle.putString(ARGS_PLAYER, playerId);
        dFrag.setArguments(bundle);
        dFrag.setRetainInstance(true);
        return dFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mLeagueId = args.getString(ARGS_LEAGUE_ID);
            mPlayerId = args.getString(ARGS_PLAYER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_player, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSavePlayer.setOnClickListener(this);
        mPlayerImage.setOnClickListener(this);
        if (savedInstanceState == null) {
            if (TextUtils.isEmpty(mPlayerId)) {
                mPlayer = new Player();
                setupViewMode();
            } else {
                loadPlayerData(mPlayerId);
            }
        } else {
            if (mPlayer != null) {
                GlideUtils.loadCircularImage(mPlayer.image, mPlayerImage);
            }
        }
    }

    protected void setupViewMode() {
        mPlayerImage.setClickable(false);
        mPlayerName.setEnabled(false);
        mPlayerNickname.setEnabled(false);
        mRating.setEnabled(!mPlayer.isGuest());
        mRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser) {
                    promptSaveRating(rating);
                }

            }
        });
        mSavePlayer.setText(R.string.close);
    }

    private void promptSaveRating(final float rating) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_rating_confirm);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String userId = getBaseActivity().getCurrentUser().getUid();
                showProgress(true);
                mPreviousRating = rating;
                RatingsDbUtils.savePlayerRating(mPlayerId, mLeagueId, userId, rating,
                                                new RatingsDbUtils.OnPlayerRateUpdateListener() {
                                                    @Override
                                                    public void onRateUpdated(float rating) {
                                                        showProgress(false);
                                                        mRating.setRating(mPreviousRating);
                                                        mPlayer.setRating(mLeagueId, rating);
                                                        EventBus.getDefault().post(mPlayer);
                                                    }
                                                });
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mRating.setRating(mPlayer.rating.get(mLeagueId));
                dialogInterface.dismiss();
            }
        });

        builder.show();
    }

    protected void loadPlayerData(final String playerId) {
        showProgress(true);
        PlayerDbUtils.getPlayer(playerId, new BaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mPlayer = dataSnapshot.getValue(Player.class);

                if (mPlayer != null) {
                    loadPreviousRating(mPlayerId);

                    mPlayerName.setText(mPlayer.getName());
                    mPlayerNickname.setText(mPlayer.getNickname());

                    if (mPlayer.hasImage()) {
                        GlideUtils.loadCircularImage(mPlayer.image, mPlayerImage);
                    }
                    setupViewMode();
                }
                showProgress(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                super.onCancelled(databaseError);
                showProgress(false);
            }
        });
    }

    protected void loadPreviousRating(String playerId) {
        if (mPlayer.isGuest()){
            mPreviousRating = mPlayer.getRating(mLeagueId);
            mRating.setRating(mPreviousRating);
        } else {
            String mUid = getBaseActivity().getCurrentUser().getUid();
            RatingsDbUtils
                    .getPlayerRateBy(playerId, mLeagueId, mUid, new BaseValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Float value = dataSnapshot.getValue(Float.class);
                            if (value != null) {
                                mPreviousRating = value;
                                mRating.setRating(mPreviousRating);
                            }
                        }
                    });
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
            case R.id.bt_save_player:
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
}
