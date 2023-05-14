package me.santio.dodgeball.api.powerups.impl;

import me.santio.dodgeball.api.models.GameMatch;
import me.santio.dodgeball.api.models.Team;
import me.santio.dodgeball.api.powerups.Powerup;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;

public class BlindnessPowerup extends Powerup {
    
    private final PotionEffect blindness = new PotionEffect(
        org.bukkit.potion.PotionEffectType.BLINDNESS,
        80,
        1,
        false,
        false
    );
    
    public BlindnessPowerup() {
        super("Blindness", Material.INK_SAC);
        color(ChatColor.DARK_GRAY);
        onPickup((state) -> {
            GameMatch match = state.getCurrentMatch();
            Team opposingTeam = state.getTeam() == Team.RED ? Team.BLUE : Team.RED;
            
            broadcast(match, state.getPlayer().getDisplayName() + " &7has blinded the opposing team!");
            
            match.getPlayers(opposingTeam).forEach((victim) -> {
                victim.getPlayer().addPotionEffect(blindness);
            });
        });
        
    }
    
}
