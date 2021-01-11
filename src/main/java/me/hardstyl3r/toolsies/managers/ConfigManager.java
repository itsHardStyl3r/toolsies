package me.hardstyl3r.toolsies.managers;

import me.hardstyl3r.toolsies.Toolsies;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class ConfigManager {

    public ConfigManager() {
        loadConfig("config");
    }

    private String getPath() {
        return "." + File.separator + "plugins" + File.separator + "toolsies" + File.separator;
    }

    private final HashMap<String, FileConfiguration> configs = new HashMap<>();

    public void loadConfig(String file) {
        File configFile = new File(getPath() + file + ".yml");
        if (!configFile.exists()) {
            if (configFile.getParentFile().mkdirs() && copy(Toolsies.getInstance().getResource(file + ".yml"), configFile)) {
                System.out.println("Created " + file + ".yml.");
            } else {
                System.out.println("Failed to create " + file + ".yml.");
            }
        }
        configs.put(file, YamlConfiguration.loadConfiguration(configFile));
    }

    public FileConfiguration getConfig() {
        return configs.get("config");
    }

    public FileConfiguration getConfig(String s){
        return configs.get(s);
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
