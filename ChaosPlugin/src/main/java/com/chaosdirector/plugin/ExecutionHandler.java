package com.chaosdirector.plugin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ExecutionHandler {

    private static final Gson gson = new Gson();

    public static void processGlobalResponse(String jsonResponse) {
        try {
            JsonObject response = gson.fromJson(jsonResponse, JsonObject.class);
            
            String message = response.has("mensaje") ? response.get("mensaje").getAsString() : null;
            JsonArray commands = response.has("comandos") ? response.getAsJsonArray("comandos") : new JsonArray();

            Bukkit.getScheduler().runTask(ChaosPlugin.getInstance(), () -> {
                
                if (message != null && !message.isEmpty()) {
                    String formattedMsg = "§8[§4§lCHAOS§8] §f" + message.replace("&", "§");
                    Bukkit.broadcastMessage(formattedMsg);
                    
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle("§4§lSENTENCIA GRUPAL", "§e" + message, 10, 70, 20);
                        p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
                    }
                }

                for (int i = 0; i < commands.size(); i++) {
                    String cmd = commands.get(i).getAsString();
                    // Ejecutamos el comando tal cual viene (la IA usará @a)
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            });
        } catch (Exception e) {
            ChaosPlugin.getInstance().getLogger().severe("Error procesando respuesta global de la IA: " + e.getMessage());
        }
    }
}
