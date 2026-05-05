package com.chaosdirector.uhcscenarios;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class UHCScenarios extends JavaPlugin {

    private final Map<String, Boolean> scenarios = new HashMap<>();
    private boolean gameStarted = false;
    private int gameTimeSeconds = 0;
    private int borderSize = 2000;
    private int graceTimeSeconds = 600; // 10 min por defecto
    private int shrinkTimeSeconds = 1200; // Al minuto 20 por defecto
    private UHCConfigGUI gui;

    @Override
    public void onEnable() {
        // Inicializar escenarios
        String[] list = {"cutclean", "timber", "hasteyboys", "barebones", "flowerpower", 
                         "timebomb", "bowless", "switcheroo", "fireless", "diamondless", 
                         "blooddiamond", "goldenfleece", "nightmare", "skyhigh"};
        for (String s : list) scenarios.put(s, false);

        this.gui = new UHCConfigGUI(this);

        // Registrar Listeners
        getServer().getPluginManager().registerEvents(new RecursosListener(this), this);
        getServer().getPluginManager().registerEvents(new CombateListener(this), this);
        getServer().getPluginManager().registerEvents(new SaludListener(this), this);
        getServer().getPluginManager().registerEvents(new EntornoListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this, gui), this);

        // Scoreboard Update Task
        Bukkit.getScheduler().runTaskTimer(this, this::updateScoreboards, 20L, 20L);

        getLogger().info("UHC La Tri Ready. ¡Habla brother!");
    }

    public boolean isScenarioEnabled(String name) {
        return scenarios.getOrDefault(name.toLowerCase(), false);
    }

    public Map<String, Boolean> getScenarios() {
        return scenarios;
    }

    public void toggleScenario(String name) {
        if (scenarios.containsKey(name.toLowerCase())) {
            scenarios.put(name.toLowerCase(), !scenarios.get(name.toLowerCase()));
        }
    }

    public int getBorderSize() { return borderSize; }
    public void setBorderSize(int size) { this.borderSize = size; }
    public int getGraceTime() { return graceTimeSeconds; }
    public void setGraceTime(int seconds) { this.graceTimeSeconds = seconds; }
    public int getShrinkTime() { return shrinkTimeSeconds; }
    public void setShrinkTime(int seconds) { this.shrinkTimeSeconds = seconds; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) return false;

        if (label.equalsIgnoreCase("uhc")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    gui.open((Player) sender);
                } else {
                    sender.sendMessage("§cUsa /uhc <start|list|gui>");
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("start")) {
                startGame();
                Bukkit.broadcastMessage("§e§l[DIRECTO DEL CAOS] §6§l¡EMPEZÓ LA CARNICERÍA!");
                return true;
            }

            if (args[0].equalsIgnoreCase("list")) {
                sender.sendMessage("§6§lEscenarios Activos:");
                scenarios.forEach((k, v) -> sender.sendMessage("§e- " + k + ": " + (v ? "§aACTIVADO" : "§cAPAGADO")));
                return true;
            }

            if (scenarios.containsKey(args[0].toLowerCase())) {
                toggleScenario(args[0]);
                sender.sendMessage("§e[UHC] §f" + args[0] + " ahora está " + (scenarios.get(args[0].toLowerCase()) ? "§aACTIVO" : "§cOFF"));
                return true;
            }
        }
        return true;
    }

    private void startGame() {
        gameStarted = true;
        World world = Bukkit.getWorld("world");
        if (world != null) {
            world.getWorldBorder().setCenter(0, 0);
            world.getWorldBorder().setSize(borderSize);
        }
    }

    private void updateScoreboards() {
        if (gameStarted) gameTimeSeconds++;
        
        String timeStr = String.format("%02d:%02d", gameTimeSeconds / 60, gameTimeSeconds % 60);
        String borderStr = (int) Bukkit.getWorld("world").getWorldBorder().getSize() + "x" + (int) Bukkit.getWorld("world").getWorldBorder().getSize();
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            org.bukkit.scoreboard.Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
            org.bukkit.scoreboard.Objective obj = sb.registerNewObjective("uhc", "dummy", "§e§lDIRECTO §6§lDEL CAOS");
            obj.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);

            obj.getScore("§f ").setScore(9);
            obj.getScore("§e§lTIEMPO: §f" + timeStr).setScore(8);
            obj.getScore("§e§lBORDE: §f" + borderStr).setScore(7);
            obj.getScore("§e§lPLAYERS: §f" + Bukkit.getOnlinePlayers().size()).setScore(6);
            
            String nextEvent = "§7En espera...";
            if (gameTimeSeconds < graceTimeSeconds) {
                nextEvent = "§aPVP: §f" + ((graceTimeSeconds - gameTimeSeconds) / 60) + "m";
            } else if (gameTimeSeconds < shrinkTimeSeconds) {
                nextEvent = "§cShrink: §f" + ((shrinkTimeSeconds - gameTimeSeconds) / 60) + "m";
            } else {
                nextEvent = "§4¡MOVIÉNDOSE!";
            }
            obj.getScore("§e§lEVENTO: " + nextEvent).setScore(5);
            
            obj.getScore("§f  ").setScore(4);
            obj.getScore("§6§m----------------").setScore(3);
            obj.getScore("§e§lDIRECTOR: §c§lONLINE").setScore(2);

            p.setScoreboard(sb);
        }

        // Lógica de Borde y Gracia
        if (gameStarted) {
            if (gameTimeSeconds == graceTimeSeconds) {
                Bukkit.broadcastMessage("§c§l[UHC] ¡EL PVP SE HA ACTIVADO! ¡DALE CON TODO!");
            }
            if (gameTimeSeconds == shrinkTimeSeconds) {
                Bukkit.broadcastMessage("§c§l[UHC] ¡EL BORDE SE ESTÁ CERRANDO, QUÉ LÁMPARA!");
                Bukkit.getWorld("world").getWorldBorder().setSize(100, 900);
            }

            // Lógica Skyhigh (Capa 101)
            if (isScenarioEnabled("skyhigh") && gameTimeSeconds >= 2700) { // Al minuto 45
                if (gameTimeSeconds % 30 == 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getLocation().getY() < 101) {
                            p.damage(1.0);
                            p.sendMessage("§c§l[SKYHIGH] §e¡Habla brother, estás muy abajo! Sube a la 101 o te vas de una.");
                        }
                    }
                }
            }
        }
    }
}
