package com.daniribalbert.letsplayfootball.ui.events;

import com.daniribalbert.letsplayfootball.data.model.Player;

/**
 * EventBus event sent when the user requests to open a player profile.
 */
public class PlayerClickedEvent {

    public Player player;

    public PlayerClickedEvent(Player player) {
        this.player = player;
    }
}
