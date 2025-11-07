package me.gamerduck.detoxify.neoforge.client;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;


@Mod(value = DetoxifyClientMod.MODID, dist = Dist.CLIENT)
public class DetoxifyClientMod {
    public static final String MODID = "detoxify";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static NeoForgeClientPlatform neoForgeClientPlatform;

    public DetoxifyClientMod() {
        try {
            neoForgeClientPlatform = new NeoForgeClientPlatform();
            NeoForge.EVENT_BUS.register(neoForgeClientPlatform);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
