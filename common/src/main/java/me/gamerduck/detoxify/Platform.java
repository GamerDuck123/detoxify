package me.gamerduck.detoxify;

import me.gamerduck.detoxify.api.DetoxifyConfig;
import me.gamerduck.detoxify.api.DetoxifyONNX;
import me.gamerduck.detoxify.api.updates.ModrinthUpdateChecker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Map;

public abstract class Platform<P> {

    protected DetoxifyConfig config;
    protected final Path platformFolder;
    protected final Path libsFolder;
    protected final Path mapsFolder;
    protected final DetoxifyONNX mod;

    public Platform(Path platformFolder, Path libsFolder, Path mapsFolder) throws Exception {
        this.platformFolder = platformFolder;
        this.libsFolder = libsFolder;
        this.mapsFolder = mapsFolder;
        config = new DetoxifyConfig(platformFolder.toFile(), this);

        if (Files.notExists(libsFolder)) {
            try {
                Files.createDirectories(libsFolder);
                if (!Files.exists(libsFolder.resolve("detoxify_quantized.onnx"))) {
                    URL url = new URL("https://github.com/GamerDuck123/detoxify/releases/download/model-download-1.0.0/detoxify_quantized.onnx");
                    Path dest = libsFolder.resolve("detoxify_quantized.onnx");
                    sendConsoleMessage("Downloading Detoxify model...");
                    try (InputStream in = url.openStream()) {
                        Files.createDirectories(dest.getParent());
                        Files.copy(in, dest);
                    }
                    String expectedSHA = "7f62ab0b50c1c4050617b8842efbf7e71ab90e382a5e8e141494d378fdf53740";
                    sendConsoleMessage("Ensuring download integrity...");
                    if (verifySHA(dest, expectedSHA)) {
                        sendConsoleMessage("Model downloaded successfully!");
                    } else {
                        throw new MatchException("The SHA256 does not match, please contact GamerDuck123", new RuntimeException());
                    }
                }
                if (!Files.exists(libsFolder.resolve("tokenizer.json"))) {
                    URL url = new URL("https://github.com/GamerDuck123/detoxify/releases/download/model-download-1.0.0/tokenizer.json");
                    Path dest = libsFolder.resolve("tokenizer.json");
                    sendConsoleMessage("Downloading Detoxify's tokenizer.json...");
                    try (InputStream in = url.openStream()) {
                        Files.createDirectories(dest.getParent());
                        Files.copy(in, dest);
                    }
                    String expectedSHA = "d241a60d5e8f04cc1b2b3e9ef7a4921b27bf526d9f6050ab90f9267a1f9e5c66";
                    sendConsoleMessage("Ensuring download integrity...");
                    if (verifySHA(dest, expectedSHA)) {
                        sendConsoleMessage("Tokenizer downloaded successfully!");
                    } else {
                        throw new MatchException("The SHA256 does not match, please contact GamerDuck123", new RuntimeException());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        mod = new DetoxifyONNX(libsFolder.resolve("detoxify_quantized.onnx").toString(), this);
        String message = getUpdateMessage();
        if (message != null) sendConsoleMessage(message);
    }


    private boolean verifySHA(Path path, String sha) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : digest.digest()) {
            sb.append(String.format("%02x", b));
        }
        String fileSha = sb.toString();
        return fileSha.equalsIgnoreCase(sha);
    }

    public abstract void sendPlayerMessage(P player, String playerMessage);
    public abstract String getPlayerName(P player);
    public abstract void sendAllStaffMessage(P offendingPlayer, String staffMessage);
    public abstract void sendConsoleMessage(String consoleMessage);

    /**
     * Gets the update message if updates is enabled and there is a new update available.
     *
     * @return the update message
     */
    public String getUpdateMessage() {
        if (config.updates() && ModrinthUpdateChecker.hasNewer()) {
            return "\u00A7cHey there is a new update for Detoxify! " +
                    "\u00A7cPlease update soon for the latest and best features! https://modrinth.com/plugin/detoxify/versions";
        } else {
            return null;
        }
    }

    public void reload() {
        config = new DetoxifyConfig(platformFolder.toFile(), this);
    }

    public boolean checkMessage(String message) {
        long start = System.nanoTime();
        try {
            Map<String, Float> result = mod.predict(message);
            if (config.debug()) result.forEach((k, v) -> System.out.printf("%s: %.3f%n", k, v));
            if (result.get("toxicity") > config.toxicity()
                    || result.get("severe_toxicity") > config.severeToxicity()
                    || result.get("obscene") > config.obscene()
                    || result.get("threat") > config.threat()
                    || result.get("insult") > config.insult()
                    || result.get("identity_attack") > config.identityAttack()) {
                return true;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        if (config.debug()) {
            double elapsedMs = (System.nanoTime() - start) / 1_000_000.0;
            System.out.printf("Moderation took %.2f ms%n", elapsedMs);
        }
        return false;
    }

    public boolean onChatEvent(String message, P player) {
        if (checkMessage(message)) {
            sendPlayerMessage(player, config.playerMessage());
            sendAllStaffMessage(player, String.format(config.staffMessage(), getPlayerName(player), message));
            sendConsoleMessage(String.format(config.consoleMessage(), getPlayerName(player), message));
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

}
