package me.santio.dodgeball.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.clip.placeholderapi.PlaceholderAPI;
import me.santio.dodgeball.api.models.Game;
import me.santio.dodgeball.api.models.GameMatch;
import me.santio.dodgeball.api.models.Team;
import me.santio.dodgeball.api.powerups.Powerup;
import me.santio.dodgeball.api.powerups.impl.BlindnessPowerup;
import me.santio.dodgeball.api.powerups.impl.SnowballRainPowerup;
import me.santio.dodgeball.api.powerups.impl.SnowballsPowerup;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DodgeballAPI {
    private DodgeballAPI() { /* Prevent instantiation */ }
    
    @Getter
    private static JavaPlugin instance = null;
    
    @Setter
    @Getter
    private static List<String> winCommands = new ArrayList<>();
    
    @Setter
    @Getter
    private static boolean usePlaceholderAPI = false;
    
    @Getter
    private static final List<Powerup> powerups = Arrays.asList(
        new BlindnessPowerup(),
        new SnowballsPowerup(),
        new SnowballRainPowerup()
    );
    
    /**
     * Sets the plugin instance, this is required for the API to work. The main
     * Dodgeball plugin will set this automatically. Setting it twice will throw
     * an exception.
     *
     * @param instance The plugin instance.
     * @throws IllegalStateException If the instance is already set.
     */
    public static void setInstance(JavaPlugin instance) throws IllegalStateException {
        if (DodgeballAPI.instance != null) throw new IllegalStateException("Instance is already set");
        DodgeballAPI.instance = instance;
    }
    
    /**
     * A list of all games that are currently registered.
     */
    @Getter
    private static final List<Game> games = new ArrayList<>();
    
    /**
     * The spawn location for all players, this isn't a per-game lobby spawn.
     */
    @Getter
    @Setter
    private static Location spawn;
    
    /**
     * Creates a new game builder, this lets you specify the options for the
     * game.
     * @return A new game builder.
     */
    public static GameBuilder createGame() {
        return new GameBuilder();
    }
    
    /**
     * Gets a list of all the game names
     * @return A list of all the game names
     */
    public static List<String> getGameNames() {
        List<String> names = new ArrayList<>();
        
        for (Game game : games) {
            names.add(game.getName());
        }
        
        return names;
    }
    
    /**
     * Get a game based on its name
     * @param name The name of the game
     * @return The game
     */
    public static Game getGame(String name) {
        for (Game game : games) {
            if (game.getName().equals(name)) {
                return game;
            }
        }
        
        return null;
    }
    
    /**
     * Parses the placeholders in a win command
     * @param match The match that has finished
     * @param command The command to parse
     * @return The parsed command
     */
    private static String parsePlaceholders(String command, GameMatch match) {
        Team winners = match.getWinners();
        Team losers = winners == Team.RED ? Team.BLUE : Team.RED;
        
        return command
            .replace("%winners%",  match.getPlayerNames(winners))
            .replace("%losers%", match.getPlayerNames(losers))
            .replace("%team%", winners.getColor() + winners.name());
    }
    
    /**
     * Executes the commands for a match win
     * @param match The match that has finished
     */
    public static void executeWinCommands(GameMatch match) {
        Team winners = match.getWinners();
        Team losers = winners == Team.RED ? Team.BLUE : Team.RED;
        
        for (String command : winCommands) {
            String parsed = parsePlaceholders(command, match);
            if (usePlaceholderAPI) parsed = PlaceholderAPI.setPlaceholders(null, parsed);
            
            if (command.contains("%winner%"))
                for (Player winner : match.getBukkitPlayers(winners))
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed.replace("%winner%", winner.getName()));
            
            if (command.contains("%loser%"))
                for (Player loser : match.getBukkitPlayers(losers))
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed.replace("%loser%", loser.getName()));
            
            if (!command.contains("%winner%") && !command.contains("%loser%"))
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }
    }
    
    /**
     * Gets a powerup based on it's name
     */
    public static Powerup getPowerup(String name) {
        for (Powerup powerup : powerups) {
            if (powerup.name().equals(name)) {
                return powerup;
            }
        }
        
        return null;
    }
    
    /**
     * A builder for creating a new game, from the built game you will be able
     * to create new matches in the same game at the same location.
     */
    @NoArgsConstructor
    @Getter
    @Setter
    @Accessors(fluent = true)
    public static class GameBuilder {
        private String name;
        private int maxPlayers;
        
        private Location lobbySpawn;
        private Location redSpawn;
        private Location blueSpawn;
        
        private Location leftBound;
        private Location rightBound;
        private int dropHeight;
        
        /**
         * Builds the match and automatically loads it into the matchmaker.
         * @return The built match.
         */
        @SuppressWarnings("UnusedReturnValue")
        public Game build() {
            Game game = new Game(
                name,
                maxPlayers,
                redSpawn,
                blueSpawn,
                lobbySpawn,
                leftBound,
                rightBound,
                dropHeight
            );
            
            DodgeballAPI.getGames().add(game);
            return game;
        }
    }
}
