package me.santio.dodgeball.api.powerups;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.santio.dodgeball.api.DodgeballAPI;
import me.santio.dodgeball.api.models.GameMatch;
import me.santio.dodgeball.api.models.PlayerState;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.function.Consumer;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
public abstract class Powerup {
    
    private final PotionEffect glowing = new PotionEffect(
        PotionEffectType.GLOWING,
        1000000,
        1,
        false,
        false
    );
    
    private final String name;
    private final Material material;
    
    private ChatColor color = ChatColor.AQUA;
    private Consumer<PlayerState> onPickup;
    
    public Powerup(String name, Material material) {
        this.name = name;
        this.material = material;
    }
    
    public ItemStack createItem() {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        
        if (meta == null) return item;
        
        meta.setDisplayName(color + name + " Powerup");
        item.setItemMeta(meta);
        
        return item;
    }
    
    public Item drop(Location loc) {
        if (loc.getWorld() == null) return null;
        
        Item item = loc.getWorld().dropItemNaturally(loc, createItem());
        item.setPickupDelay(0);
        item.setGlowing(true);
        item.setCustomName(color + name + " Powerup");
        item.setCustomNameVisible(true);
        item.setMetadata(
            "powerup",
            new FixedMetadataValue(
                DodgeballAPI.getInstance(),
                name
            )
        );
        
        return item;
    }

    public void broadcast(GameMatch match, String message) {
        match.broadcast("§b⚡ §7" + message);
    }
    
}
