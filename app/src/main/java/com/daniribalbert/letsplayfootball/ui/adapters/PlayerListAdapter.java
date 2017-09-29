package com.daniribalbert.letsplayfootball.ui.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.events.OpenPlayerEvent;
import com.daniribalbert.letsplayfootball.ui.events.RemovePlayerEvent;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.daniribalbert.letsplayfootball.data.model.Player}
 */
public class PlayerListAdapter extends RecyclerView.Adapter<PlayerListAdapter.ViewHolder> {

    private final List<Player> mValues = new ArrayList<>();

    String mLeagueId;

    public PlayerListAdapter(String leagueId) {
        mLeagueId = leagueId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.player_card, parent, false);

        final ViewHolder viewHolder = new ViewHolder(view);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Player player = mValues.get(position);
        holder.setPlayer(player, mLeagueId);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /**
     * Check if league exists and adds it to the list or update the previous entry.
     *
     * @param player League to be updated on the list.
     */
    public void addItem(Player player) {
        int itemCount = getItemCount();
        mValues.add(player);
        notifyItemInserted(itemCount);
    }

    /**
     * Check if player exists and adds it to the list or update the previous entry.
     *
     * @param player Player to be updated on the list.
     */
    public void updateItem(Player player) {
        int position = mValues.indexOf(player);
        if (position >= 0 && position < mValues.size()) {
            mValues.set(position, player);
            notifyItemChanged(position);
        }
    }

    public void addItems(Collection<Player> players) {
        int itemCount = getItemCount();
        mValues.addAll(players);
        notifyItemRangeInserted(itemCount, players.size());
    }

    public void removeItem(Player player) {
        int index = mValues.indexOf(player);
        mValues.remove(index);
        notifyItemRemoved(index);
    }

    public void clear() {
        mValues.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                                                                       View.OnLongClickListener {
        @BindView(R.id.player_card_view)
        CardView mCardView;
        @BindView(R.id.player_card_title)
        TextView mTitle;
        @BindView(R.id.player_card_image)
        ImageView mImage;
        @BindView(R.id.player_rating)
        RatingBar mRating;

        public ViewHolder(View view) {
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
        public void onClick(View view) {
            final int adapterPosition = getAdapterPosition();
            EventBus.getDefault().post(new OpenPlayerEvent(mValues.get(adapterPosition).id));
        }

        @Override
        public boolean onLongClick(View view) {
            final int adapterPosition = getAdapterPosition();
            EventBus.getDefault().post(new RemovePlayerEvent(mValues.get(adapterPosition)));
            return true;
        }
    }
}
