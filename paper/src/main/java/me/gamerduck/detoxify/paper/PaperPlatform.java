package me.gamerduck.detoxify.paper;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.gamerduck.detoxify.Platform;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public class PaperPlatform extends Platform<Player> implements Listener {

    private static final Logger LOGGER = Logger.getLogger("Detoxify");
    public PaperPlatform(Path platformFolder, Path libsFolder, Path mapsFolder, JavaPlugin plugin) throws Exception {
        super(platformFolder, libsFolder, mapsFolder);

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onChat(AsyncChatEvent e) {
                e.setCancelled(onChatEvent(PlainTextComponentSerializer.plainText().serialize(e.message()), e.getPlayer()));
            }
        }, plugin);

        plugin.registerCommand("detoxifyreload", (commandSourceStack, args) -> {
            if (commandSourceStack.getSender().hasPermission("detoxify.reload")) {
                if (commandSourceStack.getSender() instanceof Player p) {
                    onReloadCommand(p);
                } else {
                    onReloadCommand(null);
                }
            }
        });


        if (Bukkit.getPluginManager().getPlugin("Skript") != null) {
            LOGGER.info("Skript found, enabling addon");
            SkriptAddon addon = Skript.registerAddon(plugin);
            try {
                addon.loadClasses("me.gamerduck.detoxify.paper.support.skript");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
                player.sendMessage(Component.text(String.format(staffMessage, offendingPlayer.getName(), staffMessage)));
        });
    }

    @Override
    public void sendConsoleMessage(String consoleMessage) {
        LOGGER.info(consoleMessage);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPermission("detoxify.admin")) {
            String message = getUpdateMessage();
            if (message != null) event.getPlayer().sendMessage(message);
        }
    }
}
