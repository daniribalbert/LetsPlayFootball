package com.daniribalbert.letsplayfootball.data.database;

import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.data.model.SimpleLeague;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for Player database operations.
 */
public class PlayerDbUtils {

    private static final String PATH = "players";

    public static DatabaseReference getRef() {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        return dbRef.child(DbUtils.getRoot()).child(PATH);
    }

    /**
     * Creates a new user. Before creation, this method verifies if the user exists so it won't
     * override the previous player info.
     *
     * @param firebaseUser new player to be added to the database with info gathered from the
     *                     Firebase login info.
     */
    public static void createPlayer(final FirebaseUser firebaseUser) {
        final Player player = Player.fromFirebase(firebaseUser);
        final DatabaseReference dbRef = getRef();
        dbRef.child(player.id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    LogUtils.i("Creating user!");
                    dbRef.child(player.id).setValue(player);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * Creates a new Player.
     *
     * @param player new player to be added to the database.
     */
    public static Player createGuestPlayer(final Player player) {
        final DatabaseReference dbRef = getRef();
        DatabaseReference push = dbRef.push();
        player.id = push.getKey();
        push.setValue(player);
        return player;
    }

    public static void getPlayer(String playerId, ValueEventListener valueEventListener) {
        DatabaseReference dbRef = getRef();
        dbRef.child(playerId).addListenerForSingleValueEvent(valueEventListener);
    }

    public static void getPlayersFromLeague(String leagueId,
                                            ValueEventListener valueEventListener) {
        DatabaseReference dbRef = getRef();
        Query query = dbRef.orderByChild("leagues/" + leagueId + "/league_id").equalTo(leagueId);

        query.addListenerForSingleValueEvent(valueEventListener);
    }

    public static void updatePlayer(Player player) {
        DatabaseReference dbRef = getRef();
        dbRef.child(player.id).setValue(player);
    }

    public static void addLeague(String playerId, League league) {
        DatabaseReference ref = getRef();
        ref.child(playerId).child("leagues").child(league.id).setValue(new SimpleLeague(league));
    }

    public static void updatePlayerLeague(String playerId, League league) {
        DatabaseReference ref = getRef();
        ref.child(playerId).child("leagues").child(league.id).setValue(new SimpleLeague(league));
    }

    public static void removePlayer(Player player, String leagueId) {
        boolean isGuest = player.leagues.size() < 3;
        if (isGuest) {
            removeGuestPlayer(player.id);
        } else{
            removePlayerFromLeague(player, leagueId);
        }
    }

    private static void removeGuestPlayer(String playerId) {
        DatabaseReference ref = getRef();
        ref.child(playerId).removeValue();
    }

    private static void removePlayerFromLeague(Player player, String leagueId) {
        player.leagues.remove(leagueId);
        player.rating.remove(leagueId);

        DatabaseReference ref = getRef();
        Map<String, Object> updateMap = new HashMap<String, Object>();
        updateMap.put("rating", player.rating);
        updateMap.put("leagues", player.leagues);
        ref.child(player.id).updateChildren(updateMap);
    }
}
