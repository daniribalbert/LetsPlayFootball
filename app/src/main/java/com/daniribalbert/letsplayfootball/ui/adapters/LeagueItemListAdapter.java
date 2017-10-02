package com.daniribalbert.letsplayfootball.ui.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.adapters.viewholders.PlayerCardViewHolder;
import com.daniribalbert.letsplayfootball.ui.events.OpenMatchEvent;
import com.daniribalbert.letsplayfootball.ui.events.OpenPlayerEvent;
import com.daniribalbert.letsplayfootball.ui.events.RemoveMatchEvent;
import com.daniribalbert.letsplayfootball.ui.events.RemovePlayerEvent;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link RecyclerView.Adapter} that can display items from the League
 */
public class LeagueItemListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MATCH = 1;
    private static final int TYPE_PLAYER = 2;

    private final List<Player> mPlayers = new ArrayList<>();
    private final List<Match> mUpcomingMatches = new ArrayList<>();

    String mLeagueId;

    public LeagueItemListAdapter(String leagueId) {
        mLeagueId = leagueId;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mUpcomingMatches.size()) {
            return TYPE_MATCH;
        }
        return TYPE_PLAYER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        RecyclerView.ViewHolder viewHolder;
        switch (viewType) {
            case TYPE_MATCH:
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.card_match, parent, false);
                viewHolder = new MatchViewHolder(view);
                break;
            case TYPE_PLAYER:
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.card_player, parent, false);
                viewHolder = new LeaguePlayerViewHolder(view);
                break;
            default:
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.card_player, parent, false);
                viewHolder = new LeaguePlayerViewHolder(view);
                break;
        }


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        switch (itemViewType) {
            case TYPE_MATCH:
                Match match = mUpcomingMatches.get(position);
                ((MatchViewHolder) holder).setMatch(match);
                break;
            case TYPE_PLAYER:
                int playerPosition = position - mUpcomingMatches.size();
                Player player = mPlayers.get(playerPosition);
                ((LeaguePlayerViewHolder) holder).setPlayer(player, mLeagueId);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mPlayers.size() + mUpcomingMatches.size();
    }

    /**
     * Add Player to list.
     *
     * @param player player to be added on the list.
     */
    public void addPlayer(Player player) {
        int itemCount = getItemCount();
        mPlayers.add(player);
        notifyItemInserted(itemCount);
    }

    /**
     * Add Match to the list
     *
     * @param match match to be added to the list.
     */
    public void addMatch(Match match) {
        int itemCount = mUpcomingMatches.size();
        mUpcomingMatches.add(match);
        notifyItemInserted(itemCount);
    }

    /**
     * Check if player exists and adds it to the list or update the previous entry.
     *
     * @param player Player to be updated on the list.
     */
    public void updatePlayer(Player player) {
        int position = mPlayers.indexOf(player);
        if (position >= 0 && position < mPlayers.size()) {
            mPlayers.set(position, player);
            notifyItemChanged(mUpcomingMatches.size() + position);
        }
    }

    public void addPlayers(Collection<Player> players) {
        int itemCount = getItemCount();
        mPlayers.addAll(players);
        notifyItemRangeInserted(itemCount, players.size());
    }

    public void removePlayer(Player player) {
        int index = mPlayers.indexOf(player);
        mPlayers.remove(index);
        notifyItemRemoved(index);
    }

    public void removePlayer(String playerId) {
        int index = -1;
        for (int i = 0; i < mPlayers.size(); i++) {
            if (mPlayers.get(i).id.equalsIgnoreCase(playerId)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            mPlayers.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void clear() {
        mPlayers.clear();
        mUpcomingMatches.clear();
        notifyDataSetChanged();
    }
    public void clearUpcomingMatches() {
        int size = mUpcomingMatches.size();
        mUpcomingMatches.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void clearPlayers() {
        int size = mPlayers.size();
        mPlayers.clear();
        notifyItemRangeRemoved(mUpcomingMatches.size(), size);
    }

    public void removeMatch(Match match) {
        int index = mUpcomingMatches.indexOf(match);
        mUpcomingMatches.remove(index);
        notifyItemRemoved(index);
    }


    public class LeaguePlayerViewHolder extends PlayerCardViewHolder {

        public LeaguePlayerViewHolder(View view) {
            super(view);
        }

        @Override
        public void onClick(View view) {
            Player player = mPlayers.get(getAdapterPosition() - mUpcomingMatches.size());
            EventBus.getDefault().post(new OpenPlayerEvent(player.id, player.getName()));
        }

        @Override
        public boolean onLongClick(View view) {
            final int adapterPosition = getAdapterPosition();
            Player player = mPlayers.get(adapterPosition - mUpcomingMatches.size());
            EventBus.getDefault().post(new RemovePlayerEvent(player));
            return true;
        }
    }

    public class MatchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                                                                            View.OnLongClickListener {
        @BindView(R.id.match_card_view)
        CardView mCardView;
        @BindView(R.id.match_card_day)
        TextView mDay;
        @BindView(R.id.match_card_time)
        TextView mTime;
        @BindView(R.id.match_card_image)
        ImageView mImage;


        public MatchViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        public void setMatch(Match match) {
            setTime(match.getTime());
            setDay(match.getDate());
            setImage(match.image);
        }

        public void setTime(String timeStr) {
            mTime.setText(timeStr);
        }

        @Override
        public String toString() {
            return mTime.getText().toString() + " " + mDay.getText().toString();
        }

        public void setImage(String image) {
            GlideUtils.loadCircularImage(image, mImage);
        }

        public void setDay(String dayStr) {
            mDay.setText(dayStr);
        }

        @Override
        public void onClick(View view) {
            Match match = mUpcomingMatches.get(getAdapterPosition());
            EventBus.getDefault().post(new OpenMatchEvent(match.id));
        }

        @Override
        public boolean onLongClick(View view) {
            final int adapterPosition = getAdapterPosition();
            EventBus.getDefault().post(new RemoveMatchEvent(mUpcomingMatches.get(adapterPosition)));
            return true;
        }
    }
}
