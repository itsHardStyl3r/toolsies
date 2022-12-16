package me.hardstyl3r.tchat.listeners;

import me.hardstyl3r.tchat.TChat;
import me.hardstyl3r.toolsies.utils.StringUtils;
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
        String coloredMessage = StringUtils.translateBothColorCodes(e.getMessage());
        if (ChatColor.stripColor(coloredMessage).length() == 0) return;
        e.setMessage(coloredMessage);
    }
}
