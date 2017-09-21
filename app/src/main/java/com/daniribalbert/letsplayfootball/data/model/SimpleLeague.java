package com.daniribalbert.letsplayfootball.data.model;

/**
 * League object nested withing the Player.
 */
public class SimpleLeague {
    public String id;
    public String title;
    public String description;
    public String image;
    public int nPlayers;

    public SimpleLeague() {//Firebase constructor
    }

    public SimpleLeague(League league) {
        this.id = league.id;
        this.title = league.title;
        this.description = league.description;
        this.image = league.image;
        this.nPlayers = league.players.size();
    }

    @Override
    public String toString() {
        return title + "\n" + description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimpleLeague){
            SimpleLeague league = (SimpleLeague) obj;
            return league.id.equalsIgnoreCase(this.id);
        }
        if (obj instanceof League){
            League league = (League) obj;
            return league.id.equalsIgnoreCase(this.id);
        }
        return super.equals(obj);
    }
}

