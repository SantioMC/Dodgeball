package me.santio.dodgeball.api.events;

import lombok.NonNull;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * A simple template for creating bukkit events, so that we don't have to
 * create a new HandlerList every time.
 */
public class BukkitEvent extends Event {
    public static final HandlerList handlers = new HandlerList();
    
    @Override
    @NonNull
    public HandlerList getHandlers() {
        return handlers;
    }
    
    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
