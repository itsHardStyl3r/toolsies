package me.hardstyl3r.toolsies.listeners;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.User;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChatListener implements Listener {

    private final UserManager userManager;
    private final LocaleManager localeManager;

    public AsyncPlayerChatListener(Toolsies plugin, UserManager userManager, LocaleManager localeManager) {
        this.userManager = userManager;
        this.localeManager = localeManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        User u = userManager.getUser(e.getPlayer());
        if (e.getPlayer().hasPermission("toolsies.chat.color")) {
            e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));
        }
        String format = localeManager.getConfig().getString("groups.format." + u.getMainGroup());
        if (e.getPlayer().isOp()) {
            format = format.replace("<name>", localeManager.getConfig().getString("groups.op-prefix"));
        }
        if (format == null) {
            e.setFormat("[" + u.getMainGroup() + "] " + e.getFormat());
        } else {
            e.setFormat(ChatColor.translateAlternateColorCodes('&', format)
                    .replace("<name>", "%1$s")
                    .replace("<message>", "%2$s"));
        }
    }
}
