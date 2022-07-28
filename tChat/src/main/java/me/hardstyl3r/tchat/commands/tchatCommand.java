package me.hardstyl3r.tchat.commands;

import me.hardstyl3r.tchat.TChat;
import me.hardstyl3r.tchat.managers.ChatManager;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class tchatCommand implements CommandExecutor, TabCompleter {

    private final LocaleManager localeManager;
    private final UserManager userManager;
    private final ChatManager chatManager;

    public tchatCommand(TChat plugin, UserManager userManager, LocaleManager localeManager, ChatManager chatManager) {
        plugin.getCommand("tchat").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
        this.chatManager = chatManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.tchat")) {
            sender.sendMessage(l.getColoredString("no_permission").replace("<permission>", "toolsies.tchat"));
            return true;
        }
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("cooldown")) {
                if (args[1].equalsIgnoreCase("chat")) {
                    if (args.length == 3) {
                        if (args[2].equalsIgnoreCase("toggle")) {
                            chatManager.toggleChatCooldown();
                            sender.sendMessage(l.getColoredString("tchat.cooldown.chat.toggle")
                                    .replace("<status>", localeManager.translateBoolean(l, chatManager.isChatCooldownEnabled())));
                            return true;
                        } else if (args[2].equalsIgnoreCase("clear")) {
                            chatManager.resetChatCooldowns();
                            sender.sendMessage(l.getColoredString("tchat.cooldown.chat.clear"));
                            return true;
                        }
                    }
                    sender.sendMessage(l.getColoredString("tchat.cooldown.chat.status")
                            .replace("<status>", localeManager.translateBoolean(l, chatManager.isChatCooldownEnabled()))
                            .replace("<default>", localeManager.translateBoolean(l, chatManager.getChatCooldownDefaultState())));
                } else
                    localeManager.sendUsage(sender, cmd, l);
            } else
                localeManager.sendUsage(sender, cmd, l);
        } else
            localeManager.sendUsage(sender, cmd, l);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (sender.hasPermission("toolsies.tchat")) {
            if (args.length == 1)
                return Collections.singletonList("cooldown");
            else if (args.length == 2)
                return Collections.singletonList("chat");
            else if (args.length == 3 && (args[1].equalsIgnoreCase("chat")))
                return new ArrayList<>(localeManager.formatTabArguments(args[1], Arrays.asList("clear", "toggle")));
        }
        return Collections.emptyList();
    }
}
