package me.gamerduck.detoxify.spigot;

import org.bukkit.plugin.java.JavaPlugin;

public class DetoxifyPlugin extends JavaPlugin {

    private static SpigotPlatform spigotPlatform;

    @Override
    public void onEnable() {
        try {
            spigotPlatform = new SpigotPlatform(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.getServer().getPluginManager().registerEvents(spigotPlatform, this);
    }

    public static SpigotPlatform skriptOnly$spigotPlatform() {
        return spigotPlatform;
    }
}
