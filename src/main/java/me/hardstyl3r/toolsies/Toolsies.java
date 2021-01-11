package me.hardstyl3r.toolsies;

import me.hardstyl3r.toolsies.commands.toolsiesCommand;
import me.hardstyl3r.toolsies.listeners.PlayerJoinListener;
import me.hardstyl3r.toolsies.managers.ConfigManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Toolsies extends JavaPlugin {

    private static Toolsies instance;
    public UserManager userManager;
    public ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        initManagers();
        initCommands();
        initListeners();
    }

    @Override
    public void onDisable() {
        if (Hikari.getHikari() != null) {
            Hikari.getHikari().close();
        }
    }

    private void initManagers() {
        configManager = new ConfigManager();
        new Hikari(configManager);
        userManager = new UserManager();
    }

    private void initCommands() {
        new toolsiesCommand(this, userManager, configManager);
    }

    private void initListeners() {
        new PlayerJoinListener(this, userManager);
    }

    public static Toolsies getInstance() {
        return instance;
    }
}
