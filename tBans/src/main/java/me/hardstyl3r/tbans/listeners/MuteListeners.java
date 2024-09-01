package me.hardstyl3r.tbans.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;

import static me.hardstyl3r.tbans.enums.PunishmentType.MUTE;

public class MuteListeners implements Listener {

    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;
    private final UserManager userManager;
    private final FileConfiguration config;

    public MuteListeners(TBans plugin, PunishmentManager punishmentManager, LocaleManager localeManager, UserManager userManager, FileConfiguration config) {
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
        this.userManager = userManager;
        this.config = config;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatMute(AsyncChatEvent e) {
        Player target = e.getPlayer();
        UUID uuid = target.getUniqueId();
        if (!punishmentManager.isPunished(uuid, MUTE)) return;
        Punishment mute = punishmentManager.getPunishment(uuid, MUTE);
        Locale l = userManager.determineLocale(target);
        target.sendMessage(l.getStringComponent("mute.muted_chat_attempt",
                Formatter.choice("choice", mute.getRemaining()),
                Placeholder.unparsed("remaining", localeManager.parseTimeWithTranslate(mute.getRemaining(), l))));
        e.setCancelled(true);
    }

    @EventHandler
    public void onCommandMute(PlayerCommandPreprocessEvent e) {
        Player target = e.getPlayer();
        UUID uuid = target.getUniqueId();
        if (!punishmentManager.isPunished(uuid, MUTE)) return;
        String cmd = Bukkit.getCommandMap().getCommand(e.getMessage().split(" ")[0].replace("/", "")).getName();
        if (!config.getStringList("muteDisabledCommands").contains(cmd)) return;
        Punishment mute = punishmentManager.getPunishment(uuid, MUTE);
        Locale l = userManager.determineLocale(target);
        target.sendMessage(l.getStringComponent("mute.muted_cmd_attempt",
                Formatter.choice("choice", mute.getRemaining()),
                Placeholder.unparsed("remaining", localeManager.parseTimeWithTranslate(mute.getRemaining(), l))));
        e.setCancelled(true);
    }
}
