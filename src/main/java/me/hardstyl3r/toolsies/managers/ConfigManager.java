package me.hardstyl3r.toolsies.managers;

import me.hardstyl3r.toolsies.Toolsies;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ConfigManager {

    public ConfigManager() {
        config = loadConfig("config");
    }

    private String getPath() {
        return "." + File.separator + "plugins" + File.separator + "toolsies" + File.separator;
    }

    private FileConfiguration config;

    public YamlConfiguration loadConfig(String file) {
        File configFile = new File(getPath() + file + ".yml");
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                copy(Toolsies.getInstance().getResource(file + ".yml"), configFile);
                System.out.println("Created " + file + ".yml.");
            } catch (Exception e) {
                System.out.println("Failed to create " + file + ".yml.");
                return null;
            }
        }
        return YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
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
            e.printStackTrace();
            return false;
        }
    }
}
