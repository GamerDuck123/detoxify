package me.gamerduck.detoxify.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class DetoxifyClientMod implements ClientModInitializer {
    private static FabricClientPlatform fabricClientPlatform;

    @Override
    public void onInitializeClient() {

        ClientLifecycleEvents.CLIENT_STARTED.register(minecraft -> {
            try {
                fabricClientPlatform = new FabricClientPlatform(minecraft);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
