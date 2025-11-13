package me.gamerduck.detoxify.neoforge;

import com.mojang.logging.LogUtils;
import me.gamerduck.detoxify.Platform;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class NeoForgePlatform extends Platform<ServerPlayer> {

    private static final Path modFolder = Path.of("/mods/Detoxify");
    private MinecraftServer minecraftServer;
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("detoxify");

    public NeoForgePlatform() throws Exception {
        super(modFolder, modFolder.resolve("libs"), modFolder.resolve("maps"));
    }

    @SubscribeEvent
    public void commandStart(RegisterCommandsEvent event) {
        if (event.getCommandSelection() == Commands.CommandSelection.DEDICATED) {
            event.getDispatcher().register(Commands.literal("detoxifyreload")
                    .requires(commandSourceStack -> commandSourceStack.hasPermission(4) || !commandSourceStack.isPlayer())
                    .executes(stack -> {
                        onReloadCommand(stack.getSource().getPlayer());
                        return 1;
                    }));
        }
    }
    @SubscribeEvent
    public void serverStart(ServerStartedEvent event) {
        this.minecraftServer = event.getServer();
    }

    @SubscribeEvent
    public void onChatEvent(ServerChatEvent event) {
        event.setCanceled(onChatEvent(event.getRawText(), event.getPlayer()));
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().getPermissionLevel() >= 4) {
            String message = getUpdateMessage();
            if (message != null) ((ServerPlayer) event.getEntity()).sendSystemMessage(Component.literal(message));
        }
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
