package com.daniribalbert.letsplayfootball.data.database;

import com.daniribalbert.letsplayfootball.data.model.Player;
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

    /**
     * Creates a new user. Before creation, this method verifies if the user exists so it won't
     * override the previous player info.
     * @param player new player to be added to the database.
     */
    public static void createUser(final Player player) {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(PATH).child(player.id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    LogUtils.i("Creating user!");
                    dbRef.child(PATH).child(player.id).setValue(player);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static void getPlayer(String playerId, ValueEventListener valueEventListener) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(PATH).child(playerId).addListenerForSingleValueEvent(valueEventListener);
    }

    public static void updatePlayer(Player player) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(PATH).child(player.id).setValue(player);
    }
}
