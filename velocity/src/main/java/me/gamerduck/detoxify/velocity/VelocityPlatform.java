package me.gamerduck.detoxify.velocity;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.gamerduck.detoxify.Platform;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.nio.file.Path;
import java.util.logging.Logger;

public class VelocityPlatform extends Platform<Player> {
    public VelocityPlatform(ProxyServer server, Logger logger, Path dataDirectory) throws Exception {
        super(dataDirectory, dataDirectory.resolve("libs"), dataDirectory.resolve("maps"));
    }

    @Override
    public void sendPlayerMessage(Player player, String playerMessage) {
        player.sendMessage(Component.text(playerMessage));
    }

    @Override
    public String getPlayerName(Player player) {
        return player.getUsername();
    }

    @Override
    public void sendAllStaffMessage(Player offendingPlayer, String staffMessage) {

    }

    @Override
    public void sendConsoleMessage(String consoleMessage) {
    }
}
