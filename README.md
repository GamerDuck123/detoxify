# Detoxify

**Detoxify** is a lightweight yet powerful AI chat moderation tool that classifies messages into multiple categories, allowing you to finely tune what gets caught by the filter. Despite its performance, it uses **less than 150MB of RAM**, making it extremely efficient for both server and client use.

---

### How It Works

Detoxify uses the **detoxify AI model** to classify chat messages. Each message is analyzed and assigned scores for categories such as:

* Toxicity
* Severe Toxicity
* Obscene content
* Threats
* Insults
* Identity Attacks

These scores allow you to configure thresholds and customize exactly which messages are filtered.

---

### Installation

#### Server

1. Download the correct server version of Detoxify.
2. Place the `.jar` file in your `plugins` or `mods` folder.
3. Start your server.
4. The configuration will be located in:

    * Modded platforms: `config/Detoxify`
    * Plugin platforms: `plugins/Detoxify`

#### Client

1. Download the correct client version of Detoxify.
2. Place the `.jar` file in your `mods` folder.
3. Any messages classified as toxic will be hidden in chat, but still logged for review.
4. The configuration will be located in:
   `.minecraft/config/Detoxify`

---

### Configuration Options

This configuration works for both client and server. It allows fine-tuning of message thresholds and debug options:

```hocon
# Enables debug mode for message computation and threshold tuning
debug {
  enabled = false
}

# Custom messages displayed when a message is blocked
messages {
  player = "§cYour message was removed for violating chat rules."
  staff = "%s's message has been removed [%s]"
  console = "%s's message has been removed [%s]"
}

# Threshold values for AI classification categories
values {
  toxicity = 0.9
  severe-toxicity = 0.5
  obscene = 0.25
  threat = -1
  insult = -1
  identity-attack = -1
}
```

---

### Skript Support

> Note: Only available for Spigot and Paper platforms

Detoxify integrates with Skript for custom chat filtering. It provides a single expression that returns `true` or `false` depending on whether a message is considered toxic:

```skript
is [the] %string% toxic
detoxify %string%
```

#### Examples

```skript
on chat:
    if detoxify message is true:
        cancel event
        send "&cThat message was blocked for toxicity!" to player
```

```skript
on chat:
    if is the message toxic is true:
        cancel event
        send "&cThat message was blocked for toxicity!" to player
```