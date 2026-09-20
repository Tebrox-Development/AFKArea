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

        return miniMessage.deserialize(translateLegacyCodes(message), resolvers);
    }

    public void send(Audience audience, String message, TagResolver... resolvers) {
        if(message == null || message.isBlank()) {
            return;
        }

        Component prefix = parse(config.get().prefix);
        Component content = parse(message, resolvers);

        audience.sendMessage(prefix.append(content));
    }

    private String translateLegacyCodes(String message) {
        StringBuilder output = new StringBuilder(message.length());
        boolean insideMiniMessageTag = false;

        for(int index = 0; index < message.length(); index++) {
            char current = message.charAt(index);

            if(current == '<' && isMiniMessageTagStart(message, index)) {
                insideMiniMessageTag = true;
                output.append(current);
                continue;
            }

            if(current == '>' && insideMiniMessageTag) {
                insideMiniMessageTag = false;
                output.append(current);
                continue;
            }

            if(insideMiniMessageTag || current != '&' || index + 1 >= message.length()) {
                output.append(current);
                continue;
            }

            if(isSimpleHexCode(message, index)) {
                String hex = message.substring(index + 2, index + 8);
                output.append("<reset><#" + hex + ">");
                index += 7;
                continue;
            }

            String bungeeHex = readBungeeHexCode(message, index);
            if(bungeeHex != null) {
                output.append("<reset><#" + bungeeHex + ">");
                index += 13;
                continue;
            }

            String replacement = legacyReplacement(message.charAt(index + 1));
            if(replacement == null) {
                output.append(current);
                continue;
            }

            output.append(replacement);
            index++;
        }

        return output.toString();
    }

    private boolean isMiniMessageTagStart(String message, int index) {
        if(index > 0 && message.charAt(index - 1) == '\\') return false;
        if(index + 1 >= message.length()) return false;

        char next = message.charAt(index + 1);

        return Character.isLetter(next) || next == '/' || next == '!' || next == '#';
    }

    private boolean isSimpleHexCode(String message, int index) {
        if(index + 7 >= message.length() || message.charAt(index + 1) != '#') return false;

        for(int offet = 2; offet <= 7; offet++) {
            if(!isHexDigit(message.charAt(index + offet))) return false;
        }

        return true;
    }

    private String readBungeeHexCode(String message, int index) {
        if(index + 13 >= message.length() || Character.toLowerCase(message.charAt(index + 1)) != 'x') return null;

        StringBuilder hex = new StringBuilder(6);

        for(int part = 0; part < 6; part++) {
            int ampersandIndex = index + 2 + (part * 2);
            int digitIndex = ampersandIndex + 1;
            if(message.charAt(ampersandIndex) != '&') return null;

            char digit = message.charAt(digitIndex);
            if(!isHexDigit(digit)) return null;
            hex.append(digit);
        }
        return hex.toString();
    }

    private boolean isHexDigit(char value) {
        return Character.digit(value, 16) >= 0;
    }

    private String legacyReplacement(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> "<reset><black>";
            case '1' -> "<reset><dark_blue>";
            case '2' -> "<reset><dark_green>";
            case '3' -> "<reset><dark_aqua>";
            case '4' -> "<reset><dark_red>";
            case '5' -> "<reset><dark_purple>";
            case '6' -> "<reset><gold>";
            case '7' -> "<reset><gray>";
            case '8' -> "<reset><dark_gray>";
            case '9' -> "<reset><blue>";
            case 'a' -> "<reset><green>";
            case 'b' -> "<reset><aqua>";
            case 'c' -> "<reset><red>";
            case 'd' -> "<reset><light_purple>";
            case 'e' -> "<reset><yellow>";
            case 'f' -> "<reset><white>";

            case 'k' -> "<obfuscated>";
            case 'l' -> "<bold>";
            case 'm' -> "<strikethrough>";
            case 'n' -> "<underlined>";
            case 'o' -> "<italic>";
            case 'r' -> "<reset>";

            default -> null;
        };
    }
}
