package me.hardstyl3r.toolsies.listeners;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final UserManager userManager;

    public PlayerQuitListener(Toolsies plugin, UserManager userManager) {
        this.userManager = userManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage("");
        Player target = e.getPlayer();
        for (Player p : Bukkit.getOnlinePlayers()) {
            User users = userManager.getUser(p);
            p.sendMessage(users.getLocale().getColoredString("players.quit.broadcast").replace("<name>", target.getName()));
        }
    }
}
