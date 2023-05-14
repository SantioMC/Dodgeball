package me.santio.dodgeball.api.models;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.santio.dodgeball.api.DodgeballAPI;
import me.santio.dodgeball.api.MatchMaker;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents the state of a player in an ongoing match.
 */
@FieldDefaults(level= AccessLevel.PRIVATE)
@Setter
@Getter
@RequiredArgsConstructor
public class PlayerState {
    final UUID uniqueId;
    UUID attacker;
    
    GameMatch currentMatch;
    Team team;
    
    /**
     * Gets the Bukkit player object for this player.
     * @return A Bukkit player, or null if they are not online.
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(this.uniqueId);
    }
    
    /**
     * Sets the team for this player.
     * @param team The team to set.
     */
    public void setTeam(Team team) {
        this.team = team;
        
        Player player = this.getPlayer();
        if (player != null) {
            player.setDisplayName(team.getColor() + player.getName());
        }
    }
    
    /**
     * Resets the player's state.
     */
    public void reset() {
        Player player = this.getPlayer();
        
        if (player != null) {
            player.setDisplayName(Team.NONE.getColor() + player.getName());
            player.teleport(DodgeballAPI.getSpawn());
            player.getInventory().clear();
            player.setGameMode(GameMode.ADVENTURE);
        }
        
        this.currentMatch.getPlayers().remove(this);
        MatchMaker.getStates().remove(this);
    }
}
