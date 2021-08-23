package me.hardstyl3r.tbans.listeners.own;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.events.PlayerPunishedEvent;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerPunishedListener implements Listener {

    private final UserManager userManager;
    private final LocaleManager localeManager;

    public PlayerPunishedListener(TBans plugin, UserManager userManager, LocaleManager localeManager) {
        this.userManager = userManager;
        this.localeManager = localeManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPunish(PlayerPunishedEvent e) {
        if (!e.isCancelled()) {
            if (e.getPunishment().getType().equals(PunishmentType.BAN)) {
                Punishment ban = e.getPunishment();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Locale l = userManager.determineLocale(p);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString(ban.getDuration() != null ? "tempban.tempban_broadcast" : "ban.ban_broadcast"))
                            .replace("<name>", ban.getName())
                            .replace("<admin>", e.getSender())
                            .replace("<duration>", localeManager.parseTimeWithTranslate(ban.getRemaining(), l)));
                }
            }
        }
    }
}
