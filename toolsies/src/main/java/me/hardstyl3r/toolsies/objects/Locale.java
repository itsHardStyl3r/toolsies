package me.hardstyl3r.toolsies.objects;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Locale {

    private final String id;
    private final FileConfiguration config;
    private List<String> aliases;
    private String name;

    public Locale(String id, FileConfiguration config) {
        this.id = id;
        this.config = config;
    }

    public String getId() {
        return id;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void setAliases(List<String> l) {
        this.aliases = l;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setName(String s) {
        this.name = s;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return getName();
    }

    public String getString(String path) {
        return config.getString(path, path);
    }

    public String getColoredString(String path){
        return ChatColor.translateAlternateColorCodes('&', getString(path));
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public Set<String> getConfigurationSection(String path) {
        ConfigurationSection configurationSection = config.getConfigurationSection(path);
        return configurationSection == null ? Collections.emptySet() : configurationSection.getKeys(false);
    }
}
