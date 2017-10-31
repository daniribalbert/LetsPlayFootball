package com.daniribalbert.letsplayfootball.data.firebase;

import com.daniribalbert.letsplayfootball.data.cache.LeagueCache;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.JoinLeagueRequest;
import com.daniribalbert.letsplayfootball.data.model.League;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.data.model.SimpleLeague;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Utility class for Player database operations.
 */
public class RequestsDbUtils {

    private static final String PATH = "pending_requests";

    private static DatabaseReference getRef() {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        return dbRef.child(PATH);
    }

    public static void sendRequestToJoinLeague(JoinLeagueRequest request) {
        DatabaseReference ref = getRef();
        if (request.isPlayerRequest()) {
            // Player sends request to join league.
            String requestId = request.league.league_id + "_" + request.senderId;
            ref.child(request.league.league_id).child(requestId).setValue(request);
        } else {
            // League manager sends requests for player to join league.
            String requestId = request.league.league_id + "_" + request.senderId;
            ref.child(request.playerId).child(requestId).setValue(request);
        }
    }

    public static void acceptJoinLeagueRequest(JoinLeagueRequest request) {
        PlayerDbUtils.addLeague(request.playerId, request.league.league_id);
        removeJoinLeagueRequest(request);
    }

    public static void removeJoinLeagueRequest(JoinLeagueRequest request) {
        DatabaseReference ref = getRef();
        if (request.playerId.equalsIgnoreCase(request.senderId)) {
            // Player sends request to join league.
            ref.child(request.league.league_id).removeValue();
        } else {
            // League manager sends requests for player to join league.
            ref.child(request.playerId).removeValue();
        }
    }

    public static void loadMyRequests(Player player, final Listener listener) {
        List<String> myManagedLeagues = new LinkedList<>();

        if (player.leagues != null) {
            for (String leagueId : player.leagues.keySet()) {
                SimpleLeague playerLeague = player.leagues.get(leagueId);
                if (playerLeague.manager) {
                    myManagedLeagues.add(playerLeague.league_id);
                } else { // Check cache
                    League league = LeagueCache.getLeagueInfo(leagueId);
                    if (league != null && league.isManager(player.id)) {
                        myManagedLeagues.add(leagueId);
                    }
                }
            }
        }

        final CountDownLatch counter = new CountDownLatch(1 + myManagedLeagues.size());
        final List<JoinLeagueRequest> pendingRequests = new LinkedList<>();

        getRef().child(player.id).addListenerForSingleValueEvent(new BaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogUtils.d("My Requests: " + dataSnapshot);
                for (DataSnapshot next : dataSnapshot.getChildren()) {
                    JoinLeagueRequest request = next.getValue(JoinLeagueRequest.class);
                    if (request != null) {
                        pendingRequests.add(request);
                    }
                }
                counter.countDown();
                if (counter.getCount() == 0) {
                    listener.onLoadFinished(pendingRequests);
                }
            }
        });

        for (final String leagueId : myManagedLeagues) {
            getRef().child(leagueId).addListenerForSingleValueEvent(new BaseValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    LogUtils.d("My League " + leagueId + " request: " + dataSnapshot);
                    for (DataSnapshot next : dataSnapshot.getChildren()) {
                        JoinLeagueRequest request = next.getValue(JoinLeagueRequest.class);
                        if (request != null) {
                            pendingRequests.add(request);
                        }
                    }

                    counter.countDown();
                    if (counter.getCount() == 0) {
                        listener.onLoadFinished(pendingRequests);
                    }
                }
            });
        }
    }

    public interface Listener {
        void onLoadFinished(List<JoinLeagueRequest> pendingRequests);
    }
}
