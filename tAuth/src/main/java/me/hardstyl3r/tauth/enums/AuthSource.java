package me.hardstyl3r.tauth.enums;

public enum AuthSource {
    SERVER,
    WEBPANEL;

    public String getName() {
        return this.name().toLowerCase();
    }
}