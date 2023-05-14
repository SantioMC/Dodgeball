package me.santio.dodgeball.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

/**
 * A small collection of utilities for reading and writing to the config file.
 */
public final class ConfigUtils {
    private ConfigUtils() { /* Prevent instantiation */ }
    
    public static Location readAsLocation(ConfigurationSection config, String path) {
        String value = config.getString(path);
        if (value == null) return null;
        
        String[] data = value.split(";");
        if (data.length < 4) return null;
        
        World world = Bukkit.getWorld(data[0]);
        if (world == null) return null;
        
        double x, y, z;
        float yaw = 0, pitch = 0;
        
        try {
            x = Double.parseDouble(data[1]);
            y = Double.parseDouble(data[2]);
            z = Double.parseDouble(data[3]);
            
            if (data.length > 4)
                yaw = Float.parseFloat(data[4]);
            
            if (data.length > 5)
                pitch = Float.parseFloat(data[5]);
        } catch (NumberFormatException e) {
            return null;
        }
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
}
