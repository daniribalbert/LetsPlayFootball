package com.daniribalbert.letsplayfootball.ui.events;

import android.support.annotation.NonNull;

/**
 * EventBus event sent when the user requests to open a player profile.
 */
public class OpenMatchEvent {

    public String matchId;
    public String leagueId;

    public OpenMatchEvent(@NonNull String matchId) {
        this.matchId = matchId;
    }

    public OpenMatchEvent(@NonNull String matchId, @NonNull String leagueId) {
        this.matchId = matchId;
        this.leagueId = leagueId;
    }
}
