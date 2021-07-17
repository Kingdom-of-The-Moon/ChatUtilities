package net.dreemurr.chattimestamp.mixin;

import net.dreemurr.chattimestamp.ChatTimeStamp;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalTime;
import java.util.Deque;
import java.util.List;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    private int spam = 1;
    private int queueSize = 0;

    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private Deque<Text> messageQueue;
    @Shadow @Final private List<ChatHudLine<Text>> messages;
    @Shadow @Final private List<ChatHudLine<OrderedText>> visibleMessages;

    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;)V", cancellable = true)
    public void addMessage(Text message, CallbackInfo ci) {
        /////////////
        //anti spam//
        /////////////

        if (messageQueue.size() >= queueSize && chattimestamp$antiSpam(message, false)) {
            ci.cancel();
            return;
        }

        /////////////
        //timestamp//
        /////////////

        //get current time
        LocalTime timeNow = LocalTime.now();

        //format time
        int hour = timeNow.getHour();
        hour -= hour >= 13 ? 12 : 0;
        int minute = timeNow.getMinute();
        //int second = timeNow.getSecond();

        //define if should send time message
        boolean newTime = /* second != ChatTimeStamp.lastSecond || */ minute != ChatTimeStamp.lastMinute || hour != ChatTimeStamp.lastHour || messages.isEmpty();

        if (newTime) {
            //save current time
            ChatTimeStamp.lastHour = hour;
            ChatTimeStamp.lastMinute = minute;
            //ChatTimeStamp.lastSecond = second;

            //add time message
            Text time = new LiteralText(" [" + String.format("%02d", hour) + ":" + String.format("%02d", minute) + /* ":" + String.format("%02d", second) + */ "] ").formatted(Formatting.GRAY);
            this.addMessage(new LiteralText("").append(ChatTimeStamp.style).append(time).append(ChatTimeStamp.style), 0);
        }
    }

    @Inject(at = @At("HEAD"), method = "queueMessage", cancellable = true)
    public void queueMessage(Text message, CallbackInfo ci) {
        //anti spam
        if (chattimestamp$antiSpam(message, !this.messageQueue.isEmpty()))
            ci.cancel();

        //save size
        queueSize = this.messageQueue.size();
    }

    @Shadow protected abstract void addMessage(Text message, int messageId);

    @Shadow public abstract int getWidth();

    @Shadow public abstract double getChatScale();

    //anti spam
    public boolean chattimestamp$antiSpam(Text message, boolean isQueue) {
        //get messages
        String messageString = message.getString();
        Text lastMessage = null;

        if (isQueue) //get from queue
            lastMessage = this.messageQueue.getLast();
        else if (!this.messages.isEmpty()) //get from message list
            lastMessage = this.messages.get(0).getText();

        //add spam count to the current message if theres spam
        messageString += spam > 1 ? " x" + spam : "";

        //compare messages
        if (lastMessage != null && messageString.equals(lastMessage.getString())) {
            //increase spam
            spam++;

            //edit old message
            Text formatted = (((MutableText) message).append(new LiteralText(" x" + spam).formatted(Formatting.DARK_GRAY, Formatting.ITALIC)));

            if (isQueue) {
                this.messageQueue.removeLast();
                this.messageQueue.addLast(formatted);
            } else {
                ChatHudLine<Text> lastChatMessage = this.messages.get(0);

                //message history
                this.messages.set(0, new ChatHudLine<>(lastChatMessage.getCreationTick(), formatted, lastChatMessage.getId()));

                //hud message list
                int i = MathHelper.floor((double)this.getWidth() / this.getChatScale());
                List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(formatted, i, this.client.textRenderer);
                for (int j = 0, k = list.size() - 1; j < list.size(); j++, k--) {
                    this.visibleMessages.set(k, new ChatHudLine<>(lastChatMessage.getCreationTick(), list.get(j), lastChatMessage.getId()));
                }
            }

            //return
            return true;
        }

        //reset count if not spam
        spam = 1;

        return false;
    }
}
