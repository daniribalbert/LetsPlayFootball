package com.daniribalbert.letsplayfootball.data.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.daniribalbert.letsplayfootball.application.App;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.utils.GsonUtils;

import java.util.HashMap;

/**
 * Shared preferences file used to cache user Leagues info.
 */
public class LeagueCache {

    private static final String LEAGUE_CACHE_FILE = "LEAGUE_CACHE_FILE";
    private static final String LEAGUE_PREFS = "LEAGUE_PREFS_";

    private static HashMap<String, League> sMemoryCache = new HashMap<>();

    private static SharedPreferences getPrefs() {
        final Context context = App.getContext();
        return context.getSharedPreferences(LEAGUE_CACHE_FILE, Context.MODE_PRIVATE);
    }

    public static League getLeagueInfo(String leagueId) {
        League league = sMemoryCache.get(leagueId);
        if (league != null){
            return league;
        }
        final SharedPreferences prefs = getPrefs();
        String leagueStr = prefs.getString(LEAGUE_PREFS + leagueId, "");
        if (TextUtils.isEmpty(leagueStr)) {
            return null;
        }

        league = GsonUtils.fromJson(leagueStr, League.class);
        sMemoryCache.put(leagueId, league);
        return league;
    }

    public static void saveLeagueInfo(League league) {
        final SharedPreferences prefs = getPrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LEAGUE_PREFS + league.id, GsonUtils.toJson(league));
        editor.apply();
    }
}
