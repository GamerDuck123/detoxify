package me.gamerduck.detoxify.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class DetoxifyMod  implements ModInitializer {

    private static FabricPlatform fabricPlatform;

    @Override
    public void onInitialize() {

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (server.isDedicatedServer()) {
                try {
                    fabricPlatform = new FabricPlatform(server);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

}
