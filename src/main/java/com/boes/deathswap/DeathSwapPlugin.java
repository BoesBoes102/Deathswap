package com.boes.deathswap;

import com.boes.deathswap.commands.DeathSwapCommand;
import com.boes.deathswap.gamelogic.DeathSwapGame;
import com.boes.deathswap.listeners.PlayerDeathListener;  // <-- Import listener
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class DeathSwapPlugin extends JavaPlugin implements Listener {

    private DeathSwapGame game;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(this.getCommand("deathswap")).setExecutor(new DeathSwapCommand(this));

        World world = Bukkit.getWorlds().getFirst();
        world.getWorldBorder().setSize(20);
        world.getWorldBorder().setCenter(world.getSpawnLocation());
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGameMode(org.bukkit.GameMode.SPECTATOR);
        }

        game = new DeathSwapGame(this);

        // Register the death listener here
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(game), this);

        getLogger().info("DeathSwap plugin enabled.");
    }

    @Override
    public void onDisable() {
        if (game != null) {
            game.stop();
        }
        getLogger().info("DeathSwap plugin disabled.");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().setGameMode(org.bukkit.GameMode.SPECTATOR);
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent event) {
        if (!game.isRunning() && event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            event.setCancelled(true);
        }
    }

    public DeathSwapGame getGame() {
        return game;
    }
}
