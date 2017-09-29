package com.daniribalbert.letsplayfootball.data.database.listener;

import java.util.Set;

/**
 * Interface created to notify a Firebase Search operation has finished.
 */
public interface SearchListener<T> {
    void onDataReceived(Set<T> results);
}