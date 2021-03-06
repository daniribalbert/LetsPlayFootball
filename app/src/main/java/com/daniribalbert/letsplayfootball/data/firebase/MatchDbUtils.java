package com.daniribalbert.letsplayfootball.data.firebase;

import android.support.annotation.NonNull;

import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for Player database operations.
 * Matches follow the scheme:
 * { matches : {league_id} : {match_id} : match_data }.
 */
public class MatchDbUtils {

    private static final String PATH = "matches";

    public static DatabaseReference getRef() {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        return dbRef.child(PATH);
    }

    /**
     * Creates a new Match in the database.
     *
     * @param match new match to be added to the database.
     */
    public static String createMatch(final Match match) {
        final DatabaseReference dbRef = getRef();
        String matchId = String.valueOf(match.time);
        match.id = matchId;
        dbRef.child(match.leagueId).child(matchId).setValue(match);
        return matchId;
    }

    public static void updateMatch(Match match) {
        if (match.id.equalsIgnoreCase(String.valueOf(match.time))) {
            getRef().child(match.leagueId).child(match.id).setValue(match);
        } else {
            removeMatch(match);
            createMatch(match);
        }
    }

    public static void updatePostMatch(Match match) {
        DatabaseReference ref = getRef();
        DatabaseReference matchRef = ref.child(match.leagueId).child(match.id);

        Map<String, Object> updates = new HashMap<>();
        updates.put("image", match.getImage());
        updates.put("description", match.description);

        matchRef.updateChildren(updates);
    }

    public static void getPastMatches(String leagueId, ValueEventListener listener) {
        final String now = String.valueOf(System.currentTimeMillis());
        DatabaseReference dbRef = getRef();
        dbRef.child(leagueId).orderByKey().endAt(now)
             .addListenerForSingleValueEvent(listener);
    }

    public static void getUpcomingMatches(String leagueId, ValueEventListener listener) {
        final String now = String.valueOf(System.currentTimeMillis());
        DatabaseReference dbRef = getRef();
        dbRef.child(leagueId).orderByKey().startAt(now)
             .addListenerForSingleValueEvent(listener);
    }

    public static void getUpcomingMatch(String leagueId, ValueEventListener listener) {
        final String now = String.valueOf(System.currentTimeMillis());
        DatabaseReference dbRef = getRef();
        dbRef.child(leagueId).orderByKey().startAt(now).limitToFirst(1)
             .addListenerForSingleValueEvent(listener);
    }

    public static void getMatch(@NonNull String leagueId, @NonNull String matchId, ValueEventListener listener) {
        DatabaseReference dbRef = getRef();
        dbRef.child(leagueId).child(matchId).addListenerForSingleValueEvent(listener);
    }

    public static void removeMatch(Match match) {
        DatabaseReference ref = getRef();
        ref.child(match.leagueId).child(match.id).removeValue();
    }

    public static Match markCheckIn(Match match, String playerId) {
        DatabaseReference ref = getRef();
        match.players.put(playerId, System.currentTimeMillis());
        Map<String, Object> playersMap = new HashMap<>();

        playersMap.put("players", match.players);
        ref.child(match.leagueId).child(match.id).updateChildren(playersMap);
        LogUtils.d("CHECKIN: " + playerId);
        return match;
    }

    public static Match markCheckOut(Match match, String playerId) {
        DatabaseReference ref = getRef();
        match.players.put(playerId, -1L);
        Map<String, Object> playersMap = new HashMap<>();

        playersMap.put("players", match.players);
        ref.child(match.leagueId).child(match.id).updateChildren(playersMap);
        LogUtils.d("CHECKOUT: " + playerId);
        return match;
    }

    public static void saveTeam(Match match) {
        DatabaseReference ref = getRef();
        Map<String, Object> playersMap = new HashMap<>();

        playersMap.put("teams", match.teams);
        ref.child(match.leagueId).child(match.id).updateChildren(playersMap);
    }
}
