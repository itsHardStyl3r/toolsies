package me.hardstyl3r.tbans.listeners;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class AsyncPlayerChatListener implements Listener {

    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;
    private final UserManager userManager;

    public AsyncPlayerChatListener(TBans plugin, PunishmentManager punishmentManager, LocaleManager localeManager, UserManager userManager) {
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
        this.userManager = userManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatMute(AsyncPlayerChatEvent e) {
        Player target = e.getPlayer();
        UUID uuid = target.getUniqueId();
        if (punishmentManager.isPunished(PunishmentType.MUTE, uuid)) {
            Punishment mute = punishmentManager.getPunishment(PunishmentType.MUTE, uuid);
            if (!punishmentManager.deleteIfExpired(mute)) {
                Locale l = userManager.determineLocale(target);
                target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString(mute.getDuration() != null ? "tempmute.chat_muted" : "mute.chat_muted"))
                        .replace("<duration>", localeManager.parseTimeWithTranslate(mute.getRemaining(), l)));
                e.setCancelled(true);
            }
        }
    }
}
