package me.santio.dodgeball.events;

import me.santio.dodgeball.api.DodgeballAPI;
import me.santio.dodgeball.api.MatchMaker;
import me.santio.dodgeball.api.models.PlayerState;
import me.santio.dodgeball.api.powerups.Powerup;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class PowerupListener implements Listener {

    @EventHandler
    private void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        if (event.getItem().hasMetadata("powerup")) {
            event.setCancelled(true);
            
            PlayerState state = MatchMaker.getPlayerState(player.getUniqueId());
            if (state == null) return;
            
            event.getItem().remove();

            String name = event.getItem().getMetadata("powerup").get(0).asString();
            Powerup powerup = DodgeballAPI.getPowerup(name);
            
            if (powerup == null) return;
            if (powerup.onPickup() != null) powerup.onPickup().accept(state);
        }
    }
    
}
