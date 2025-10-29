package me.gamerduck.detoxify.fabric;

import me.gamerduck.detoxify.Platform;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.logging.Logger;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class FabricPlatform extends Platform<ServerPlayer> {

    private static final Path modFolder = Path.of("/config/Detoxify");
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("detoxify");
    private MinecraftServer minecraftServer;

    public FabricPlatform(MinecraftServer minecraftServer) throws Exception {
        super(minecraftServer.getServerDirectory().resolve(modFolder), minecraftServer.getServerDirectory().resolve(modFolder).resolve("libs"), minecraftServer.getServerDirectory().resolve(modFolder).resolve("maps"));
        this.minecraftServer = minecraftServer;

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((server, player, message) -> !onChatEvent(message.toString(), player));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (environment == Commands.CommandSelection.DEDICATED) {
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
    public void sendPlayerMessage(ServerPlayer player, String playerMessage) {
        player.sendSystemMessage(Component.literal(playerMessage));
    }

    @Override
    public String getPlayerName(ServerPlayer player) {
        return player.getGameProfile().name();
    }

    @Override
    public void sendAllStaffMessage(ServerPlayer offendingPlayer, String staffMessage) {
        minecraftServer.getPlayerList().getPlayers().forEach(player -> {
            if (player.getPermissionLevel() >= 4)
                player.sendSystemMessage(Component.literal(String.format(staffMessage, offendingPlayer.getName(), staffMessage)));
        });
    }

    @Override
    public void sendConsoleMessage(String consoleMessage) {
        LOGGER.info(consoleMessage);
    }
}
