package me.gamerduck.detoxify.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(id = "detoxify", name = "Detoxify", version = "0.1.0-SNAPSHOT",
        url = "https://example.org", description = "I did it!", authors = {"Me"})
public class DetoxifyPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public DetoxifyPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            new VelocityPlatform(server, logger, dataDirectory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
