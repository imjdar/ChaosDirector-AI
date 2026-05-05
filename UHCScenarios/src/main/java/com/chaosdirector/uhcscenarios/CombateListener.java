package com.chaosdirector.uhcscenarios;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CombateListener implements Listener {

    private final UHCScenarios plugin;

    public CombateListener(UHCScenarios plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location loc = player.getLocation();

        // BAREBONES DEATH DROPS
        if (plugin.isScenarioEnabled("barebones")) {
            event.getDrops().clear();
            event.getDrops().add(new ItemStack(Material.DIAMOND, 1));
            event.getDrops().add(new ItemStack(Material.GOLDEN_APPLE, 1));
            event.getDrops().add(new ItemStack(Material.ARROW, 32));
            event.getDrops().add(new ItemStack(Material.STRING, 2));
        }

        // TIMEBOMB
        if (plugin.isScenarioEnabled("timebomb")) {
            event.getDrops().clear();
            Block block = loc.getBlock();
            block.setType(Material.CHEST);
            Chest chest = (Chest) block.getState();
            Inventory inv = chest.getInventory();
            for (ItemStack item : event.getDrops()) {
                if (item != null) inv.addItem(item);
            }
            
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                block.setType(Material.AIR);
                loc.getWorld().createExplosion(loc, 4.0f, false, false);
                Bukkit.broadcastMessage("§c[Timebomb] ¡El cofre de " + player.getName() + " ha explotado!");
            }, 600L); // 30 segundos
        }
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (plugin.isScenarioEnabled("bowless") && event.getEntity() instanceof Player) {
            event.setCancelled(true);
            event.getEntity().sendMessage("§c[Bowless] Los arcos están desactivados.");
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (plugin.isScenarioEnabled("fireless") && event.getEntity() instanceof Player) {
            EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK || 
                cause == EntityDamageEvent.DamageCause.LAVA || cause == EntityDamageEvent.DamageCause.HOT_FLOOR) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (plugin.isScenarioEnabled("switcheroo") && event.getDamager() instanceof Arrow arrow && event.getEntity() instanceof Player victim) {
            if (arrow.getShooter() instanceof Player shooter) {
                Location shooterLoc = shooter.getLocation();
                Location victimLoc = victim.getLocation();
                shooter.teleport(victimLoc);
                victim.teleport(shooterLoc);
                shooter.sendMessage("§a[Switcheroo] ¡Intercambio!");
                victim.sendMessage("§c[Switcheroo] ¡Intercambio!");
            }
        }
    }
}
