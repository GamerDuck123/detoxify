package me.gamerduck.detoxify.paper.support.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

import static me.gamerduck.detoxify.paper.DetoxifyPlugin.skriptOnly$paperPlatform;

public class ExprDetoxifyCheck extends SimpleExpression<Boolean> {

    private Expression<String> message;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        message = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected Boolean[] get(Event e) {
        String msg = message.getSingle(e);
        if (msg == null) return new Boolean[]{false};

        return new Boolean[]{skriptOnly$paperPlatform().checkMessage(msg)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "detoxify check of " + message.toString(e, debug);
    }
}
