package com.daniribalbert.letsplayfootball.ui.events;

import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.SimpleLeague;

/**
 * EventBus event sent when the user requests to edit a league.
 */
public class EditLeagueEvent {

    SimpleLeague league;

    public EditLeagueEvent(SimpleLeague league) {
        this.league = league;
    }

    public EditLeagueEvent(League league) {
        this.league = new SimpleLeague(league);
    }

    public SimpleLeague getLeague() {
        return league;
    }
}
