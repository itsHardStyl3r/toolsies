package me.hardstyl3r.tauth.managers;

import me.hardstyl3r.tauth.enums.AuthType;
import me.hardstyl3r.tauth.events.PlayerAuthSuccessfulEvent;
import me.hardstyl3r.tauth.objects.AuthUser;
import me.hardstyl3r.toolsies.managers.LocationManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LoginManagement {

    private final LoginManager loginManager;
    private final FileConfiguration config;
    private final LocationManager locationManager;

    public LoginManagement(LoginManager loginManager, FileConfiguration config, LocationManager locationManager) {
        this.loginManager = loginManager;
        this.config = config;
        this.locationManager = locationManager;
    }

    public void performLogin(Player p, String comment) {
        AuthUser authUser = loginManager.getAuth(p);
        authUser.setLoggedIn(true);
        authUser.setLastLoginDate(System.currentTimeMillis());
        authUser.setIp(p.getAddress().getAddress().getHostAddress());

        Bukkit.getPluginManager().callEvent(new PlayerAuthSuccessfulEvent(p, authUser, AuthType.LOGIN));
        loginManager.pushAuthHistory(authUser, AuthType.LOGIN, true, comment);

        if (config.getBoolean("login.applyBlindnessToUnauthorised")) {
            p.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        if (config.getBoolean("login.useWalkSpeedFlySpeed")) {
            p.setWalkSpeed(0.2F); //0
            p.setFlySpeed(0.1F); //0
        }
        p.setCollidable(true);
        p.teleport(authUser.getLastLocation());

        loginManager.stopKickTask(p);
        loginManager.updateAuth(authUser);
    }

    public void performLogin(Player p) {
        performLogin(p, null);
    }

    public void performLogout(Player p, String comment) {
        AuthUser authUser = loginManager.getAuth(p);

        loginManager.pushAuthHistory(authUser, AuthType.LOGOUT, true, comment);
        Bukkit.getPluginManager().callEvent(new PlayerAuthSuccessfulEvent(p, authUser, AuthType.LOGOUT));

        performQuit(p);
        initAuth(p);

        loginManager.setKickTask(p, AuthType.LOGOUT);
    }

    public void performLogout(Player p) {
        performLogout(p, null);
    }

    public void performQuit(Player p) {
        AuthUser authUser = loginManager.getAuth(p);
        if (!authUser.isRegistered()) return;
        if (authUser.isLoggedIn()) authUser.setLastLocation(p.getLocation());
        authUser.setPlaytime(authUser.getPlaytime());
        authUser.setLoggedIn(false);
        loginManager.updateAuth(authUser);
    }

    public void initAuth(Player p) {
        if (config.getBoolean("login.applyBlindnessToUnauthorised")) {
            PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 99999, 2);
            p.addPotionEffect(blindness);
        }
        if (config.getBoolean("login.teleportUnauthorisedToSpawn")) {
            p.teleport(locationManager.getDefaultSpawn().getLocation());
        }
        if (config.getBoolean("login.useWalkSpeedFlySpeed")) {
            p.setWalkSpeed(0F); //0.2
            p.setFlySpeed(0F); //0.1
        }
        p.setCollidable(false);
    }

    /*public void performRegistration(String name, String password){

    }

    public void performRegistration(Player p, String password){
        performRegistration(p.getName(), password);
    }

    public void performRegistration(AuthUser authUser, String password){
        performRegistration(authUser.getName(), password);
    }*/
}
