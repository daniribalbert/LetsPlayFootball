package com.daniribalbert.letsplayfootball.data.database;

import com.daniribalbert.letsplayfootball.data.model.League;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Set;

/**
 * Utility class for League database operations.
 */
public class LeagueDbUtils {

    private static final String PATH = "leagues";

    public static DatabaseReference getRef(){
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        return dbRef.child(DbUtils.getRoot()).child(PATH);
    }

    /**
     * Creates a new League.
     * @param league new league to be added to the database.
     * @param ownerId Owner Id - Typically this will be the current user ID.
     */
    public static String createLeague(final League league, String ownerId) {
        final DatabaseReference dbRef = getRef();
        DatabaseReference pushRef = dbRef.push();
        league.ownersId.put(ownerId, true);

        String leagueId = pushRef.getKey();
        league.id = leagueId;
        pushRef.setValue(league);
        PlayerDbUtils.addLeague(ownerId, league);
        return leagueId;
    }

    public static void getLeague(String leagueId, ValueEventListener valueEventListener) {
        DatabaseReference dbRef = getRef();
        dbRef.child(leagueId).addListenerForSingleValueEvent(valueEventListener);
    }

    public static void updateLeague(League league) {
        DatabaseReference dbRef = getRef();
        dbRef.child(league.id).setValue(league);
        Set<String> userIds = league.ownersId.keySet();
        for (String userId : userIds){
            PlayerDbUtils.updatePlayerLeague(userId, league);
        }
    }
}
