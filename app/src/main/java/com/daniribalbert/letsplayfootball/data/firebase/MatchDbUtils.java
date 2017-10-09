package com.daniribalbert.letsplayfootball.data.firebase;

import com.daniribalbert.letsplayfootball.data.model.Match;
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
        return dbRef.child(DbUtils.getRoot()).child(PATH);
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

    public static void getMatch(String leagueId, String matchId, ValueEventListener listener) {
        DatabaseReference dbRef = getRef();
        dbRef.child(leagueId).child(matchId).addListenerForSingleValueEvent(listener);
    }

    public static void removeMatch(Match match) {
        DatabaseReference ref = getRef();
        ref.child(match.leagueId).child(match.id).removeValue();
    }

    public static void markCheckIn(Match match, String playerId) {
        DatabaseReference ref = getRef();
        match.players.put(playerId, true);
        Map<String, Object> playersMap = new HashMap<>();

        playersMap.put("players", match.players);
        ref.child(match.leagueId).child(match.id).updateChildren(playersMap);
    }

    public static void markCheckOut(Match match, String playerId) {
        DatabaseReference ref = getRef();
        match.players.put(playerId, false);
        Map<String, Object> playersMap = new HashMap<>();

        playersMap.put("players", match.players);
        ref.child(match.leagueId).child(match.id).updateChildren(playersMap);
    }
}
