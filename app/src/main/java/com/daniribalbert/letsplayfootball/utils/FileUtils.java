package com.daniribalbert.letsplayfootball.utils;

import android.content.Context;
import android.net.Uri;

import com.daniribalbert.letsplayfootball.data.firebase.StorageUtils;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseUploadListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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

    public static void uploadImage(Uri imageUri, final BaseUploadListener listener) {
        StorageReference ref = StorageUtils.getRef();
        StorageReference fileRef = ref.child(imageUri.getLastPathSegment());
        UploadTask uploadTask = fileRef.putFile(imageUri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(listener).addOnSuccessListener(listener);
    }
}
