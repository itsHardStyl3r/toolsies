package me.hardstyl3r.toolsies;

import me.hardstyl3r.toolsies.commands.kitCommand;
import me.hardstyl3r.toolsies.commands.localeCommand;
import me.hardstyl3r.toolsies.commands.toolsiesCommand;
import me.hardstyl3r.toolsies.listeners.PlayerJoinListener;
import me.hardstyl3r.toolsies.managers.ConfigManager;
import me.hardstyl3r.toolsies.managers.KitManager;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Toolsies extends JavaPlugin {

    private static Toolsies instance;
    public UserManager userManager;
    public ConfigManager configManager;
    public KitManager kitManager;
    public LocaleManager localeManager;

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
        localeManager = new LocaleManager(configManager);
        userManager = new UserManager(configManager, localeManager);
        kitManager = new KitManager(configManager);
    }

    private void initCommands() {
        new toolsiesCommand(this, userManager);
        new kitCommand(this, userManager, kitManager);
        new localeCommand(this, userManager, localeManager);
    }

    private void initListeners() {
        new PlayerJoinListener(this, userManager);
    }

    public static Toolsies getInstance() {
        return instance;
    }
}
