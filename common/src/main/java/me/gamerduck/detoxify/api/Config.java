package me.gamerduck.detoxify.api;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class Config {

    @Comment("Debug-related options")
    public Debug debug = new Debug();

    @Comment("Chat messages displayed to players and staff")
    public Messages messages = new Messages();

    @Comment("Threshold values for toxicity categories")
    public Values values = new Values();

    @ConfigSerializable
    public static class Debug {
        public boolean enabled = false;
    }

    @ConfigSerializable
    public static class Messages {
        public String player = "§cYour message was removed for violating chat rules.";
        public String staff = "%s's message has been removed [%s]";
        public String console = "%s's message has been removed [%s]";
    }

    @ConfigSerializable
    public static class Values {
        public double toxicity = 0.8;
        public double severeToxicity = 0.8;
        public double obscene = -1.0;
        public double threat = -1.0;
        public double insult = -1.0;
        public double identityAttack = -1.0;
    }
}
