package me.hardstyl3r.tbans.listeners.own;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.events.PlayerUnpunishedEvent;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerUnpunishedListener implements Listener {

    private final UserManager userManager;

    public PlayerUnpunishedListener(TBans plugin, UserManager userManager) {
        this.userManager = userManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPunish(PlayerUnpunishedEvent e) {
        if (!e.isCancelled()) {
            if (e.getPunishment().getType().equals(PunishmentType.BAN)) {
                Punishment ban = e.getPunishment();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    userManager.determineLocale(p).getString("unban.unban_broadcast"))
                            .replace("<name>", ban.getName())
                            .replace("<admin>", e.getSender()));
                }
            }
        }
    }
}
