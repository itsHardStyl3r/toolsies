package me.hardstyl3r.toolsies.listeners;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final UserManager userManager;

    public PlayerJoinListener(Toolsies plugin, UserManager userManager) {
        this.userManager = userManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /*
    We need to create a stable base for all the addons.
    LOW priority will ensure that everything else running at EventPriority.NORMAL
    will have the data they need.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onJoinPlayer(PlayerJoinEvent e) {
        Player target = e.getPlayer();
        if (!userManager.hasPlayedBefore(target)) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p != target) {
                    User users = userManager.getUser(p);
                    p.sendMessage(users.getLocale().getColoredString("players.join.new_player").replace("<name>", target.getName()));
                }
            }
            userManager.createUser(target);
        }
        User u = userManager.getUser(target);
        /*
        Users (in online-mode: true) can change their name.
        If user has changed their name, update it when they join the server.
         */
        if (!u.getName().equals(target.getName())) {
            u.setName(target.getName());
            userManager.updateUser(u);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoinPlayerMessages(PlayerJoinEvent e) {
        e.setJoinMessage("");
        Player target = e.getPlayer();
        User u = userManager.getUser(target);
        for (Player p : Bukkit.getOnlinePlayers()) {
            User users = userManager.getUser(p);
            p.sendMessage(users.getLocale().getColoredString("players.join.broadcast").replace("<name>", target.getName()));
        }

        for (String s : u.getLocale().getStringList("players.join.motd")) {
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', s)
                    .replace("<name>", target.getName()));
        }
    }
}
