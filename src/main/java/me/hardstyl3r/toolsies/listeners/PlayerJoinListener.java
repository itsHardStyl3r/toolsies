package me.hardstyl3r.toolsies.listeners;

import me.hardstyl3r.toolsies.Toolsies;
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

    public PlayerJoinListener(Toolsies plugin, UserManager userManager) {
        this.userManager = userManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoinPlayer(PlayerJoinEvent e) {
        Player target = e.getPlayer();
        if (!userManager.hasPlayedBefore(target)) {
            for(Player p : Bukkit.getOnlinePlayers()){
                if(p != target) {
                    User u = userManager.getUser(p);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            u.getLocale().getConfig().getString("new_player")).replace("<name>", target.getName()));
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
    }
}
