package me.santio.dodgeball.events;

import me.santio.dodgeball.Dodgeball;
import me.santio.dodgeball.api.MatchMaker;
import me.santio.dodgeball.api.models.EliminationReason;
import me.santio.dodgeball.api.models.GameMatch;
import me.santio.dodgeball.api.models.PlayerState;
import me.santio.dodgeball.api.models.Team;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SnowballListener implements Listener {
    
    private void playParticle(Location loc, float radius, int count) {
        if (loc.getWorld() == null) return;
        loc.getWorld().spawnParticle(
            Particle.BLOCK_DUST,
            loc,
            count,
            radius,
            radius,
            radius,
            0,
            Material.SNOW_BLOCK.createBlockData()
        );
    }
    
    @EventHandler
    private void onSnowballLand(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball)) return;
        if (event.getEntity().getShooter() == null) return;
        
        Player thrower = (Player) event.getEntity().getShooter();
        
        // If the snowball is over air, we'll throw it back
        World world = event.getEntity().getWorld();
        boolean isOverAir = world.getHighestBlockAt(
            event.getEntity().getLocation()
        ).getType().isAir();
        
        if (event.getHitBlock() != null && !isOverAir) {
            // Play a splash effect upon hitting a block
            playParticle(
                event.getHitBlock().getLocation(),
                0.2f,
                20
            );
            
            // Add a cool lil bounce effect
            Vector velocity = event.getEntity().getVelocity();
            velocity.setY(-velocity.getY());
            velocity.normalize().multiply(0.3).add(new Vector(0, 0.1, 0));
            
            // Get the location of the snowball, if it's not air we're going to spawn it a bit lower
            Location locationAbove = event.getEntity().getLocation().clone().add(0, 0.5, 0);
            
            // Choose the best location to drop the snowball
            Location dropLoc = locationAbove.getBlock().getType().isAir()
                ? event.getEntity().getLocation()
                : event.getEntity().getLocation().clone().add(0, -0.5, 0);
            
            Item item = event.getEntity().getWorld().dropItem(
                dropLoc,
                new ItemStack(Material.SNOWBALL)
            );
            
            item.setVelocity(velocity);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (item.isValid() && !item.isOnGround()) {
                        // It's been 5s, and we haven't hit the ground yet, we'll just drop it
                        item.remove();
                        
                        // Find the closest spawn point
                        PlayerState state = MatchMaker.getPlayerState(thrower.getUniqueId());
                        if (state == null) return;
                        
                        GameMatch match = state.getCurrentMatch();
                        match.dropSnowball(item.getLocation());
                    }
                }
            }.runTaskLater(Dodgeball.getInstance(), 100);
        }
        
        if (isOverAir) {
            // If we hit are over air, we'll drop it to the closest spawn point
            Location location = event.getEntity().getLocation().clone();
            
            playParticle(
                location,
                1f,
                50
            );
            
            event.getEntity().remove();
            
            // Find the closest spawn point
            PlayerState state = MatchMaker.getPlayerState(thrower.getUniqueId());
            if (state == null) return;
            
            GameMatch match = state.getCurrentMatch();
            match.dropSnowball(location);
        }
        
        if (event.getHitEntity() != null) {
            if (!(event.getHitEntity() instanceof Player victim)) return;
            
            PlayerState state = MatchMaker.getPlayerState(thrower.getUniqueId());
            PlayerState victimState = MatchMaker.getPlayerState(victim.getUniqueId());
            if (state == null || victimState == null) return;
            
            GameMatch match = state.getCurrentMatch();
            assert match != null;
            
            // If the player is in the same team, we'll give the snowball to the other team
            if (state.getTeam() == victimState.getTeam()) {
                playParticle(
                    victim.getLocation(),
                    1f,
                    10
                );
                event.getEntity().remove();
                match.dropSnowball(state.getTeam() == Team.RED ? Team.BLUE : Team.RED);
                return;
            }
            
            // If the player is in the opposite team, we'll kill them
            victimState.setAttacker(thrower.getUniqueId());
            match.eliminate(victim.getUniqueId(), EliminationReason.DIED);
            victim.sendTitle(
                "§c§lYOU DIED!",
                "§7You were killed by §e" + thrower.getName(),
                0,
                40,
                0
            );
            
            playParticle(
                victim.getLocation(),
                1f,
                30
            );
        }
    }
    
    @EventHandler
    private void onSnowballThrow(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Snowball)) return;
        
        new BukkitRunnable() {
            @Override public void run() {
                if (event.getEntity().isDead() || event.getEntity().isOnGround()) {
                    cancel();
                    return;
                }
                
                if (event.getEntity().getLocation().getY() < Dodgeball.lowestY) {
                    if (event.getEntity().getShooter() == null) return;
                    Player thrower = (Player) event.getEntity().getShooter();
                    
                    PlayerState state = MatchMaker.getPlayerState(thrower.getUniqueId());
                    assert state != null;
                    
                    GameMatch match = state.getCurrentMatch();
                    match.dropSnowball(event.getEntity().getLocation());
                    
                    playParticle(
                        event.getEntity().getLocation(),
                        1f,
                        50
                    );
                    
                    event.getEntity().remove();
                    cancel();
                    
                    return;
                }
                
                // Add a particle trail to snowballs
                event.getEntity().getWorld().spawnParticle(
                    Particle.REDSTONE,
                    event.getEntity().getLocation(),
                    2,
                    0.01,
                    0.01,
                    0.01,
                    0,
                    new Particle.DustOptions(
                        Color.WHITE,
                        1
                    )
                );
            }
        }.runTaskTimer(Dodgeball.getInstance(), 0L, 1L);
    }
    
    @EventHandler
    private void onItemMerge(ItemMergeEvent event) {
        if (event.getEntity().getItemStack().getType() == Material.SNOWBALL) event.setCancelled(true);
    }
    
}
