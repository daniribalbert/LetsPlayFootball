package com.daniribalbert.letsplayfootball.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.LeagueCache;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.adapters.viewholders.PlayerCardViewHolder;
import com.daniribalbert.letsplayfootball.ui.events.PlayerClickedEvent;
import com.daniribalbert.letsplayfootball.ui.events.PlayerLongClickEvent;
import com.daniribalbert.letsplayfootball.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.daniribalbert.letsplayfootball.data.model.Player}
 */
public class PlayerListAdapter extends RecyclerView.Adapter<PlayerListAdapter.PlayerViewHolder> {

    private final List<Player> mValues = new ArrayList<>();

    String mLeagueId;
    private List<Integer> mSelectedPlayersIndexes = new ArrayList<>();
    private boolean mPlayerSelectionEnabled;
    private boolean mIsInSelectionMode = false;

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
        holder.setPlayer(player, LeagueCache.getLeagueInfo(mLeagueId));
        boolean isSelected = mSelectedPlayersIndexes.contains(position);
        holder.itemView.setSelected(isSelected);
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
    public void updatePlayer(Player player) {
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

    public List<Integer> getSelectedPlayersIndexes() {
        return mSelectedPlayersIndexes;
    }

    public void clear() {
        mValues.clear();
        notifyDataSetChanged();
    }

    public void setSelectionMode(boolean selectionMode) {
        this.mIsInSelectionMode = selectionMode;
    }

    public void clearSelection() {
        mSelectedPlayersIndexes.clear();
    }

    public void setPlayerSelectionEnabled(boolean enabled) {
        mPlayerSelectionEnabled = enabled;
        if (!enabled){
            mIsInSelectionMode = false;
        }
    }

    public class PlayerViewHolder extends PlayerCardViewHolder {

        public PlayerViewHolder(View view) {
            super(view);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Player player = mValues.get(position);
            EventBus.getDefault().post(new PlayerClickedEvent(player));
            if (mIsInSelectionMode) {
                if (mSelectedPlayersIndexes.contains(position)) {
                    mSelectedPlayersIndexes.remove(mSelectedPlayersIndexes.indexOf(position));
                } else {
                    mSelectedPlayersIndexes.add(position);
                }
                notifyItemChanged(position);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (mPlayerSelectionEnabled) {
                final int adapterPosition = getAdapterPosition();
                EventBus.getDefault().post(new PlayerLongClickEvent(mValues.get(adapterPosition)));
                mSelectedPlayersIndexes.add(getAdapterPosition());
                notifyItemChanged(getAdapterPosition());
            }
            return true;
        }
    }
}
