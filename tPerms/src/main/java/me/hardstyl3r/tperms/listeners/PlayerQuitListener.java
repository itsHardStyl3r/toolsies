package me.hardstyl3r.tperms.listeners;

import me.hardstyl3r.tperms.TPerms;
import me.hardstyl3r.tperms.managers.PermissionsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final PermissionsManager permissionsManager;

    public PlayerQuitListener(TPerms plugin, PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        permissionsManager.stopPermissions(e.getPlayer());
    }
}
