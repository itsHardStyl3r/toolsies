package me.hardstyl3r.tbans;

import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import org.bukkit.Bukkit;

import java.util.Set;

public class AsyncCleanupTask implements Runnable {

    private final PunishmentManager punishmentManager;

    /**
     * An asynchronous task to remove expired punishments from memory.
     *
     * @param period Time in seconds
     */
    public AsyncCleanupTask(TBans plugin, PunishmentManager punishmentManager, int period) {
        this.punishmentManager = punishmentManager;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, period * 20L, period * 20L);
    }

    @Override
    public void run() {
        Set<Punishment> punishments = punishmentManager.getPunishments();
        if (punishments.isEmpty()) return;
        for (Punishment punishment : punishments) {
            if (punishment.isExpired()) punishmentManager.deletePunishment(punishment);
        }
    }
}