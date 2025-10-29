package me.gamerduck.detoxify.neoforge;

import com.mojang.logging.LogUtils;
import me.gamerduck.detoxify.api.Config;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Paths;

@Mod(DetoxifyMod.MODID)
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
