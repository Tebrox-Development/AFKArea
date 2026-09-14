package de.tebrox.afkarea.message;

import de.tebrox.afkarea.config.MessageConfig;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.function.Supplier;

public final class MessageService {

    private final Supplier<MessageConfig> config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MessageService(Supplier<MessageConfig> config) {
        this.config = config;
    }

    public Component parse(String message, TagResolver... resolvers) {
        if(message == null || message.isBlank()) return Component.empty();

        return miniMessage.deserialize(message, resolvers);
    }

    public void send(Audience audience, String message, TagResolver... resolvers) {
        if(message == null || message.isBlank()) {
            return;
        }

        Component prefix = parse(config.get().prefix);
        Component content = parse(message, resolvers);

        audience.sendMessage(prefix.append(content));
    }

}
