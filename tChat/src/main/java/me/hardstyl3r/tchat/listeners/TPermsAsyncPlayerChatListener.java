package me.hardstyl3r.tchat.listeners;

import me.hardstyl3r.tchat.TChat;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.tperms.managers.PermissibleUserManager;
import me.hardstyl3r.tperms.objects.Group;
import me.hardstyl3r.tperms.objects.PermissibleUser;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class TPermsAsyncPlayerChatListener implements Listener {

    private final PermissibleUserManager permissibleUserManager;
    private final LocaleManager localeManager;

    public TPermsAsyncPlayerChatListener(TChat plugin, PermissibleUserManager permissibleUserManager, LocaleManager localeManager) {
        this.permissibleUserManager = permissibleUserManager;
        this.localeManager = localeManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent e) {
        PermissibleUser u = permissibleUserManager.getUser(e.getPlayer());
        Group g = u.getMainGroup();
        String format = localeManager.getConfig().getString("groups.format." + g.getName());
        if (format == null) return;
        if (e.getPlayer().isOp())
            format = format.replace("<name>", localeManager.getConfig().getString("groups.op-prefix"));
        e.setFormat(ChatColor.translateAlternateColorCodes('&', format)
                .replace("<name>", "%1$s")
                .replace("<message>", "%2$s"));
    }
}
