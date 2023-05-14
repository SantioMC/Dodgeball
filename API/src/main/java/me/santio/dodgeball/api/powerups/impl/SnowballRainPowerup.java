package me.santio.dodgeball.api.powerups.impl;

import me.santio.dodgeball.api.DodgeballAPI;
import me.santio.dodgeball.api.powerups.Powerup;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SnowballRainPowerup extends Powerup {
    
    public SnowballRainPowerup() {
        super("Snowball Rain", Material.BEACON);
        color(ChatColor.AQUA);
        onPickup((state) -> {
            broadcast(state.getCurrentMatch(), state.getPlayer().getDisplayName() + " &7made it rain snowballs!");
            
            new BukkitRunnable() {
                int remaining = 5;
                
                @Override public void run() {
                    if (remaining <= 0) {
                        cancel();
                        return;
                    }
                    
                    Location location = state.getCurrentMatch().getGame().getRandomPowerupLocation();
                    if (location == null || location.getWorld() == null) return;
                    
                    location.getWorld().dropItemNaturally(location, new ItemStack(Material.SNOWBALL, 1));
                    remaining--;
                }
            }.runTaskTimer(DodgeballAPI.getInstance(), 20, 20);
        });
        
    }
    
}
