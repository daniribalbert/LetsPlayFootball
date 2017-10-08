package com.daniribalbert.letsplayfootball.data.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Pair;

import com.daniribalbert.letsplayfootball.application.App;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.utils.GsonUtils;

/**
 * Shared preferences file used to cache Players info.
 */
public class PlayersCache {

    private static final String PLAYERS_CACHE_FILE = "PLAYERS_CACHE_FILE";
    private static final String PLAYER_PREF = "PLAYER_PREF_";


    private static SharedPreferences getPrefs() {
        final Context context = App.getContext();
        return context.getSharedPreferences(PLAYERS_CACHE_FILE, Context.MODE_PRIVATE);
    }

    public static Player getPlayerInfo(String playerId) {
        final SharedPreferences prefs = getPrefs();
        String playerStr = prefs.getString(PLAYER_PREF + playerId, "");
        if (TextUtils.isEmpty(playerStr)) {
            return null;
        }
        return GsonUtils.fromJson(playerStr, Player.class);
    }

    public static void savePlayerInfo(Player player) {
        final SharedPreferences prefs = getPrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PLAYER_PREF + player.id, GsonUtils.toJson(player));
        editor.apply();
    }
}
