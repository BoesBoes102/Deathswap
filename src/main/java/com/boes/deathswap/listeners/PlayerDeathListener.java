package com.boes.deathswap.listeners;

import com.boes.deathswap.gamelogic.DeathSwapGame;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;

public class PlayerDeathListener implements Listener {

    private final DeathSwapGame deathSwapGame;

    public PlayerDeathListener(DeathSwapGame deathSwapGame) {
        this.deathSwapGame = deathSwapGame;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        Bukkit.getScheduler().runTaskLater(deathSwapGame.plugin, () -> deathSwapGame.onPlayerDeath(player), 1L);
    }
}
