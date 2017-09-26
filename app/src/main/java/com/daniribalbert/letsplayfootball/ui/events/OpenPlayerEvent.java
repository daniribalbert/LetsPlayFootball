package com.daniribalbert.letsplayfootball.ui.events;

import com.daniribalbert.letsplayfootball.data.model.Player;

/**
 * EventBus event sent when the user requests to open a player profile.
 */
public class OpenPlayerEvent {

    public String playerId;

    public OpenPlayerEvent(String playerId) {
        this.playerId = playerId;
    }
}
