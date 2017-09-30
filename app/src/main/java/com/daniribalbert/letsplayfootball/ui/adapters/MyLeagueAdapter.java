package com.daniribalbert.letsplayfootball.ui.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.model.SimpleLeague;
import com.daniribalbert.letsplayfootball.ui.events.OpenLeagueEvent;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.daniribalbert.letsplayfootball.data.model.League}
 */
public class MyLeagueAdapter extends RecyclerView.Adapter<MyLeagueAdapter.ViewHolder> {

    private final List<SimpleLeague> mValues = new ArrayList<>();

    public MyLeagueAdapter() {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.card_league, parent, false);

        final ViewHolder viewHolder = new ViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int adapterPosition = viewHolder.getAdapterPosition();
                EventBus.getDefault().post(new OpenLeagueEvent(mValues.get(adapterPosition)));
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        SimpleLeague league = mValues.get(position);
        holder.setLeague(league);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /**
     * Check if league exists and adds it to the list or update the previous entry.
     *
     * @param league League to be updated on the list.
     */
    public void addItem(SimpleLeague league) {
        int itemCount = getItemCount();
        mValues.add(league);
        notifyItemInserted(itemCount);
    }

    /**
     * Check if league exists and adds it to the list or update the previous entry.
     *
     * @param league League to be updated on the list.
     */
    public void updateItem(SimpleLeague league) {
        int position = mValues.indexOf(league);
        if (position >= 0 && position < mValues.size()) {
            mValues.set(position, league);
            notifyItemChanged(position);
        }
    }

    public void addItems(List<SimpleLeague> leagues) {
        int itemCount = getItemCount();
        mValues.addAll(leagues);
        notifyItemRangeInserted(itemCount, leagues.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.league_card_view)
        CardView mCardView;
        @BindView(R.id.league_card_title)
        TextView mTitle;
        @BindView(R.id.league_card_image)
        ImageView mImage;
        public SimpleLeague mLeague;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void setLeague(SimpleLeague league) {
            setTitle(league.toString());
            setImage(league.image);
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
    }
}
