package com.daniribalbert.letsplayfootball.ui.events;

/**
 * EventBus event sent when the user requests to open a player profile.
 */
public class OpenPlayerEvent {

    public String playerId;
    public String playerName;

    public OpenPlayerEvent(String playerId) {
        this.playerId = playerId;
    }
    public OpenPlayerEvent(String playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }
}
