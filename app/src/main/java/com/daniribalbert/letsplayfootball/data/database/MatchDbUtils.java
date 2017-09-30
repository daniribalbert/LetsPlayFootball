package com.daniribalbert.letsplayfootball.data.database;

import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Set;

/**
 * Utility class for Player database operations.
 */
public class MatchDbUtils {

    private static final String PATH = "matches";

    public static DatabaseReference getRef(){
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        return dbRef.child(DbUtils.getRoot()).child(PATH);
    }

    /**
     * Creates a new League.
     * @param match new match to be added to the database.
     */
    public static String createMatch(final Match match) {
        final DatabaseReference dbRef = getRef();
        DatabaseReference pushRef = dbRef.push();

        String matchId = pushRef.getKey();
        match.id = matchId;
        pushRef.setValue(match);
        return matchId;
    }

    public static void getMatch(String matchId, ValueEventListener valueEventListener) {
        DatabaseReference dbRef = getRef();
        dbRef.child(matchId).addListenerForSingleValueEvent(valueEventListener);
    }

    public static void updateMatch(Match match) {
        DatabaseReference dbRef = getRef();
        dbRef.child(match.id).setValue(match);
    }

    public static void getUpcomingMatches(String leagueId, String playerId) {

    }
}
