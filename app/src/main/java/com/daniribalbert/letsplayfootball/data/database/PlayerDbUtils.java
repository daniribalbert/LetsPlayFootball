package com.daniribalbert.letsplayfootball.data.database;

import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.data.model.SimpleLeague;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Utility class for Player database operations.
 */
public class PlayerDbUtils {

    private static final String PATH = "players";

    public static DatabaseReference getRef(){
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        return dbRef.child(DbUtils.getRoot()).child(PATH);
    }

    /**
     * Creates a new user. Before creation, this method verifies if the user exists so it won't
     * override the previous player info.
     * @param player new player to be added to the database.
     */
    public static void createUser(final Player player) {
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

    public static void getPlayer(String playerId, ValueEventListener valueEventListener) {
        DatabaseReference dbRef = getRef();
        dbRef.child(playerId).addListenerForSingleValueEvent(valueEventListener);
    }

    public static void updatePlayer(Player player) {
        DatabaseReference dbRef = getRef();
        dbRef.child(player.id).setValue(player);
    }

    public static void addLeague(String playerId, League league){
        DatabaseReference ref = getRef();
        ref.child(playerId).child("leagues").child(league.id).setValue(new SimpleLeague(league));
    }

    public static void updatePlayerLeague(String playerId, League league) {
        DatabaseReference ref = getRef();
        ref.child(playerId).child("leagues").child(league.id).setValue(new SimpleLeague(league));
    }
}