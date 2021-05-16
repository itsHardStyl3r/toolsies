package me.hardstyl3r.tperms;

import me.hardstyl3r.toolsies.Hikari;
import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.utils.LogUtil;
import me.hardstyl3r.tperms.commands.groupCommand;
import me.hardstyl3r.tperms.commands.permissionCommand;
import me.hardstyl3r.tperms.listeners.AsyncPlayerChatListener;
import me.hardstyl3r.tperms.listeners.PlayerJoinListener;
import me.hardstyl3r.tperms.listeners.PlayerQuitListener;
import me.hardstyl3r.tperms.managers.PermissibleUserManager;
import me.hardstyl3r.tperms.managers.PermissionsManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class TPerms extends JavaPlugin {

    private static TPerms instance;
    private Toolsies toolsies;
    public PermissionsManager permissionsManager;
    public PermissibleUserManager permissibleUserManager;

    @Override
    public void onEnable() {
        long current = System.currentTimeMillis();
        instance = this;
        toolsies = (Toolsies) Bukkit.getServer().getPluginManager().getPlugin("toolsies");
        if (!toolsies.isEnabled() || toolsies == null) {
            LogUtil.warn("Could not hook into toolsies.");
            this.setEnabled(false);
        }
        double version = Double.parseDouble(toolsies.getDescription().getVersion().split("-")[0]);
        if (version < 0.9) {
            LogUtil.error("Unsupported toolsies version.");
            this.setEnabled(false);
        }
        addTables();
        initManagers();
        initCommands();
        initListeners();
        LogUtil.info("[tPerms] Enabled tPerms. (took " + (System.currentTimeMillis() - current) + "ms)");
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

    private void addTables() {
        Connection connection = null;
        Statement p = null;
        try {
            connection = Hikari.getHikari().getConnection();
            p = connection.createStatement();
            DatabaseMetaData metaData = connection.getMetaData();
            if (Hikari.isColumnMissing(metaData, "users", "groups")) {
                p.executeUpdate("ALTER TABLE `users` ADD COLUMN `groups` TEXT;");
            }
            if (Hikari.isColumnMissing(metaData, "users", "permissions")) {
                p.executeUpdate("ALTER TABLE `users` ADD COLUMN `permissions` TEXT;");
            }
        } catch (SQLException e) {
            LogUtil.error("addTables(): " + e + ".");
        } finally {
            Hikari.close(connection, p, null);
        }
    }
}
