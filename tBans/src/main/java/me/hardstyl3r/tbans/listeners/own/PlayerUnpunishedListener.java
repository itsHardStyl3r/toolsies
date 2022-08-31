package me.hardstyl3r.tbans.listeners.own;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.events.PlayerUnpunishedEvent;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.UserManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
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
                    p.sendMessage(userManager.determineLocale(p).getStringComponent("unban.unban_broadcast", Placeholder.unparsed("name", ban.getName()), Placeholder.unparsed("admin", e.getSender())));
                }
            }
        }
    }
}
