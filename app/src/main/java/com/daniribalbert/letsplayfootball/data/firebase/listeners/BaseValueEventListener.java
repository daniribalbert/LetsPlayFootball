package com.daniribalbert.letsplayfootball.data.firebase.listeners;

import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Base class with a generic implementation of Firebase's ValueEventListener.
 */
public abstract class BaseValueEventListener implements ValueEventListener {
    @Override
    public void onCancelled(DatabaseError databaseError) {
        LogUtils.e(databaseError.toString());
    }
}
