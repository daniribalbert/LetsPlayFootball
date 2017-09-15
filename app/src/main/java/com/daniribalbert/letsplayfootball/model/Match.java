package com.daniribalbert.letsplayfootball.model;

import java.util.Date;
import java.util.List;

/**
 * Football match model class.
 */
public class Match {

    /**
     * Match ID.
     */
    private String id;

    /**
     * Time when this match is gonna happen.
     */
    private Date time;

    /**
     * List of players participating in this match.
     */
    private List<Player> players;
}
