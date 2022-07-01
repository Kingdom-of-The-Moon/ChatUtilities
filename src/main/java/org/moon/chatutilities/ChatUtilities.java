package org.moon.chatutilities;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.moon.chatutilities.config.ConfigManager;

import java.util.regex.Pattern;

public class ChatUtilities implements ClientModInitializer {
    public static int lastHour = -1;
    public static int lastMinute = -1;
    public static int lastSecond = -1;
    public static final Text STYLE =Text.literal("                       ").formatted(Formatting.DARK_GRAY, Formatting.STRIKETHROUGH);
    public static SoundEvent soundEvent = null;
    public static Pattern pingRegex = null;

    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitializeClient() {
        //init config
        ConfigManager.initialize();
    }

    private static TextureManager textureManager;
    public static TextureManager getTextureManager() {
        if (textureManager == null) textureManager = MinecraftClient.getInstance().getTextureManager();
        return textureManager;
    }

    //object to get the String from an OrderedText because mojank
    //yoinked from uwurst
    public static class JustGiveMeTheStringVisitor implements CharacterVisitor {
        StringBuilder sb = new StringBuilder();

        @Override
        public boolean accept(int index, Style style, int codePoint) {
            sb.appendCodePoint(codePoint);
            return true;
        }

        @Override
        public String toString() {
            return sb.toString();
        }

        public static String get(OrderedText text) {
            JustGiveMeTheStringVisitor visitor = new JustGiveMeTheStringVisitor();
            text.accept(visitor);
            return visitor.toString();
        }
    }
}
