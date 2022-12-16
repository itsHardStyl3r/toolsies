package me.hardstyl3r.toolsies.managers;

import me.hardstyl3r.toolsies.utils.LogUtil;
import me.hardstyl3r.toolsies.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class KitManager {

    private final FileConfiguration config;

    public KitManager(ConfigManager configManager) {
        config = configManager.loadConfig(null, "kits");
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public boolean isKit(String kit) {
        return getKits().contains(kit);
    }

    public Set<String> getKits() {
        if (config.getConfigurationSection("kits") == null) {
            return Collections.emptySet();
        } else {
            return config.getConfigurationSection("kits").getKeys(false);
        }
    }

    public Set<String> getKits(CommandSender sender) {
        Set<String> kits = getKits();
        for (String s : getKits()) {
            if (!sender.hasPermission("toolsies.kits." + s)) {
                kits.remove(s);
            }
        }
        return kits;
    }

    public void giveKit(Player p, String kit) {
        kit = kit.toLowerCase();
        for (String kitMaterial : config.getConfigurationSection("kits." + kit + ".items").getKeys(false)) {
            Material material = Material.matchMaterial((kitMaterial.contains("-") ? kitMaterial.split("-")[0] : kitMaterial));
            if (material == null) {
                LogUtil.warn("[toolsies] Kit " + kit + ": " + kitMaterial + " is not correct Material.");
                continue;
            }
            String amount = config.getString("kits." + kit + ".items." + kitMaterial + ".amount");
            if (Integer.valueOf(amount) == null || Integer.parseInt(amount) <= 0) {
                LogUtil.warn("[toolsies] Kit " + kit + ": Amount (" + amount + ") of " + kitMaterial + " is wrong.");
                continue;
            }
            ItemStack item = new ItemStack(material, Integer.parseInt(amount));
            ItemMeta im = item.getItemMeta();
            List<String> lore = config.getStringList("kits." + kit + ".items." + kitMaterial + ".lore");
            if (!lore.isEmpty()) {
                List<String> colorlore = new ArrayList<>();
                for (String s : lore) {
                    colorlore.add(StringUtils.translateBothColorCodes(s)
                            .replace("<name>", p.getName()).replace("<date>", String.valueOf(System.currentTimeMillis())));
                }
                im.setLore(colorlore);
            }
            String name = config.getString("kits." + kit + ".items." + kitMaterial + ".name");
            if (name != null) {
                im.setDisplayName(StringUtils.translateBothColorCodes(name));
            }
            item.setItemMeta(im);
            p.getInventory().addItem(item);
        }
    }
}
