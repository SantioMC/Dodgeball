package me.santio.dodgeball.commands;

import me.santio.dodgeball.api.DodgeballAPI;
import me.santio.dodgeball.api.MatchMaker;
import me.santio.dodgeball.api.models.Game;
import me.santio.dodgeball.api.models.GameMatch;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A super simple command for handling games, no reason to use a command framework for this.
 */
public class GameCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§cUsage: /game <list|join|leave" +
                (sender.hasPermission("dodgeball.admin") ? "|start>" : ">")
            );
            
            return true;
        }
        
        else if (!args[0].equalsIgnoreCase("list") && !(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        
        else if (args[0].equalsIgnoreCase("list")) {
            for (Game game : DodgeballAPI.getGames()) {
                sender.sendMessage("§7- §e" + game.getName() + "§7: Running §e" + game.getMatches().size() + "§7 matches.");
            }
            
            if (DodgeballAPI.getGames().isEmpty())
                sender.sendMessage("§cThere are no games currently running.");
            
            return true;
        }
        
        else if (args[0].equalsIgnoreCase("join")) {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /game join <game>");
                return true;
            }
            
            if (MatchMaker.getPlayerState(((Player) sender).getUniqueId()) != null) {
                sender.sendMessage("§cYou are already in a game.");
                return true;
            }
            
            Game game = DodgeballAPI.getGame(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
            if (game == null) {
                sender.sendMessage("§cThere is no game with that name.");
                return true;
            }
            
            boolean success = MatchMaker.addPlayer(((Player) sender).getUniqueId(), game);
            if (!success) sender.sendMessage("§cThere are no matches available for that game.");
        }
        
        else if (args[0].equalsIgnoreCase("start") && sender.hasPermission("dodgeball.admin")) {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /game start <game>");
                return true;
            }
            
            String gameName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            Game game = DodgeballAPI.getGame(gameName);
            
            if (game == null) {
                sender.sendMessage("§cThere is no game with the name §e" + gameName + "§c.");
                return true;
            }
            
            int started = 0;
            for (GameMatch match : game.getMatches()) {
                if (match.getState() == GameMatch.State.WAITING) {
                    match.start();
                    started++;
                }
            }
            
            sender.sendMessage("§7Started §e" + started + "§7 matches for game §e" + gameName + "§7.");
        }
        
        else if (args[0].equalsIgnoreCase("leave")) {
            MatchMaker.removePlayer(((Player) sender).getUniqueId());
        }
        
        else {
            sender.sendMessage("§cUnknown sub-command, use §n/game help§c for help.");
        }
        
        return true;
    }
    
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> suggestions;
        
        // game <arg>
        if (args.length == 1) suggestions = sender.hasPermission("dodgeball.admin")
            ? Arrays.asList("list", "join", "leave", "start")
            : Arrays.asList("list", "join", "leave");
        
        // game ... <arg>
        else if (args.length == 2) {
            
            // game join <arg>
            if (args[0].equals("join")) {
                suggestions = DodgeballAPI.getGameNames();
                // game start <arg> (admin only)
            } else if (args[0].equalsIgnoreCase("start") && sender.hasPermission("dodgeball.admin")) {
                suggestions = DodgeballAPI.getGameNames();
                // game <arg>
            } else suggestions = Collections.emptyList();
            
            // everything else
        } else return Collections.emptyList();
        
        // Filter the suggestions based on what the player has typed so far.
        int lastArg = args.length - 1;
        return suggestions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[lastArg].toLowerCase()))
            .toList();
    }
    
}
