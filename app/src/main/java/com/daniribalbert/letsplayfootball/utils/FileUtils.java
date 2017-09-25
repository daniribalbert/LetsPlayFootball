package com.daniribalbert.letsplayfootball.utils;

import android.content.Context;

import java.io.File;

/**
 * File Utility class.
 */
public class FileUtils {

    private static final String TEMP_IMAGE_NAME = "tempImage";

    public static File getTempFile(Context context) {
        File imageFile = new File(context.getExternalCacheDir(), TEMP_IMAGE_NAME);
        imageFile.getParentFile().mkdirs();
        return imageFile;
    }
}
