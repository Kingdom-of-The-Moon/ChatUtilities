package net.dreemurr.chattimestamp.mixin;

import net.dreemurr.chattimestamp.ChatTimeStamp;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalTime;
import java.util.List;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @Shadow @Final private List<ChatHudLine<Text>> messages;

    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;)V")
    public void addMessage(Text message, CallbackInfo ci) {
        LocalTime timeNow = LocalTime.now();

        int hour   = timeNow.getHour() % 12;
        int minute = timeNow.getMinute();
        //int second = timeNow.getSecond();

        boolean newTime = hour != ChatTimeStamp.lastHour || minute != ChatTimeStamp.lastMinute /* || second != ChatTimeStamp.lastSecond */ || this.messages.size() == 0;

        if (newTime) {
            ChatTimeStamp.lastHour   = hour;
            ChatTimeStamp.lastMinute = minute;
            //ChatTimeStamp.lastSecond = second;
            Text time = new LiteralText("[" + String.format("%02d", hour) + ":" + String.format("%02d", minute) /* + ":" + String.format("%02d", second) */ + "]").formatted(Formatting.GRAY);

            this.addMessage(new LiteralText("").append(ChatTimeStamp.style).append(" ").append(time).append(" ").append(ChatTimeStamp.style), 0);
        }
    }

    @Shadow
    private void addMessage(Text message, int messageId) {}
}
