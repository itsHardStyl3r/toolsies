package me.hardstyl3r.toolsies.managers;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocaleManager {

    private final ConfigManager configManager;
    private final FileConfiguration config;

    public LocaleManager(ConfigManager configManager) {
        this.configManager = configManager;
        config = configManager.loadConfig(null, "messages");
        loadLocales();
    }

    private final HashMap<String, Locale> locales = new HashMap<>();

    private void loadLocales() {
        for (String s : configManager.getConfig().getStringList("locales")) {
            String file = "messages-" + s;
            if (Toolsies.getInstance().getResource(file + ".yml") != null) {
                FileConfiguration config = configManager.loadConfig(null, file, "messages");
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

    public FileConfiguration getConfig() {
        return config;
    }

    public Locale getLocale(String s) {
        s = s.toLowerCase();
        for (Locale l : locales.values()) {
            if (l.getId().equalsIgnoreCase(s) || l.getAliases().contains(s) || s.equalsIgnoreCase(l.getName())) {
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

    public Locale getDefault() {
        return getLocale(configManager.getConfig().getString("default.locale"));
    }

    public void sendUsage(CommandSender sender, Command cmd, Locale locale) {
        String command = cmd.getName();
        FileConfiguration config = locale.getConfig();
        if (!(sender instanceof Player)) {
            for (String s : config.getConfigurationSection(command + ".usage").getKeys(false)) {
                if (s.startsWith("console-")) {
                    sender.sendMessage(config.getString(command + ".usage." + s));
                }
            }
            return;
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString(command + ".usage.header") == null ? config.getString("usage.header") : config.getString(command + ".usage.header")));
        String style = (config.getString(command + ".usage.style") == null ? config.getString("usage.format") : config.getString(command + ".usage.style"));
        String arg = (config.getString(command + ".usage.argument") == null ? config.getString("usage.argument") : config.getString(command + ".usage.argument"));
        for (String s : config.getConfigurationSection(command + ".usage").getKeys(false)) {
            if (!s.startsWith("console-") && !s.equals("header") && !s.equals("style")) {
                if (!s.startsWith("arg")) {
                    String permission = "toolsies." + s.replace("-", ".").replaceAll("\\d", "");
                    if (sender.hasPermission(permission)) {
                        System.out.println("Command: Checking for permission: toolsies." + s.replace("-", "."));
                        String usage = config.getString(command + ".usage." + s);
                        if (sender.hasPermission(permission + ".others")) {
                            usage = usage.replace("arg-player", config.getString("common.player"));
                        } else {
                            usage = usage.replace("[arg-player] ", "").replace("<arg-player> ", "");
                        }
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', style.replace("<usages>", usage)));
                    }
                } else {
                    String val = s.replace("-", ".").replace("arg", "").replaceAll("\\d", "");
                    if (sender.hasPermission("toolsies." + (val.isEmpty() ? command : val))) {
                        System.out.println("Command: Checking for permission: toolsies." + (val.isEmpty() ? command : val));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', arg.replace("<args>", config.getString(command + ".usage." + s))));
                    }
                }
            }
        }
    }

    public String createMessage(String[] args, int startFrom) {
        StringBuilder msg = new StringBuilder();
        for (int i = startFrom; i < args.length; i++) {
            msg.append(args[i]);
            if (i <= args.length - 2) {
                msg.append(" ");
            }
        }
        return msg.toString();
    }

    public List<String> formatTabArguments(String argsStart, List<String> allarguments) {
        ArrayList<String> firstargument = new ArrayList<>();
        if (!argsStart.isEmpty()) {
            for (String arg : allarguments) {
                if (arg.toLowerCase().startsWith(argsStart.toLowerCase())) {
                    firstargument.add(arg);
                }
            }
        } else {
            return allarguments;
        }
        return firstargument;
    }

    public String translateBoolean(Locale l, boolean b) {
        return l.getConfig().getString("common.boolean." + b);
    }
}
