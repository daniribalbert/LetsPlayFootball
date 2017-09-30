package com.daniribalbert.letsplayfootball.ui.events;

import com.daniribalbert.letsplayfootball.data.model.Match;

/**
 * EventBus event sent when the user requests to remove a player from the league.
 */
public class RemoveMatchEvent {

    public Match match;

    public RemoveMatchEvent(Match match) {
        this.match = match;
    }
}
