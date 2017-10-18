package com.daniribalbert.letsplayfootball.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.LeagueCache;
import com.daniribalbert.letsplayfootball.data.cache.PlayersCache;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.adapters.viewholders.PlayerCardViewHolder;
import com.daniribalbert.letsplayfootball.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter which handles how Players inside a match queue are loaded.
 */
public class MatchPlayerQueueAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CHECKED_IN_TITLE = 200;
    private static final int TYPE_CHECKED_OUT_TITLE = 201;
    private static final int TYPE_WAITING_LIST_TITLE = 202;

    private static final int TYPE_CHECKED_IN_PLAYER = 100;
    private static final int TYPE_CHECKED_OUT_PLAYER = 101;
    private static final int TYPE_WAITING_LIST_PLAYER = 102;

    private Match mMatch;

    private List<Player> mCheckedInList = new ArrayList<>();
    private List<Player> mCheckedOutList = new ArrayList<>();
    private List<Player> mWaitingList = new ArrayList<>();

    public MatchPlayerQueueAdapter(Match match) {
        mMatch = match;
        setupAdapter();
    }

    private void setupAdapter() {
        HashMap<String, Player> currentLeaguePlayers = PlayersCache.getCurrentLeaguePlayers();
        for (Map.Entry<String, Long> entry : mMatch.players.entrySet()) {
            Player player = currentLeaguePlayers.get(entry.getKey());
            if (entry.getValue() > 0) {
                if (mMatch.getMaxPlayers() == Match.NUMBER_OF_PLAYERS_UNDEFINED
                        || mCheckedInList.size() <= mMatch.maxPlayers) {
                    mCheckedInList.add(player);
                } else {
                    mWaitingList.add(player);
                }
            } else {
                mCheckedOutList.add(player);
            }
        }
        LogUtils.d("Checked in: " + mCheckedInList.size());
        LogUtils.d("Checked out: " + mCheckedOutList.size());
        LogUtils.d("waiting: " + mWaitingList.size());
    }

    @Override
    public int getItemCount() {
        return mMatch.players.size() + sectionCount();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        String sectionTitle = null;
        switch (viewType) {
            case TYPE_CHECKED_IN_TITLE:
                sectionTitle = parent.getContext().getString(R.string.check_in);
                break;
            case TYPE_CHECKED_OUT_TITLE:
                sectionTitle = parent.getContext().getString(R.string.cant_go);
                break;
            case TYPE_WAITING_LIST_TITLE:
                sectionTitle = parent.getContext().getString(R.string.waiting_list);
                break;
        }
        if (sectionTitle == null) {
            View view = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.card_player, parent, false);
            return new PlayerCardViewHolder(view) {
                @Override
                public void onClick(View view) {

                }

                @Override
                public boolean onLongClick(View view) {
                    return false;
                }
            };
        } else {
            TextView textView = new TextView(parent.getContext());
            textView.setText(sectionTitle);
            return new TextViewHolder(textView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) >= TYPE_CHECKED_IN_TITLE) {
            return;
        } else {
            PlayerCardViewHolder playerHolder = (PlayerCardViewHolder) holder;
            League league = LeagueCache.getLeagueInfo(mMatch.leagueId);
            int listPosition;
            if (getItemViewType(position) == TYPE_CHECKED_IN_PLAYER) {
                listPosition = position - 1;
                playerHolder.setPlayer(mCheckedInList.get(listPosition), league);
            } else if (getItemViewType(position) == TYPE_WAITING_LIST_PLAYER) {
                listPosition = position - (2 + mCheckedInList.size());
                playerHolder.setPlayer(mWaitingList.get(listPosition), league);
            } else if (getItemViewType(position) == TYPE_CHECKED_OUT_PLAYER) {
                listPosition = position - (3 + mCheckedInList.size() + mWaitingList.size());
                playerHolder.setPlayer(mCheckedOutList.get(listPosition), league);
            }

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_CHECKED_IN_TITLE;
        }
        int nCheckedIns = mCheckedInList.size();
        int nWaiting = mWaitingList.size();
        int nCheckedOuts = mCheckedOutList.size();
        if (nCheckedIns > 0 && position < 1 + nCheckedIns) {
            return TYPE_CHECKED_IN_PLAYER;
        }
        if (position == 1 + nCheckedIns) {
            return TYPE_WAITING_LIST_TITLE;
        }
        if (mWaitingList.size() > 0 && position < 2 + nCheckedIns + nWaiting) {
            return TYPE_WAITING_LIST_PLAYER;
        }
        if (position == 2 + nCheckedIns + nWaiting) {
            return TYPE_CHECKED_OUT_TITLE;
        }

        return TYPE_CHECKED_OUT_PLAYER;
    }

    private int sectionCount() {
        return 3;
    }

    private class TextViewHolder extends RecyclerView.ViewHolder {

        public TextViewHolder(View itemView) {
            super(itemView);
        }
    }
}
