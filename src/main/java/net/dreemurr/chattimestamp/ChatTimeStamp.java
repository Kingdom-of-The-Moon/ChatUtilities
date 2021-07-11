package net.dreemurr.chattimestamp;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChatTimeStamp {
    public static int lastHour = -1;
    public static int lastMinute = -1;
    //public static int lastSecond = -1;
    public static final Text style = new LiteralText("                       ").formatted(Formatting.DARK_GRAY, Formatting.STRIKETHROUGH);
}
