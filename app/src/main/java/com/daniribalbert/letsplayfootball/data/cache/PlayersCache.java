package com.daniribalbert.letsplayfootball.data.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.daniribalbert.letsplayfootball.application.App;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.utils.GsonUtils;

import java.util.Collection;
import java.util.HashMap;

/**
 * Shared preferences file used to cache Players info.
 */
public class PlayersCache {

    private static final String PLAYERS_CACHE_FILE = "PLAYERS_CACHE_FILE";
    private static final String CURRENT_USER_CACHE_FILE = "CURRENT_USER_CACHE_FILE";
    private static final String PLAYER_PREF = "PLAYER_PREF_";

    private static HashMap<String, Player> sMemoryCache = new HashMap<>();

    private static SharedPreferences getPrefs() {
        final Context context = App.getContext();
        return context.getSharedPreferences(PLAYERS_CACHE_FILE, Context.MODE_PRIVATE);
    }

    public static Player getPlayerInfo(String playerId) {
        if (sMemoryCache.containsKey(playerId)){
            return sMemoryCache.get(playerId);
        }
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
        if (sMemoryCache.containsKey(player.id)){
            sMemoryCache.put(player.id, player);
        }
    }

    public static void saveCurrentPlayerInfo(Player player) {
        savePlayerInfo(player);
        final SharedPreferences prefs = getPrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CURRENT_USER_CACHE_FILE, GsonUtils.toJson(player));
        editor.apply();
    }

    public static Player getCurrentPlayerInfo(){
        final SharedPreferences prefs = getPrefs();
        String playerJson = prefs.getString(CURRENT_USER_CACHE_FILE, "");
        if (TextUtils.isEmpty(playerJson)) {
            return null;
        }
        return GsonUtils.fromJson(playerJson, Player.class);
    }

    public static void saveLeaguePlayersInfo(Collection<Player> playerList){
        sMemoryCache.clear();
        for (Player player : playerList){
            sMemoryCache.put(player.id, player);
        }
    }

    public static HashMap<String, Player> getCurrentLeaguePlayers(){
        return sMemoryCache;
    }

    public static void clear() {
        getPrefs().edit().clear().apply();
    }
}
