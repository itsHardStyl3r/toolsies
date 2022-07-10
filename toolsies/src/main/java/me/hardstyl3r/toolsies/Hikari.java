package me.hardstyl3r.toolsies;

import com.zaxxer.hikari.HikariDataSource;
import me.hardstyl3r.toolsies.managers.ConfigManager;
import me.hardstyl3r.toolsies.utils.LogUtil;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;

public class Hikari {

    public static HikariDataSource hikari;
    private final FileConfiguration config;

    public Hikari(ConfigManager configManager) {
        this.config = configManager.getConfig();
        if (config.getString("database.user") == null || config.getString("database.password") == null ||
                config.getString("database.user").isBlank() || config.getString("database.password").isBlank()) {
            LogUtil.error("[toolsies] MySQL connection credentials are not specified in config.yml.");
            throw new RuntimeException("Incorrect connection credentials.");
        }
        initializeDataSource();
        createTables();
    }

    public static HikariDataSource getHikari() {
        return hikari;
    }

    private void initializeDataSource() {
        hikari = new HikariDataSource();

        hikari.setMaximumPoolSize(10);
        //hikari.setDataSourceClassName(config.getString("database.class"));
        hikari.setJdbcUrl("jdbc:mysql://" + config.getString("database.host") + ":" + config.getString("database.port") + "/toolsies");
        //hikari.addDataSourceProperty("serverName", config.getString("database.host"));
        //hikari.addDataSourceProperty("port", config.getString("database.port"));
        //hikari.addDataSourceProperty("databaseName", "toolsies");
        hikari.addDataSourceProperty("user", config.getString("database.user"));
        hikari.addDataSourceProperty("password", config.getString("database.password", ""));
    }

    private void createTables() {
        Connection connection = null;
        Statement p = null;
        try {
            connection = hikari.getConnection();
            p = connection.createStatement();
            p.executeUpdate("CREATE TABLE IF NOT EXISTS `users` (`id` MEDIUMINT(8) UNSIGNED AUTO_INCREMENT, PRIMARY KEY (`id`)) CHARACTER SET = utf8;");
            DatabaseMetaData metaData = connection.getMetaData();
            if (isColumnMissing(metaData, "users", "uuid")) {
                p.executeUpdate("ALTER TABLE `users` ADD COLUMN `uuid` VARCHAR(36) NOT NULL AFTER `id`;");
            }
            if (isColumnMissing(metaData, "users", "name")) {
                p.executeUpdate("ALTER TABLE `users` ADD COLUMN `name` VARCHAR(16) NOT NULL AFTER `uuid`;");
            }
            if (isColumnMissing(metaData, "users", "locale")) {
                p.executeUpdate("ALTER TABLE `users` ADD COLUMN `locale` VARCHAR(5);");
            }
        } catch (SQLException e) {
            LogUtil.error("[toolsies] createTables(): " + e + ".");
        } finally {
            close(connection, p, null);
        }
    }

    public static boolean isColumnMissing(DatabaseMetaData metaData, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
            return !rs.next();
        }
    }

    public static void close(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LogUtil.error("[toolsies] close(Connection): " + e + ".");
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                LogUtil.error("[toolsies] close(PreparedStatement): " + e + ".");
            }
        }
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LogUtil.error("[toolsies] close(ResultSet): " + e + ".");
            }
        }
    }

    public static void close(Connection connection, Statement statement, ResultSet resultSet) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LogUtil.error("[toolsies] close(Statement): " + e + ".");
            }
        }
        close(connection, null, resultSet);
    }
}