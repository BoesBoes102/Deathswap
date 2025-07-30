package com.boes.deathswap.commands;

import com.boes.deathswap.DeathSwapPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DeathSwapCommand implements CommandExecutor, TabCompleter {

    private final DeathSwapPlugin plugin;

    public DeathSwapCommand(DeathSwapPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /deathswap <start|stop|totaltime|cooldown>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                plugin.getGame().start();
                sender.sendMessage("§aDeathSwap started.");
                break;

            case "stop":
                plugin.getGame().stop();
                sender.sendMessage("§cDeathSwap stopped.");
                break;

            case "totaltime":
                if (args.length != 2) {
                    sender.sendMessage("§cUsage: /deathswap totaltime <seconds>");
                    break;
                }
                try {
                    int seconds = Integer.parseInt(args[1]);
                    plugin.getGame().setTotalTimeSeconds(seconds);
                    sender.sendMessage("§aTotal time set to " + seconds + " seconds.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number.");
                }
                break;

            case "cooldown":
                if (args.length != 2) {
                    sender.sendMessage("§cUsage: /deathswap cooldown <seconds>");
                    break;
                }
                try {
                    int seconds = Integer.parseInt(args[1]);
                    plugin.getGame().setCooldownSeconds(seconds);
                    sender.sendMessage("§aCooldown set to " + seconds + " seconds.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number.");
                }
                break;

            default:
                sender.sendMessage("§cUnknown subcommand.");
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "stop", "totaltime", "cooldown");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("totaltime") || args[0].equalsIgnoreCase("cooldown"))) {
            return Collections.singletonList("<seconds>");
        }
        return Collections.emptyList();
    }
}
