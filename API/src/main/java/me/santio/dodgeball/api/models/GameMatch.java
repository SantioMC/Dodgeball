package me.santio.dodgeball.api.models;

import lombok.*;
import lombok.experimental.FieldDefaults;
import me.santio.dodgeball.api.DodgeballAPI;
import me.santio.dodgeball.api.MatchMaker;
import me.santio.dodgeball.api.TeamOrganizer;
import me.santio.dodgeball.api.events.impl.MatchCountdownStartEvent;
import me.santio.dodgeball.api.events.impl.MatchEndEvent;
import me.santio.dodgeball.api.events.impl.MatchResetEvent;
import me.santio.dodgeball.api.events.impl.MatchStartEvent;
import me.santio.dodgeball.api.powerups.Powerup;
import me.santio.dodgeball.api.tasks.MatchCountdown;
import me.santio.dodgeball.api.tasks.RandomDelayTask;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a running match of dodgeball.
 */
@FieldDefaults(level= AccessLevel.PRIVATE)
@Setter
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class GameMatch {

    @Getter(AccessLevel.NONE)
    private final Random random = new Random();
    
    private RandomDelayTask powerupSpawner;
    
    /**
     * The game this match is running under.
     */
    final Game game;
    
    /**
     * All players that are currently in this match.
     */
    final List<PlayerState> players = new ArrayList<>();
    
    /**
     * The current state of the match.
     */
    State state = State.WAITING;
    
    /**
     * Gets the team that won the match, only available after the match has ended.
     */
    Team winners = Team.NONE;
    
    /**
     * Gets all players on a specific team.
     * @apiNote You can use Team.DEAD to get all players that are dead.
     * @param team The team to get players from.
     * @return A list of players on the requested team.
     */
    public List<PlayerState> getPlayers(Team team) {
        List<PlayerState> teamPlayers = new ArrayList<>();
        
        for (PlayerState player : this.players) {
            if (player.getTeam() == team) {
                teamPlayers.add(player);
            }
        }
        
        return teamPlayers;
    }
    
    /**
     * Gets a string of player names on a specific team.
     * @param team The team to get players from.
     * @return A string with all names concatenated together.
     */
    public String getPlayerNames(Team team) {
        StringBuilder builder = new StringBuilder();
        
        for (PlayerState player : this.getPlayers(team)) {
            builder.append(player.getPlayer().getName()).append(", ");
        }
        
        String result = builder.toString();
        return result.length() < 2 ? "" : result.substring(0, result.length() - 2);
    }
    
    /**
     * Get a list of all players on a specific team.
     * @param team The team to get players from.
     * @return A list of players on the requested team.
     */
    public List<Player> getBukkitPlayers(Team team) {
        List<Player> teamPlayers = new ArrayList<>();
        
        for (PlayerState player : this.players) {
            if (player.getTeam() == team) {
                teamPlayers.add(player.getPlayer());
            }
        }
        
        return teamPlayers;
    }
    
    /**
     * Find a specific player's state in this match.
     * @param player The player to find.
     * @return The player's state, or null if they are not in this match.
     */
    public PlayerState getPlayer(UUID player) {
        for (PlayerState state : this.players) {
            if (state.getUniqueId().equals(player)) {
                return state;
            }
        }
        
        return null;
    }
    
    /**
     * Sends a broadcast to anyone currently in the match.
     * @param message The message to send.
     */
    public void broadcast(String message) {
        for (PlayerState player : this.players) {
            player.getPlayer().sendMessage(
                ChatColor.translateAlternateColorCodes(
                    '&',
                    message
                )
            );
        }
    }
    
    /**
     * Sends a broadcast to a specific team currently in the match.
     * @param message The message to send.
     */
    public void broadcast(Team team, String message) {
        for (PlayerState player : getPlayers(team)) {
            player.getPlayer().sendMessage(
                ChatColor.translateAlternateColorCodes(
                    '&',
                    message
                )
            );
        }
    }
    
    /**
     * Eliminate a player from the match.
     * @param player The player to eliminate.
     * @param reason The reason the player was eliminated (or null if no reason).
     * @return Whether the player was eliminated.
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean eliminate(UUID player, @Nullable EliminationReason reason) {
        PlayerState state = this.getPlayer(player);
        if (state == null || state.getTeam() == Team.DEAD) return false;
        
        reason = reason == null ? EliminationReason.UNKNOWN : reason;
        PlayerState attacker = MatchMaker.getPlayerState(state.getAttacker());
        String attackerName = attacker == null ? "Unknown" : attacker.getPlayer().getDisplayName();
        
        broadcast(
            "§4☠ §7" + reason.getMessage()
                .replace("{victim}", state.getPlayer().getDisplayName() + "§7")
                .replace("{attacker}", attackerName + "§7")
        );
        
        state.setTeam(Team.DEAD);
        state.getPlayer().setGameMode(GameMode.SPECTATOR);
        
        // Check if that marks the end of the game
        if (this.getPlayers(Team.RED).isEmpty()) {
            this.win(Team.BLUE);
        } else if (this.getPlayers(Team.BLUE).isEmpty()) {
            this.win(Team.RED);
        }
        
        return true;
    }
    
    /**
     * Ends the match and resets the players back to their original state.
     */
    public void stop() {
        powerupSpawner.stop();
        
        Bukkit.getPluginManager().callEvent(new MatchResetEvent(this, this.players));
        new ArrayList<>(this.players).forEach(PlayerState::reset);
        
        this.game.getMatches().remove(this);
        
        World world = this.getGame().getBlueSpawn().getWorld();
        assert world != null;
        
        // Delete all items, while doing it this way isn't ideal, with the way we have the demo
        // server setup, it should be more than optimal for the sake of showcasing.
        for (Entity item : world.getEntitiesByClasses(Item.class)) {
            if (getGame().isInBounds(item.getLocation())) item.remove();
        }
    }
    
    /**
     * Start the match, this will begin the countdown and teleport players to the arena.
     */
    public void start() {
        setState(State.STARTING);
        
        // Put players in their teams
        TeamOrganizer.organize(getPlayers(), Arrays.asList(Team.RED, Team.BLUE));
        for (PlayerState player : this.players) {
            player.getPlayer().sendMessage(
                player.getTeam().getColor() + "You were placed on the " + player.getTeam().name() + " team!"
            );
            
            player.getPlayer().teleport(this.getSpawn(player.getTeam()));
        }
        
        // Emit our bukkit event
        Bukkit.getPluginManager().callEvent(new MatchCountdownStartEvent(this));
        
        // Start countdown
        new MatchCountdown()
            .match(this)
            .seconds(30)
            .onComplete(this::startMatch)
            .start();
    }
    
    /**
     * Cancels the match start and puts players back in the game lobby.
     */
    public void cancelStart() {
        setState(State.WAITING);
        
        for (PlayerState player : this.players) {
            player.getPlayer().teleport(this.getGame().getLobbySpawn());
            player.getPlayer().sendMessage("§cThe match start was cancelled!");
        }
    }
    
    /**
     * Starts the match, this will actually start the match itself rather than the countdown.
     * If you want to start the countdown, use {@link #start()} instead.
     */
    private void startMatch() {
        setState(State.RUNNING);
        
        // Emit our bukkit event
        Bukkit.getPluginManager().callEvent(new MatchStartEvent(this));
        
        // Start the match
        for (PlayerState state : this.players) {
            Player player = state.getPlayer();
            
            player.teleport(getSpawn(state.getTeam()));
            player.sendMessage("§aThe match has started!");
            player.getInventory().addItem(new ItemStack(Material.SNOWBALL, 1));
        }
        
        // Start spawning powerups
        powerupSpawner = new RandomDelayTask()
            .min(100)
            .max(600)
            .onComplete(() -> {
                Powerup powerup = DodgeballAPI.getPowerups().get(
                    random.nextInt(DodgeballAPI.getPowerups().size())
                );
                
                // Get a random location
                Location loc = getGame().getRandomPowerupLocation();
                if (loc == null) return;
                
                // Spawn the powerup
                powerup.drop(loc);
                powerup.broadcast(this, "A powerup has spawned!");
            })
            .start();
    }
    
    /**
     * Mark the game as finished and select a winner
     * @param team The winning team
     */
    public void win(Team team) {
        setState(State.FINISHED);
        setWinners(team);
        
        // Emit our bukkit event
        Bukkit.getPluginManager().callEvent(new MatchEndEvent(this, team));
        
        // Announce the winner
        broadcast(
            "§6✯ §7The " + team.getColor() + team.name() + " §7team won the match!"
        );
        
        DodgeballAPI.executeWinCommands(this);
        
        // Stop the match
        powerupSpawner.stop();
        Bukkit.getScheduler().runTaskLater(DodgeballAPI.getInstance(), this::stop, 100);
    }
    
    /**
     * Check if the match is full.
     * @return Whether the match is full.
     */
    public boolean isFull() {
        return this.players.size() >= this.game.getMaxPlayers();
    }
    
    /**
     * Check if the match is accepting new players.
     * @return Whether the match is accepting new players.
     */
    public boolean isAcceptingPlayers() {
        return (this.state == State.STARTING || this.state == State.WAITING) && !this.isFull();
    }
    
    /**
     * Gets the appropriate spawn point for a team.
     * @param team The team to get the spawn point for.
     * @return The spawn point for the team.
     */
    public Location getSpawn(Team team) {
        return team == Team.RED ? this.game.getRedSpawn() : this.game.getBlueSpawn();
    }
    
    /**
     * Drops a snowball at the closest spawn point, or random if location is null
     * @param location The location to drop the snowball at.
     */
    public void dropSnowball(@Nullable Location location) {
        if (location != null && location.getWorld() == null) return;
        
        Team team;
        if (location == null) {
            team = random.nextBoolean() ? Team.RED : Team.BLUE;
        } else {
            team = location.distanceSquared(this.game.getRedSpawn()) < location.distanceSquared(this.game.getBlueSpawn())
                ? Team.RED
                : Team.BLUE;
        }
        
        dropSnowball(team);
    }
    
    /**
     * Drop a snowball at a specific team's spawn point
     * @param team The team to drop the snowball at.
     */
    public void dropSnowball(Team team) {
        World world = getSpawn(team).getWorld();
        if (world == null) return;
        
        world.spawnParticle(
            Particle.REDSTONE,
            getSpawn(team),
            50,
            0.1,
            0.1,
            0.1,
            0,
            new Particle.DustOptions(
                Color.WHITE,
                1
            )
        );
        
        Item item = world.dropItemNaturally(
            getSpawn(team),
            new ItemStack(Material.SNOWBALL, 1)
        );
    }
    
    public enum State {
        WAITING,
        STARTING,
        RUNNING,
        FINISHED
    }
    
}
