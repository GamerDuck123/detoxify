package me.gamerduck.detoxify.neoforge;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;


@Mod(value = DetoxifyMod.MODID, dist = Dist.DEDICATED_SERVER)
public class DetoxifyMod {
    public static final String MODID = "detoxify";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static NeoForgePlatform neoForgePlatform;

    public DetoxifyMod() {
        try {
            neoForgePlatform = new NeoForgePlatform();
            NeoForge.EVENT_BUS.register(neoForgePlatform);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
