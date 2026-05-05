package com.chaosdirector.plugin;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class ChaosPlugin extends JavaPlugin {

    private static ChaosPlugin instance;
    private boolean aiEnabled = false;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[ChaosDirector] " + ChatColor.YELLOW + "¡El Cerebro Sádico ha despertado!");
        
        // Ejecutar cada 2 minutos (2400 ticks)
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (!aiEnabled || getServer().getOnlinePlayers().isEmpty()) return;

            // Tomar al primer jugador como referencia del estado global (bioma, dimensión, etc.)
            org.bukkit.entity.Player ref = getServer().getOnlinePlayers().iterator().next();
            
            double health = ref.getHealth();
            String biome = ref.getLocation().getBlock().getBiome().name();
            String dimension = ref.getWorld().getName();
            
            java.util.List<String> inventoryData = new java.util.ArrayList<>();
            // Solo mandamos el inventario de la "referencia" para ahorrar tokens
            for (org.bukkit.inventory.ItemStack item : ref.getInventory().getArmorContents()) {
                if (item != null && !item.getType().isAir()) inventoryData.add(item.getType().name());
            }
            
            // Enviamos el estado como una llamada GLOBAL
            BrainClient.sendGlobalState(ref, health, biome, dimension, inventoryData);
            
        }, 2400L, 2400L);
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("chaos") && sender.isOp()) {
            if (args.length > 0 && args[0].equalsIgnoreCase("start")) {
                aiEnabled = true;
                org.bukkit.Bukkit.broadcastMessage("§8[§4§l!§8] §c§lEL DIRECTOR HA COMENZADO A OBSERVAR...");
                for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                    p.sendTitle("§4§lOJO AVIZOR", "§cEl Director está analizando tu alma...", 20, 100, 20);
                    p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.2f);
                }
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("stop")) {
                aiEnabled = false;
                sender.sendMessage("§aIA Desactivada.");
                return true;
            }
        }
        return false;
    }

    public void setAiEnabled(boolean enabled) {
        this.aiEnabled = enabled;
    }

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public static ChaosPlugin getInstance() {
        return instance;
    }
}
