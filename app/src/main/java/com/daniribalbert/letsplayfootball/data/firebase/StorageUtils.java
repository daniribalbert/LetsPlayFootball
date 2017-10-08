package com.daniribalbert.letsplayfootball.data.firebase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Firebase Storage utility class.
 */
public class StorageUtils {

    private static final String PATH = "images";

    public static StorageReference getRef(){
        final StorageReference ref = FirebaseStorage.getInstance().getReference();
        return ref.child(DbUtils.getRoot()).child(PATH);
    }
}
