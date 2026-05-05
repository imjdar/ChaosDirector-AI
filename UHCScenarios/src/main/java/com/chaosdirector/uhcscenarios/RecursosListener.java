package com.chaosdirector.uhcscenarios;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RecursosListener implements Listener {

    private final UHCScenarios plugin;
    private final Random random = new Random();

    public RecursosListener(UHCScenarios plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // CUTCLEAN
        if (plugin.isScenarioEnabled("cutclean")) {
            handleCutClean(event);
        }

        // TIMBER
        if (plugin.isScenarioEnabled("timber") && isLog(block.getType())) {
            breakTree(block);
        }

        // FLOWER POWER
        if (plugin.isScenarioEnabled("flowerpower") && isFlower(block.getType())) {
            event.setDropItems(false);
            Material randomMaterial = Material.values()[random.nextInt(Material.values().length)];
            while (!randomMaterial.isItem()) {
                randomMaterial = Material.values()[random.nextInt(Material.values().length)];
            }
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(randomMaterial));
        }

        // BAREBONES & BLOOD DIAMOND
        if (block.getType() == Material.DIAMOND_ORE || block.getType() == Material.DEEPSLATE_DIAMOND_ORE) {
            if (plugin.isScenarioEnabled("blooddiamond")) {
                player.damage(1.0);
            }
            if (plugin.isScenarioEnabled("barebones") || plugin.isScenarioEnabled("diamondless")) {
                event.setDropItems(false);
                if (plugin.isScenarioEnabled("barebones")) {
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.IRON_INGOT));
                } else if (plugin.isScenarioEnabled("diamondless")) {
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.GOLD_INGOT));
                }
            }
        }
        
        if (block.getType() == Material.GOLD_ORE || block.getType() == Material.DEEPSLATE_GOLD_ORE) {
            if (plugin.isScenarioEnabled("barebones")) {
                event.setDropItems(false);
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.IRON_INGOT));
            }
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!plugin.isScenarioEnabled("hasteyboys")) return;
        
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        
        Material type = item.getType();
        if (type.name().contains("PICKAXE") || type.name().contains("AXE") || type.name().contains("SHOVEL")) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.addEnchant(Enchantment.EFFICIENCY, 3, true);
                meta.addEnchant(Enchantment.UNBREAKING, 3, true);
                item.setItemMeta(meta);
            }
        }
    }

    private void handleCutClean(BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        ItemStack drop = null;
        
        switch (type) {
            case IRON_ORE: case DEEPSLATE_IRON_ORE: drop = new ItemStack(Material.IRON_INGOT); break;
            case GOLD_ORE: case DEEPSLATE_GOLD_ORE: drop = new ItemStack(Material.GOLD_INGOT); break;
            case COPPER_ORE: case DEEPSLATE_COPPER_ORE: drop = new ItemStack(Material.COPPER_INGOT, 3); break;
            default: return;
        }
        
        event.setDropItems(false);
        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
    }

    private void breakTree(Block block) {
        List<Block> logs = new ArrayList<>();
        findLogs(block, logs);
        for (Block log : logs) {
            log.breakNaturally();
        }
    }

    private void findLogs(Block start, List<Block> logs) {
        if (logs.size() > 100 || !isLog(start.getType()) || logs.contains(start)) return;
        logs.add(start);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    findLogs(start.getRelative(x, y, z), logs);
                }
            }
        }
    }

    private boolean isLog(Material m) {
        return m.name().contains("_LOG") || m.name().contains("_WOOD");
    }

    private boolean isFlower(Material m) {
        return m.name().endsWith("_FLOWER") || m == Material.DANDELION || m == Material.POPPY || 
               m == Material.BLUE_ORCHID || m == Material.ALLIUM || m == Material.AZURE_BLUET || 
               m == Material.ORANGE_TULIP || m == Material.PINK_TULIP || m == Material.RED_TULIP || 
               m == Material.WHITE_TULIP || m == Material.OXEYE_DAISY || m == Material.CORNFLOWER || 
               m == Material.LILY_OF_THE_VALLEY || m == Material.SUNFLOWER || m == Material.LILAC || 
               m == Material.ROSE_BUSH || m == Material.PEONY;
    }
}
