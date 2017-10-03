package com.daniribalbert.letsplayfootball.ui.adapters.viewholders;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * ViewHolder for Player cards.
 */
public abstract class PlayerCardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                                                                                      View.OnLongClickListener {
    @BindView(R.id.player_card_view)
    CardView mCardView;
    @BindView(R.id.player_card_title)
    TextView mTitle;
    @BindView(R.id.player_card_image)
    ImageView mImage;
    @BindView(R.id.player_rating)
    RatingBar mRating;

    public PlayerCardViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
    }

    public void setPlayer(Player player, String leagueId) {
        setTitle(player.toString());
        setImage(player.image);
        setRating(player.getRating(leagueId));
    }

    public void setTitle(String title) {
        mTitle.setText(title);
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
}
