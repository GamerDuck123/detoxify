package me.gamerduck.detoxify.api;

import me.gamerduck.detoxify.Platform;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Configuration manager for Detoxify settings.
 * <p>
 * Handles loading, saving, and accessing configuration properties from a config.properties file.
 * Automatically migrates old configurations by adding new properties, removing deprecated ones,
 * and maintaining property order.
 * </p>
 * <p>
 * Configuration includes:
 * <ul>
 *     <li>Debug mode setting</li>
 *     <li>Toxicity detection thresholds (toxicity, severe-toxicity, obscene, threat, insult, identity-attack)</li>
 *     <li>Custom messages (player notification, staff notification, console logging)</li>
 * </ul>
 * </p>
 */
public class DetoxifyConfig {
    private final LinkedHashMap<String, String> properties;

    private final LinkedHashMap<String, String> comments;

    private final File configFile;

    private final Platform platform;

    private static final boolean DEFAULT_DEBUG = false;
    private static final String DEFAULT_PLAYER_MESSAGE = "§cYour message was removed for violating chat rules.";
    private static final String DEFAULT_STAFF_MESSAGE = "%s's message has been removed [%s]";
    private static final String DEFAULT_CONSOLE_MESSAGE = "%s's message has been removed [%s]";
    private static final double DEFAULT_TOXICITY = 0.9;
    private static final double DEFAULT_SEVERE_TOXICITY = 0.5;
    private static final double DEFAULT_OBSCENE = 0.25;
    private static final double DEFAULT_THREAT = -1;
    private static final double DEFAULT_INSULT = -1;
    private static final double DEFAULT_IDENTITY_ATTACK = -1;

    /**
     * Constructs a new DetoxifyConfig instance.
     * <p>
     * Creates or loads the configuration file from the specified data folder.
     * If the folder doesn't exist, it will be created. Automatically loads
     * configuration from disk or creates a new file with default values.
     * </p>
     *
     * @param dataFolder the directory where config.properties should be stored
     * @param platform the platform implementation for logging
     */
    public DetoxifyConfig(File dataFolder, Platform platform) {
        this.configFile = new File(dataFolder, "config.properties");
        this.platform = platform;
        this.properties = new LinkedHashMap<>();
        this.comments = new LinkedHashMap<>();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        loadConfig();
    }

    /**
     * Loads configuration from disk and migrates if necessary.
     * <p>
     * If the config file exists, it reads all properties and comments, then:
     * <ul>
     *     <li>Adds any new properties from defaults</li>
     *     <li>Removes deprecated properties</li>
     *     <li>Updates property comments if changed</li>
     *     <li>Reorders properties to match defaults</li>
     * </ul>
     * If the file doesn't exist or an error occurs, creates a new config with default values.
     * </p>
     */
    private void loadConfig() {
        setDefaults();

        if (configFile.exists()) {
            LinkedHashMap<String, String> defaults = new LinkedHashMap<>(properties);
            LinkedHashMap<String, String> defaultComments = new LinkedHashMap<>(comments);

            properties.clear();
            comments.clear();

            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                String line;
                String lastComment = null;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (line.isEmpty()) {
                        continue;
                    }

                    if (line.startsWith("#")) {
                        lastComment = line.substring(1).trim();
                        continue;
                    }

                    int equalsIndex = line.indexOf('=');
                    if (equalsIndex > 0) {
                        String key = line.substring(0, equalsIndex).trim();
                        String value = line.substring(equalsIndex + 1).trim();
                        properties.put(key, value);

                        if (lastComment != null) {
                            comments.put(key, lastComment);
                            lastComment = null;
                        }
                    }
                }

                boolean configChanged = false;

                for (String key : defaults.keySet()) {
                    if (!properties.containsKey(key)) {
                        properties.put(key, defaults.get(key));
                        comments.put(key, defaultComments.get(key));
                        configChanged = true;
                        platform.sendConsoleMessage("Added new config option: " + key);
                    }
                }

                LinkedHashMap<String, String> filtered = new LinkedHashMap<>();
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    if (defaults.containsKey(entry.getKey())) {
                        filtered.put(entry.getKey(), entry.getValue());
                    } else {
                        configChanged = true;
                        platform.sendConsoleMessage("Removed deprecated config option: " + entry.getKey());
                    }
                }

                for (String key : filtered.keySet()) {
                    String oldComment = comments.get(key);
                    String newComment = defaultComments.get(key);

                    // Comment was added
                    if ((oldComment == null || oldComment.isEmpty()) && newComment != null && !newComment.isEmpty()) {
                        comments.put(key, newComment);
                        configChanged = true;
                    }
                    // Comment was removed
                    else if ((oldComment != null && !oldComment.isEmpty()) && (newComment == null || newComment.isEmpty())) {
                        comments.remove(key);
                        configChanged = true;
                    }
                    // Comment was changed
                    else if (oldComment != null && newComment != null && !oldComment.equals(newComment)) {
                        comments.put(key, newComment);
                        configChanged = true;
                    }
                }

                LinkedHashMap<String, String> reordered = new LinkedHashMap<>();
                for (String key : defaults.keySet()) {
                    if (filtered.containsKey(key)) {
                        reordered.put(key, filtered.get(key));
                    }
                }
                properties.clear();
                properties.putAll(reordered);

                if (configChanged) {
                    platform.sendConsoleMessage("Configuration updated, saving changes...");
                    saveConfig();
                }

                platform.sendConsoleMessage("Configuration loaded from " + configFile.getName());
            } catch (IOException e) {
                platform.sendConsoleMessage("Failed to load config, using defaults: " + e.getMessage());
                properties.clear();
                properties.putAll(defaults);
                comments.clear();
                comments.putAll(defaultComments);
                saveConfig();
            }
        } else {
            saveConfig();
        }
    }

    /**
     * Sets all default configuration values and their comments.
     * <p>
     * This method defines the complete configuration schema including:
     * default values, comments for documentation, and the order properties
     * should appear in the config file.
     * </p>
     */
    private void setDefaults() {
        properties.clear();
        comments.clear();
        addComment("####################################\n"
                + "#                                  #\n"
                + "#      Detoxify Configuration      #\n"
                + "#                                  #\n"
                + "####################################");
        setProperty("debug", String.valueOf(DEFAULT_DEBUG), "Whether or not there should be debug message");

        addComment("\n############################\n"
                + "#      Value Settings      #\n"
                + "############################");
        setProperty("toxicity", String.valueOf(DEFAULT_TOXICITY), "");
        setProperty("severe-toxicity", String.valueOf(DEFAULT_SEVERE_TOXICITY), "");
        setProperty("obscene", String.valueOf(DEFAULT_OBSCENE), "");
        setProperty("threat", String.valueOf(DEFAULT_THREAT), "");
        setProperty("insult", String.valueOf(DEFAULT_INSULT), "");
        setProperty("identity-attack", String.valueOf(DEFAULT_IDENTITY_ATTACK), "");

        // Messages
        addComment("\n############################\n"
                + "#     Message Settings     #\n"
                + "############################");
        setProperty("player", DEFAULT_PLAYER_MESSAGE, "");
        setProperty("staff", DEFAULT_STAFF_MESSAGE, "");
        setProperty("console", DEFAULT_CONSOLE_MESSAGE, "");
    }

    /**
     * Sets a property with an optional comment.
     * <p>
     * Comments can contain multiple lines separated by newline characters.
     * The comment will appear above the property in the saved config file.
     * </p>
     *
     * @param key the property name
     * @param value the property value
     * @param comment the comment to display above the property, or null for no comment
     */
    private void setProperty(String key, String value, String comment) {
        properties.put(key, value);
        if (comment != null && !comment.isEmpty()) {
            comments.put(key, comment);
        }
    }

    /**
     * Adds a standalone comment or section header to the config file.
     * <p>
     * The comment will appear exactly where this method is called in relation to other
     * properties and comments, maintaining the order defined in setDefaults().
     * You handle all formatting yourself - the text you provide will be written
     * as-is to the config file without any modification or prefix.
     * </p>
     * <p>
     * This is useful for adding section headers or visual separators in the config file.
     * For example:
     * <pre>
     * addComment("# ========================================");
     * addComment("#           Database Settings");
     * addComment("# ========================================");
     * setProperty("database.type", "h2", "Database type");
     * </pre>
     * </p>
     *
     * @param commentText the text to write to the config file (you handle formatting)
     */
    private void addComment(String commentText) {
        String commentKey = "__COMMENT__" + ThreadLocalRandom.current().nextInt();
        properties.put(commentKey, "");
        comments.put(commentKey, commentText);
    }

    /**
     * Saves the current configuration to disk.
     * <p>
     * Writes all properties to the config file with a formatted header,
     * including comments above each property. Properties are written in
     * the order they were added to maintain a consistent file structure.
     * </p>
     */
    public void saveConfig() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                // Check if this is a header (standalone comment)
                if (key.startsWith("__COMMENT__")) {
                    // Only write the comment, not the property
                    if (comments.containsKey(key)) {
                        writer.write(comments.get(key));
                        writer.newLine();
                    }
                    writer.newLine();
                } else {
                    // Regular property - write comment if exists
                    if (comments.containsKey(key)) {
                        writer.write("# " + comments.get(key));
                        writer.newLine();
                    }

                    // Write property
                    writer.write(key + "=" + value);
                    writer.newLine();
                }
            }

            platform.sendConsoleMessage("Configuration saved to " + configFile.getName());
        } catch (IOException e) {
            platform.sendConsoleMessage("Failed to save config: " + e.getMessage());
        }
    }

    /**
     * Gets the debug mode setting.
     * <p>
     * When debug mode is enabled, additional diagnostic information is logged to help
     * with threshold tuning and troubleshooting.
     * </p>
     *
     * @return {@code true} if debug mode is enabled, {@code false} otherwise
     */
    public Boolean debug() {
        return Boolean.parseBoolean(properties.getOrDefault("debug", String.valueOf(DEFAULT_DEBUG)));
    }

    /**
     * Gets the toxicity detection threshold.
     * <p>
     * This threshold detects general toxic behavior in messages. Values range from 0.0 to 1.0,
     * where higher values require stronger toxicity signals to trigger. Set to -1 to disable
     * toxicity detection entirely.
     * </p>
     *
     * @return the toxicity threshold value, or -1 if disabled
     */
    public Double toxicity() {
        return Double.parseDouble(properties.getOrDefault("toxicity", String.valueOf(DEFAULT_TOXICITY)));
    }

    /**
     * Gets the severe toxicity detection threshold.
     * <p>
     * This threshold detects extremely toxic messages that are particularly harmful.
     * Values range from 0.0 to 1.0, where higher values require stronger signals.
     * Set to -1 to disable severe toxicity detection.
     * </p>
     *
     * @return the severe toxicity threshold value, or -1 if disabled
     */
    public Double severeToxicity() {
        return Double.parseDouble(properties.getOrDefault("severe-toxicity", String.valueOf(DEFAULT_SEVERE_TOXICITY)));
    }

    /**
     * Gets the obscene content detection threshold.
     * <p>
     * This threshold detects profanity and sexually explicit content in messages.
     * Values range from 0.0 to 1.0. Set to -1 to disable obscene content detection.
     * </p>
     *
     * @return the obscene content threshold value, or -1 if disabled
     */
    public Double obscene() {
        return Double.parseDouble(properties.getOrDefault("obscene", String.valueOf(DEFAULT_OBSCENE)));
    }

    /**
     * Gets the threat detection threshold.
     * <p>
     * This threshold detects threatening language or violent content in messages.
     * Values range from 0.0 to 1.0. Set to -1 to disable threat detection.
     * Disabled by default.
     * </p>
     *
     * @return the threat detection threshold value, or -1 if disabled
     */
    public Double threat() {
        return Double.parseDouble(properties.getOrDefault("threat", String.valueOf(DEFAULT_THREAT)));
    }

    /**
     * Gets the insult detection threshold.
     * <p>
     * This threshold detects insulting or derogatory language in messages.
     * Values range from 0.0 to 1.0. Set to -1 to disable insult detection.
     * Disabled by default.
     * </p>
     *
     * @return the insult detection threshold value, or -1 if disabled
     */
    public Double insult() {
        return Double.parseDouble(properties.getOrDefault("insult", String.valueOf(DEFAULT_INSULT)));
    }

    /**
     * Gets the identity attack detection threshold.
     * <p>
     * This threshold detects attacks based on identity characteristics such as race,
     * religion, gender, or sexual orientation. Values range from 0.0 to 1.0.
     * Set to -1 to disable identity attack detection. Disabled by default.
     * </p>
     *
     * @return the identity attack threshold value, or -1 if disabled
     */
    public Double identityAttack() {
        return Double.parseDouble(properties.getOrDefault("identity-attack", String.valueOf(DEFAULT_IDENTITY_ATTACK)));
    }

    /**
     * Gets the message displayed to the player whose message was blocked.
     * <p>
     * This message supports Minecraft color codes (§ prefix). The default message
     * is "§cYour message was removed for violating chat rules."
     * </p>
     *
     * @return the player notification message
     */
    public String playerMessage() {
        return properties.getOrDefault("player", DEFAULT_PLAYER_MESSAGE);
    }

    /**
     * Gets the message displayed to staff members when a message is blocked.
     * <p>
     * This message supports String format placeholders:
     * <ul>
     *     <li>%s (first) - The player's name</li>
     *     <li>%s (second) - The toxicity category that triggered the block</li>
     * </ul>
     * The default format is "%s's message has been removed [%s]"
     * </p>
     *
     * @return the staff notification message template
     */
    public String staffMessage() {
        return properties.getOrDefault("staff", DEFAULT_STAFF_MESSAGE);
    }

    /**
     * Gets the message logged to console when a message is blocked.
     * <p>
     * This message supports String format placeholders:
     * <ul>
     *     <li>%s (first) - The player's name</li>
     *     <li>%s (second) - The toxicity category that triggered the block</li>
     * </ul>
     * The default format is "%s's message has been removed [%s]"
     * </p>
     *
     * @return the console log message template
     */
    public String consoleMessage() {
        return properties.getOrDefault("console", DEFAULT_CONSOLE_MESSAGE);
    }

    /**
     * Returns a string representation of this configuration.
     * <p>
     * Includes all current values for debug mode, all toxicity thresholds,
     * and all message templates.
     * </p>
     *
     * @return a string containing all configuration values
     */
    @Override
    public String toString() {
        return "SessionConfig{" +
                "debug=" + debug() +
                ", toxicity=" + toxicity() +
                ", severe-toxicity=" + severeToxicity() +
                ", obscene=" + obscene() +
                ", threat=" + threat() +
                ", insult=" + insult() +
                ", identity-attack=" + identityAttack() +
                ", player=" + playerMessage() +
                ", staff=" + staffMessage() +
                ", console=" + consoleMessage() +
                '}';
    }
}