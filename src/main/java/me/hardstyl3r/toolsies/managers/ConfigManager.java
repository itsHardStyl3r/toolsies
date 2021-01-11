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
        loadConfig();
    }

    private String getPath() {
        return "." + File.separator + "plugins" + File.separator + "toolsies" + File.separator;
    }

    private final File configFile = new File(getPath() + "config.yml");
    private FileConfiguration configConfig;

    public void loadConfig() {
        if (!configFile.exists()) {
            if (configFile.getParentFile().mkdirs() && copy(Toolsies.getInstance().getResource("config.yml"), configFile)) {
                System.out.println("Created config.yml.");
            } else {
                System.out.println("Failed to create config.yml.");
            }
        }
        configConfig = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return configConfig;
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
