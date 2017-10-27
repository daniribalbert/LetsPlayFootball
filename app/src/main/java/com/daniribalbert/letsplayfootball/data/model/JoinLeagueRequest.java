package com.daniribalbert.letsplayfootball.data.model;

import com.daniribalbert.letsplayfootball.utils.GsonUtils;
import com.google.firebase.database.Exclude;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model class for Requests to player join a league.
 */
public class JoinLeagueRequest {

    @SerializedName("senderId")
    public String senderId;
    @SerializedName("senderImage")
    public String senderImage;
    @SerializedName("senderName")
    public String senderName;
    @SerializedName("playerId")
    public String playerId;
    @SerializedName("league")
    public SimpleLeague league;
    @SerializedName("receiverIds")
    List<String> receiverIds;

    public JoinLeagueRequest() {
        // Firebase
    }

    public JoinLeagueRequest(SimpleLeague league, Player sender, String playerId) {
        this.league = league;
        this.playerId = playerId;
        senderId = sender.id;
        senderImage = isPlayerRequest() ? sender.image : league.image;
        senderName = sender.getName();
    }

    public JoinLeagueRequest(League league, Player sender, String playerId) {
        this(new SimpleLeague(league), sender, playerId);
    }

    /**
     * Is the player requesting to join the league?
     *
     * @return true if the request came from the player.
     */
    @Exclude
    public boolean isPlayerRequest() {
        return playerId.equalsIgnoreCase(senderId);
    }

    @Override
    public String toString() {
        return senderName + " | " + senderId + " request " + playerId + " be added to league "
                + league.title;
    }

}
