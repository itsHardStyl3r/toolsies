package me.hardstyl3r.tchat.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.hardstyl3r.tchat.TChat;
import me.hardstyl3r.tchat.managers.ChatManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AsyncChatListener implements Listener {

    private final ChatManager chatManager;
    private final UserManager userManager;

    public AsyncChatListener(TChat plugin, ChatManager chatManager, UserManager userManager) {
        this.chatManager = chatManager;
        this.userManager = userManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        Player p = e.getPlayer();
        if (!chatManager.isLocked() || p.hasPermission("toolsies.chat.lock.bypass")) {
            return;
        }
        Locale l = userManager.determineLocale(p);
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getString("chat.toggle.chat_locked")));
        e.setCancelled(true);
    }
}
