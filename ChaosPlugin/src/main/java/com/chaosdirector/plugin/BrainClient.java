package com.chaosdirector.plugin;

import org.bukkit.entity.Player;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrainClient {

    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String BRAIN_URL = "http://ia-brain:8000/analyze";
    private static final Gson gson = new Gson();

    public static void sendGlobalState(Player referencePlayer, double health, String biome, String dimension, List<String> inventory) {
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("jugador", "GRUPO_UHC"); // Identificador global
        payloadMap.put("salud", health);
        payloadMap.put("bioma", biome);
        payloadMap.put("dimension", dimension);
        payloadMap.put("inventario", inventory);

        String jsonPayload = gson.toJson(payloadMap);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BRAIN_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    ExecutionHandler.processGlobalResponse(responseBody);
                })
                .exceptionally(e -> {
                    ChaosPlugin.getInstance().getLogger().warning("Error contactando la IA: " + e.getMessage());
                    return null;
                });
    }
}
