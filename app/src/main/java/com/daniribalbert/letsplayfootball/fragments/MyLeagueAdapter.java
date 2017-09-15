package com.daniribalbert.letsplayfootball.fragments;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.fragments.MyLeaguesFragment.OnListFragmentInteractionListener;
import com.daniribalbert.letsplayfootball.model.League;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.daniribalbert.letsplayfootball.model.League}
 * and makes a call to the specified {@link OnListFragmentInteractionListener}.
 */
public class MyLeagueAdapter extends RecyclerView.Adapter<MyLeagueAdapter.ViewHolder> {

    private final List<League> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyLeagueAdapter(List<League> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.league_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        League league = mValues.get(position);
        holder.setLeague(league);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mLeague);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.league_card_view)  CardView mCardView;
        @BindView(R.id.league_card_title) TextView mTitle;
        public League mLeague;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void setLeague(League league){
            setTitle(league.toString());
        }

        public void setTitle(String title){
            mTitle.setText(title);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitle.getText() + "'";
        }
    }
}
