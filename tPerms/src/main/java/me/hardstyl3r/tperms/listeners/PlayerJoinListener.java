package me.hardstyl3r.tperms.listeners;

import me.hardstyl3r.tperms.TPerms;
import me.hardstyl3r.tperms.managers.PermissibleUserManager;
import me.hardstyl3r.tperms.managers.PermissionsManager;
import me.hardstyl3r.tperms.objects.PermissibleUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final PermissibleUserManager permissibleUserManager;
    private final PermissionsManager permissionsManager;

    public PlayerJoinListener(TPerms plugin, PermissibleUserManager permissibleUserManager, PermissionsManager permissionsManager) {
        this.permissibleUserManager = permissibleUserManager;
        this.permissionsManager = permissionsManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /*
    Low priority, we should get the permissions ready as soon as possible.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent e) {
        Player target = e.getPlayer();
        if (!permissibleUserManager.hasPlayedBefore(target)) {
            permissibleUserManager.createPermissibleUser(target);
        }
        PermissibleUser user = permissibleUserManager.getUser(target);
        permissionsManager.startPermissions(target, user);
    }
}
