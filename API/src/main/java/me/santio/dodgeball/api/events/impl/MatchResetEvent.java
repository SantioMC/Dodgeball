package me.santio.dodgeball.api.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.santio.dodgeball.api.events.BukkitEvent;
import me.santio.dodgeball.api.models.GameMatch;
import me.santio.dodgeball.api.models.PlayerState;

import java.util.List;

@Getter
@AllArgsConstructor
public class MatchResetEvent extends BukkitEvent {
    private GameMatch match;
    private List<PlayerState> players;
}
