package com.daniribalbert.letsplayfootball.ui.adapters.viewholders;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * ViewHolder for Player cards.
 */
public abstract class PlayerCardViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener,
                   View.OnLongClickListener {
    @BindView(R.id.player_card_view)
    CardView mCardView;
    @BindView(R.id.player_card_title)
    TextView mTitle;
    @BindView(R.id.player_card_image)
    ImageView mImage;
    @BindView(R.id.player_rating)
    RatingBar mRating;
    @BindView(R.id.player_card_position_icon)
    ImageView mPlayerPositionIcon;
    @BindView(R.id.player_card_manager_icon)
    ImageView mPlayerManagerIcon;
    @BindView(R.id.player_card_check_in_icon)
    protected
    ImageView mPlayerCheckInIcon;

    public PlayerCardViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
    }

    public void setPlayer(Player player, League league) {
        setTitle(player);
        setImage(player.image);
        setRating(player.getRating(league.id));
        setPositionIcon(player.isGoalkeeper());
        setManagerIcon(league.isManager(player.id));
    }

    private void setManagerIcon(boolean isManager) {
        int visibility = isManager ? View.VISIBLE : View.GONE;
        mPlayerManagerIcon.setVisibility(visibility);
    }

    public void setTitle(Player player) {
        mTitle.setText(player.toString());
        if (player.isGuest()) {
            Drawable drawable = ContextCompat
                    .getDrawable(mTitle.getContext(), R.drawable.ic_non_user);
            mTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);
        } else {
            mTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " '" + mTitle.getText() + "'";
    }

    public void setImage(String image) {
        GlideUtils.loadCircularImage(image, mImage);
    }

    public void setRating(float rating) {
        mRating.setRating(rating);
    }

    @Override
    public abstract void onClick(View view);

    @Override
    public abstract boolean onLongClick(View view);

    public void setPositionIcon(boolean isGoalkeeper) {
        int resId = isGoalkeeper ? R.drawable.ic_goalkeeper : R.drawable.ic_player_default;
        mPlayerPositionIcon.setImageResource(resId);
    }
}
