package me.hardstyl3r.tauth;

import me.hardstyl3r.tauth.commands.*;
import me.hardstyl3r.tauth.listeners.AuthPlayerListeners;
import me.hardstyl3r.tauth.listeners.AuthPlayerPropsListeners;
import me.hardstyl3r.tauth.listeners.PlayerAuthSuccessfulListener;
import me.hardstyl3r.tauth.listeners.UnAuthPlayerListeners;
import me.hardstyl3r.tauth.managers.LoginManagement;
import me.hardstyl3r.tauth.managers.LoginManager;
import me.hardstyl3r.toolsies.Hikari;
import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.utils.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class TAuth extends JavaPlugin {

    private static TAuth instance;
    private Toolsies toolsies;
    private LoginManager loginManager;
    private FileConfiguration config;
    private FileConfiguration emailConfig;
    private LoginManagement loginManagement;
    private boolean crash = false;

    public static TAuth getInstance() {
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
            LogUtil.error("[tAuth] Could not hook into toolsies: " + e + ". Disabling.");
            crash = true;
            this.setEnabled(false);
            return;
        }
        createTables();
        initManagers();
        initCommands();
        initListeners();
        if (Bukkit.spigot().getPaperConfig().getBoolean("settings.enable-player-collisions")) {
            LogUtil.warn("[tAuth] Please disable player collisions in paper.yml config! (settings.enable-player-collisions)");
        }
        LogUtil.info("[tAuth] Enabled tAuth. (took " + (System.currentTimeMillis() - current) + "ms)");
    }

    @Override
    public void onDisable() {
        //to-do: I doubt it can be asynchronous, but maybe it could
        if (crash) return;
        LogUtil.info("[tAuth] Stopping! Starting synchronous task to save data.");
        loginManager.savePlayers();
    }

    private void initCommands() {
        new loginCommand(this, toolsies.userManager, toolsies.localeManager, loginManager, loginManagement);
        new registerCommand(this, toolsies.userManager, toolsies.localeManager, loginManager, loginManagement);
        new logoutCommand(this, toolsies.userManager, toolsies.localeManager, loginManager, loginManagement);
        new authCommand(this, toolsies.userManager, toolsies.localeManager, loginManager, loginManagement);
        new changepasswordCommand(this, toolsies.userManager, loginManager, toolsies.localeManager);
        new playtimeCommand(this, toolsies.userManager, toolsies.localeManager, loginManager);
        new emailCommand(this, toolsies.userManager, toolsies.localeManager, loginManager, emailConfig);

    }

    private void initManagers() {
        config = toolsies.configManager.loadConfig(this, "config");
        emailConfig = toolsies.configManager.loadConfig(this, "email");
        loginManager = new LoginManager(this, config, toolsies.userManager);
        loginManagement = new LoginManagement(loginManager, config, toolsies.locationManager);
    }

    private void initListeners() {
        new AuthPlayerListeners(this, config, toolsies.userManager, loginManager, toolsies.localeManager);
        new UnAuthPlayerListeners(this, loginManager);
        new PlayerAuthSuccessfulListener(this, loginManager);
        new AuthPlayerPropsListeners(this, loginManager, toolsies.userManager, loginManagement);
    }

    private void createTables() {
        Connection connection = null;
        Statement p = null;
        try {
            connection = Hikari.getHikari().getConnection();
            p = connection.createStatement();
            p.executeUpdate("CREATE TABLE IF NOT EXISTS `logins` (`id` MEDIUMINT(8) UNSIGNED AUTO_INCREMENT, PRIMARY KEY (`id`)) CHARACTER SET = utf8;");
            DatabaseMetaData metaData = connection.getMetaData();
            if (Hikari.isColumnMissing(metaData, "logins", "uuid")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `uuid` VARCHAR(36) NOT NULL AFTER `id`;");
            }
            if (Hikari.isColumnMissing(metaData, "logins", "name")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `name` VARCHAR(16) NOT NULL AFTER `uuid`;");
            }
            if (Hikari.isColumnMissing(metaData, "logins", "password")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `password` VARCHAR(255) NOT NULL;");
            }
            if (Hikari.isColumnMissing(metaData, "logins", "ip")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `ip` VARCHAR(32);");
            }
            if (Hikari.isColumnMissing(metaData, "logins", "regip")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `regip` VARCHAR(32);");
            }
            if (Hikari.isColumnMissing(metaData, "logins", "regdate")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `regdate` BIGINT(20);");
            }
            if (Hikari.isColumnMissing(metaData, "logins", "lastlogin")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `lastlogin` BIGINT(20);");
            }
            if (Hikari.isColumnMissing(metaData, "logins", "email")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `email` VARCHAR(255);");
            }
            if (Hikari.isColumnMissing(metaData, "logins", "loggedin")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `loggedin` BOOL;");
            }
            if (Hikari.isColumnMissing(metaData, "logins", "hassession")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `hassession` BOOL;");
            }
            if (Hikari.isColumnMissing(metaData, "logins", "x")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `x` DOUBLE;");
            }
            if (Hikari.isColumnMissing(metaData, "logins", "y")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `y` DOUBLE;");
            }
            if (Hikari.isColumnMissing(metaData, "logins", "z")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `z` DOUBLE;");
            }
            if (Hikari.isColumnMissing(metaData, "logins", "yaw")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `yaw` FLOAT;");
            }
            if (Hikari.isColumnMissing(metaData, "logins", "pitch")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `pitch` FLOAT;");
            }
            if (Hikari.isColumnMissing(metaData, "logins", "world")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `world` VARCHAR(255);");
            }
            if (Hikari.isColumnMissing(metaData, "logins", "playtime")) {
                p.executeUpdate("ALTER TABLE `logins` ADD COLUMN `playtime` BIGINT(20);");
            }
            p.executeUpdate("CREATE TABLE IF NOT EXISTS `logins_history` (`id` MEDIUMINT(8) UNSIGNED AUTO_INCREMENT, PRIMARY KEY (`id`)) CHARACTER SET = utf8;");
            if (Hikari.isColumnMissing(metaData, "logins_history", "uuid")) {
                p.executeUpdate("ALTER TABLE `logins_history` ADD COLUMN `uuid` VARCHAR(36) NOT NULL AFTER `id`;");
            }
            if (Hikari.isColumnMissing(metaData, "logins_history", "ip")) {
                p.executeUpdate("ALTER TABLE `logins_history` ADD COLUMN `ip` VARCHAR(32);");
            }
            if (Hikari.isColumnMissing(metaData, "logins_history", "date")) {
                p.executeUpdate("ALTER TABLE `logins_history` ADD COLUMN `date` BIGINT(20);");
            }
            if (Hikari.isColumnMissing(metaData, "logins_history", "success")) {
                p.executeUpdate("ALTER TABLE `logins_history` ADD COLUMN `success` BOOL;");
            }
            if (Hikari.isColumnMissing(metaData, "logins_history", "action")) {
                p.executeUpdate("ALTER TABLE `logins_history` ADD COLUMN `action` VARCHAR(255);");
            }
            if (Hikari.isColumnMissing(metaData, "logins_history", "comment")) {
                p.executeUpdate("ALTER TABLE `logins_history` ADD COLUMN `comment` VARCHAR(255);");
            }
            if (Hikari.isColumnMissing(metaData, "logins_history", "silent")) {
                p.executeUpdate("ALTER TABLE `logins_history` ADD COLUMN `silent` BOOL;");
            }
        } catch (SQLException e) {
            LogUtil.error("[tAuth] createTables(): " + e + ".");
        } finally {
            Hikari.close(connection, p, null);
        }
    }
}