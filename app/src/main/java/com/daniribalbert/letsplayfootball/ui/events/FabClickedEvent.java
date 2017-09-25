package com.daniribalbert.letsplayfootball.ui.events;

import android.support.design.widget.FloatingActionButton;

/**
 * Event sent by EventBus when HomeActivity Floating Action Button is clicked.
 */
public class FabClickedEvent {

    public FloatingActionButton fab;

    public FabClickedEvent(FloatingActionButton fab){
        this.fab = fab;
    }
}
