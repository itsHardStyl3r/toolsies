package me.hardstyl3r.toolsies.managers;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.utils.LogUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocaleManager {

    private final ConfigManager configManager;
    private final FileConfiguration config;
    private final StringBuilder sb = new StringBuilder();

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
            } else {
                LogUtil.info("loadLocales(): Could not find messages-" + s + ".");
            }
        }
        LogUtil.info("loadLocales(): Loaded " + locales.size() + " locales.");
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
        if (!(sender instanceof Player)) {
            for (String s : locale.getConfigurationSection(command + ".usage")) {
                if (s.startsWith("console-")) {
                    sender.sendMessage(locale.getString(command + ".usage." + s));
                }
            }
            return;
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', locale.getString(command + ".usage.header") == null ? locale.getString("usage.header") : locale.getString(command + ".usage.header")));
        String style = (locale.getString(command + ".usage.style") == null ? locale.getString("usage.format") : locale.getString(command + ".usage.style"));
        String arg = (locale.getString(command + ".usage.argument") == null ? locale.getString("usage.argument") : locale.getString(command + ".usage.argument"));
        for (String s : locale.getConfigurationSection(command + ".usage")) {
            if (!s.startsWith("console-") && !s.equals("header") && !s.equals("style")) {
                if (!s.startsWith("arg")) {
                    String permission = "toolsies." + s.replace("-", ".").replaceAll("\\d", "");
                    if (sender.hasPermission(permission)) {
                        LogUtil.info("[toolsies] Command: Checking for permission: toolsies." + s.replace("-", "."));
                        String usage = locale.getString(command + ".usage." + s);
                        if (sender.hasPermission(permission + ".others")) {
                            usage = usage.replace("arg-player", locale.getString("common.player"));
                        } else {
                            usage = usage.replace("[arg-player] ", "").replace("<arg-player> ", "");
                        }
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', style.replace("<usages>", usage)));
                    }
                } else {
                    String val = s.replace("-", ".").replace("arg", "").replaceAll("\\d", "");
                    if (sender.hasPermission("toolsies." + (val.isEmpty() ? command : val))) {
                        LogUtil.info("[toolsies] Command: Checking for permission: toolsies." + (val.isEmpty() ? command : val));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', arg.replace("<args>", locale.getString(command + ".usage." + s))));
                    }
                }
            }
        }
    }

    public String createMessage(String[] args, int startFrom) {
        reuseStringBuilder();
        for (int i = startFrom; i < args.length; i++) {
            sb.append(args[i]);
            if (i <= args.length - 2) {
                sb.append(" ");
            }
        }
        return sb.toString();
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
        return l.getString("common.boolean." + b);
    }

    public String formatArgument(String argument, boolean required) {
        return (required ? "<" : "[") + argument + (required ? ">" : "]");
    }

    public StringBuilder getStringBuilder(boolean clean){
        if(clean) reuseStringBuilder();
        return sb;
    }

    private void reuseStringBuilder(){
        sb.setLength(0);
    }

    public String getFullDate(long czas) {
        return new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss").format(new Date(czas));
    }

    public String getDate(long czas) {
        return new SimpleDateFormat("dd.MM.yyyy").format(new Date(czas));
    }

    public String getFullTime(long czas) {
        return new SimpleDateFormat("HH:mm:ss").format(new Date(czas));
    }

    public String getTime(long czas) {
        return new SimpleDateFormat("HH:mm").format(new Date(czas));
    }

    public String parseTimeWithTranslate(long millis, Locale l) {
        reuseStringBuilder();
        if (millis == 0L) {
            return "0";
        }
        sb.setLength(0);

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        if (days > 0L) {
            millis -= TimeUnit.DAYS.toMillis(days);
            if (l.getString("common.time.days." + plural((int) days)) == null) {
                sb.append(days).append(" ").append(l.getString("common.time.days." + (plural((int) days) - 1)));
            } else {
                sb.append(days).append(" ").append(l.getString("common.time.days." + plural((int) days)));
            }
        }
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        if (hours > 0L) {
            if (days > 0L) {
                sb.append(" ");
            }
            millis -= TimeUnit.HOURS.toMillis(hours);
            if (l.getString("common.time.hours." + plural((int) hours)) == null) {
                sb.append(hours).append(" ").append(l.getString("common.time.hours." + (plural((int) hours) - 1)));
            } else {
                sb.append(hours).append(" ").append(l.getString("common.time.hours." + plural((int) hours)));
            }
        }
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        if (minutes > 0L) {
            if (hours > 0L) {
                sb.append(" ");
            }
            millis -= TimeUnit.MINUTES.toMillis(minutes);
            if (l.getString("common.time.minutes." + plural((int) minutes)) == null) {
                sb.append(minutes).append(" ").append(l.getString("common.time.minutes." + (plural((int) minutes) - 1)));
            } else {
                sb.append(minutes).append(" ").append(l.getString("common.time.minutes." + plural((int) minutes)));
            }
        }
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        if (seconds > 0L) {
            if (minutes > 0L) {
                sb.append(" ");
            }
            if (l.getString("common.time.seconds." + plural((int) seconds)) == null) {
                sb.append(seconds).append(" ").append(l.getString("common.time.seconds." + (plural((int) seconds) - 1)));
            } else {
                sb.append(seconds).append(" ").append(l.getString("common.time.seconds." + plural((int) seconds)));
            }
        }
        return sb.toString();
    }

    public long parseTimeFromString(String s) {
        Pattern pattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[, \\s]*)?(?:([0-9]+)\\s*mo[a-z]*[, \\s]*)?(?:([0-9]+)\\s*d[a-z]*[, \\s]*)?(?:([0-9]+)\\s*h[a-z]*[, \\s]*)?(?:([0-9]+)\\s*m[a-z]*[, \\s]*)?(?:([0-9]+)\\s*(?:s[a-z]*)?)?");
        Matcher matcher = pattern.matcher(s);
        long czas = 0L;
        boolean found = false;
        while (matcher.find()) {
            if ((matcher.group() != null) && (!matcher.group().isEmpty())) {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    if ((matcher.group(i) != null) && (!matcher.group(i).isEmpty())) {
                        found = true;
                        break;
                    }
                }
                if ((matcher.group(1) != null) && (!matcher.group(1).isEmpty())) {
                    czas += 31536000L * Integer.parseInt(matcher.group(1));
                }
                if ((matcher.group(2) != null) && (!matcher.group(2).isEmpty())) {
                    czas += 2592000L * Integer.parseInt(matcher.group(2));
                }
                if ((matcher.group(3) != null) && (!matcher.group(3).isEmpty())) {
                    czas += 86400L * Integer.parseInt(matcher.group(3));
                }
                if ((matcher.group(4) != null) && (!matcher.group(4).isEmpty())) {
                    czas += 3600L * Integer.parseInt(matcher.group(4));
                }
                if ((matcher.group(5) != null) && (!matcher.group(5).isEmpty())) {
                    czas += 60L * Integer.parseInt(matcher.group(5));
                }
                if ((matcher.group(6) != null) && (!matcher.group(6).isEmpty())) {
                    czas += Integer.parseInt(matcher.group(6));
                }
            }
        }
        if (!found) {
            return -1L;
        }
        return czas * 1000L;
    }

    public boolean isValidStringTime(String s) {
        Pattern pattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[, \\s]*)?(?:([0-9]+)\\s*mo[a-z]*[, \\s]*)?(?:([0-9]+)\\s*d[a-z]*[, \\s]*)?(?:([0-9]+)\\s*h[a-z]*[, \\s]*)?(?:([0-9]+)\\s*m[a-z]*[, \\s]*)?(?:([0-9]+)\\s*(?:s[a-z]*)?)?");
        Matcher matcher = pattern.matcher(s);
        return matcher.find();
    }

    private int plural(Integer value) {
        if (value == 1) {
            return 1;
        } else if (value % 10 >= 2 && value % 10 <= 4 && (value % 100 < 10 || value % 100 >= 20)) {
            return 2;
        } else {
            return 3;
        }
    }
}
