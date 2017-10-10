package com.daniribalbert.letsplayfootball.ui.events;

/**
 * EventBus event sent when the user requests to open a player profile.
 */
public class OpenMatchEvent {

    public String matchId;
    public String leagueId;

    public OpenMatchEvent(String matchId) {
        this.matchId = matchId;
    }

    public OpenMatchEvent(String matchId, String leagueId) {
        this.matchId = matchId;
        this.leagueId = leagueId;
    }
}
