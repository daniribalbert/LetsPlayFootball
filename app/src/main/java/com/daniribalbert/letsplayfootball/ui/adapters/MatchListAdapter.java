package com.daniribalbert.letsplayfootball.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.ui.adapters.viewholders.HistoryMatchViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display items from the League
 */
public class MatchListAdapter extends RecyclerView.Adapter<HistoryMatchViewHolder> {

    private final List<Match> mMatches = new ArrayList<>();

    public MatchListAdapter() {
    }

    @Override
    public HistoryMatchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        view = LayoutInflater.from(parent.getContext())
                             .inflate(R.layout.card_match_ended, parent, false);
        return new HistoryMatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final HistoryMatchViewHolder holder, int position) {
        Match match = mMatches.get(position);
         holder.setMatch(match);
    }

    @Override
    public int getItemCount() {
        return mMatches.size();
    }

    /**
     * Add Match to the list
     *
     * @param match match to be added to the list.
     */
    public void addMatch(Match match) {
        int itemCount = mMatches.size();
        mMatches.add(match);
        notifyItemInserted(itemCount);
    }

    public void clear() {
        mMatches.clear();
        notifyDataSetChanged();
    }

    public void removeMatch(Match match) {
        int index = mMatches.indexOf(match);
        mMatches.remove(index);
        notifyItemRemoved(index);
    }
}
