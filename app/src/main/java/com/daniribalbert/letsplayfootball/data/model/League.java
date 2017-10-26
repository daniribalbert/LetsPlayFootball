package com.daniribalbert.letsplayfootball.data.model;

import android.text.TextUtils;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * League model class.
 */
public class League {

    /**
     * League ID.
     */
    public String id;

    /**
     * List with the Ids of owners.
     * Using list just in case in the future we add leagues with multiple owners.
     */
    public HashMap<String, Boolean> ownersId = new HashMap<>();

    /**
     * League title. Example: Premier League.
     */
    public String title;

    /**
     * League description.
     */
    public String description;

    /**
     * League rules.
     */
    public String rules;


    /**
     * League description.
     */
    public String image;

    /**
     * List of player who participate in this league.
     */
    public List<Player> players = new ArrayList<>();

    public League() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    @Exclude
    public boolean isOwner(String userId){
        return ownersId.containsKey(userId);
    }

    @Override
    public String toString() {
        return title + "\n" + description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof League){
            League league = (League) obj;
            return league.id.equalsIgnoreCase(this.id);
        }
        return super.equals(obj);
    }

    public boolean hasImage() {
        return !TextUtils.isEmpty(image);
    }

    @Override
    public int hashCode() {
        return ownersId.size() + players.size(); // Java... -_-
    }
}
