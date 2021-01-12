package me.hardstyl3r.toolsies.managers;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocaleManager {

    private final ConfigManager configManager;

    public LocaleManager(ConfigManager configManager) {
        this.configManager = configManager;
        loadLocales();
    }

    private final HashMap<String, Locale> locales = new HashMap<>();

    private void loadLocales() {
        for (String s : configManager.getConfig().getStringList("locales")) {
            String file = "messages-" + s;
            if (Toolsies.getInstance().getResource(file + ".yml") != null) {
                FileConfiguration config = configManager.loadConfig(file, "messages");
                Locale l = new Locale(s, config);
                l.setAliases(config.getStringList("config.aliases"));
                l.setName(config.getString("config.name"));
                locales.put(s, l);
                System.out.println("Loaded locale " + l.getName() + " (" + l.getId() + ").");
            } else {
                System.out.println("Could not find messages-" + s + ".");
            }
        }
    }

    public Locale getLocale(String s) {
        s = s.toLowerCase();
        for (Locale l : locales.values()) {
            if (l.getId().equalsIgnoreCase(s) || l.getAliases().contains(s)) {
                return l;
            }
        }
        return null;
    }

    public List<String> getLocales() {
        ArrayList<String> current = new ArrayList<>();
        for (Locale l : locales.values()) {
            current.add(l.getName());
        }
        return current;
    }
}
