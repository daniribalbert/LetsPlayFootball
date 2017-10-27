package com.daniribalbert.letsplayfootball.data.firebase;

import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.SimpleLeague;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility class for League database operations.
 */
public class LeagueDbUtils {

    private static final String PATH = "leagues";

    public static DatabaseReference getRef() {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        return dbRef.child(PATH);
    }

    /**
     * Creates a new League.
     *
     * @param league  new league to be added to the database.
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
    }

    public static void updateLeagueOwners(League league) {
        DatabaseReference dbRef = getRef();
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("ownersId", league.ownersId);
        dbRef.child(league.id).updateChildren(updateMap);
    }

    /**
     * Searches the league database for Leagues with {name}
     *
     * @param name     League name query.
     * @param listener event listener
     */
    public static void searchLeague(final String name, final LeagueSearchListener listener) {
        final DatabaseReference dbRef = getRef();
        dbRef.orderByChild("title").startAt(name).limitToFirst(20)
             .addListenerForSingleValueEvent(new BaseValueEventListener() {
                 @Override
                 public void onDataChange(DataSnapshot dataSnapshot) {
                     Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                     List<SimpleLeague> searchResults = new ArrayList<>();
                     while (iterator.hasNext()) {
                         DataSnapshot next = iterator.next();
                         League league = next.getValue(League.class);
                         if (league != null) {
                             if (league.title.contains(name)) {
                                 searchResults.add(new SimpleLeague(league));
                             }
                         }
                     }
                     listener.onLeagueSearchResult(searchResults);
                 }
             });
    }

    public static void saveLeagueRules(String leagueId, String rulesText) {
        DatabaseReference dbRef = getRef();
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("rules", rulesText);
        dbRef.child(leagueId).updateChildren(updateMap);
    }

    public interface LeagueSearchListener {
        void onLeagueSearchResult(List<SimpleLeague> leagues);
    }
}
