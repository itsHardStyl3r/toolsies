package me.hardstyl3r.toolsies.listeners;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.PermissionsManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final UserManager userManager;
    private final PermissionsManager permissionsManager;

    public PlayerJoinListener(Toolsies plugin, UserManager userManager, PermissionsManager permissionsManager) {
        this.userManager = userManager;
        this.permissionsManager = permissionsManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoinPlayer(PlayerJoinEvent e) {
        e.setJoinMessage("");
        Player target = e.getPlayer();
        if (!userManager.hasPlayedBefore(target)) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p != target) {
                    User users = userManager.getUser(p);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            users.getLocale().getConfig().getString("players.join.new_player")).replace("<name>", target.getName()));
                }
            }
            userManager.createUser(target);
        }
        User u = userManager.getUser(target);
        /*
        Users can change their name.
        If user has changed their name, update it when they join the server.
         */
        if (!u.getName().equals(target.getName())) {
            u.setName(target.getName());
            userManager.updateUser(u);
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            User users = userManager.getUser(p);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    users.getLocale().getConfig().getString("players.join.broadcast")).replace("<name>", target.getName()));
        }

        for (String s : u.getLocale().getConfig().getStringList("players.join.motd")) {
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', s).replace("<name>", target.getName()));
        }
        permissionsManager.startPermissions(target, u);
    }
}
