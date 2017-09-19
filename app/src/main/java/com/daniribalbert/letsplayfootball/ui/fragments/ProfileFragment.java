package com.daniribalbert.letsplayfootball.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.events.FabClickedEvent;
import com.daniribalbert.letsplayfootball.model.Player;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * User profile fragment class.
 */
public class ProfileFragment extends BaseFragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    @BindView(R.id.profile_pic)
    ImageView mProfilePic;
    @BindView(R.id.profile_nickname)
    EditText mPlayerNickname;
    @BindView(R.id.profile_name)
    EditText mPlayerName;

    private Player mPlayer;

    public static ProfileFragment newInstance(@NonNull Player player) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.mPlayer = player;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_player_profile, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Glide.with(this).load(mPlayer.getImage()).apply(RequestOptions.circleCropTransform()).into(mProfilePic);
        mPlayerName.setText(getString(R.string.profile_full_name, mPlayer.getName()));
        mPlayerNickname.setText(mPlayer.getNickname());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onFabClicked(FabClickedEvent event) {
        toggleEditMode();
        if (mPlayerName.isEnabled()) {
            event.fab.setImageResource(R.drawable.ic_check);
        } else {
            event.fab.setImageResource(android.R.drawable.ic_menu_edit);
            // TODO: Save new profile info.
        }
    }

    public void toggleEditMode() {
        boolean toggle = !mPlayerName.isEnabled();

        mPlayerName.setEnabled(toggle);
        mPlayerNickname.setEnabled(toggle);
    }
}
