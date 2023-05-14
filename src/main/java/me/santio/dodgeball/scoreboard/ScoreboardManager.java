package me.santio.dodgeball.scoreboard;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ScoreboardManager {
    private ScoreboardManager() { /* Prevent instantiation */ }
    public static final Map<UUID, PlayerScoreboard> scoreboards = new HashMap<>();
    
    /**
     * Creates a scoreboard for a player and adds it to them
     * @param player The player to create the scoreboard for.
     */
    public static void createScoreboard(Player player) {
        PlayerScoreboard scoreboard = new PlayerScoreboard(player);
        scoreboards.put(player.getUniqueId(), scoreboard);
    }
    
    /**
     * Set the lines of a players scoreboard, this is a shortcut for
     * {@link PlayerScoreboard#set(String...)}.
     * @param player The player to set the scoreboard for.
     * @param lines The lines to set.
     */
    public static void setScoreboard(Player player, String... lines) {
        if (player == null) return;
        
        PlayerScoreboard scoreboard = scoreboards.get(player.getUniqueId());
        if (scoreboard == null) return;
        
        scoreboard.set(lines);
    }
    
}
