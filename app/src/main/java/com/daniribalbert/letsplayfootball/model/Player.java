package com.daniribalbert.letsplayfootball.model;

import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * Player model class.
 */
public class Player {

    private Player() {
    }

    /**
     * Player ID.
     */
    private String id;

    /*
    * Player name.
    */
    private String name;

    /**
     * Profile pic URL.
     */
    private String image;

    /**
     * Leagues where this player is registered.
     */
    private List<League> leagues;

    /**
     * Player nickname (Optional).
     */
    private String nickname;

    // Skill set used to rank this player.
    private float stamina;
    private float pass;
    private float kick;
    private float speed;
    private float skill;
    private float defense;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getNickname() {
        return TextUtils.isEmpty(nickname) ? name : nickname;
    }

    public float getStamina() {
        return stamina;
    }

    public float getPass() {
        return pass;
    }

    public float getKick() {
        return kick;
    }

    public float getSpeed() {
        return speed;
    }

    public float getSkill() {
        return skill;
    }

    public float getDefense() {
        return defense;
    }

    public List<League> getLeagues() {
        return leagues;
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

}
