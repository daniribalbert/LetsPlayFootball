package com.daniribalbert.letsplayfootball.data.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Team model class with some functions to handle team creation, sorting, sharing.
 */
public class Team implements Comparable {

    private String title;
    private String leagueId;
    private List<Player> players;

    public Team(String leagueId, List<Player> players) {
        this.players = new ArrayList<>(players);
        this.leagueId = leagueId;
    }

    public Team(String title, String leagueId, List<Player> players) {
        this(leagueId, players);
        this.title = title;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public String getLeagueId() {
        return leagueId;
    }

    public void clearPlayers() {
        players.clear();
    }

    public float getRating() {
        float rating = 0f;
        for (Player player : players) {
            rating += player.getRating(leagueId);
        }
        return rating;
    }

    @Override
    public String toString() {
        String toString = title;
        for (Player player : players) {
            toString +=  "\n" + player.toString() + " - (" + player.getRating(leagueId) + ")";
        }
        return toString;
    }

    @Override
    public int compareTo(@NonNull Object obj) {
        if (!(obj instanceof Team)) {
            return Integer.MIN_VALUE;
        }
        Team team = (Team) obj;
        return Float.compare(getRating(), team.getRating());
    }
}
