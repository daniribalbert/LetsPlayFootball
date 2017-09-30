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
import com.daniribalbert.letsplayfootball.ui.adapters.viewholders.PlayerCardViewHolder;
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
public class PlayerListAdapter extends RecyclerView.Adapter<PlayerListAdapter.PlayerViewHolder> {

    private final List<Player> mValues = new ArrayList<>();

    String mLeagueId;

    public PlayerListAdapter(String leagueId) {
        mLeagueId = leagueId;
    }

    @Override
    public PlayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.card_player, parent, false);

        final PlayerViewHolder viewHolder = new PlayerViewHolder(view);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final PlayerViewHolder holder, int position) {
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

    public void removeItem(String playerId) {
        int index = -1;
        for (int i = 0; i < mValues.size(); i++) {
            if (mValues.get(i).id.equalsIgnoreCase(playerId)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            mValues.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void clear() {
        mValues.clear();
        notifyDataSetChanged();
    }

    public class PlayerViewHolder extends PlayerCardViewHolder {

        public PlayerViewHolder(View view) {
            super(view);
        }

        @Override
        public void onClick(View view) {
            Player player = mValues.get(getAdapterPosition());
            EventBus.getDefault().post(new OpenPlayerEvent(player.id, player.getName()));
        }

        @Override
        public boolean onLongClick(View view) {
            final int adapterPosition = getAdapterPosition();
            EventBus.getDefault().post(new RemovePlayerEvent(mValues.get(adapterPosition)));
            return true;
        }
    }
}
