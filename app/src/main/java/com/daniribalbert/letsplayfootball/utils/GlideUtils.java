package com.daniribalbert.letsplayfootball.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

/**
 * Glide Utility class.
 */
public class GlideUtils {

    public static void loadCircularImage(Object image, ImageView view) {
        final Context context = view.getContext();
        Glide.with(context).load(image).apply(RequestOptions.circleCropTransform()).into(view);
    }
}
