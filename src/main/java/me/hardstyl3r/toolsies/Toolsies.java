package me.hardstyl3r.toolsies;

import me.hardstyl3r.toolsies.commands.*;
import me.hardstyl3r.toolsies.listeners.AsyncPlayerChatListener;
import me.hardstyl3r.toolsies.listeners.PlayerJoinListener;
import me.hardstyl3r.toolsies.listeners.PlayerQuitListener;
import me.hardstyl3r.toolsies.listeners.PlayerRespawnListener;
import me.hardstyl3r.toolsies.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Toolsies extends JavaPlugin {

    private static Toolsies instance;
    public UserManager userManager;
    public ConfigManager configManager;
    public KitManager kitManager;
    public LocaleManager localeManager;
    public PermissionsManager permissionsManager;
    public LocationManager locationManager;

    @Override
    public void onEnable() {
        instance = this;
        initManagers();
        initCommands();
        initListeners();

        /*
        Permissions are reloaded after /reload.
         */
        for (Player p : Bukkit.getOnlinePlayers()) {
            permissionsManager.startPermissions(p, userManager.getUser(p));
        }
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
        permissionsManager = new PermissionsManager(configManager);
        userManager = new UserManager(localeManager, permissionsManager);
        kitManager = new KitManager(configManager);
        locationManager = new LocationManager(configManager);
    }

    private void initCommands() {
        new toolsiesCommand(this, userManager);
        new kitCommand(this, userManager, kitManager, localeManager);
        new localeCommand(this, userManager, localeManager);
        new groupCommand(this, userManager, permissionsManager, localeManager);
        new broadcastCommand(this, userManager, localeManager);
        new permissionCommand(this, userManager, localeManager, permissionsManager);
        new spawnCommand(this, userManager, localeManager, locationManager);
        new setspawnCommand(this, userManager, localeManager, locationManager);
        new getspawnCommand(this, userManager, localeManager, locationManager);
        new delspawnCommand(this, userManager, localeManager, locationManager);
    }

    private void initListeners() {
        new PlayerJoinListener(this, userManager, permissionsManager);
        new PlayerQuitListener(this, userManager, permissionsManager);
        new AsyncPlayerChatListener(this, userManager, localeManager);
        new PlayerRespawnListener(this, locationManager);
    }

    public static Toolsies getInstance() {
        return instance;
    }
}
