package me.hardstyl3r.toolsies.objects;

import me.hardstyl3r.toolsies.utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

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

    public String getColoredString(String path) {
        return StringUtils.translateBothColorCodes(getString(path));
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public Set<String> getConfigurationSection(String path) {
        ConfigurationSection configurationSection = config.getConfigurationSection(path);
        return configurationSection == null ? Collections.emptySet() : configurationSection.getKeys(false);
    }

    /**
     * A function supporting MiniMessage API to return message Component.
     *
     * @param path A path in messages-*.yml
     * @return A deserialized component from String.
     */
    public Component getStringComponent(String path) {
        return miniMessage.deserialize(getString(path));
    }

    /**
     * A function supporting MiniMessage API to return message Component.
     *
     * @param path A path in messages-*.yml
     * @param tags Placeholders to be replaced in the message.
     * @return A deserialized component from String with Placeholders.
     */
    public Component getStringComponent(String path, TagResolver... tags) {
        return miniMessage.deserialize(getString(path), tags);
    }
}
