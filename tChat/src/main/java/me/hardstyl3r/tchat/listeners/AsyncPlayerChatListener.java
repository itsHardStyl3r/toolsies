package me.hardstyl3r.tchat.listeners;

import me.hardstyl3r.tchat.TChat;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChatListener implements Listener {

    public AsyncPlayerChatListener(TChat plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChatColor(AsyncPlayerChatEvent e) {
        if (!e.getPlayer().hasPermission("toolsies.chat.color")) return;
        String message = e.getMessage();
        if (ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message)).length() == 0) return;
        e.setMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
