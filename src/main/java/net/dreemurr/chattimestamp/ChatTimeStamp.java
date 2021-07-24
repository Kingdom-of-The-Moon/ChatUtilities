package net.dreemurr.chattimestamp;

import net.dreemurr.chattimestamp.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatTimeStamp implements ClientModInitializer {
    public static int lastHour = -1;
    public static int lastMinute = -1;
    public static int lastSecond = -1;
    public static final Text style = new LiteralText("                       ").formatted(Formatting.DARK_GRAY, Formatting.STRIKETHROUGH);

    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitializeClient() {
        //init config
        Config.initialize();
    }
}
