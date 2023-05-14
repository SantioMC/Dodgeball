package me.santio.dodgeball.api.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.santio.dodgeball.api.events.BukkitEvent;
import me.santio.dodgeball.api.models.GameMatch;
import me.santio.dodgeball.api.models.Team;

@Getter
@AllArgsConstructor
public class MatchEndEvent extends BukkitEvent {
    private GameMatch match;
    private Team winners;
}
