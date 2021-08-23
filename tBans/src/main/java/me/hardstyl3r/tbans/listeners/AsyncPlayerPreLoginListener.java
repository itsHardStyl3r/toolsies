package me.hardstyl3r.tbans.listeners;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class AsyncPlayerPreLoginListener implements Listener {

    public PunishmentManager punishmentManager;
    public LocaleManager localeManager;
    public UserManager userManager;

    public AsyncPlayerPreLoginListener(TBans plugin, PunishmentManager punishmentManager, LocaleManager localeManager, UserManager userManager) {
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
        this.userManager = userManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent e) {
        Locale l = userManager.determineLocale(e.getUniqueId());
        if (e.getAddress() != null) {
            punishmentManager.deleteIfExpired(e.getAddress());
            if (punishmentManager.isBanned(e.getAddress())) {
                Punishment ban = punishmentManager.getBan(e.getAddress());
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, punishmentManager.formatMessage(ban, l, "join"));
                return;
            }
        } else {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, l.getConfig().getString("ban.unresolved_hostname"));
            return;
        }
        punishmentManager.deleteIfExpired(PunishmentType.BAN, e.getName());
        punishmentManager.deleteIfExpired(PunishmentType.BAN, e.getUniqueId());
        if (punishmentManager.isPunished(PunishmentType.BAN, e.getUniqueId())) {
            Punishment ban = punishmentManager.getPunishment(PunishmentType.BAN, e.getUniqueId());
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, punishmentManager.formatMessage(ban, l, "join"));
        } else if (punishmentManager.isPunished(PunishmentType.BAN, e.getName())) {
            Punishment ban = punishmentManager.getPunishment(PunishmentType.BAN, e.getName());
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, punishmentManager.formatMessage(ban, l, "join"));
        }
    }
}
