package me.hardstyl3r.tauth.listeners;

import me.hardstyl3r.tauth.TAuth;
import me.hardstyl3r.tauth.managers.LoginManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class UnAuthPlayerListeners implements Listener {

    private final LoginManager loginManager;

    public UnAuthPlayerListeners(TAuth plugin, LoginManager loginManager) {
        this.loginManager = loginManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        Location from = e.getFrom();
        Location to = e.getTo();
        if ((from.getX() != to.getX()) || (from.getY() != to.getY()) || (from.getZ() != to.getZ()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onMove(EntityTeleportEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (loginManager.getAuth(p).isLoggedIn()) return;
            e.setCancelled(true);
        }
    }

    /*@EventHandler
    public void onMove(PlayerTeleportEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.getPlayer().sendMessage("I've been blocked: PlayerTeleportEvent");
        e.setCancelled(true);
    }*/

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (loginManager.getAuth((Player) e.getPlayer()).isLoggedIn()) return;
        e.getPlayer().closeInventory();
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryOpen(InventoryClickEvent e) {
        if (loginManager.getAuth((Player) e.getWhoClicked()).isLoggedIn()) return;
        e.getWhoClicked().closeInventory();
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent e) {
        if (loginManager.getAuth((Player) e.getWhoClicked()).isLoggedIn()) return;
        e.getWhoClicked().closeInventory();
        e.setCancelled(true);
    }


    @EventHandler
    public void onAirChange(EntityAirChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            if (loginManager.getAuth((Player) e.getEntity()).isLoggedIn()) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onSign(SignChangeEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent e) {
        if (loginManager.getAuth(e.getEnchanter()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onEnchantPrepare(PrepareItemEnchantEvent e) {
        if (loginManager.getAuth(e.getEnchanter()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            if (loginManager.getAuth((Player) e.getEntity()).isLoggedIn()) return;
            e.setCancelled(true);
        }
        if (e.getDamager() instanceof Player) {
            if (loginManager.getAuth((Player) e.getDamager()).isLoggedIn()) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            if (loginManager.getAuth((Player) e.getEntity()).isLoggedIn()) return;
            e.getEntity().setFireTicks(0);
            e.setDamage(0);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player) {
            if (loginManager.getAuth((Player) e.getEntity()).isLoggedIn()) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent e) {
        if (e.getTarget() instanceof Player) {
            if (loginManager.getAuth((Player) e.getTarget()).isLoggedIn()) return;
            e.setTarget(null);
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent e) {
        if (e.getTarget() instanceof Player) {
            if (loginManager.getAuth((Player) e.getTarget()).isLoggedIn()) return;
            e.setTarget(null);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingEntityBreak(HangingBreakByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            if (loginManager.getAuth((Player) e.getRemover()).isLoggedIn()) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingEntityPlace(HangingPlaceEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            if (loginManager.getAuth((Player) e.getWhoClicked()).isLoggedIn()) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onAnimation(PlayerAnimationEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBookEdit(PlayerEditBookEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onItemMend(PlayerItemMendEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onShear(PlayerShearEntityEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onHandSwap(PlayerSwapHandItemsEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onToggleSprint(PlayerToggleSprintEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onUnleashEntity(PlayerUnleashEntityEvent e) {
        if (loginManager.getAuth(e.getPlayer()).isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent e) {
        if (e.getEntered() instanceof Player) {
            if (loginManager.getAuth((Player) e.getEntered()).isLoggedIn()) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent e) {
        if (e.getExited() instanceof Player) {
            if (loginManager.getAuth((Player) e.getExited()).isLoggedIn()) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof Player) {
            if (loginManager.getAuth((Player) e.getEntity()).isLoggedIn()) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityFoodLevelChange(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            if (loginManager.getAuth((Player) e.getEntity()).isLoggedIn()) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent e) {
        if (e.getEntity() instanceof Player) {
            if (loginManager.getAuth((Player) e.getEntity()).isLoggedIn()) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityRegainHealth(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player) {
            if (loginManager.getAuth((Player) e.getEntity()).isLoggedIn()) return;
            e.setCancelled(true);
        }
    }
}
