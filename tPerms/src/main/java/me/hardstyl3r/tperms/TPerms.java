package me.hardstyl3r.tperms;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.tperms.commands.groupCommand;
import me.hardstyl3r.tperms.commands.permissionCommand;
import me.hardstyl3r.tperms.listeners.AsyncPlayerChatListener;
import me.hardstyl3r.tperms.listeners.PlayerJoinListener;
import me.hardstyl3r.tperms.listeners.PlayerQuitListener;
import me.hardstyl3r.tperms.managers.PermissibleUserManager;
import me.hardstyl3r.tperms.managers.PermissionsManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TPerms extends JavaPlugin {

    private static TPerms instance;
    private Toolsies toolsies;
    public PermissionsManager permissionsManager;
    public PermissibleUserManager permissibleUserManager;

    @Override
    public void onEnable() {
        instance = this;
        toolsies = (Toolsies) Bukkit.getServer().getPluginManager().getPlugin("toolsies");
        if (!toolsies.isEnabled() || toolsies == null) {
            System.out.println("Could not hook into toolsies.");
            this.setEnabled(false);
        }

        /*
        The expected versioning of toolsies is:
        0.00-ALPHA/BETA/RC-00 and what we're interested in is 0.00.
         */
        double version = Double.parseDouble(toolsies.getDescription().getVersion().split("-")[0]);
        double SUPPORTED_TOOLSIES = 0.6;
        if (version < SUPPORTED_TOOLSIES) {
            System.out.println("Unsupported toolsies version.");
            this.setEnabled(false);
        }
        initManagers();
        initCommands();
        initListeners();
    }

    @Override
    public void onDisable() {
    }

    public static TPerms getInstance() {
        return instance;
    }

    private void initManagers() {
        permissionsManager = new PermissionsManager(this, toolsies.configManager);
        permissibleUserManager = new PermissibleUserManager(permissionsManager);
    }

    private void initListeners() {
        new AsyncPlayerChatListener(this, permissibleUserManager, toolsies.localeManager);
        new PlayerJoinListener(this, permissibleUserManager, permissionsManager);
        new PlayerQuitListener(this, permissionsManager);
    }

    private void initCommands() {
        new groupCommand(this, toolsies.userManager, permissionsManager, toolsies.localeManager, permissibleUserManager);
        new permissionCommand(this, toolsies.userManager, toolsies.localeManager, permissionsManager, permissibleUserManager);
    }
}
