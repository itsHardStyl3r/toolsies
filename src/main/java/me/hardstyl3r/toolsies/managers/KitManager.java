package me.hardstyl3r.toolsies.managers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class KitManager {

    private FileConfiguration config;

    public KitManager(ConfigManager configManager) {
        config = configManager.loadConfig("kits");
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public boolean isKit(String kit) {
        return getConfig().get("kits." + kit.toLowerCase()) != null;
    }

    public Set<String> getKits() {
        return getConfig().getConfigurationSection("kits").getKeys(false);
    }

    public void giveKit(Player p, String kit) {
        kit = kit.toLowerCase();
        for (String kitMaterial : getConfig().getConfigurationSection("kits." + kit + ".items").getKeys(false)) {
            Material material = Material.matchMaterial(kitMaterial);
            if (material == null) {
                System.out.println("Kit " + kit + ": " + kitMaterial + " is not correct Material.");
                return;
            }
            String amount = getConfig().getString("kits." + kit + ".items." + kitMaterial + ".amount");
            if (Integer.valueOf(amount) == null || Integer.parseInt(amount) <= 0) {
                System.out.println("Kit " + kit + ": Amount (" + amount + ") of " + kitMaterial + " is wrong.");
                return;
            }
            ItemStack item = new ItemStack(material, Integer.parseInt(amount));
            ItemMeta im = item.getItemMeta();
            List<String> lore = getConfig().getStringList("kits." + kit + ".items." + kitMaterial + ".lore");
            if (!lore.isEmpty()) {
                List<String> colorlore = new ArrayList<>();
                for (String s : lore) {
                    colorlore.add(ChatColor.translateAlternateColorCodes('&', s)
                            .replace("<name>", p.getName()).replace("<date>", String.valueOf(System.currentTimeMillis())));
                }
                im.setLore(colorlore);
            }
            String name = getConfig().getString("kits." + kit + ".items." + kitMaterial + ".name");
            if (name != null) {
                im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            }
            p.getInventory().addItem(item);
        }
    }
}
