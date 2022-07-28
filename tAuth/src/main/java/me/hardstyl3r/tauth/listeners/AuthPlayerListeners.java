package me.hardstyl3r.tauth.listeners;

import me.hardstyl3r.tauth.TAuth;
import me.hardstyl3r.tauth.enums.AuthType;
import me.hardstyl3r.tauth.managers.LoginManager;
import me.hardstyl3r.tauth.objects.AuthUser;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class AuthPlayerListeners implements Listener {

    private final FileConfiguration config;
    private final LoginManager loginManager;
    private final UserManager userManager;
    private final LocaleManager localeManager;

    public AuthPlayerListeners(TAuth plugin, FileConfiguration config, UserManager userManager, LoginManager loginManager, LocaleManager localeManager) {
        this.config = config;
        this.userManager = userManager;
        this.loginManager = loginManager;
        this.localeManager = localeManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLoginLowest(AsyncPlayerPreLoginEvent e) {
        String name = e.getName();
        if (!name.matches("^[\\w.]+$")) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    userManager.determineLocale(e.getUniqueId()).getColoredString("login.wrong_nick"));
        }
        if (name.length() < config.getInt("login.minNicknameLength")) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    userManager.determineLocale(e.getUniqueId()).getColoredString("login.nick_too_short"));
        }
        if (name.length() > config.getInt("login.maxNicknameLength")) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    userManager.determineLocale(e.getUniqueId()).getColoredString("login.nick_too_long"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLoginHighest(AsyncPlayerPreLoginEvent e) {
        if ((Bukkit.getPlayerExact(e.getName()) != null || Bukkit.getPlayer(e.getUniqueId()) != null) && !config.getBoolean("login.allowJoinWhenOnline")) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    userManager.determineLocale(e.getUniqueId()).getColoredString("login.online_kick"));
            return;
        }
        //See: LoginManager.getAuth();
        AuthUser authUser = loginManager.getAuth(e.getName());
        if (authUser != null && !authUser.getName().equals(e.getName())) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    userManager.determineLocale(e.getUniqueId()).getColoredString("login.wrong_nick_casing"));
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        if (e.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            Player p = e.getPlayer();
            if (userManager.getUser(p) != null) {
                if ((config.getBoolean("login.allowOperatorsOnFullServer") && p.isOp())) {
                    p.sendMessage(userManager.determineLocale(p).getColoredString("login.full_server_join"));
                    e.allow();
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        AuthUser authUser = loginManager.getAuth(p);
        if (authUser.isLoggedIn()) {
            long chatDifference = localeManager.parseTimeFromString(config.getString("login.chat.minimumPlaytimeToChat")) - authUser.getPlaytime();
            if (chatDifference >= 0) {
                Locale l = userManager.determineLocale(p);
                p.sendMessage(l.getColoredString("auth.playtime_chat").replace("<time>", localeManager.parseTimeWithTranslate(chatDifference, l)));
                e.setCancelled(true);
            }
        } else {
            if (!config.getBoolean("login.chat.allowUnauthorisedToReceiveChat"))
                for (Player rec : loginManager.getOnlineUnauthed()) e.getRecipients().remove(rec);
            if (config.getBoolean("login.chat.notifyUnauthorisedOnChat"))
                p.sendMessage(userManager.determineLocale(p).getColoredString(authUser.isRegistered() ? "login.login" : "register.register"));
            if (!config.getBoolean("login.chat.allowUnauthorisedToChat")) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        AuthUser login = loginManager.getAuth(p);
        String command = e.getMessage().substring(1).split(" ")[0].toLowerCase();
        if (!login.isRegistered()) {
            if (!loginManager.getAllowedCommands(AuthType.REGISTER).contains(command)) {
                if (config.getBoolean("login.commands.kickOnDisallowedCommand")) {
                    p.kickPlayer(userManager.determineLocale(p).getColoredString("register.command_kick"));
                } else {
                    p.sendMessage(userManager.determineLocale(p).getColoredString("register.register"));
                }
                e.setCancelled(true);
            }
        } else if (!login.isLoggedIn()) {
            if (!loginManager.getAllowedCommands(AuthType.LOGIN).contains(command)) {
                if (config.getBoolean("login.commands.kickOnDisallowedCommand")) {
                    p.kickPlayer(userManager.determineLocale(p).getColoredString("login.command_kick"));
                } else {
                    p.sendMessage(userManager.determineLocale(p).getColoredString("login.login"));
                }
                e.setCancelled(true);
            }
        }
    }
}
