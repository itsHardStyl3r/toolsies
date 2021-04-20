package me.hardstyl3r.tperms;

import me.hardstyl3r.toolsies.Toolsies;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TPerms extends JavaPlugin {

    private Toolsies toolsies;
    private double SUPPORTED_TOOLSIES = 0.6;

    @Override
    public void onEnable() {
        toolsies = (Toolsies) Bukkit.getServer().getPluginManager().getPlugin("toolsies");
        if (!toolsies.isEnabled() || toolsies == null) {
            System.out.println("Could not hook into toolsies.");
            this.setEnabled(false);
        }

        /*
        The expected versioning of toolsies is:
        0.00-ALPHA/BETA/RC-00 and what we're interested in is 0.00.
         */
        double version = Double.parseDouble(toolsies.getDescription().getVersion().split("-")[0]);
        if (version < SUPPORTED_TOOLSIES) {
            System.out.println("Unsupported toolsies version.");
            this.setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
    }
}
