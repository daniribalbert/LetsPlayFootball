package com.daniribalbert.letsplayfootball.model;

import java.util.Date;
import java.util.List;

/**
 * League model class.
 */
public class League {

    /**
     * League ID.
     */
    private String id;

    /**
     * League title. Example: Premier League.
     */
    private String title;

    /**
     * League description.
     */
    private String description;

    /**
     * List of matches in this league.
     */
    private List<Match> matches;

    /**
     * List of player who participate in this league.
     */
    private List<Player> players;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    @Override
    public String toString() {
        return title + "\n" + description;
    }
}
