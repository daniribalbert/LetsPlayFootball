package com.daniribalbert.letsplayfootball.data.model;

import android.text.TextUtils;
import android.text.format.DateFormat;

import com.daniribalbert.letsplayfootball.application.App;
import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Football match model class.
 */
public class Match {

    /**
     * Match ID.
     */
    public String id;

    /**
     * Match image (Maybe someone wants to add the winner team pic?)
     */
    public String image;

    /**
     * Time when this match is gonna happen.
     */
    public long time;

    public Match() {
        //Firebase constructor.
        Date now = new Date();
        now.setTime(now.getTime() + TimeUnit.DAYS.toMillis(1));
        time = now.getTime();
    }

    /**
     * List of players participating in this match.
     */
    public HashMap<String, Boolean> players;

    @Exclude
    public String getDate() {
        java.text.DateFormat format = DateFormat.getDateFormat(App.getContext());
        return format.format(new Date(time));
    }

    @Exclude
    public String getTime() {
        java.text.DateFormat format = DateFormat.getTimeFormat(App.getContext());
        return format.format(new Date(time));
    }

    public String toString() {
        return getDate() + "\n" + getTime();
    }

    public boolean hasImage() {
        return !TextUtils.isEmpty(image);
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
}
