package me.santio.dodgeball.api.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.santio.dodgeball.api.events.BukkitEvent;
import me.santio.dodgeball.api.models.GameMatch;

@Getter
@AllArgsConstructor
public class MatchStartEvent extends BukkitEvent {
    private GameMatch match;
}
