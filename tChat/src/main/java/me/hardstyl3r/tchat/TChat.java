package me.hardstyl3r.tchat;

import me.hardstyl3r.tchat.commands.chatCommand;
import me.hardstyl3r.tchat.listeners.AsyncChatListener;
import me.hardstyl3r.tchat.managers.ChatManager;
import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.utils.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TChat extends JavaPlugin {

    private static TChat instance;
    private Toolsies toolsies;
    private ChatManager chatManager;

    public static TChat getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        long current = System.currentTimeMillis();
        instance = this;
        try {
            toolsies = (Toolsies) Bukkit.getServer().getPluginManager().getPlugin("toolsies");
            if (!toolsies.isEnabled() || toolsies == null)
                throw new Exception("toolsies is null or not enabled");
            double version = Double.parseDouble(toolsies.getDescription().getVersion().split("-")[0]);
            if (version < 0.10)
                throw new Exception("unsupported toolsies version (<0.10)");
        } catch (Exception e) {
            LogUtil.error("[tChat] Could not hook into toolsies: " + e + ". Disabling.");
            this.setEnabled(false);
        }
        initManagers();
        initListeners();
        initCommands();
        LogUtil.info("[tChat] Enabled tChat. (took " + (System.currentTimeMillis() - current) + "ms)");
    }

    @Override
    public void onDisable() {
    }

    private void initManagers(){
        chatManager = new ChatManager();
    }

    private void initListeners(){
        new AsyncChatListener(this, chatManager, toolsies.userManager);
    }

    private void initCommands() {
        new chatCommand(this, toolsies.userManager, toolsies.localeManager, chatManager);
    }
}
