package me.gamerduck.detoxify.spigot;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import me.gamerduck.detoxify.Platform;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotPlatform extends Platform<Player> {
    private final JavaPlugin bootstrap;

    public SpigotPlatform(JavaPlugin bootstrap) throws Exception {
        super(bootstrap.getDataFolder().toPath(), bootstrap.getDataFolder().toPath().resolve("libs"), bootstrap.getDataFolder().toPath().resolve("maps"));
        this.bootstrap = bootstrap;

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onChat(AsyncPlayerChatEvent e) {
                e.setCancelled(onChatEvent(e.getMessage(), e.getPlayer()));
            }
        }, bootstrap);

        bootstrap.getCommand("detoxifyreload").setExecutor((commandSender, command, s, strings) -> {
            if ((commandSender instanceof Player player && commandSender.hasPermission("detoxify.reload"))) {
                return onReloadCommand(player);
            } else {
                return onReloadCommand(null);
            }
        });

        if (Bukkit.getPluginManager().getPlugin("Skript") != null) {
            bootstrap.getLogger().info("Skript found, enabling addon");
            SkriptAddon addon = Skript.registerAddon(bootstrap);
            addon.loadClasses("me.gamerduck.detoxify.spigot.support.skript");
        }
    }

    @Override
    public void sendPlayerMessage(Player player, String playerMessage) {
        player.sendMessage(playerMessage);
    }

    @Override
    public String getPlayerName(Player player) {
        return player.getName();
    }

    @Override
    public void sendAllStaffMessage(Player offendingPlayer, String staffMessage) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.hasPermission("detoxify.notify"))
                player.sendMessage(String.format(staffMessage, offendingPlayer.getName(), staffMessage));
        });
    }

    @Override
    public void sendConsoleMessage(String consoleMessage) {
        bootstrap.getLogger().info(consoleMessage);
    }
}
