package com.daniribalbert.letsplayfootball.data.model;

import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

/**
 * Player model class.
 */
public class Player {

    /**
     * Player ID.
     */
    public String id;

    /*
    * Player name.
    */
    public String name;

    /**
     * Player nickname (Optional).
     */
    public String nickname;

    /**
     * Profile pic URL.
     */
    public String image;

    /**
     * Leagues where this player is registered.
     */
    public HashMap<String, SimpleLeague> leagues = new HashMap<>();

    public float rating;

    // Skills set used to rank this player.
    // TODO: For future releases add skills.
//    public float stamina;
//    public float pass;
//    public float kick;
//    public float speed;
//    public float skill;
//    public float defense;

    public Player() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    /**
     * Get the name the player is usually called, usually the name in the shirt
     * Ex: Ronaldinho.
     *
     * @return nickname if available, else return name.
     */
    public String getDisplayName() {
        return TextUtils.isEmpty(nickname) ? name : nickname;
    }

    public static Player fromFirebase(FirebaseUser user) {
        Player player = new Player();
        player.id = user.getUid();
        try {
            player.image = user.getPhotoUrl().toString();
        } catch (NullPointerException npe) {
            player.image = "";
        }
        player.name = user.getDisplayName();
        return player;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(name);
        if (!TextUtils.isEmpty(nickname)) {
            builder.append(" (");
            builder.append(nickname);
            builder.append(')');
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Player){
            Player player = (Player) obj;
            return player.id.equalsIgnoreCase(this.id);
        }
        return super.equals(obj);
    }
}
