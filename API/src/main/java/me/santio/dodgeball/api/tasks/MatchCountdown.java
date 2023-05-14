package me.santio.dodgeball.api.tasks;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.santio.dodgeball.api.DodgeballAPI;
import me.santio.dodgeball.api.events.impl.MatchStartCancelledEvent;
import me.santio.dodgeball.api.models.GameMatch;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

@SuppressWarnings("UnusedReturnValue")
@Getter
@Setter
@Accessors(chain = true, fluent = true)
public class MatchCountdown extends BukkitRunnable {
    private int seconds = 30;
    private BukkitTask task;
    private GameMatch match;
    private Runnable onComplete;
    
    public MatchCountdown build() {
        if (task == null) throw new NullPointerException("Task cannot be null");
        if (match == null) throw new NullPointerException("Match cannot be null");
        
        return this;
    }
    
    public BukkitRunnable start() {
        if (match == null) throw new NullPointerException("Match cannot be null");
        
        task = runTaskTimerAsynchronously(DodgeballAPI.getInstance(), 0L, 20L);
        return this;
    }
    
    @Override
    public void run() {
        if (match.getState() != GameMatch.State.STARTING) {
            Bukkit.getScheduler().runTask(DodgeballAPI.getInstance(), () -> {
                Bukkit.getPluginManager().callEvent(new MatchStartCancelledEvent(match));
            });
            
            task.cancel();
            return;
        }
        
        if (seconds == 0) {
            task.cancel();
            if (onComplete != null) {
                Bukkit.getScheduler().runTask(DodgeballAPI.getInstance(), () -> onComplete.run());
            }
            return;
        }
        
        match.getPlayers().forEach(state -> {
            Player player = state.getPlayer();
            if (player == null) return;
            
            if (seconds % 5 == 0 || seconds < 5) {
                player.sendMessage("§7The match will start in §e" + seconds + " second" + (seconds == 1 ? "" : "s") + "§7!");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
            }
        });
        
        seconds--;
    }
}
