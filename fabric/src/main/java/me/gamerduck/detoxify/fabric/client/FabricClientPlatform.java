package me.gamerduck.detoxify.fabric.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import me.gamerduck.detoxify.Platform;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class FabricClientPlatform extends Platform<Player> {

    private static final Path modFolder = Path.of("/config/Detoxify");
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("detoxify");
    private Minecraft minecraftClient;

    public FabricClientPlatform(Minecraft minecraftClient) throws Exception {
        super(minecraftClient.gameDirectory.toPath().resolve(modFolder), minecraftClient.gameDirectory.toPath().resolve(modFolder).resolve("libs"), minecraftClient.gameDirectory.toPath().resolve(modFolder).resolve("maps"));
        this.minecraftClient = minecraftClient;

        // Since this is client we need to use mixins for the event
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (checkMessage(message.getString())) {
                sendConsoleMessage(String.format(staffMessage(), sender.name() == null ? "System" : sender.name(), message));
                return true;
            }
            return false;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (environment == Commands.CommandSelection.INTEGRATED) {
                dispatcher.register(Commands.literal("detoxifyreload")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4) || !commandSourceStack.isPlayer())
                        .executes(stack -> {
                            onReloadCommand(stack.getSource().getPlayer());
                            return 1;
                        }));
            }
        });
    }

    @Override
    public void sendPlayerMessage(Player player, String playerMessage) {
        // Dont need to send player message on client
    }

    @Override
    public String getPlayerName(Player player) {
        // Needed for logging
        return player.getGameProfile().name();
    }

    @Override
    public void sendAllStaffMessage(Player offendingPlayer, String staffMessage) {
        // No staff on client
    }

    @Override
    public void sendConsoleMessage(String consoleMessage) {
        // Log inappropriate messages
        LOGGER.info(consoleMessage);
    }
}
