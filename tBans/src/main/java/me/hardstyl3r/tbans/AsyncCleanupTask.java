package me.hardstyl3r.tbans;

import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.utils.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class AsyncCleanupTask implements Runnable {

    private final PunishmentManager punishmentManager;

    public AsyncCleanupTask(TBans plugin, PunishmentManager punishmentManager, FileConfiguration config) {
        this.punishmentManager = punishmentManager;
        long period = config.getInt("cleanupTaskTimer") * 20L;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, period, period);
    }

    @Override
    public void run() {
        if (punishmentManager.getPunishments() == null) {
            return;
        }
        for (Punishment punishment : punishmentManager.getPunishments()) {
            if (punishment != null) {
                if (punishmentManager.deleteIfExpired(punishment)) {
                    LogUtil.info("[tBans] AsyncCleanupTask(): " + punishment.getType() + "(" + punishment.getName() + ") expired.");
                }
            }
        }
    }
}
