package com.daniribalbert.letsplayfootball.data.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.daniribalbert.letsplayfootball.application.App;
import com.daniribalbert.letsplayfootball.data.cache.LeagueCache;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Football match model class.
 */
public class Match implements Comparable {

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

    /**
     * List of players participating in this match.
     */
    public HashMap<String, Boolean> players = new HashMap<>();

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
}
