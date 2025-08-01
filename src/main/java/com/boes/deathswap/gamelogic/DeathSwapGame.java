package com.boes.deathswap.gamelogic;

import com.boes.deathswap.DeathSwapPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeathSwapGame {

    public final DeathSwapPlugin plugin;

    private int totalTimeSeconds = 300;
    private int cooldownSeconds = 30;
    private boolean running = false;
    private BossBar bossBar;
    private int timeRemaining;
    private BukkitRunnable mainTask;

    public DeathSwapGame(DeathSwapPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (running) {
            plugin.getLogger().info("DeathSwap already running!");
            return;
        }

        running = true;

        Bukkit.getWorlds().getFirst().getWorldBorder().setSize(2000);
        Bukkit.getWorlds().getFirst().getWorldBorder().setCenter(0, 0);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getInventory().clear();
            p.setGameMode(GameMode.SURVIVAL);
            p.teleport(new Location(p.getWorld(), 0.0, p.getWorld().getHighestBlockYAt(0, 0) + 1, 0.0));
            p.setHealth(20.0);
            p.setFoodLevel(20);
            p.setSaturation(20f);
            p.sendMessage(ChatColor.GREEN + "DeathSwap has started! Survive and swap wisely.");
        }

        bossBar = Bukkit.createBossBar("DeathSwap Time Remaining", BarColor.RED, BarStyle.SEGMENTED_20);
        bossBar.setVisible(true);
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }

        timeRemaining = totalTimeSeconds;

        plugin.getLogger().info("DeathSwap started!");

        mainTask = new BukkitRunnable() {

            private int swapCooldown = cooldownSeconds;

            @Override
            public void run() {
                if (!running) {
                    cancel();
                    return;
                }

                bossBar.setProgress(Math.max(0, (double) timeRemaining / totalTimeSeconds));
                bossBar.setTitle("Time Remaining: " + formatTime(timeRemaining));

                List<Player> alive = getAlivePlayers();

                if (alive.size() <= 1) {
                    endGame(alive.size() == 1 ? alive.getFirst() : null);
                    cancel();
                    return;
                }

                if (timeRemaining <= 0) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "Time's up! No one won the DeathSwap game.");
                    endGame(null);
                    cancel();
                    return;
                }

                if (swapCooldown <= 0) {
                    doSwap(alive);
                    swapCooldown = cooldownSeconds;
                } else {
                    if (swapCooldown <= 10) {
                        Bukkit.broadcastMessage(ChatColor.GOLD + "DeathSwap swapping in " + swapCooldown + " seconds!");
                    }
                    swapCooldown--;
                }

                timeRemaining--;
            }
        };

        mainTask.runTaskTimer(plugin, 0L, 20L);
    }

    public void stop() {
        if (!running) return;
        running = false;
        if (mainTask != null) mainTask.cancel();
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.setVisible(false);
            bossBar = null;
        }

        Bukkit.getWorlds().getFirst().getWorldBorder().setSize(10);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(p.getWorld().getSpawnLocation());
            p.setGameMode(GameMode.ADVENTURE);
            p.sendMessage(ChatColor.RED + "DeathSwap has ended.");
        }

        plugin.getLogger().info("DeathSwap stopped!");
    }

    public void onPlayerDeath(Player player) {
        if (!running) return;
        if (player.getGameMode() != GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(ChatColor.RED + "You died and are now a spectator.");
        }

        List<Player> alive = getAlivePlayers();
        if (alive.size() <= 1) {
            endGame(alive.size() == 1 ? alive.getFirst() : null);
        }
    }

    private List<Player> getAlivePlayers() {
        List<Player> alive = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOnline() && !p.isDead() && p.getGameMode() == GameMode.SURVIVAL) {
                alive.add(p);
            }
        }
        return alive;
    }

    private void endGame(Player winner) {
        if (winner != null) {
            Bukkit.broadcastMessage(ChatColor.GREEN + "DeathSwap winner: " + winner.getName() + "!");
            plugin.getLogger().info("DeathSwap winner: " + winner.getName());
        } else {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "DeathSwap ended with no winner.");
            plugin.getLogger().info("DeathSwap ended with no winner.");
        }
        stop();
    }

    private void doSwap(List<Player> alivePlayers) {
        plugin.getLogger().info("Executing DeathSwap player swap.");

        if (alivePlayers.size() < 2) return;

        Map<Player, PlayerData> swapData = new HashMap<>();

        for (int i = 0; i < alivePlayers.size(); i++) {
            Player from = alivePlayers.get(i);
            Player to = alivePlayers.get((i + 1) % alivePlayers.size());

            PlayerData data = new PlayerData(
                    to.getLocation().clone(),
                    to.getHealth(),
                    to.getFoodLevel(),
                    to.getSaturation()
            );

            swapData.put(from, data);
        }

        for (Player player : alivePlayers) {
            PlayerData data = swapData.get(player);
            if (data == null) continue;

            player.teleport(data.location);
            player.setHealth(Math.min(data.health, 20.0));
            player.setFoodLevel(data.foodLevel);
            player.setSaturation(data.saturation);
            player.sendMessage(ChatColor.AQUA + "You have been swapped!");
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "DeathSwap Swap!");

        plugin.getLogger().info("DeathSwap swap completed for " + alivePlayers.size() + " players.");
    }

    private static class PlayerData {
        Location location;
        double health;
        int foodLevel;
        float saturation;

        PlayerData(Location location, double health, int foodLevel, float saturation) {
            this.location = location;
            this.health = health;
            this.foodLevel = foodLevel;
            this.saturation = saturation;
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setTotalTimeSeconds(int totalTimeSeconds) {
        this.totalTimeSeconds = totalTimeSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

}
