package com.chaosdirector.uhcscenarios;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final UHCScenarios plugin;
    private final UHCConfigGUI gui;

    public GUIListener(UHCScenarios plugin, UHCConfigGUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(UHCConfigGUI.TITLE)) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String name = clickedItem.getItemMeta().getDisplayName();
        
        // Botón Start
        if (clickedItem.getType() == Material.NETHER_STAR) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.performCommand("uhc start");
            player.closeInventory();
            return;
        }

        // Configuración de Borde
        if (clickedItem.getType() == Material.MAP) {
            if (event.isShiftClick()) plugin.setBorderSize(2000);
            else plugin.setBorderSize(plugin.getBorderSize() + 500);
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1.5f);
            gui.open(player);
            return;
        }

        // Configuración de Gracia
        if (clickedItem.getType() == Material.CLOCK) {
            if (event.isShiftClick()) plugin.setGraceTime(600);
            else plugin.setGraceTime(plugin.getGraceTime() + 60);
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1.5f);
            gui.open(player);
            return;
        }

        // Configuración de Shrink
        if (clickedItem.getType() == Material.COMPASS) {
            if (event.isShiftClick()) plugin.setShrinkTime(1200);
            else plugin.setShrinkTime(plugin.getShrinkTime() + 300);
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1.5f);
            gui.open(player);
            return;
        }

        // Botón Chaos Toggle
        if (clickedItem.getType() == Material.ENDER_EYE) {
            player.performCommand("chaos start");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
            player.closeInventory();
            return;
        }

        // Toggles de escenarios
        String scenarioName = ChatColor.stripColor(name).toLowerCase();
        if (plugin.getScenarios().containsKey(scenarioName)) {
            plugin.toggleScenario(scenarioName);
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            gui.open(player); // Refrescar GUI
        }
    }
}
