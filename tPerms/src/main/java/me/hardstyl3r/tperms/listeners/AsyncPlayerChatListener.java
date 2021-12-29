package me.hardstyl3r.tperms.listeners;

import me.hardstyl3r.tperms.TPerms;
import me.hardstyl3r.tperms.managers.PermissibleUserManager;
import me.hardstyl3r.tperms.objects.PermissibleUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChatListener implements Listener {

    private final PermissibleUserManager permissibleUserManager;

    public AsyncPlayerChatListener(TPerms plugin, PermissibleUserManager permissibleUserManager) {
        this.permissibleUserManager = permissibleUserManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        PermissibleUser u = permissibleUserManager.getUser(e.getPlayer());
        if (!u.getMainGroup().isDefault())
            e.setFormat("[" + u.getMainGroup().getName() + "] " + e.getFormat());
    }
}
