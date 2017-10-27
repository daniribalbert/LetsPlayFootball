package com.daniribalbert.letsplayfootball.data.firebase;

import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.JoinLeagueRequest;
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
        if (request.playerId.equalsIgnoreCase(request.playerId)) {
            // Player sends request to join league.
            ref.child(request.league.league_id).removeValue();
        } else {
            // League manager sends requests for player to join league.
            ref.child(request.playerId).removeValue();
        }
    }

    public static void loadMyRequests(String playerId, List<String> myLeagues,
                                      final Listener listener) {
        final CountDownLatch counter = new CountDownLatch(1 + myLeagues.size());
        final List<JoinLeagueRequest> pendingRequests = new LinkedList<>();

        getRef().child(playerId).addListenerForSingleValueEvent(new BaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogUtils.d("My Requests: " + dataSnapshot);
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    DataSnapshot next = iterator.next();
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

        for (final String leagueId : myLeagues) {
            getRef().child(leagueId).addListenerForSingleValueEvent(new BaseValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    LogUtils.d("My League " + leagueId + " request: " + dataSnapshot);
                    Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot next = iterator.next();
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
