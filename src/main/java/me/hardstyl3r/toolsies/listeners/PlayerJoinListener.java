package me.hardstyl3r.toolsies.listeners;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final UserManager userManager;

    public PlayerJoinListener(Toolsies plugin, UserManager userManager) {
        this.userManager = userManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoinPlayer(PlayerJoinEvent e) {
        if (!userManager.hasPlayedBefore(e.getPlayer())) {
            Bukkit.broadcastMessage("New player: " + e.getPlayer().getName());
            userManager.createUser(e.getPlayer());
        }
        User u = userManager.getUser(e.getPlayer());
        /*
        Users can change their name.
        If user has changed their name, update it when they join the server.
         */
        if (!u.getName().equals(e.getPlayer().getName())) {
            u.setName(e.getPlayer().getName());
            userManager.updateUser(u);
        }
    }
}
