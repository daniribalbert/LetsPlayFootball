package com.daniribalbert.letsplayfootball.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * Activity results utility class.
 */
public class ActivityUtils {

    /**
     * Extracts the image Uri from intent resulted from selecting picture from camera/gallery.
     * @param data intent data
     * @param context Activity context.
     * @return file Uri.
     */
    public static Uri extractImageUri(Intent data, @NonNull final Context context) {
        File imageFile = FileUtils.getTempFile(context);
        final boolean isCamera;
        if (data == null) {
            isCamera = true;
        } else {
            final String action = data.getAction();
            if (action == null) {
                isCamera = false;
            } else {
                isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            }
        }

        if (isCamera) {
            return Uri.fromFile(imageFile);
        } else {
            return data == null ? null : data.getData();
        }
    }
}
