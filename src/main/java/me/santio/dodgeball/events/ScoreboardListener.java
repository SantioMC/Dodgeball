package me.santio.dodgeball.events;

import me.santio.dodgeball.api.events.impl.*;
import me.santio.dodgeball.api.models.GameMatch;
import me.santio.dodgeball.api.models.PlayerState;
import me.santio.dodgeball.scoreboard.ScoreboardManager;
import me.santio.dodgeball.scoreboard.ScoreboardPresets;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ScoreboardListener implements Listener {
    
    private void setSidebar(GameMatch match, String[] lines) {
        for (PlayerState state : match.getPlayers()) {
            ScoreboardManager.setScoreboard(
                state.getPlayer(),
                lines
            );
        }
    }
    
    // Queue and unqueue events, we update for all players to
    // update the player count.
    
    @EventHandler
    private void onPlayerQueued(PlayerQueuedEvent event) {
        setSidebar(event.getMatch(), ScoreboardPresets.getGameLobbyScoreboard(event.getMatch()));
    }
    
    @EventHandler
    private void onPlayerUnqueued(PlayerUnqueuedEvent event) {
        ScoreboardManager.setScoreboard(event.getPlayer().getPlayer(), ScoreboardPresets.getLobbyScoreboard());
    }
    
    // Handle the mid-game events, so we can have the proper counts for players
    
    @EventHandler
    private void onGameStart(MatchStartEvent event) {
        setSidebar(event.getMatch(), ScoreboardPresets.getGameScoreboard(event.getMatch()));
    }
    
    @EventHandler
    private void onPlayerEliminated(PlayerEliminatedEvent event) {
        setSidebar(event.getMatch(), ScoreboardPresets.getGameScoreboard(event.getMatch()));
    }
    
    // Handle game end event, we update for all players to
    // show the winner.
    
    @EventHandler
    private void onGameEnd(MatchEndEvent event) {
        setSidebar(event.getMatch(), ScoreboardPresets.getWinScoreboard(event.getMatch()));
    }
    
    @EventHandler
    private void onReset(MatchResetEvent event) {
        for (PlayerState state : event.getPlayers()) {
            ScoreboardManager.setScoreboard(
                state.getPlayer(),
                ScoreboardPresets.getLobbyScoreboard()
            );
        }
    }
    
}
