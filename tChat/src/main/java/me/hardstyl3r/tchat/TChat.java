package me.hardstyl3r.tchat;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.utils.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TChat extends JavaPlugin {

    private static TChat instance;
    private Toolsies toolsies;

    public static TChat getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        long current = System.currentTimeMillis();
        instance = this;
        try {
            toolsies = (Toolsies) Bukkit.getServer().getPluginManager().getPlugin("toolsies");
            if (!toolsies.isEnabled() || toolsies == null)
                throw new Exception("toolsies is null or not enabled");
            double version = Double.parseDouble(toolsies.getDescription().getVersion().split("-")[0]);
            if (version < 0.10)
                throw new Exception("unsupported toolsies version (<0.10)");
        } catch (Exception e) {
            LogUtil.error("[tChat] Could not hook into toolsies: " + e + ". Disabling.");
            this.setEnabled(false);
        }
        LogUtil.info("[tChat] Enabled tChat. (took " + (System.currentTimeMillis() - current) + "ms)");
    }

    @Override
    public void onDisable() {
    }
}
