package me.hardstyl3r.toolsies;

import com.zaxxer.hikari.HikariDataSource;
import me.hardstyl3r.toolsies.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Hikari {

    public static HikariDataSource hikari;
    private final FileConfiguration config;

    public Hikari(ConfigManager configManager) {
        this.config = configManager.getConfig();
        initializeDataSource();
        createTables();
    }

    public static HikariDataSource getHikari() {
        return hikari;
    }

    private void initializeDataSource() {
        hikari = new HikariDataSource();

        hikari.setMaximumPoolSize(10);
        hikari.setDataSourceClassName(config.getString("database.class"));
        hikari.addDataSourceProperty("serverName", config.getString("database.host"));
        hikari.addDataSourceProperty("port", config.getString("database.port"));
        hikari.addDataSourceProperty("databaseName", "toolsies");
        hikari.addDataSourceProperty("user", config.getString("database.user"));
        hikari.addDataSourceProperty("password", config.getString("database.password"));
    }

    private void createTables() {
        Bukkit.getScheduler().runTaskAsynchronously(Toolsies.getInstance(), () -> {
            Connection connection = null;
            PreparedStatement p = null;

            String users = "CREATE TABLE IF NOT EXISTS `users` (`uuid` VARCHAR(36) NOT NULL, `name` VARCHAR(16) NOT NULL, `locale` VARCHAR(5), `groups` TEXT, `permissions` TEXT)";
            try {
                connection = hikari.getConnection();
                p = connection.prepareStatement(users);
                p.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                close(connection, p, null);
            }
        });
    }

    public static void close(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}