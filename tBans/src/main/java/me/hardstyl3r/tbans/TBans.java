package me.hardstyl3r.tbans;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.utils.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TBans extends JavaPlugin {

    private static TBans instance;
    private Toolsies toolsies;

    @Override
    public void onEnable() {
        instance = this;
        toolsies = (Toolsies) Bukkit.getServer().getPluginManager().getPlugin("toolsies");
        if (!toolsies.isEnabled() || toolsies == null) {
            LogUtil.warn("Could not hook into toolsies.");
            this.setEnabled(false);
        }
        double version = Double.parseDouble(toolsies.getDescription().getVersion().split("-")[0]);
        if (version < 0.9) {
            LogUtil.error("Unsupported toolsies version.");
            this.setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
    }

    public static TBans getInstance() {
        return instance;
    }
}
