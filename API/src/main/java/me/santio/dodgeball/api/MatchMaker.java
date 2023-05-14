package me.santio.dodgeball.api;

import lombok.Getter;
import me.santio.dodgeball.api.events.impl.PlayerQueuedEvent;
import me.santio.dodgeball.api.events.impl.PlayerUnqueuedEvent;
import me.santio.dodgeball.api.models.*;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The matchmaker is responsible for creating and managing matches.
 */
public final class MatchMaker {
    private MatchMaker() { /* Prevent instantiation */ }
    
    /**
     * A list of all matches that are currently running.
     */
    @Getter
    private static final List<GameMatch> matches = new ArrayList<>();
    
    /**
     * A list of all players that are currently in a match.
     */
    @Getter
    private static final List<PlayerState> states = new ArrayList<>();
    
    /**
     * Gets a player state from a UUID.
     * @param uuid The UUID of the player.
     * @return The player state.
     */
    public static PlayerState getPlayerState(UUID uuid) {
        for (PlayerState state : states) {
            if (state.getUniqueId() == uuid) {
                return state;
            }
        }
        
        return null;
    }
    
    /**
     * Adds a player to a game.
     * @param uuid The UUID of the player.
     * @param game The game to add the player to.
     * @return Weather or not a game was found and the player was added.
     */
    public static boolean addPlayer(UUID uuid, Game game) {
        // Create a state for the player
        PlayerState state = new PlayerState(uuid);
        states.add(state);
        
        GameMatch match = getMatch(game, true);
        if (match == null) return false;
        
        match.getPlayers().add(state);
        state.getPlayer().teleport(game.getLobbySpawn());
        state.setCurrentMatch(match);
        
        if (state.getCurrentMatch().getState() == GameMatch.State.WAITING && state.getCurrentMatch().getPlayers().size() >= 2) {
            state.getCurrentMatch().start();
        }
        
        Bukkit.getPluginManager().callEvent(new PlayerQueuedEvent(match, state));
        
        state.getPlayer().sendMessage("§7You have joined §a" + game.getName() + "§7!");
        return true;
    }
    
    /**
     * Removes a player from their game.
     * @param uuid The UUID of the player.
     */
    public static void removePlayer(UUID uuid) {
        PlayerState state = getPlayerState(uuid);
        if (state == null) return;
        
        if (state.getCurrentMatch() != null) {
            GameMatch match = state.getCurrentMatch();
            
            if (state.getTeam() != Team.DEAD && match.getState() != GameMatch.State.FINISHED) {
                match.eliminate(state.getUniqueId(), EliminationReason.LEFT);
            }
            
            match.getPlayers().remove(state);
            
            if (match.getState() == GameMatch.State.STARTING && match.getPlayers().size() == 1)
                match.cancelStart();
            
            else if (match.getPlayers().isEmpty())
                state.getCurrentMatch().stop();
            
            Bukkit.getPluginManager().callEvent(new PlayerUnqueuedEvent(match, state));
        }
        
        state.getPlayer().sendMessage("§7You have left §c" + state.getCurrentMatch().getGame().getName() + "§7!");
        state.reset();
    }
    
    /**
     * Gets a match from a game, or creates one if none are available if createIfNull is true.
     * @apiNote Multiple matches per game is not supported, however this functionality is here for future use.
     * @param game The game to get a match from.
     * @param createIfNull Whether to create a match if none are available.
     * @return The match, or null if none are available.
     */
    public static GameMatch getMatch(Game game, boolean createIfNull) {
        for (GameMatch match : game.getMatches()) {
            if (match.isAcceptingPlayers()) {
                return match;
            }
        }
        
        return createIfNull ? game.createMatch() : null;
    }
}
