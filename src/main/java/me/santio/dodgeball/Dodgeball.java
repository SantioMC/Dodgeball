package me.santio.dodgeball;

import me.santio.dodgeball.api.DodgeballAPI;
import me.santio.dodgeball.commands.GameCommand;
import me.santio.dodgeball.events.PlayerListener;
import me.santio.dodgeball.events.PowerupListener;
import me.santio.dodgeball.events.ScoreboardListener;
import me.santio.dodgeball.events.SnowballListener;
import me.santio.dodgeball.hooks.DodgeballHook;
import me.santio.dodgeball.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.plugin.java.JavaPlugin;

public class Dodgeball extends JavaPlugin {
    
    public static int lowestY = 0;
    public static DodgeballHook placeholderHook = null;
    
    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onEnable() {
        // Create our games, we'll load this from a configuration
        saveDefaultConfig();
        loadGames();
        Dodgeball.lowestY = getConfig().getInt("lowest_y");
        
        // Load and setup the dodgeball API
        DodgeballAPI.setInstance(this);
        DodgeballAPI.setSpawn(ConfigUtils.readAsLocation(getConfig(), "spawn"));
        DodgeballAPI.setWinCommands(getConfig().getStringList("win_commands"));
        
        // Register our events
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new SnowballListener(), this);
        getServer().getPluginManager().registerEvents(new ScoreboardListener(), this);
        getServer().getPluginManager().registerEvents(new PowerupListener(), this);
        
        // PlaceholderAPI Hook
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            if (new DodgeballHook().register()) {
                getLogger().info("PlaceholderAPI hook enabled.");
                DodgeballAPI.setUsePlaceholderAPI(true);
            } else getLogger().severe("Failed to register PlaceholderAPI hook.");
        }
        
        // Register our command
        GameCommand command = new GameCommand();
        getCommand("game").setExecutor(command);
        getCommand("game").setTabCompleter(command);
        
        // Kill any remaining items from previous sessions
        Bukkit.getWorlds()
            .forEach(world ->
                world.getEntitiesByClasses(Item.class)
                    .forEach(Entity::remove)
            );
    }
    
    @Override
    public void onDisable() {
        Bukkit.getWorlds()
            .forEach(world ->
                world.getEntitiesByClasses(Item.class)
                    .forEach(Entity::remove)
            );
    }
    
    public void loadGames() {
        ConfigurationSection games = getConfig().getConfigurationSection("games");
        if (games == null) {
            getLogger().severe("No games were found in the configuration file.");
            return;
        }
        
        for (String key : games.getKeys(false)) {
            ConfigurationSection game = games.getConfigurationSection(key);
            if (game == null) continue;
            
            DodgeballAPI.createGame()
                .name(game.getString("name"))
                .maxPlayers(game.getInt("max_players"))
                .lobbySpawn(ConfigUtils.readAsLocation(game, "lobby"))
                .redSpawn(ConfigUtils.readAsLocation(game, "red_spawn"))
                .blueSpawn(ConfigUtils.readAsLocation(game, "blue_spawn"))
                .leftBound(ConfigUtils.readAsLocation(game, "powerups.left_bound"))
                .rightBound(ConfigUtils.readAsLocation(game, "powerups.right_bound"))
                .dropHeight(game.getInt("powerups.drop_height"))
                .build();
        }
    }
    
    public static Dodgeball getInstance() {
        return getPlugin(Dodgeball.class);
    }
    
}