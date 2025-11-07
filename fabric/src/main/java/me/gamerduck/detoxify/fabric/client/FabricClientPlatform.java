package me.gamerduck.detoxify.fabric.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import me.gamerduck.detoxify.Platform;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class FabricClientPlatform extends Platform<Player> {

    private static final Path modFolder = Path.of("Detoxify");
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("detoxify");
    private Minecraft minecraftClient;

    public FabricClientPlatform(Minecraft minecraftClient) throws Exception {
        super(FabricLoader.getInstance().getConfigDir().resolve(modFolder), FabricLoader.getInstance().getConfigDir().resolve(modFolder).resolve("libs"), FabricLoader.getInstance().getConfigDir().resolve(modFolder).resolve("maps"));
        this.minecraftClient = minecraftClient;

        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (checkMessage(message.getString())) {
                sendConsoleMessage(String.format(consoleMessage(), sender.name() == null ? "System" : sender.name(), message));
                return false;
            }
            return true;
        });
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, signedMessage) -> {
            if (checkMessage(message.getString())) {
                sendConsoleMessage(String.format(consoleMessage(), "System", message));
                return false;
            }
            return true;
        });

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (checkMessage(message)) {
                sendConsoleMessage(String.format(consoleMessage(), "Me, why are you cussing", message));
                sendPlayerMessage(minecraftClient.player, String.format(playerMessage(), "Local", message));
                return false;
            }
            return true;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (environment == Commands.CommandSelection.ALL) {
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
        player.displayClientMessage(Component.literal(playerMessage), false);
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
