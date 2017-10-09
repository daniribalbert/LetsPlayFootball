package com.daniribalbert.letsplayfootball.ui.adapters.viewholders;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.ui.events.OpenMatchEvent;
import com.daniribalbert.letsplayfootball.ui.events.RemoveMatchEvent;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Match View Holder
 */
public class MatchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @BindView(R.id.match_card_view)
    CardView mCardView;
    @BindView(R.id.match_card_day)
    TextView mDay;
    @BindView(R.id.match_card_time)
    TextView mTime;
    @BindView(R.id.match_card_image)
    ImageView mImage;

    private String mMatchId;


    public MatchViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        view.setOnClickListener(this);
    }

    public void setMatch(Match match) {
        setTime(match.getTimeStr(match.time));
        setDay(match.getDateString(match.time));
        setImage(match.image);
        mMatchId = match.id;
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
        EventBus.getDefault().post(new OpenMatchEvent(mMatchId));
    }
}
