package com.daniribalbert.letsplayfootball.data.database.listeners;

/**
 * Interface to notify some Firebase operation has completed.
 */
public interface CompletionListener {
    void onComplete(boolean success);
}
