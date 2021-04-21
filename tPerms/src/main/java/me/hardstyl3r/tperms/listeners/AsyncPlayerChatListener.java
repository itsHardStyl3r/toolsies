package me.hardstyl3r.tperms.listeners;

import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.tperms.TPerms;
import me.hardstyl3r.tperms.managers.PermissibleUserManager;
import me.hardstyl3r.tperms.objects.PermissibleUser;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChatListener implements Listener {

    private final PermissibleUserManager permissibleUserManager;
    private final LocaleManager localeManager;

    public AsyncPlayerChatListener(TPerms plugin, PermissibleUserManager permissibleUserManager, LocaleManager localeManager) {
        this.permissibleUserManager = permissibleUserManager;
        this.localeManager = localeManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        PermissibleUser u = permissibleUserManager.getUser(e.getPlayer());
        if (e.getPlayer().hasPermission("toolsies.chat.color")) {
            e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));
        }
        String format = localeManager.getConfig().getString("groups.format." + u.getMainGroup().getName());
        if (e.getPlayer().isOp()) {
            format = format.replace("<name>", localeManager.getConfig().getString("groups.op-prefix"));
        }
        if (format == null) {
            e.setFormat("[" + u.getMainGroup().getName() + "] " + e.getFormat());
        } else {
            e.setFormat(ChatColor.translateAlternateColorCodes('&', format)
                    .replace("<name>", "%1$s")
                    .replace("<message>", "%2$s"));
        }
    }
}
