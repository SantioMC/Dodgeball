package me.santio.dodgeball.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.santio.dodgeball.api.DodgeballAPI;
import me.santio.dodgeball.api.MatchMaker;
import me.santio.dodgeball.api.models.Game;
import me.santio.dodgeball.api.models.GameMatch;
import me.santio.dodgeball.api.models.PlayerState;
import me.santio.dodgeball.api.models.Team;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DodgeballHook extends PlaceholderExpansion {
    
    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        PlayerState state = MatchMaker.getPlayerState(player.getUniqueId());
        
        if (identifier.equals("team")) {
            if (state == null) return null;
            return state.getTeam().name();
        }
        
        if (identifier.startsWith("game_")) {
            String[] split = identifier.split("_");
            if (split.length < 3) return null;
            
            Game game = DodgeballAPI.getGame(split[1]);
            String option = split[2];
            
            if (game == null) return null;
            
            if (option.equals("team")) {
                if (split.length < 4) return null;
                Team team = Team.fromName(split[3]);
                if (team == null) return null;
                
                GameMatch match = game.getMatches().get(0);
                if (match == null) return null;
                
                return String.valueOf(match.getPlayers(team).size());
            }
        }
        
        return null;
    }
    
    
    @Override
    public @NotNull String getIdentifier() {
        return "dodgeball";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return "Santio71";
    }
    
    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }
}
