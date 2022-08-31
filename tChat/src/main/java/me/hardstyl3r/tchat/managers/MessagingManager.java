package me.hardstyl3r.tchat.managers;

import me.hardstyl3r.tchat.TChat;
import me.hardstyl3r.toolsies.managers.ConfigManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.utils.LogUtil;
import me.hardstyl3r.toolsies.utils.StringUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.UUID;
import java.util.WeakHashMap;

public class MessagingManager {

    private final WeakHashMap<UUID, UUID> conversations = new WeakHashMap<>();
    private final WeakHashMap<UUID, BukkitTask> clearTask = new WeakHashMap<>();
    private final TChat plugin;
    private final ConfigManager configManager;
    private final YamlConfiguration config;
    private final YamlConfiguration storage;
    private final ArrayList<String> msgtoggles = new ArrayList<>();
    private final ArrayList<String> socialspies = new ArrayList<>();
    private final UserManager userManager;

    public MessagingManager(TChat plugin, ConfigManager configManager, UserManager userManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.config = configManager.loadConfig(plugin, "config");
        this.storage = configManager.loadConfig(plugin, "storage");
        this.userManager = userManager;
        loadData();
    }

    /**
     * Setting up a conversation between two UUIDs.
     *
     * @param attacker Message sender
     * @param victim   Message recipient
     */
    public void setConversation(UUID attacker, UUID victim) {
        /* Reply logic */
        if (attacker == victim) return;
        if (conversations.get(attacker) != victim) conversations.put(attacker, victim);
        if (conversations.get(victim) != attacker && config.getBoolean("msg.backAndForthConversation") && !hasMsgToggled(victim))
            conversations.put(victim, attacker);

        /* Clear tasks */
        if (config.getString("msg.replyTimeout", "120").equals("0")) return;
        long timeout = config.getInt("msg.replyTimeout") * 20L;

        if (clearTask.containsKey(attacker)) clearTask.get(attacker).cancel();
        clearTask.put(attacker, Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> clearConversation(attacker), timeout));
        if (config.getBoolean("msg.backAndForthConversation") && !hasMsgToggled(victim)) {
            if (clearTask.containsKey(victim)) clearTask.get(victim).cancel();
            clearTask.put(victim, Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> clearConversation(victim), timeout));
        }
    }

    public UUID getConversation(UUID attacker) {
        if (!conversations.containsKey(attacker)) return null;
        return conversations.get(attacker);
    }

    public UUID getConversation(Player attacker) {
        return getConversation(attacker.getUniqueId());
    }

    public void clearConversation(UUID sender) {
        conversations.remove(sender);
    }

    private void loadData() {
        for (String s : storage.getStringList("msgtoggles")) if (StringUtils.isUUID(s)) msgtoggles.add(s);
        for (String s : storage.getStringList("socialspies")) if (StringUtils.isUUID(s)) socialspies.add(s);
        //LogUtil.info("[tChat] loadToggles(): Loaded " + msgtoggles.size() + " msg toggles and " + socialspies.size() + " spies.");
    }

    public void toggleMsgToggle(UUID uuid) {
        if (!hasMsgToggled(uuid)) msgtoggles.add(uuid.toString());
        else msgtoggles.remove(uuid.toString());
    }

    public boolean hasMsgToggled(UUID uuid) {
        return msgtoggles.contains(uuid.toString());
    }

    public void saveData() {
        storage.set("msgtoggles", msgtoggles);
        storage.set("socialspies", socialspies);
        configManager.saveConfig(plugin, storage, "storage");
    }

    public void toggleSocialspy(UUID uuid) {
        if (!hasSocialspyToggled(uuid)) socialspies.add(uuid.toString());
        else socialspies.remove(uuid.toString());
    }

    public boolean hasSocialspyToggled(UUID uuid) {
        return socialspies.contains(uuid.toString());
    }

    public void doSocialSpy(CommandSender attacker, Player victim, String message) {
        Player sender = (Player) attacker;
        for (Player socialSpy : Bukkit.getOnlinePlayers())
            if (hasSocialspyToggled(socialSpy.getUniqueId())) {
                if (!socialSpy.hasPermission("toolsies.socialspy")) {
                    LogUtil.info(socialSpy.getName() + " should not receive Socialspy anymore. Deleting.");
                    toggleSocialspy(socialSpy.getUniqueId());
                    return;
                }
                if (sender.getUniqueId().equals(socialSpy.getUniqueId()) ||
                        victim.getUniqueId().equals(socialSpy.getUniqueId()) ||
                        hasSocialspyToggled(victim.getUniqueId()))
                    return;
                Locale l = userManager.determineLocale(socialSpy);
                socialSpy.sendMessage(l.getStringComponent("socialspy.broadcast", Placeholder.unparsed("attacker", attacker.getName()), Placeholder.unparsed("victim", victim.getName()), Placeholder.unparsed("message", ChatColor.stripColor(message))));
            }
    }
}
