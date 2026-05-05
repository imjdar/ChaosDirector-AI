package com.chaosdirector.uhcscenarios;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UHCConfigGUI {

    private final UHCScenarios plugin;
    public static final String TITLE = "§8Configuración de UHC";

    public UHCConfigGUI(UHCScenarios plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        
        // Llenar bordes decorativos
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gMeta = glass.getItemMeta();
        if (gMeta != null) {
            gMeta.setDisplayName(" ");
            glass.setItemMeta(gMeta);
        }
        
        int[] borders = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,50,51,52,53};
        for (int i : borders) inv.setItem(i, glass);

        int slot = 10;
        for (String scenario : plugin.getScenarios().keySet()) {
            // Asegurarse de no pisar bordes
            while (isBorder(slot)) slot++;
            
            boolean enabled = plugin.isScenarioEnabled(scenario);
            inv.setItem(slot, createItem(scenario, enabled));
            slot++;
        }

        // Botones de control
        inv.setItem(47, createConfigItem(Material.MAP, "§6§lRADIO DEL BORDE", "§7Actual: §e" + plugin.getBorderSize(), "§e+500 bloques por click"));
        inv.setItem(48, createConfigItem(Material.CLOCK, "§a§lTIEMPO DE GRACIA", "§7Actual: §e" + (plugin.getGraceTime()/60) + "m", "§e+1m por click"));
        inv.setItem(50, createConfigItem(Material.COMPASS, "§c§lCIERRE DE BORDE", "§7Actual: §e" + (plugin.getShrinkTime()/60) + "m", "§e+5m por click"));
        
        inv.setItem(52, createChaosToggleButton());
        inv.setItem(53, createStartButton());
        
        player.openInventory(inv);
    }

    private ItemStack createConfigItem(Material mat, String name, String value, String action) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add(value);
            lore.add("");
            lore.add(action);
            lore.add("§7Shift-Click para resetear");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createChaosToggleButton() {
        ItemStack item = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§4§lMASHI DIRECTOR");
            List<String> lore = new ArrayList<>();
            lore.add("§7¿Quieres que el Director");
            lore.add("§7te pegue una puteada?");
            lore.add("");
            lore.add("§6¡Pilas con la IA, brother!");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean isBorder(int slot) {
        int[] borders = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,49,51};
        for (int i : borders) if (i == slot) return true;
        return false;
    }

    private ItemStack createItem(String name, boolean enabled) {
        Material mat = getMaterialForScenario(name);
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§e" + name.toUpperCase());
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Estado: " + (enabled ? "§aACTIVADO" : "§cDESACTIVADO"));
            lore.add("");
            lore.add("§b¡Haz clic para cambiar!");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createStartButton() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l¡INICIAR PARTIDA!");
            List<String> lore = new ArrayList<>();
            lore.add("§7Haz clic para cerrar el borde");
            lore.add("§7y empezar el contador.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private Material getMaterialForScenario(String name) {
        switch (name.toLowerCase()) {
            case "cutclean": return Material.IRON_INGOT;
            case "timber": return Material.OAK_LOG;
            case "hasteyboys": return Material.DIAMOND_PICKAXE;
            case "barebones": return Material.BONE;
            case "flowerpower": return Material.POPPY;
            case "timebomb": return Material.TNT;
            case "bowless": return Material.BOW;
            case "switcheroo": return Material.ENDER_PEARL;
            case "fireless": return Material.LAVA_BUCKET;
            case "diamondless": return Material.DIAMOND;
            case "blooddiamond": return Material.REDSTONE;
            case "goldenfleece": return Material.WHITE_WOOL;
            case "nightmare": return Material.ZOMBIE_HEAD;
            case "skyhigh": return Material.FEATHER;
            default: return Material.PAPER;
        }
    }
}
