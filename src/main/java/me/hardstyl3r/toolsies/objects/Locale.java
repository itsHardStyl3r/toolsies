package me.hardstyl3r.toolsies.objects;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Locale {

    private final String id;
    private final FileConfiguration config;
    private List<String> aliases;
    private String name;

    public Locale(String id, FileConfiguration config){
        this.id = id;
        this.config = config;
    }

    public String getId() {
        return id;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void setAliases(List<String> l){
        this.aliases = l;
    }

    public List<String> getAliases(){
        return aliases;
    }

    public void setName(String s){
        this.name = s;
    }

    public String getName() {
        return name;
    }

    public String toString(){
        return getName();
    }
}
