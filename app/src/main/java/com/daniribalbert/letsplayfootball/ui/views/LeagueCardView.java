package com.daniribalbert.letsplayfootball.ui.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * League card view;
 */
public class LeagueCardView extends LinearLayout {
    @BindView(R.id.league_card_view)
    CardView mCardView;
    @BindView(R.id.league_card_title)
    TextView mTitle;
    @BindView(R.id.league_card_image)
    ImageView mImage;

    public LeagueCardView(Context context) {
        super(context);
        init();
    }

    public LeagueCardView(Context context,
                          @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LeagueCardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.card_league, this);
        ButterKnife.bind(this);
    }

    public void setCard(League league){
        GlideUtils.loadCircularImage(league.image, mImage);
        mTitle.setText(league.title);
    }
}
