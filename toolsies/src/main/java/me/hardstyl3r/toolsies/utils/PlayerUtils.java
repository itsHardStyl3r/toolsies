package me.hardstyl3r.toolsies.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerUtils {

    /**
     * A method to get item which player holds in either hand.
     * Prioritizes main hand.
     *
     * @param p A player to get the item from
     * @return Item in either hand or 1 AIR if both are empty.
     */
    public static ItemStack getItemInEitherHand(Player p) {
        ItemStack mainHand = p.getInventory().getItemInMainHand();
        ItemStack offHand = p.getInventory().getItemInOffHand();
        if (mainHand.getType().isAir()) {
            if (offHand.getType().isAir())
                return new ItemStack(Material.AIR, 1);
            return offHand;
        }
        return mainHand;
    }
}
