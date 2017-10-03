package com.daniribalbert.letsplayfootball.ui.events;

import com.daniribalbert.letsplayfootball.data.model.Player;

/**
 * EventBus event sent when the user requests to open a player profile.
 */
public class OpenPlayerEvent {

    public Player player;

    public OpenPlayerEvent(Player player) {
        this.player = player;
    }
}
