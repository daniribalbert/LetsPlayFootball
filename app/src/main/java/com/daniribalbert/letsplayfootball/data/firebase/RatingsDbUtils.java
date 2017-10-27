package com.daniribalbert.letsplayfootball.data.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

/**
 * Utility class for database operations regarding Player Ratings.
 * Ratings are updated as follows.
 * <p>
 * Players have their map rating as: "leagueId" > rating.
 * This rating is updated every time a new rate is added to the "ratings" table.
 * <p>
 * The "ratings" table is organized as:
 * <p>
 * {
 * "ratings" {
 * "leagueId"{
 * "player1"{
 * "rateByX": 3.0,
 * "rateByY": 4.0,
 * "rateByZ": 3.5
 * },
 * "player2"{
 * "rateByX": 3.0,
 * "rateByY": 4.0,
 * "rateByZ": 3.5
 * }
 * }
 * }
 * }
 */
public class RatingsDbUtils {

    private static final String PATH = "ratings";

    private static DatabaseReference getRef() {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        return dbRef.child(PATH);
    }

    /**
     * Adds a new rating to the player. This must be done as a Transaction because it can be updated
     * by multiple users at the same time and it must update both the ratings table AND the player.
     *
     * @param playerId  Id of the player.
     * @param leagueId  Id of the league where this player is being rated.
     * @param ratedById Id of the user who rated the player.
     * @param rating    player rating on the League.
     */
    public static void savePlayerRating(final String playerId, final String leagueId,
                                        final String ratedById, final float rating,
                                        final OnPlayerRateUpdateListener listener) {
        final DatabaseReference dbRef = getRef();
        dbRef.child(leagueId).child(playerId).runTransaction(
                new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        mutableData.child(ratedById).setValue(rating);
                        Iterable<MutableData> children = mutableData.getChildren();
                        Iterator<MutableData> iterator = children.iterator();
                        float sumRatings = 0f;
                        int nRatings = 0;
                        while (iterator.hasNext()) {
                            MutableData next = iterator.next();
                            Float value = next.getValue(Float.class);
                            if (value != null) {
                                sumRatings += value;
                                nRatings++;
                            }
                        }
                        float playerRating = nRatings == 0 ? 0 : sumRatings / nRatings;
                        PlayerDbUtils.updatePlayerRating(playerId, leagueId, playerRating);
                        listener.onRateUpdated(playerRating);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b,
                                           DataSnapshot dataSnapshot) {

                    }
                });
    }

    public static void getPlayerRateBy(final String playerId, final String leagueId,
                                       final String ratedBy, ValueEventListener listener) {
        final DatabaseReference dbRef = getRef();
        dbRef.child(leagueId).child(playerId).child(ratedBy)
             .addListenerForSingleValueEvent(listener);
    }

    public static void removePlayerFromLeague(final String playerId, final String leagueId) {
        final DatabaseReference dbRef = getRef();
        dbRef.child(leagueId).child(playerId).removeValue();
    }

    public interface OnPlayerRateUpdateListener{
        void onRateUpdated(float rating);
    }
}
