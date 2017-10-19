package com.daniribalbert.letsplayfootball.data.model;

import com.daniribalbert.letsplayfootball.application.App;
import com.google.firebase.database.Exclude;

/**
 * Model class for Requests to player join a league.
 */
public class JoinLeagueRequest {


    public String senderId;
    public String senderImage;
    public String senderName;
    public String playerId;
    public SimpleLeague league;

    public JoinLeagueRequest(){
        // Firebase
    }

    public JoinLeagueRequest(SimpleLeague league, Player sender, String playerId) {
        this.league = league;
        senderId = sender.id;
        senderImage = sender.image;
        senderName = sender.getName();
        this.playerId = playerId;
    }

    public JoinLeagueRequest(League league, Player sender, String playerId){
        this(new SimpleLeague(league), sender, playerId);
    }

    @Exclude
    public boolean isPlayerRequest(){
        return playerId.equalsIgnoreCase(senderId);
    }

}
