package me.santio.dodgeball.events;

import me.santio.dodgeball.Dodgeball;
import me.santio.dodgeball.api.DodgeballAPI;
import me.santio.dodgeball.api.MatchMaker;
import me.santio.dodgeball.api.models.EliminationReason;
import me.santio.dodgeball.api.models.PlayerState;
import me.santio.dodgeball.api.models.Team;
import me.santio.dodgeball.scoreboard.PlayerScoreboard;
import me.santio.dodgeball.scoreboard.ScoreboardManager;
import me.santio.dodgeball.scoreboard.ScoreboardPresets;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

public class PlayerListener implements Listener {
    
    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(Team.NONE.getColor() + event.getPlayer().getDisplayName() + " joined the game!");
        
        ScoreboardManager.createScoreboard(event.getPlayer());
        ScoreboardManager.setScoreboard(event.getPlayer(), ScoreboardPresets.getLobbyScoreboard());
        
        Bukkit.getScheduler().runTaskLater(Dodgeball.getInstance(), () -> {
            event.getPlayer().teleport(DodgeballAPI.getSpawn());
            event.getPlayer().setGameMode(GameMode.ADVENTURE);
            event.getPlayer().getInventory().clear();
        }, 1L);
        
        Bukkit.getScheduler().runTaskTimerAsynchronously(Dodgeball.getInstance(), () -> {
            
            PlayerState state = MatchMaker.getPlayerState(event.getPlayer().getUniqueId());
            if (state == null) return;
            
            Block blockUnder = event.getPlayer().getLocation().getBlock().getRelative(0, -1, 0);
            if (blockUnder.getType().isAir()) blockUnder = blockUnder.getRelative(0, -1, 0);
            
            if (blockUnder.getType() == Material.OBSIDIAN) {
                if (state.getCurrentMatch() == null) return;
                
                Vector from = event.getPlayer().getLocation().toVector().clone();
                Vector to = state.getCurrentMatch().getSpawn(state.getTeam()).toVector();
                
                Vector direction = to.subtract(from).normalize();
                event.getPlayer().setVelocity(direction.multiply(0.75));
            }
            
        }, 0L, 1L);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage("§7" + event.getPlayer().getDisplayName() + " §7left the game!");
        
        PlayerState player = MatchMaker.getPlayerState(event.getPlayer().getUniqueId());
        if (player == null || player.getCurrentMatch() == null) return;
        
        player.getCurrentMatch().eliminate(player.getUniqueId(), EliminationReason.DISCONNECT);
        
        PlayerScoreboard scoreboard = ScoreboardManager.scoreboards.get(event.getPlayer().getUniqueId());
        if (scoreboard != null) scoreboard.delete();
    }
    
    @EventHandler
    private void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) event.setCancelled(true);
    }
    
    @EventHandler
    private void onHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
        event.getEntity().setFoodLevel(20);
    }
    
    @EventHandler
    private void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }
    
}
