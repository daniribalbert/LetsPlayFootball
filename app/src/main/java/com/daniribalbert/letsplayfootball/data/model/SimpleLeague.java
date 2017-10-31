package com.daniribalbert.letsplayfootball.data.model;

/**
 * League object nested withing the Player.
 */
public class SimpleLeague {
    public String league_id;
    public String title;
    public String image;
    public boolean manager;

    public SimpleLeague() {//Firebase constructor
    }

    /**
     * Simple league for Guest players which are not registered within the app and only
     * available for a specific league.
     * @param id league id.
     */
    public SimpleLeague(String id) {
        this.league_id = id;
    }

    public SimpleLeague(League league) {
        this.league_id = league.id;
        this.title = league.title;
        this.image = league.image;
        this.manager = false;
    }

    public SimpleLeague(League league, boolean isManager) {
        this.league_id = league.id;
        this.title = league.title;
        this.image = league.image;
        this.manager = isManager;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimpleLeague){
            SimpleLeague league = (SimpleLeague) obj;
            return league.league_id.equalsIgnoreCase(this.league_id);
        }
        if (obj instanceof League){
            League league = (League) obj;
            return league.id.equalsIgnoreCase(this.league_id);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        // Java... -_-
        if (league_id == null){
            return 0;
        }

        return league_id.hashCode();
    }
}

