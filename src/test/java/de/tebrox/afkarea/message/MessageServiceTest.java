package de.tebrox.afkarea.message;

import de.tebrox.afkarea.config.MessageConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageServiceTest {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final MessageService messageService = new MessageService(MessageConfig::new);

    @Test
    void keepsMiniMessageSupport() {
        Component expected = miniMessage.deserialize("<green>Hello</green>");
        Component actual = messageService.parse("<green>Hello</green>");

        assertEquals(expected, actual);
    }

    @Test
    void parsesLegacyColorsAroundPlaceholder() {
        Component expected = miniMessage.deserialize("<green>You received </green><yellow>diamond</yellow><green>!</green>");
        Component actual = messageService.parse("&aYou received &e<reward>&a!", Placeholder.unparsed("reward", "diamond"));

        assertEquals(expected, actual);
    }

    @Test
    void parsesLegacyHexColor() {
        Component expected = miniMessage.deserialize("<reset><#55ff55>Hello");
        Component actual = messageService.parse("&#55ff55Hello");

        assertEquals(expected, actual);
    }
}