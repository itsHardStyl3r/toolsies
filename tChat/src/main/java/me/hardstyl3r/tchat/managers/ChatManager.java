package me.hardstyl3r.tchat.managers;

import me.hardstyl3r.tchat.TChat;
import me.hardstyl3r.tchat.enums.CooldownType;
import me.hardstyl3r.tchat.objects.Cooldown;
import me.hardstyl3r.toolsies.managers.ConfigManager;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.tperms.managers.PermissibleUserManager;
import me.hardstyl3r.tperms.objects.PermissibleUser;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class ChatManager {

    private final TChat plugin;
    private final FileConfiguration config;
    private final LocaleManager localeManager;
    private final PermissibleUserManager permissibleUserManager;
    private boolean chatCooldownEnabled;

    public ChatManager(TChat plugin, ConfigManager configManager, LocaleManager localeManager, PermissibleUserManager permissibleUserManager) {
        this.plugin = plugin;
        config = configManager.loadConfig(plugin, "config");
        this.localeManager = localeManager;
        this.permissibleUserManager = permissibleUserManager;
        chatCooldownEnabled = config.getBoolean("chat.cooldown.enabled");
    }

    public HashMap<UUID, Cooldown> cooldowns = new HashMap<>();

    public boolean chatLocked = false;

    public boolean isLocked() {
        return chatLocked;
    }

    public void toggleLocked() {
        chatLocked = !chatLocked;
    }

    public void applyChatCooldown(Player p) {
        if (!isChatCooldownEnabled() || p.hasPermission("toolsies.chat.cooldown.bypass"))
            return;
        long duration = localeManager.parseTimeFromString(config.getString("chat.cooldown.duration"));
        if (plugin.isTPermsAvailable()) {
            PermissibleUser user = permissibleUserManager.getUser(p);
            if (config.getString("chat.cooldown.group-duration." + user.getMainGroup()) != null)
                duration = localeManager.parseTimeFromString(config.getString("chat.cooldown.group-duration." + user.getMainGroup()));
        }
        Cooldown chatCooldown = new Cooldown(p, CooldownType.CHAT);
        chatCooldown.setDuration(duration);
        cooldowns.put(p.getUniqueId(), chatCooldown);
    }

    public boolean hasChatCooldown(Player p) {
        if (!isChatCooldownEnabled() || p.hasPermission("toolsies.chat.cooldown.bypass"))
            return false;
        return (getChatCooldown(p) != null);
    }

    public Cooldown getChatCooldown(Player p) {
        for (Cooldown c : cooldowns.values()) {
            if (!(c.getType() == CooldownType.CHAT && c.getUUID().equals(p.getUniqueId()))) continue;
            if (c.hasExpired()) {
                cooldowns.remove(p.getUniqueId(), c);
            } else {
                return c;
            }
        }
        return null;
    }

    public void resetChatCooldowns() {
        cooldowns.values().removeIf(cooldown -> cooldown.getType() == CooldownType.CHAT);
    }

    public boolean isChatCooldownEnabled() {
        return chatCooldownEnabled;
    }

    public void toggleChatCooldown() {
        chatCooldownEnabled = !chatCooldownEnabled;
    }

    public boolean getChatCooldownDefaultState() {
        return config.getBoolean("chat.cooldown.enabled");
    }
}
