package com.daniribalbert.letsplayfootball.data.firebase;

import com.daniribalbert.letsplayfootball.data.cache.PlayersCache;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.CompletionListener;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.SearchListener;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for Player database operations.
 */
public class PlayerDbUtils {

    private static final String PATH = "players";

    private static DatabaseReference getRef() {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        return dbRef.child(PATH);
    }

    /**
     * Creates a new user. Before creation, this method verifies if the user exists so it won't
     * override the previous player info.
     *
     * @param firebaseUser new player to be added to the database with info gathered from the
     *                     Firebase login info.
     */
    public static void createPlayer(final FirebaseUser firebaseUser,
                                    final CompletionListener listener) {
        final Player player = Player.fromFirebase(firebaseUser);
        final DatabaseReference dbRef = getRef();
        dbRef.child(player.id).addListenerForSingleValueEvent(new BaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    LogUtils.i("Creating user!");
                    dbRef.child(player.id).setValue(player);
                }
                listener.onComplete(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                super.onCancelled(databaseError);
                listener.onComplete(false);
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
        player.guestify();

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

    public static void searchPlayers(final String searchQuery,
                                     final SearchListener<Player> listener) {
        final int MAX_RESULTS = 50;
        DatabaseReference dbRef = getRef();
        Query nameQuery = dbRef.orderByChild("name").startAt(searchQuery).limitToFirst(MAX_RESULTS);
        Query nicknameQuery = dbRef.orderByChild("nickname").startAt(searchQuery)
                                   .limitToFirst(MAX_RESULTS);
        final AtomicInteger nQuery = new AtomicInteger(2);
        final Set<Player> result = new HashSet<Player>();


        ValueEventListener eventListener = new BaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogUtils.w(dataSnapshot.toString());
                for (DataSnapshot next : dataSnapshot.getChildren()) {
                    Player player = next.getValue(Player.class);
                    if (player != null
                            && (player.getName().startsWith(searchQuery)
                            || player.getNickname().startsWith(searchQuery))) {
                        result.add(player);
                    }
                }
                if (nQuery.decrementAndGet() <= 0) {
                    listener.onDataReceived(result);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                super.onCancelled(databaseError);
                if (nQuery.decrementAndGet() <= 0) {
                    listener.onDataReceived(result);
                }
            }
        };

        nameQuery.addListenerForSingleValueEvent(eventListener);
        nicknameQuery.addListenerForSingleValueEvent(eventListener);
    }

    public static void updatePlayer(Player player) {
        DatabaseReference dbRef = getRef();
        if (player.isGuest()) {
            player.guestify();
        }
        dbRef.child(player.id).setValue(player);
    }

    public static void addLeague(String playerId, League league) {
        DatabaseReference ref = getRef();
        ref.child(playerId).child("leagues").child(league.id).setValue(new SimpleLeague(league));
    }

    public static void addLeague(final String playerId, final String leagueId) {
        LeagueDbUtils.getLeague(leagueId, new BaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DatabaseReference ref = getRef();
                League league = dataSnapshot.getValue(League.class);
                SimpleLeague playerLeague = new SimpleLeague(league);
                ref.child(playerId).child("leagues").child(leagueId).setValue(playerLeague);
            }
        });
    }

    public static void updatePlayerLeague(String playerId, League league) {
        DatabaseReference ref = getRef();
        ref.child(playerId).child("leagues").child(league.id).setValue(new SimpleLeague(league));
    }

    public static void removePlayer(Player player, String leagueId) {
        if (player.isGuest()) {
            removeGuestPlayer(player.id);
        } else {
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

        //  Also needs to remove previous entries from the ratings table.
        RatingsDbUtils.removePlayerFromLeague(player.id, leagueId);
    }

    public static void updatePlayerRating(String playerId, String leagueId, float playerRating) {
        DatabaseReference ref = getRef();
        ref.child(playerId).child("rating").child(leagueId).setValue(playerRating);
    }

    public static void updateCurrentPlayerPushToken(String refreshedToken) {
        DatabaseReference ref = getRef();

        Player currentUser = PlayersCache.getCurrentPlayerInfo();
        currentUser.pushToken = refreshedToken;
        PlayersCache.saveCurrentPlayerInfo(currentUser);

        Map<String, Object> updateMap = new HashMap<String, Object>();
        updateMap.put("pushToken", refreshedToken);
        ref.child(currentUser.id).updateChildren(updateMap);
    }
}
