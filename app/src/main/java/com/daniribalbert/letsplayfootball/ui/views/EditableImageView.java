package com.daniribalbert.letsplayfootball.ui.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Extension of default ImageView with custom settings the view is clickable.
 */
public class EditableImageView extends android.support.v7.widget.AppCompatImageView {

    public EditableImageView(Context context) {
        super(context);
    }

    public EditableImageView(Context context,
                             @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EditableImageView(Context context,
                             @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setClickable(boolean clickable) {
        super.setClickable(clickable);
        setAlpha(clickable ? 0.5f : 1f);
    }
}
