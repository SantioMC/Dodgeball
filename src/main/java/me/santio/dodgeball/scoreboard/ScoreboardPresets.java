package me.santio.dodgeball.scoreboard;

import me.santio.dodgeball.api.models.GameMatch;
import me.santio.dodgeball.api.models.Team;

public final class ScoreboardPresets {
    private ScoreboardPresets() { /* Prevent instantiation */ }
    
    public static String[] getGameLobbyScoreboard(GameMatch match) {
        return new String[] {
            "§1§7§m                          §r",
            "§7Players: §e" + match.getPlayers().size() + "/" + match.getGame().getMaxPlayers(),
            "§7Map: §e" + match.getGame().getName(),
            "§2§7§m                          §r",
            "§7Waiting for players..."
        };
    }
    
    public static String[] getGameScoreboard(GameMatch match) {
        return new String[] {
            "§1§7§m                          §r",
            "§7Players: §e" + match.getPlayers().size() + "/" + match.getGame().getMaxPlayers(),
            "§7Map: §e" + match.getGame().getName(),
            "§2§7§m                          §r",
            "§cRed: §4" + match.getPlayers(Team.RED).size(),
            "§bBlue: §9" + match.getPlayers(Team.BLUE).size(),
            "§3§7§m                          §r"
        };
    }
    
    public static String[] getWinScoreboard(GameMatch match) {
        return new String[] {
            "§1§7§m                          §r",
            "§7Players: §e" + match.getPlayers().size() + "/" + match.getGame().getMaxPlayers(),
            "§7Map: §e" + match.getGame().getName(),
            "§2§7§m                          §r",
            "§7Winner: §e" + match.getWinners().getColor() + match.getWinners().name(),
            "§3§7§m                          §r"
        };
    }
    
    public static String[] getLobbyScoreboard() {
        return new String[] {
            "§1",
            "§7Use §e§n/game join§7 to join",
            "§7a game!",
            "§2"
        };
    }
    
}
