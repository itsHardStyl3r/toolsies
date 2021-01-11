package me.hardstyl3r.toolsies;

import com.zaxxer.hikari.HikariDataSource;
import me.hardstyl3r.toolsies.managers.ConfigManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Hikari {

    public static HikariDataSource hikari;
    private final FileConfiguration config;

    public Hikari(ConfigManager configManager) {
        this.config = configManager.getConfig();
        connectToDatabase();
        initDatabase();
    }

    public static HikariDataSource getHikari() {
        return hikari;
    }

    public void connectToDatabase() {
        hikari = new HikariDataSource();

        hikari.setMaximumPoolSize(10);
        hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikari.addDataSourceProperty("serverName", config.getString("database.host"));
        hikari.addDataSourceProperty("port", config.getString("database.port"));
        hikari.addDataSourceProperty("databaseName", "toolsies");
        hikari.addDataSourceProperty("user", config.getString("database.user"));
        hikari.addDataSourceProperty("password", config.getString("database.password"));
    }

    private void initDatabase() {
        Connection connection = null;
        PreparedStatement p = null;

        String players = "CREATE TABLE IF NOT EXISTS `users` (`uuid` VARCHAR(36) NOT NULL, `name` VARCHAR(16) NOT NULL)";
        try {
            connection = hikari.getConnection();
            p = connection.prepareStatement(players);
            p.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (p != null) {
                try {
                    p.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}