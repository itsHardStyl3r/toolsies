package me.hardstyl3r.toolsies.managers;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.utils.LogUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ConfigManager {

    public ConfigManager() {
        config = loadConfig(null, "config");
    }

    private String getPath(JavaPlugin plugin) {
        return "." + File.separator + "plugins" + File.separator + (plugin == null ? "toolsies" : plugin.getName()) + File.separator;
    }

    private final FileConfiguration config;

    /**
     * Shortened version of loadConfig(JavaPlugin, String, String);
     *
     * @param plugin instance of plugin that contains wanted file
     * @param file   file you want to load
     * @return loaded YamlConfiguration of the file
     */
    public YamlConfiguration loadConfig(JavaPlugin plugin, String file) {
        return loadConfig(plugin, file, "");
    }

    /**
     * This method is used to create .yml from plugin's resources if it doesn't exist and load it if it does.
     *
     * @param plugin instance of plugin that contains wanted file
     * @param file   file you want to load
     * @param to     directory where the file should be placed
     * @return loaded YamlConfiguration of the file
     */
    public YamlConfiguration loadConfig(JavaPlugin plugin, String file, String to) {
        if (!to.equals("")) to = to + File.separator;
        File configFile = new File(getPath(plugin) + to + file + ".yml");
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                copy((plugin == null ? Toolsies.getInstance() : plugin).getResource(file + ".yml"), configFile);
                LogUtil.info("[toolsies] Created " + file + ".yml.");
            } catch (Exception e) {
                LogUtil.error("[toolsies] loadConfig(): " + e + ".");
                return null;
            }
        }
        return YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }


    public boolean saveConfig(JavaPlugin plugin, FileConfiguration config, String file) {
        return saveConfig(plugin, config, file, "");
    }

    public boolean saveConfig(JavaPlugin plugin, FileConfiguration config, String file, String to) {
        if (!to.equals("")) {
            to = to + File.separator;
        }
        File configFile = new File(getPath(plugin) + to + file + ".yml");
        try {
            config.save(configFile);
            LogUtil.info("[toolsies] saveConfig(): Saved " + file + ".yml.");
            return true;
        } catch (Exception e) {
            LogUtil.error("[toolsies] saveConfig(): " + e + ".");
        }
        return false;
    }

    private boolean copy(InputStream source, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte['Ѐ'];
            int len;
            while ((len = source.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            source.close();
            return true;
        } catch (Exception e) {
            LogUtil.error("[toolsies] copy(" + file + ".yml): " + e + ".");
            return false;
        }
    }
}
