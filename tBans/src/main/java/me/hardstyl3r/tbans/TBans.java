package me.hardstyl3r.tbans;

import me.hardstyl3r.tbans.commands.*;
import me.hardstyl3r.tbans.listeners.AsyncPlayerChatListener;
import me.hardstyl3r.tbans.listeners.AsyncPlayerPreLoginListener;
import me.hardstyl3r.tbans.listeners.own.PlayerPunishedListener;
import me.hardstyl3r.tbans.listeners.own.PlayerUnpunishedListener;
import me.hardstyl3r.tbans.managers.PunishmentManager;
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

public class TBans extends JavaPlugin {

    private static TBans instance;
    private Toolsies toolsies;
    private PunishmentManager punishmentManager;
    private FileConfiguration config;

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
            LogUtil.error("[tBans] Could not hook into toolsies: " + e + ". Disabling.");
            this.setEnabled(false);
            return;
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
    }

    public static TBans getInstance() {
        return instance;
    }

    private void initManagers() {
        config = toolsies.configManager.loadConfig(this, "config");
        punishmentManager = new PunishmentManager(toolsies.localeManager, config);
    }

    private void initCommands() {
        new banCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new banlistCommand(this, toolsies.userManager, toolsies.localeManager);
        new unbanCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new tempbanCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new getbanCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new banipCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager, config);
        new unbanipCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new warnCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new unwarnCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new getwarnCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new warnsCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new muteCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new tempmuteCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new unmuteCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new tempwarnCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
        new kickCommand(this, toolsies.userManager, toolsies.localeManager);
        new tempbanipCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager, config);
        new getbanipCommand(this, toolsies.userManager, punishmentManager, toolsies.localeManager);
    }

    private void initListeners() {
        new PlayerPunishedListener(this, toolsies.userManager, toolsies.localeManager);
        new PlayerUnpunishedListener(this, toolsies.userManager);

        new AsyncPlayerPreLoginListener(this, punishmentManager, toolsies.localeManager, toolsies.userManager);
        new AsyncPlayerChatListener(this, punishmentManager, toolsies.localeManager, toolsies.userManager);
    }

    private void initTasks() {
        Bukkit.getScheduler().cancelTasks(this);
        if (config.getBoolean("cleanupTaskEnabled")) {
            new AsyncCleanupTask(this, punishmentManager, config);
            LogUtil.info("initTasks(): Cleanup task enabled.");
        } else {
            LogUtil.info("initTasks(): Cleanup task disabled.");
        }
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
            p.executeUpdate("CREATE TABLE IF NOT EXISTS `punishments_history` LIKE `punishments`;");
        } catch (SQLException e) {
            LogUtil.error("createTables(): " + e + ".");
        } finally {
            Hikari.close(connection, p, null);
        }
    }
}
