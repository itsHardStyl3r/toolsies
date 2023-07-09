package me.hardstyl3r.tchat;

import me.hardstyl3r.tchat.commands.*;
import me.hardstyl3r.tchat.listeners.AsyncChatListener;
import me.hardstyl3r.tchat.listeners.AsyncPlayerChatListener;
import me.hardstyl3r.tchat.listeners.TPermsAsyncPlayerChatListener;
import me.hardstyl3r.tchat.managers.ChatManager;
import me.hardstyl3r.tchat.managers.MessagingManagement;
import me.hardstyl3r.tchat.managers.MessagingManager;
import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.utils.LogUtil;
import me.hardstyl3r.tperms.TPerms;
import me.hardstyl3r.tperms.managers.PermissibleUserManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class TChat extends JavaPlugin {

    private static TChat instance;
    private Toolsies toolsies;
    private TPerms tPerms;
    private ChatManager chatManager;
    private PermissibleUserManager permissibleUserManager;
    private MessagingManager messagingManager;
    private MessagingManagement messagingManagement;
    private YamlConfiguration config;
    private boolean crash = false;

    public static TChat getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        long current = System.currentTimeMillis();
        instance = this;
        try {
            toolsies = (Toolsies) Bukkit.getServer().getPluginManager().getPlugin("toolsies");
            if (toolsies == null || !toolsies.isEnabled())
                throw new Exception("toolsies is null or not enabled");
            double version = Double.parseDouble(toolsies.getDescription().getVersion().split("-")[0]);
            if (version < 0.13)
                throw new Exception("unsupported toolsies version (<0.13)");
        } catch (Exception e) {
            LogUtil.error("[tChat] Could not hook into toolsies: " + e + ". Disabling.");
            crash = true;
            this.setEnabled(false);
            return;
        }
        try {
            tPerms = (TPerms) Bukkit.getServer().getPluginManager().getPlugin("tPerms");
            double version = Double.parseDouble(tPerms.getDescription().getVersion().split("-")[0]);
            if (version < 0.6)
                throw new Exception("unsupported tPerms version (<0.6)");
            LogUtil.info("[tChat] Found tPerms!");
            permissibleUserManager = tPerms.permissibleUserManager;
        } catch (Exception e) {
            tPerms = null;
            LogUtil.info("[tChat] Could not hook into tPerms: " + e + ".");
        }
        initManagers();
        initListeners();
        initCommands();
        initTasks();
        LogUtil.info("[tChat] Enabled tChat. (took " + (System.currentTimeMillis() - current) + "ms)");
    }

    @Override
    public void onDisable() {
        if (crash) return;
        LogUtil.info("[tChat] Stopping! Starting synchronous task to save data.");
        messagingManager.saveData();
    }

    public boolean isTPermsAvailable() {
        return (tPerms != null);
    }

    private void initManagers() {
        config = toolsies.configManager.loadConfig(this, "config");
        chatManager = new ChatManager(this, toolsies.configManager, toolsies.localeManager, permissibleUserManager);
        messagingManager = new MessagingManager(this, toolsies.configManager, toolsies.userManager);
        messagingManagement = new MessagingManagement(messagingManager, toolsies.localeManager, toolsies.userManager);
    }

    private void initListeners() {
        new AsyncChatListener(this, chatManager, toolsies.userManager, toolsies.localeManager);
        new AsyncPlayerChatListener(this, config);
        if (isTPermsAvailable())
            new TPermsAsyncPlayerChatListener(this, tPerms.permissibleUserManager, toolsies.localeManager);
    }

    private void initCommands() {
        new chatCommand(this, toolsies.userManager, toolsies.localeManager, chatManager);
        new tchatCommand(this, toolsies.userManager, toolsies.localeManager, chatManager);
        new msgCommand(this, toolsies.userManager, toolsies.localeManager, messagingManagement);
        new replyCommand(this, toolsies.userManager, toolsies.localeManager, messagingManager, messagingManagement);
        new msgtoggleCommand(this, toolsies.userManager, messagingManager);
        new socialspyCommand(this, toolsies.userManager, messagingManager);
    }

    private void initTasks() {
        Bukkit.getScheduler().cancelTasks(this);
        new AsyncStorageSaveTask(this, messagingManager, config);
        LogUtil.info("[tChat] initTasks(): Save-to-file task enabled.");
    }
}
