package me.santio.dodgeball.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Arrays;
import java.util.UUID;

@SuppressWarnings("unused")
public class PlayerScoreboard {
    
    private final UUID player;
    private final Objective objective;
    private final Scoreboard scoreboard;
    
    private String[] lines = new String[15];
    
    /**
     * Creates a new scoreboard for a player, you should not call this directly but rather
     * use {@link ScoreboardManager#createScoreboard(Player)}.
     * @param player The player to create the scoreboard for.
     */
    public PlayerScoreboard(Player player) {
        this.player = player.getUniqueId();
        
        assert Bukkit.getScoreboardManager() != null;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        
        objective = scoreboard.registerNewObjective(
            "dodgeball-" + UUID.randomUUID(),
            Criteria.DUMMY,
            "§6§lDODGEBALL"
        );
        
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
    }
    
    /**
     * Sets the lines of the scoreboard.
     * @param lines The lines to set.
     */
    public void set(String... lines) {
        this.lines = lines;
        update();
    }
    
    /**
     * Set a line of the scoreboard.
     * @param line The line to set. (0-14)
     * @param text Any text value.
     */
    public void line(int line, String text) {
        lines[line] = text;
        update();
    }
    
    /**
     * Remove a line from the scoreboard.
     * @param line The line to remove. (0-14)
     */
    public void remove(int line) {
        lines[line] = null;
        update();
    }
    
    /**
     * Clears the scoreboard.
     */
    public void clear() {
        Arrays.fill(lines, null);
        update();
    }
    
    /**
     * Updates the scoreboard. (Called automatically)
     */
    public void update() {
        scoreboard.getEntries().forEach(scoreboard::resetScores);
        for (int i = 0; i < lines.length; i++) {
            String text = lines[i];
            
            if (text == null) {
                if (scoreboard.getEntries().contains(String.valueOf(i))) {
                    scoreboard.resetScores(String.valueOf(i));
                }
                
                continue;
            }
            
            objective.getScore(text).setScore(15 - i);
        }
    }
    
    /**
     * Delete the scoreboard and remove it from the manager.
     */
    public void delete() {
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        scoreboard.getEntries().forEach(scoreboard::resetScores);
        objective.unregister();
        
        ScoreboardManager.scoreboards.remove(player);
    }
    
}
