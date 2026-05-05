package com.chaosdirector.uhcscenarios;

import org.bukkit.Material;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class SaludListener implements Listener {

    private final UHCScenarios plugin;
    private final Random random = new Random();

    public SaludListener(UHCScenarios plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (plugin.isScenarioEnabled("goldenfleece") && event.getEntity() instanceof Sheep) {
            int chance = random.nextInt(100);
            if (chance < 10) { // 10% chance
                event.getDrops().add(new ItemStack(Material.GOLD_INGOT));
            } else if (chance < 15) { // 5% chance extra para manzana
                event.getDrops().add(new ItemStack(Material.GOLDEN_APPLE));
            }
        }
        
        // CutClean Mobs (Food)
        if (plugin.isScenarioEnabled("cutclean")) {
            for (ItemStack drop : event.getDrops()) {
                switch (drop.getType()) {
                    case BEEF: drop.setType(Material.COOKED_BEEF); break;
                    case PORKCHOP: drop.setType(Material.COOKED_PORKCHOP); break;
                    case CHICKEN: drop.setType(Material.COOKED_CHICKEN); break;
                    case MUTTON: drop.setType(Material.COOKED_MUTTON); break;
                    case RABBIT: drop.setType(Material.COOKED_RABBIT); break;
                }
            }
        }
    }
}
