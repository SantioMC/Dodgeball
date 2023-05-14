package me.santio.dodgeball.api.tasks;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.santio.dodgeball.api.DodgeballAPI;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
public class RandomDelayTask implements Runnable {
   
    private static final Random random = new Random();
    private BukkitTask task;
    
    private int min = 5;
    private int max = 20;
    private Runnable onComplete;
    
    public RandomDelayTask start() {
        task = Bukkit.getScheduler().runTaskLater(DodgeballAPI.getInstance(),
            this,
            random.nextInt(max - min) + min
        );
        return this;
    }
    
    public RandomDelayTask stop() {
        if (task != null) task.cancel();
        return this;
    }
    
    @Override
    public void run() {
        if (onComplete != null) onComplete.run();
        start();
    }
}
