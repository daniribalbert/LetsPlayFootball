package com.daniribalbert.letsplayfootball.data.database;

import com.daniribalbert.letsplayfootball.data.database.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for database operations regarding Player Ratings.
 */
public class RatingsDbUtils {

    private static final String PATH = "ratings";

    private static DatabaseReference getRef() {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        return dbRef.child(DbUtils.getRoot()).child(PATH);
    }

    /**
     * Adds a new rating to the player.
     *
     * @param playerId  Id of the player.
     * @param leagueId  Id of the league where this player is being rated.
     * @param ratedById Id of the user who rated the player.
     * @param rating    player rating on the League.
     */
    public static void addPlayerRating(final String playerId, final String leagueId,
                                       final String ratedById, final float rating) {
        final DatabaseReference dbRef = getRef();
        dbRef.child(leagueId).child(playerId).child(ratedById).setValue(rating);
    }

    public static void getPlayerRatings(String playerId, String leagueId,
                                        final RatingListener listener) {
        DatabaseReference dbRef = getRef();
        dbRef.child(leagueId).child(playerId).addListenerForSingleValueEvent(
                new BaseValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        float rating = 0f;
                        LogUtils.i("Ratings: " + dataSnapshot.toString());
                        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                        List<Float> ratings = new ArrayList<Float>();
                        int counter = 0;
                        while (iterator.hasNext()) {
                            DataSnapshot next = iterator.next();
                            Float nextRating = next.getValue(Float.class);
                            if (nextRating != null) {
                                rating += nextRating;
                                counter++;
                            }
                        }

                        // Avoid division by 0.
                        if (counter == 0) {
                            rating = 0f;
                        } else {
                            rating = rating / counter;
                        }
                        listener.onRatingLoaded(rating);
                    }
                });
    }

    public interface RatingListener {
        void onRatingLoaded(float rating);
    }

}
