package me.santio.dodgeball.api;

import me.santio.dodgeball.api.models.GameMatch;
import me.santio.dodgeball.api.models.PlayerState;
import me.santio.dodgeball.api.models.Team;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Handles putting players into teams as fairly as possible.
 */
public final class TeamOrganizer {
    private TeamOrganizer() { /* Prevent instantiation */ }
    private static final Random random = new Random();
    
    /**
     * Organizes a list of player states into teams.
     * @param players The players to organize.
     * @param teams The teams to organize the players into.
     */
    public static void organize(List<PlayerState> players, List<Team> teams) {
        int index = 0;
        
        // Shuffle the players
        Collections.shuffle(players, random);
        
        // Loop through the players and put them into teams
        for (PlayerState player : players) {
            // Get the team
            Team team = teams.get(index);
            
            // Set the player's team
            player.setTeam(team);
            
            // Increment the index
            index = ++index % teams.size();
        }
    }
    
    /**
     * Find the smallest team in the match, or randomly pick one if they are equal.
     * @return A team object representing the smallest team.
     */
    public static Team getSmallestTeam(GameMatch match) {
        int red = match.getPlayers(Team.RED).size();
        int blue = match.getPlayers(Team.BLUE).size();
        
        if (red == blue) {
            return random.nextBoolean() ? Team.RED : Team.BLUE;
        } else {
            return red < blue ? Team.RED : Team.BLUE;
        }
    }
    
}
