package com.daniribalbert.letsplayfootball.data.model;

import java.util.Date;
import java.util.List;

/**
 * Football match model class.
 */
public class Match {

    /**
     * Match ID.
     */
    public String id;

    /**
     * Time when this match is gonna happen.
     */
    public Date time;

    /**
     * List of players participating in this match.
     */
    public List<Player> players;


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Match) {
            Match match = (Match) obj;
            return match.id.equalsIgnoreCase(this.id);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return (int) time.getTime(); // Java... -_-
    }
}
