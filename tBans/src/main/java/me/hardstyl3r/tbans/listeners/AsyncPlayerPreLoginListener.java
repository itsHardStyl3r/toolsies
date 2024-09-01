package me.hardstyl3r.tbans.listeners;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

import static me.hardstyl3r.tbans.enums.PunishmentType.BAN;
import static me.hardstyl3r.tbans.enums.PunishmentType.IP;

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
        Punishment ban = null;

        UUID uuid = e.getUniqueId();
        if (punishmentManager.isPunished(uuid, BAN)) ban = punishmentManager.getPunishment(uuid, BAN);
        String name = e.getName();
        if (punishmentManager.isPunished(name, BAN)) ban = punishmentManager.getPunishment(uuid, BAN);
        String address = e.getAddress().getHostAddress();
        if (punishmentManager.isPunished(address, IP)) ban = punishmentManager.getPunishment(address, IP);
        if (ban == null) return;

        Component message = l.getStringComponent("ban.messages." + (ban.getType() == IP ? "ip_" : "") + "join_header",
                Placeholder.unparsed("sender_name", ban.getSender()),
                Formatter.choice("choice", ban.getDuration()));
        if (ban.getReason() != null) message = message.append(l.getStringComponent("ban.messages.reason",
                Placeholder.unparsed("reason", ban.getReason())));
        message = message.append(l.getStringComponent("ban.messages.banned_on",
                Placeholder.unparsed("banned_on", localeManager.getFullDate(ban.getDate()))));
        if (ban.getDuration() > 1) message = message.append(l.getStringComponent("ban.messages.expires_after",
                Placeholder.unparsed("remaining", localeManager.parseTimeWithTranslate(ban.getRemaining(), l)),
                Placeholder.unparsed("remaining_date", localeManager.getFullDate(ban.getDuration()))));
        message = message.append(l.getStringComponent("ban.messages.footer"));
        e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message);
    }
}
