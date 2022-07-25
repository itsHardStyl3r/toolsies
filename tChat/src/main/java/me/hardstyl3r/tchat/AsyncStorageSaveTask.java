package me.hardstyl3r.tchat;

import me.hardstyl3r.tchat.managers.MessagingManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

public class AsyncStorageSaveTask implements Runnable {

    private final MessagingManager messagingManager;

    public AsyncStorageSaveTask(TChat plugin, MessagingManager messagingManager, YamlConfiguration config) {
        this.messagingManager = messagingManager;
        long period = config.getInt("saveToFilePeriod", 1200) * 20L;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, period, period);
    }

    @Override
    public void run() {
        messagingManager.saveData();
    }
}
