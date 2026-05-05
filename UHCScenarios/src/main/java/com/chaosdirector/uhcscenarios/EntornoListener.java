package com.chaosdirector.uhcscenarios;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EntornoListener implements Listener {

    private final UHCScenarios plugin;

    public EntornoListener(UHCScenarios plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        if (!plugin.isScenarioEnabled("nightmare")) return;
        
        if (event.getEntity() instanceof LivingEntity entity) {
            if (entity instanceof Zombie zombie) {
                AttributeInstance speed = zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                if (speed != null) speed.setBaseValue(speed.getBaseValue() * 1.5);
                zombie.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!plugin.isScenarioEnabled("nightmare")) return;

        if (event.getDamager() instanceof Spider && event.getEntity() instanceof Player player) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
        }
    }
}
