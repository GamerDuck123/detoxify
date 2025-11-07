package me.gamerduck.detoxify.neoforge.client;

import me.gamerduck.detoxify.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.nio.file.Path;

public class NeoForgeClientPlatform extends Platform<Player> {

    private static final Path modFolder = Path.of("config/Detoxify");
    private Minecraft minecraftClient;

    public NeoForgeClientPlatform() throws Exception {
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
    public void clientStart(ClientStartedEvent event) {
        this.minecraftClient = event.getClient();
    }

    @SubscribeEvent
    public void onChatEvent(ClientChatReceivedEvent event) {
        if (checkMessage(event.getMessage().getString())) {
            sendConsoleMessage(String.format(consoleMessage(), event.isSystem() ? "System" : event.getSender().toString(), event.getMessage().getString()));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onChatEvent(ClientChatEvent event) {
        if (checkMessage(event.getMessage())) {
            sendConsoleMessage(String.format(consoleMessage(), "Me", event.getMessage()));
            sendPlayerMessage(minecraftClient.player, String.format(playerMessage(), "Local", event.getMessage()));
            event.setCanceled(true);
        }
    }

    @Override
    public void sendPlayerMessage(Player player, String playerMessage) {
        player.displayClientMessage(Component.literal(playerMessage), false);
    }

    @Override
    public String getPlayerName(Player player) {
        return player.getGameProfile().name();
    }

    @Override
    public void sendAllStaffMessage(Player offendingPlayer, String staffMessage) {
    }

    @Override
    public void sendConsoleMessage(String consoleMessage) {
        DetoxifyClientMod.LOGGER.info(consoleMessage);
    }
}
