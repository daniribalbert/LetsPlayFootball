package com.daniribalbert.letsplayfootball.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.daniribalbert.letsplayfootball.utils.GsonUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * User profile fragment class.
 */
public class ProfileViewFragment extends BaseFragment {

    public static final String TAG = ProfileViewFragment.class.getSimpleName();

    public static String ARGS_PLAYER = "ARGS_PLAYER";

    @BindView(R.id.profile_pic)
    ImageView mProfilePic;
    @BindView(R.id.profile_nickname)
    EditText mPlayerNickname;
    @BindView(R.id.profile_name)
    EditText mPlayerName;

    @BindView(R.id.profile_goalkeeper_check)
    CheckBox mGoalkeeperCheck;

    protected Player mPlayer;

    public static ProfileViewFragment newInstance(@NonNull Player player) {
        Bundle args = new Bundle();

        String playerJson = GsonUtils.toJson(player);
        args.putString(ARGS_PLAYER, playerJson);

        ProfileViewFragment fragment = new ProfileViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            String playerJsonStr = args.getString(ARGS_PLAYER);
            this.mPlayer = GsonUtils.fromJson(playerJsonStr, Player.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_player_profile, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mPlayer.hasImage()) {
            GlideUtils.loadCircularImage(mPlayer.image, mProfilePic);
        }
        mPlayerName.setText(mPlayer.getName());
        mPlayerNickname.setText(mPlayer.getDisplayName());
        mGoalkeeperCheck.setChecked(mPlayer.isGoalkeeper());
        mGoalkeeperCheck.setEnabled(false);
    }
}
