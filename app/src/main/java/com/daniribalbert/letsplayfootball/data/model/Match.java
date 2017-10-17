package com.daniribalbert.letsplayfootball.data.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.application.App;
import com.daniribalbert.letsplayfootball.data.cache.LeagueCache;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Football match model class.
 */
public class Match implements Comparable {

    public static final int NUMBER_OF_PLAYERS_UNDEFINED = -1;
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 50;

    /**
     * Match ID.
     */
    public String id;

    /**
     * Id of the league where this match was played.
     */
    public String leagueId;

    /**
     * Match image (Maybe someone wants to add the winner team pic?)
     */
    public String image;

    /**
     * Match description, where the user can add comments about that particular match.
     */
    public String description = "";

    /**
     * Time when this match is gonna happen.
     */
    public long time;

    public long checkInStart;
    public long checkInEnds;

    public int maxPlayers;

    /**
     * List of players participating in this match.
     */
    public HashMap<String, Long> players = new HashMap<>();

    /**
     * Teams playing this match.
     * This is a list a map of the team name to the players Ids.
     */
    public HashMap<String, List<String>> teams = new HashMap<>();

    public Match() {
        //Firebase constructor.
    }

    public Match(String leagueId) {
        this.leagueId = leagueId;
        init();
    }

    private void init() {
        Date now = new Date();
        this.checkInStart = now.getTime();
        now.setTime(now.getTime() + TimeUnit.DAYS.toMillis(1));
        this.time = now.getTime();
        now.setTime(now.getTime() - TimeUnit.HOURS.toMillis(4));
        this.checkInEnds = now.getTime();
    }

    @Exclude
    public String getDateString(long time) {
        java.text.DateFormat format = DateFormat.getDateFormat(App.getContext());
        return format.format(new Date(time));
    }

    @Exclude
    public String getTimeStr(long time) {
        java.text.DateFormat format = DateFormat.getTimeFormat(App.getContext());
        return format.format(new Date(time));
    }

    public String toString() {
        return getDateString(time) + "\n" + getTimeStr(time);
    }

    public boolean hasImage() {
        return !TextUtils.isEmpty(getImage());
    }

    public String getImage() {
        if (TextUtils.isEmpty(image)) {
            League league = LeagueCache.getLeagueInfo(leagueId);
            if (league != null) {
                return league.image;
            }
        }
        return image;
    }

    @Exclude
    public boolean isCheckInOpen() {
        long now = System.currentTimeMillis();
        return now >= checkInStart && now < checkInEnds;
    }

    @Exclude
    public boolean isPastMatch() {
        long now = System.currentTimeMillis();
        return now > time;
    }

    @Exclude
    public boolean isCheckedIn(String playerId) {
        return players.containsKey(playerId) && players.get(playerId) > 0;
    }


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
        return (int) time; // Java... -_-
    }

    @Override
    public int compareTo(@NonNull Object obj) {
        if (obj instanceof Match) {
            return time > ((Match) obj).time ? 1 : -1;
        }
        return Integer.MIN_VALUE;
    }

    @Exclude
    public String getMaxPlayersText() {
        return (maxPlayers > 0)
               ? String.valueOf(maxPlayers)
               : App.getContext().getString(R.string.not_available_small);
    }


    public LinkedList<Player> sortPlayersByCheckIn(Collection<Player> values) {
        LinkedList<Player> sortedList = new LinkedList<>(); // Linked list for performance!

        for (Player player : values) {
            if (sortedList.size() == 0) {
                sortedList.add(player);
            } else {
                boolean added = false;
                for (int i = 0; i < sortedList.size(); i++) {
                    Player currentPlayer = sortedList.get(i);
                    if (players.get(player.id) < players.get(currentPlayer.id)) {
                        sortedList.add(i, player);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    sortedList.add(player);
                }
            }
        }
        return sortedList;
    }
}
