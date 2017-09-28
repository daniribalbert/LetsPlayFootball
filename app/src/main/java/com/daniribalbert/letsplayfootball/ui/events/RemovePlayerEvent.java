package com.daniribalbert.letsplayfootball.ui.events;

import com.daniribalbert.letsplayfootball.data.model.Player;

/**
 * EventBus event sent when the user requests to remove a player from the league.
 */
public class RemovePlayerEvent {

    public Player player;

    public RemovePlayerEvent(Player player) {
        this.player = player;
    }
}
