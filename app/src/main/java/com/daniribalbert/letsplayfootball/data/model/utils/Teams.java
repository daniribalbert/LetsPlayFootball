package com.daniribalbert.letsplayfootball.data.model.utils;

import android.widget.Toast;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.data.model.Team;
import com.daniribalbert.letsplayfootball.utils.ToastUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class with a few functions to handle lists of Players.
 */
public class Teams {

    public static List<Player> sortPlayersByRating(final Team team) {
        List<Player> players = team.getPlayers();
        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                String leagueId = team.getLeagueId();
                return Float.compare(player1.getRating(leagueId), player2.getRating(leagueId));
            }
        });
        return players;
    }

    public static List<Team> sortTeams(List<Team> teams, List<Player> players,
                                       boolean sortGoalkeepers) {
        return sortTeams(teams, players, sortGoalkeepers, true);
    }

    public static List<Team> sortTeams(List<Team> teams, List<Player> players,
                                       boolean sortGoalkeepers, boolean clearBeforeSort) {
        String leagueId = teams.get(0).getLeagueId();

        if (clearBeforeSort) {
            for (Team team : teams) {
                team.clearPlayers();
            }
        } else {
            for (Team team : teams) {
                for (Player player : team.getPlayers()) {
                    players.remove(player);
                }
            }
        }

        LinkedList<Player> goalkeepers = new LinkedList<>();
        for (int i = players.size() - 1; i >= 0; i--) {
            Player player = players.get(i);
            if (player.isGoalkeeper()) {
                goalkeepers.add(players.remove(i));
            }
        }

        LinkedList<Player> sortedPlayers = new LinkedList<>(
                sortPlayersByRating(new Team(leagueId, players)));
        semiRandomSort(teams, sortedPlayers);

        if (sortGoalkeepers) {
            Collections.shuffle(goalkeepers);
            while (!goalkeepers.isEmpty()) {
                for (Team team : teams) {
                    if (goalkeepers.isEmpty()) {
                        break;
                    }
                    team.getPlayers().add(goalkeepers.pollFirst());
                }
            }
        }
        return teams;
    }

    private static void semiRandomSort(List<Team> teams, LinkedList<Player> players) {
        int nTeams = teams.size();
        while (!players.isEmpty()) {
            Collections.shuffle(teams);
            for (int i = 0; i < nTeams && !players.isEmpty(); i++) {
                Player player = players.pollFirst();
                teams.get(i).getPlayers().add(player);
            }

            Collections.shuffle(teams);
            for (int i = 0; i < nTeams && !players.isEmpty(); i++) {
                Player player = players.pollLast();
                teams.get(i).getPlayers().add(player);
            }
        }
    }

    private static void balancedSort(List<Team> teams, LinkedList<Player> players) {
        int nTeams = teams.size();
        while (!players.isEmpty()) {
            Collections.shuffle(teams);
            for (int i = 0; i < nTeams && !players.isEmpty(); i++) {
                Player player = players.pollFirst();
                teams.get(i).getPlayers().add(player);
            }

            Collections.shuffle(teams);
            for (int i = 0; i < nTeams && !players.isEmpty(); i++) {
                Player player = players.pollLast();
                teams.get(i).getPlayers().add(player);
            }
        }
    }
}
