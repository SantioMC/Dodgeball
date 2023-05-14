package me.santio.dodgeball.api.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.santio.dodgeball.api.events.BukkitEvent;
import me.santio.dodgeball.api.models.GameMatch;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
public class PlayerEliminatedEvent extends BukkitEvent {
    private GameMatch match;
    private Player player;
}
