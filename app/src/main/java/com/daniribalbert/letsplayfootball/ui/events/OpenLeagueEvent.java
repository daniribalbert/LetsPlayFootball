package com.daniribalbert.letsplayfootball.ui.events;

import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.SimpleLeague;

/**
 * EventBus event sent when the user requests to open a league.
 */
public class OpenLeagueEvent {

    SimpleLeague league;

    public OpenLeagueEvent(SimpleLeague league){
        this.league = league;
    }

    public OpenLeagueEvent(League league){
        this.league = new SimpleLeague(league);
    }

    public SimpleLeague getLeague() {
        return league;
    }
}
