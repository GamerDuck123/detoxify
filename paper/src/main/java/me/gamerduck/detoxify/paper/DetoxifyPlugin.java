package me.gamerduck.detoxify.paper;

import org.bukkit.plugin.java.JavaPlugin;

public class DetoxifyPlugin extends JavaPlugin {

    private static PaperPlatform paperPlatform;

    @Override
    public void onEnable() {
        try {
            paperPlatform = new PaperPlatform(getDataPath(), getDataPath().resolve("libs"), getDataPath().resolve("maps"), this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.getServer().getPluginManager().registerEvents(paperPlatform, this);
    }

    public static PaperPlatform skriptOnly$paperPlatform() {
        return paperPlatform;
    }
}
