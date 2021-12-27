package me.hardstyl3r.tauth.objects;

import org.bukkit.Location;

import java.util.UUID;

public class AuthUser {

    private final UUID uuid;
    private String name;
    private String password;
    private String ip;
    private String regIp;
    private Long regdate;
    private Long lastlogin;
    private boolean loggedIn;
    private boolean hasSession;
    private Location lastLocation;
    private String email;
    private Long playtime;

    private boolean isRegistered;

    public AuthUser(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean b) {
        this.loggedIn = b;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean b) {
        this.isRegistered = b;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getRegisterIp() {
        return this.regIp;
    }

    public void setRegisterIp(String ip) {
        this.regIp = ip;
    }

    public Long getRegisterDate() {
        return this.regdate;
    }

    public void setRegisterDate(Long regdate) {
        this.regdate = regdate;
    }

    public Long getLastLoginDate() {
        return this.lastlogin;
    }

    public void setLastLoginDate(Long lastlogin) {
        this.lastlogin = lastlogin;
    }

    public void setHasSession(Boolean hasSession) {
        this.hasSession = hasSession;
    }

    public Boolean hasSession() {
        return this.hasSession;
    }

    public Location getLastLocation() {
        return this.lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        if (email != null) email = email.toLowerCase();
        this.email = email;
    }

    public Long getPlaytime() {
        return (this.playtime == null ? 0L : this.playtime) + getSessionDuration();
    }

    public void setPlaytime(Long l) {
        this.playtime = l;
    }

    public Long getSessionDuration() {
        if (!this.loggedIn) return 0L;
        return System.currentTimeMillis() - this.lastlogin;
    }
}
