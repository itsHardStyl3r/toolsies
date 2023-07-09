package me.hardstyl3r.tchat.listeners;

import me.hardstyl3r.tchat.TChat;
import me.hardstyl3r.toolsies.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;

public class AsyncPlayerChatListener implements Listener {

    private final FileConfiguration config;

    public AsyncPlayerChatListener(TChat plugin, FileConfiguration config) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.config = config;
    }

    @EventHandler
    public void onChatColor(AsyncPlayerChatEvent e) {
        if (!e.getPlayer().hasPermission("toolsies.chat.color")) return;
        String coloredMessage = StringUtils.translateBothColorCodes(e.getMessage());
        if (ChatColor.stripColor(coloredMessage).length() == 0) return;
        e.setMessage(coloredMessage);
    }

    @EventHandler
    public void onChatMention(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) return;
        String message = e.getMessage();
        if (!message.contains("@")) return;
        List<String> duplicates = new ArrayList<>();
        for (String mention : message.split("@")) {
            if (mention.isBlank()) continue;
            String nick = mention.split(" ")[0];
            if (nick.equalsIgnoreCase("everyone")
                    && config.getBoolean("mentions.allowEveryoneMention", true)
                    && e.getPlayer().hasPermission("toolsies.chat.mention.everyone")) {
                for (Player p : Bukkit.getOnlinePlayers()) ping(p);
                return;
            } else {
                Player target = (config.getBoolean("mentions.usePlayerExact", false) ? Bukkit.getPlayerExact(nick) : Bukkit.getPlayer(nick));
                if (target == null) continue;
                if (target.getName().equals(e.getPlayer().getName())) continue;
                if (duplicates.contains(target.getName())) continue;
                message = message.replaceFirst("@" + nick, "@" + target.getName());
                ping(target);
                duplicates.add(target.getName());
            }
        }
        e.setMessage(message);
    }

    private void ping(Player p) {
        p.playSound(p.getLocation(),
                Sound.valueOf(config.getString("mentions.notification.sound", "ENTITY_EXPERIENCE_ORB_PICKUP")),
                (float) config.getDouble("mentions.notification.volume", 0.6),
                (float) config.getDouble("mentions.notification.pitch", 1));
    }
}
