package net.dreemurr.chattimestamp.mixin;

import net.minecraft.client.gui.hud.ChatHudListener;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.time.LocalTime;

@Mixin(ChatHudListener.class)
public class ChatHudListenerMixin {

    @ModifyVariable(method = "onChatMessage", at = @At("HEAD"))
    private Text onChatMessage(Text message) {
        LocalTime time = LocalTime.now();

        Text temp = message.copy();
        Text date = new LiteralText("[" + time.getHour() % 12 + ":" + time.getMinute() + /*":" + time.getSecond() +*/ "] ").formatted(Formatting.GRAY);

        return new LiteralText("").append(date).append(temp);
    }
}
