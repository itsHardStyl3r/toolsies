package me.hardstyl3r.toolsies;

import me.hardstyl3r.toolsies.commands.*;
import me.hardstyl3r.toolsies.listeners.PlayerJoinListener;
import me.hardstyl3r.toolsies.listeners.PlayerQuitListener;
import me.hardstyl3r.toolsies.listeners.PlayerRespawnListener;
import me.hardstyl3r.toolsies.managers.*;
import me.hardstyl3r.toolsies.utils.LogUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Toolsies extends JavaPlugin {

    private static Toolsies instance;
    public UserManager userManager;
    public ConfigManager configManager;
    public KitManager kitManager;
    public LocaleManager localeManager;
    public LocationManager locationManager;
    public static Logger logger = Logger.getLogger("Minecraft");

    @Override
    public void onEnable() {
        long current = System.currentTimeMillis();
        instance = this;
        try {
            initManagers();
        } catch (SQLException e) {
            LogUtil.error("[toolsies] Could not initialize managers: " + e + ". Disabling. (took " + (System.currentTimeMillis() - current) + "ms)");
            this.setEnabled(false);
            return;
        }
        initCommands();
        initListeners();
        logger.log(Level.INFO, "[toolsies] Enabled toolsies. (took " + (System.currentTimeMillis() - current) + "ms)");
    }

    @Override
    public void onDisable() {
        if (Hikari.getHikari() != null) {
            Hikari.getHikari().close();
        }
    }

    private void initManagers() throws SQLException {
        configManager = new ConfigManager();
        new Hikari(configManager);
        localeManager = new LocaleManager(configManager);
        userManager = new UserManager(localeManager);
        kitManager = new KitManager(configManager);
        locationManager = new LocationManager(this, configManager);
    }

    private void initCommands() {
        new toolsiesCommand(this, userManager);
        new kitCommand(this, userManager, kitManager, localeManager);
        new localeCommand(this, userManager, localeManager);
        new broadcastCommand(this, userManager, localeManager);
        new spawnCommand(this, userManager, localeManager, locationManager);
        new setspawnCommand(this, userManager, localeManager, locationManager);
        new getspawnCommand(this, userManager, localeManager, locationManager);
    }

    private void initListeners() {
        new PlayerJoinListener(this, userManager);
        new PlayerQuitListener(this, userManager);
        new PlayerRespawnListener(this, locationManager);
    }

    public static Toolsies getInstance() {
        return instance;
    }
}
