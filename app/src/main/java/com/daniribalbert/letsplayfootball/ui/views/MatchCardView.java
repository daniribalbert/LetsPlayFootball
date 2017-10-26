package com.daniribalbert.letsplayfootball.ui.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * League card view;
 */
public class MatchCardView extends LinearLayout {
    @BindView(R.id.match_card_view)
    View mMatchCardView;

    @BindView(R.id.match_card_image)
    ImageView mMatchImageView;

    @BindView(R.id.match_card_time)
    TextView mMatchCardTime;

    @BindView(R.id.match_card_day)
    TextView mMatchCardDay;

    public MatchCardView(Context context) {
        super(context);
        init();
    }

    public MatchCardView(Context context,
                         @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MatchCardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.card_match, this);
        ButterKnife.bind(this);
    }

    public void setCard(Match match){
        GlideUtils.loadCircularImage(match.image, mMatchImageView);
        mMatchCardTime.setText(match.getTimeStr(match.time));
        mMatchCardDay.setText(match.getDateString(match.time));
    }
}
