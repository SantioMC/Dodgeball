package me.santio.dodgeball.api.powerups.impl;

import me.santio.dodgeball.api.powerups.Powerup;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SnowballsPowerup extends Powerup {
    
    public SnowballsPowerup() {
        super("Snowball", Material.SNOWBALL);
        color(ChatColor.WHITE);
        onPickup((state) -> {
            state.getPlayer().getInventory().addItem(new ItemStack(Material.SNOWBALL, 1));
        });
        
    }
    
}
