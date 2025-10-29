package me.gamerduck.detoxify;

import me.gamerduck.detoxify.api.Config;
import me.gamerduck.detoxify.api.ConfigManager;
import me.gamerduck.detoxify.api.DetoxifyONNX;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public abstract class Platform<P> {

    private final ConfigManager configManager;
    private final Path platformFolder;
    private final Path libsFolder;
    private final Path mapsFolder;
    private final DetoxifyONNX mod;
    private Boolean debug;
    private String playerMessage;
    private String staffMessage;
    private Double toxicity;
    private Double severe_toxicity;
    private Double obscene;
    private Double threat;
    private Double insult;
    private Double identity_attack;

    public Platform(Path platformFolder, Path libsFolder, Path mapsFolder) throws Exception {
        this.platformFolder = platformFolder;
        this.libsFolder = libsFolder;
        this.mapsFolder = mapsFolder;
        configManager = new ConfigManager(platformFolder.resolve("detoxify.conf"), getClass().getClassLoader());
        configManager.load();

        if (Files.notExists(libsFolder)) {
            try {
                Files.createDirectories(libsFolder);

                if (!Files.exists(libsFolder.resolve("detoxify_quantized.onnx"))) {
                    URL url = new URL("");
                    Path dest = libsFolder.resolve("detoxify_quantized.onnx");
                    System.out.println("Downloading Detoxify model...");
                    try (InputStream in = url.openStream()) {
                        Files.createDirectories(dest.getParent());
                        Files.copy(in, dest);
                    }
                    System.out.println("Model downloaded successfully!");
                }
                if (!Files.exists(libsFolder.resolve("tokenizer.json"))) {
                    URL url = new URL("");
                    Path dest = libsFolder.resolve("detoxify_quantized.onnx");
                    System.out.println("Downloading Detoxify's tokenizer.json...");
                    try (InputStream in = url.openStream()) {
                        Files.createDirectories(dest.getParent());
                        Files.copy(in, dest);
                    }
                    System.out.println("Tokenizer downloaded successfully!");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        mod = new DetoxifyONNX(libsFolder.resolve("detoxify_quantized.onnx").toString(), this);
        debug = configManager.config().debug.enabled;
        playerMessage = configManager.config().messages.player;
        staffMessage = configManager.config().messages.staff;
        toxicity = configManager.config().values.toxicity < 0 ? 2 : configManager.config().values.toxicity;
        severe_toxicity = configManager.config().values.severeToxicity < 0 ? 2 :  configManager.config().values.severeToxicity;
        obscene = configManager.config().values.obscene < 0 ? 2 :  configManager.config().values.obscene;
        threat = configManager.config().values.threat < 0 ? 2 :  configManager.config().values.threat;
        insult = configManager.config().values.insult < 0 ? 2 :  configManager.config().values.insult;
        identity_attack = configManager.config().values.identityAttack < 0 ? 2 :  configManager.config().values.identityAttack;
    }

    public abstract void sendPlayerMessage(P player, String playerMessage);
    public abstract String getPlayerName(P player);
    public abstract void sendAllStaffMessage(P offendingPlayer, String staffMessage);
    public abstract void sendConsoleMessage(String consoleMessage);

    public void reload() {
        debug = configManager.config().debug.enabled;
        playerMessage = configManager.config().messages.player;
        staffMessage = configManager.config().messages.staff;
        toxicity = configManager.config().values.toxicity < 0 ? 2 : configManager.config().values.toxicity;
        severe_toxicity = configManager.config().values.severeToxicity < 0 ? 2 :  configManager.config().values.severeToxicity;
        obscene = configManager.config().values.obscene < 0 ? 2 :  configManager.config().values.obscene;
        threat = configManager.config().values.threat < 0 ? 2 :  configManager.config().values.threat;
        insult = configManager.config().values.insult < 0 ? 2 :  configManager.config().values.insult;
        identity_attack = configManager.config().values.identityAttack < 0 ? 2 :  configManager.config().values.identityAttack;
    }

    public boolean checkMessage(String message) {
        long start = System.nanoTime();
        try {
            Map<String, Float> result = mod.predict(message);
            if (debug) result.forEach((k, v) -> System.out.printf("%s: %.3f%n", k, v));
            if (result.get("toxicity") > toxicity
                    || result.get("severe_toxicity") > severe_toxicity
                    || result.get("obscene") > obscene
                    || result.get("threat") > threat
                    || result.get("insult") > insult
                    || result.get("identity_attack") > identity_attack) {
                return true;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        if (debug) {
            double elapsedMs = (System.nanoTime() - start) / 1_000_000.0;
            System.out.printf("Moderation took %.2f ms%n", elapsedMs);
        }
        return false;
    }

    public boolean onChatEvent(String message, P player) {
        if (checkMessage(message)) {
            sendPlayerMessage(player, playerMessage);
            sendAllStaffMessage(player, String.format(staffMessage, getPlayerName(player), message));
            sendConsoleMessage(String.format(staffMessage, getPlayerName(player), message));
            return true;
        }
        return false;
    }

    public boolean onReloadCommand(P player) {
        if (player == null) {
            sendConsoleMessage("Reloading config");
        } else {
            sendPlayerMessage(player, "Reloading config");
        }
        reload();
        return true;
    }

    public Path platformFolder() {
        return platformFolder;
    }
    public Path libsFolder() {
        return libsFolder;
    }
    public Path mapsFolder() {
        return mapsFolder;
    }

    public DetoxifyONNX mod() {
        return mod;
    }

    public Boolean debug() {
        return debug;
    }

    public String playerMessage() {
        return playerMessage;
    }

    public String staffMessage() {
        return staffMessage;
    }

    public Double toxicity() {
        return toxicity;
    }

    public Double severe_toxicity() {
        return severe_toxicity;
    }

    public Double obscene() {
        return obscene;
    }

    public Double threat() {
        return threat;
    }

    public Double insult() {
        return insult;
    }

    public Double identity_attack() {
        return identity_attack;
    }
}
