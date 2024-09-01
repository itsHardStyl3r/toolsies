package me.hardstyl3r.tbans;

import me.hardstyl3r.tbans.commands.*;
import me.hardstyl3r.tbans.listeners.AsyncPlayerPreLoginListener;
import me.hardstyl3r.tbans.listeners.MuteListeners;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.toolsies.Hikari;
import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.utils.LogUtil;
import me.hardstyl3r.tperms.TPerms;
import me.hardstyl3r.tperms.managers.PermissibleUserManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class TBans extends JavaPlugin {

    private static TBans instance;
    private Toolsies toolsies;
    private PunishmentManager punishmentManager;
    private FileConfiguration config;
    private PermissibleUserManager permissibleUserManager;

    public static TBans getInstance() {
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
            if (version < 0.12)
                throw new Exception("unsupported toolsies version (<0.12)");
        } catch (Exception e) {
            LogUtil.error("[tBans] Could not hook into toolsies: " + e + ". Disabling.");
            this.setEnabled(false);
            return;
        }
        try {
            TPerms tPerms = (TPerms) Bukkit.getServer().getPluginManager().getPlugin("tPerms");
            double version = Double.parseDouble(tPerms.getDescription().getVersion().split("-")[0]);
            if (version < 0.6)
                throw new Exception("unsupported tPerms version (<0.6)");
            LogUtil.info("[tBans] Found tPerms!");
            permissibleUserManager = tPerms.permissibleUserManager;
        } catch (Exception e) {
            LogUtil.info("[tBans] Could not hook into tPerms: " + e + ".");
        }
        createTables();
        initManagers();
        initCommands();
        initListeners();
        initTasks();
        LogUtil.info("[tBans] Enabled tBans. (took " + (System.currentTimeMillis() - current) + "ms)");
    }

    @Override
    public void onDisable() {
        LogUtil.info("[tBans] Disabling tasks.");
        Bukkit.getScheduler().cancelTasks(this);
    }

    private void initManagers() {
        config = toolsies.configManager.loadConfig(this, "config");
        punishmentManager = new PunishmentManager(toolsies.localeManager, config, permissibleUserManager);
    }

    private void initCommands() {
        new banCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new banlistCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager, config);
        new unbanCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new getbanCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new warnCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new unwarnCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new warnsCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new muteCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new unmuteCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new kickCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
    }

    private void initListeners() {
        new AsyncPlayerPreLoginListener(this, punishmentManager, toolsies.localeManager, toolsies.userManager);
        new MuteListeners(this, punishmentManager, toolsies.localeManager, toolsies.userManager, config);
    }

    private void initTasks() {
        Bukkit.getScheduler().cancelTasks(this);
        if (config.getInt("cleanupTaskTimer") < 0) return;
        new AsyncCleanupTask(this, punishmentManager, config.getInt("cleanupTaskTimer"));
        LogUtil.info("[tBans] initTasks(): Cleanup task enabled.");
    }

    private void createTables() {
        Connection connection = null;
        Statement p = null;
        try {
            connection = Hikari.getHikari().getConnection();
            p = connection.createStatement();
            p.executeUpdate("CREATE TABLE IF NOT EXISTS `punishments` (`id` MEDIUMINT(8) UNSIGNED AUTO_INCREMENT, PRIMARY KEY (`id`)) CHARACTER SET = utf8;\n");
            DatabaseMetaData metaData = connection.getMetaData();
            if (Hikari.isColumnMissing(metaData, "punishments", "uuid")) {
                p.executeUpdate("ALTER TABLE `punishments` ADD COLUMN `uuid` VARCHAR(36) AFTER `id`;");
            }
            if (Hikari.isColumnMissing(metaData, "punishments", "name")) {
                p.executeUpdate("ALTER TABLE `punishments` ADD COLUMN `name` VARCHAR(32) NOT NULL AFTER `uuid`;");
            }
            if (Hikari.isColumnMissing(metaData, "punishments", "type")) {
                p.executeUpdate("ALTER TABLE `punishments` ADD COLUMN `type` VARCHAR(4) NOT NULL;");
            }
            if (Hikari.isColumnMissing(metaData, "punishments", "admin")) {
                p.executeUpdate("ALTER TABLE `punishments` ADD COLUMN `admin` VARCHAR(16);");
            }
            if (Hikari.isColumnMissing(metaData, "punishments", "reason")) {
                p.executeUpdate("ALTER TABLE `punishments` ADD COLUMN `reason` TEXT;");
            }
            if (Hikari.isColumnMissing(metaData, "punishments", "date")) {
                p.executeUpdate("ALTER TABLE `punishments` ADD COLUMN `date` BIGINT(20);");
            }
            if (Hikari.isColumnMissing(metaData, "punishments", "duration")) {
                p.executeUpdate("ALTER TABLE `punishments` ADD COLUMN `duration` BIGINT(20);");
            }
            if (Hikari.isColumnMissing(metaData, "punishments", "active")) {
                p.executeUpdate("ALTER TABLE `punishments` ADD COLUMN `active` BOOLEAN DEFAULT 0;");
            }
        } catch (SQLException e) {
            LogUtil.error("[tBans] createTables(): " + e + ".");
        } finally {
            Hikari.close(connection, p, null);
        }
    }
}
