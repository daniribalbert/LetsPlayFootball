package com.daniribalbert.letsplayfootball.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.application.App;

/**
 * Glide Utility class.
 */
public class GlideUtils {

    public static void loadCircularImage(Object image, @NonNull ImageView view) {
        final Context context = App.getContext();
        Glide.with(context).load(image)
             .apply(RequestOptions.errorOf(R.mipmap.ic_launcher_round))
             .apply(RequestOptions.circleCropTransform()).into(view);
    }
}
