package me.santio.dodgeball.api.models;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a playable game, you may think of this as an arena, which can have multiple
 * matches running at once in.
 */
@FieldDefaults(level= AccessLevel.PRIVATE)
@Setter
@Getter
@RequiredArgsConstructor
public class Game {
    private static final Random random = new Random();
    
    // Game Details
    final String name;
    final int maxPlayers;
    
    // Spawn Details
    final Location redSpawn;
    final Location blueSpawn;
    final Location lobbySpawn;
    
    // Powerup Details
    final Location leftBound;
    final Location rightBound;
    final int dropHeight;
    
    /**
     * A list of all matches that are currently running.
     */
    final List<GameMatch> matches = new ArrayList<>();
    
    /**
     * Creates a new match for this game.
     * @return The newly created match.
     */
    public GameMatch createMatch() {
        // In the future, if you wanted support for multiple matches running at once, you could
        // remove this line and add the functionality to get a match that is not full.
        if (!this.matches.isEmpty()) return null;
        
        GameMatch match = new GameMatch(this);
        this.matches.add(match);
        return match;
    }
    
    /**
     * Gets a random location within the bounds of the powerup area.
     * @return A bukkit location.
     */
    public Location getRandomPowerupLocation() {
        Location maxBound = new Location(
            leftBound.getWorld(),
            Math.max(leftBound.getX(), rightBound.getX()) - 5,
            dropHeight,
            Math.max(leftBound.getZ(), rightBound.getZ()) - 5
        );
        
        Location minBound = new Location(
            leftBound.getWorld(),
            Math.min(leftBound.getX(), rightBound.getX()) + 5,
            dropHeight,
            Math.min(leftBound.getZ(), rightBound.getZ()) + 5
        );
        
        int x = random.nextInt(maxBound.getBlockX() - minBound.getBlockX()) + minBound.getBlockX();
        int z = random.nextInt(maxBound.getBlockZ() - minBound.getBlockZ()) + minBound.getBlockZ();
        return new Location(leftBound.getWorld(), x, dropHeight, z);
    }
    
    /**
     * Checks if an item is within the bounds of the game.
     * @param location The location to check.
     * @return True if the location is within the bounds, false otherwise.
     */
    public boolean isInBounds(Location location) {
        double maxX = Math.max(leftBound.getX(), rightBound.getX());
        double minX = Math.min(leftBound.getX(), rightBound.getX());
        double maxZ = Math.max(leftBound.getZ(), rightBound.getZ());
        double minZ = Math.min(leftBound.getZ(), rightBound.getZ());
        
        return location.getX() <= maxX
            && location.getX() >= minX
            && location.getZ() <= maxZ
            && location.getZ() >= minZ;
    }
    
}
