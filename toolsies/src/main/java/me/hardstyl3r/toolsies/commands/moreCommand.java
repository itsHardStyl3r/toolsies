package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.utils.PlayerUtils;
import me.hardstyl3r.toolsies.utils.StringUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class moreCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;
    private final FileConfiguration config;

    public moreCommand(Toolsies plugin, UserManager userManager, LocaleManager localeManager, FileConfiguration config) {
        plugin.getCommand("more").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player p)) {
            sender.sendMessage(l.getStringComponent("console_sender"));
            return true;
        }
        if (!sender.hasPermission("toolsies.more")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.more")));
            return true;
        }
        if (args.length > 1) {
            localeManager.sendUsage(sender, cmd, l);
            return true;
        }
        ItemStack item = PlayerUtils.getItemInEitherHand(p);
        if (item.getType() == Material.AIR) {
            sender.sendMessage(l.getStringComponent("more.empty_hand"));
            return true;
        }
        if (isDisallowed(item) && !sender.hasPermission("toolsies.more.bypass")) {
            sender.sendMessage(l.getStringComponent("more.excluded_item"));
            return true;
        }
        int newAmount = item.getMaxStackSize();
        int currentAmount = item.getAmount();
        if (args.length == 1 && sender.hasPermission("toolsies.more.bypass")) {
            if (!StringUtils.isNumeric(args[0])) {
                sender.sendMessage(l.getStringComponent("more.not_numeric"));
                return true;
            }
            int enteredAmount = Integer.parseInt(args[0]);
            newAmount = (enteredAmount > 127 ? (sender.hasPermission("toolsies.more.exceedstack") ? 127 : 64) : enteredAmount);
            if (currentAmount == newAmount) {
                sender.sendMessage(l.getStringComponent("more.same_amount",
                        Formatter.choice("choice", 1)));
                return true;
            }
        }
        if (currentAmount == newAmount) {
            sender.sendMessage(l.getStringComponent("more.same_amount",
                    Formatter.choice("choice", 0)));
            return true;
        }
        sender.sendMessage(l.getStringComponent("more.set_amount",
                Formatter.choice("amounttype", (currentAmount > newAmount ? 0 : 1)),
                Placeholder.unparsed("newamount", String.valueOf(newAmount))));
        item.setAmount(newAmount);
        p.updateInventory();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.more") && sender instanceof Player) {
            if (args.length == 1)
                return Collections.singletonList(localeManager.formatArgument(
                        userManager.determineLocale(sender).getString("common.amount"), true));
        }
        return Collections.emptyList();
    }

    private boolean isDisallowed(ItemStack item) {
        String itemName = item.getType().toString();
        if (config.getBoolean("more.excludeItems")) {
            if (config.getStringList("more.excludedItems").contains(itemName)) return true;
            for (String disallowed : config.getStringList("more.excludedItemsStartsWith"))
                if (!disallowed.isBlank() && itemName.startsWith(disallowed)) return true;
            for (String disallowed : config.getStringList("more.excludedItemsEndsWith"))
                if (!disallowed.isBlank() && itemName.endsWith(disallowed)) return true;
        }
        return false;
    }
}
