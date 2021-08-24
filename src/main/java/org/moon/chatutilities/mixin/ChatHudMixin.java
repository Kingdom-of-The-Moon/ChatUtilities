package org.moon.chatutilities.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import org.moon.chatutilities.ChatUtilities;
import org.moon.chatutilities.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.moon.chatutilities.data.ImageCache;
import org.moon.chatutilities.data.ImageUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalTime;
import java.util.Deque;
import java.util.List;

import static net.minecraft.client.gui.DrawableHelper.*;

import static net.minecraft.client.gui.DrawableHelper.fill;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    private int spam = 1;
    private int queueSize = 0;

    private int x1, y1, x2, y2, color;

    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private Deque<Text> messageQueue;
    @Shadow @Final private List<ChatHudLine<Text>> messages;
    @Shadow @Final private List<ChatHudLine<OrderedText>> visibleMessages;

    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;)V", cancellable = true)
    public void addMessage(Text message, CallbackInfo ci) {
        boolean cancel = cut$onAddMessage(message);

        String imageURL = ImageUtils.getImageURL(message.getString());
        boolean hasImage = imageURL != null;
        if (hasImage) {
            //String a = message.shallowCopy().getString().replaceAll(ImageUtils.MATCH_URL.pattern(), "<Image>");
            LiteralText newMessage = new LiteralText("");
            message.getSiblings().forEach((text -> {
                if (text.getString().matches(ImageUtils.MATCH_URL.pattern())) {
                    text = new LiteralText(text.copy().getString().replace(imageURL, "<Image>"));
                }
                newMessage.append(text);
            }));

            this.addMessage(newMessage, 0);
        }

        if (cancel || hasImage) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "queueMessage", cancellable = true)
    public void queueMessage(Text message, CallbackInfo ci) {
        //anti spam
        if ((boolean) Config.entries.get("enableAntiSpam").value && cut$antiSpam(message, !this.messageQueue.isEmpty()))
            ci.cancel();

        //save size
        queueSize = this.messageQueue.size();
    }

    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;I)V")
    public void addMessageHead(Text message, int messageId, CallbackInfo ci) {
        //play sound... if valid
        if (ChatUtilities.pingRegex != null && ChatUtilities.pingRegex.matcher(message.getString()).find() && ChatUtilities.soundEvent != null)
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(ChatUtilities.soundEvent, 1));
    }

    //yeet the bg color render
    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/ChatHud;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V"
            ),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/OrderedText;FFI)I")
            )
    )
    private void renderFill(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        //save vars for future use
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
    }

    //render bg with text
    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/OrderedText;FFI)I",
                    by = 1
            )
    )
    private int renderDraw(TextRenderer textRenderer, MatrixStack matrices, OrderedText text, float x, float y, int color) {
        int bgColor = this.color;

        //bruh
        ChatUtilities.JustGiveMeTheStringVisitor lineString = new ChatUtilities.JustGiveMeTheStringVisitor();
        text.accept(lineString);

        //apply bg color
        if (ChatUtilities.pingRegex != null && ChatUtilities.pingRegex.matcher(lineString.toString()).find()) {
            String bgColorConfig = (String) Config.entries.get("pingBgColor").value;
            if (bgColorConfig.startsWith("#")) bgColorConfig = bgColorConfig.substring(1);

            bgColor += Integer.parseInt(bgColorConfig, 16);
        }

        //render bg
        matrices.translate(0.0D, 0.0D, -50.0D);
        fill(matrices, x1, y1, x2, y2, bgColor);
        matrices.translate(0.0D, 0.0D, 50.0D);

        //render text
        return this.client.textRenderer.drawWithShadow(matrices, text, x, y, color);
    }

    //render images in chat
    @Inject(at = @At("TAIL"), method = "render")
    private void render(MatrixStack matrices, int tickDelta, CallbackInfo ci) {

        ImageCache.ImageTexture t = ImageCache.getOrLoadImage("https://cdn.discordapp.com/attachments/858501113619677224/878848816206458970/unknown.png");
        RenderSystem.setShaderTexture(0, t.getGlId());

        int size = 64;
        int w = (int) (size*t.ratio), h = size;

        RenderSystem.enableBlend();
        //drawTexture(matrices, 4, -15, 0, 0, 0, w, h, h, w);
        RenderSystem.disableBlend();
    }

    @Shadow public abstract int getWidth();
    @Shadow public abstract double getChatScale();
    @Shadow protected abstract void addMessage(Text message, int messageId);

    //anti spam
    private boolean cut$antiSpam(Text message, boolean isQueue) {
        //get messages
        String messageString = message.getString();
        Text lastMessage = null;
        String[] lastMessageString = {"", ""};
        boolean hasClock = (boolean) Config.entries.get("enableClock").value && (boolean) Config.entries.get("onMessage").value && !isQueue;

        if (isQueue) //get from queue
            lastMessage = this.messageQueue.getLast();
        else if (!this.messages.isEmpty()) //get from message list
            lastMessage = this.messages.get(0).getText();

        //add spam count to the current message if theres spam
        messageString += spam > 1 ? " x" + spam : "";

        //set last message string
        if (lastMessage != null) {
            lastMessageString[1] = lastMessage.getString();

            //message clock fix
            if (hasClock) {
                lastMessageString = lastMessageString[1].split(" ", 2);
            }
        }

        //compare messages
        if (messageString.equals(lastMessageString[1])) {
            //increase spam
            spam++;

            //edit old message
            Text formatted = (((MutableText) message).append(new LiteralText(" x" + spam).formatted(Formatting.DARK_GRAY, Formatting.ITALIC)));

            if (hasClock) {
                formatted = new LiteralText("").append(new LiteralText(lastMessageString[0] + " ").formatted(Formatting.DARK_GRAY, Formatting.ITALIC)).append(formatted);
            }

            if (isQueue) {
                this.messageQueue.removeLast();
                this.messageQueue.addLast(formatted);
            } else {
                ChatHudLine<Text> lastChatMessage = this.messages.get(0);

                //message history
                this.messages.set(0, new ChatHudLine<>(this.client.inGameHud.getTicks(), formatted, lastChatMessage.getId()));

                //hud message list
                int i = MathHelper.floor((double) this.getWidth() / this.getChatScale());
                List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(formatted, i, this.client.textRenderer);
                for (int j = 0, k = list.size() - 1; j < list.size(); j++, k--) {
                    this.visibleMessages.set(k, new ChatHudLine<>(this.client.inGameHud.getTicks(), list.get(j), lastChatMessage.getId()));
                }
            }

            //return
            return true;
        }

        //reset count if not spam
        spam = 1;

        return false;
    }

    private boolean cut$onAddMessage(Text message) {
        /////////////
        //anti spam//
        /////////////

        if ((boolean) Config.entries.get("enableAntiSpam").value && messageQueue.size() >= queueSize && cut$antiSpam(message, false)) {
            return true;
        }

        /////////////
        //timestamp//
        /////////////

        //do nothing if clock is disabled
        if (!(boolean) Config.entries.get("enableClock").value)
            return true;

        //get current time
        LocalTime timeNow = LocalTime.now();

        //format time
        int hour = timeNow.getHour();
        int minute = timeNow.getMinute();
        int second = timeNow.getSecond();

        //12h
        if ((boolean) Config.entries.get("twelveHour").value) {
            hour -= hour >= 13 ? 12 : 0;
        }

        //define if should send time message
        boolean newTime =
                (boolean) Config.entries.get("onMessage").value ||
                        ((boolean) Config.entries.get("showSeconds").value && second != ChatUtilities.lastSecond) ||
                        minute != ChatUtilities.lastMinute ||
                        hour != ChatUtilities.lastHour ||
                        messages.isEmpty();

        //add time message
        if (newTime) {
            //save current time
            ChatUtilities.lastHour = hour;
            ChatUtilities.lastMinute = minute;
            ChatUtilities.lastSecond = second;

            //create time message
            MutableText time = new LiteralText("[" + String.format("%02d", hour) + ":" + String.format("%02d", minute)).formatted(Formatting.GRAY);

            //add seconds if enabled
            if ((boolean) Config.entries.get("showSeconds").value)
                time.append(new LiteralText(":" + String.format("%02d", second)));

            //close time text
            time.append("] ");

            //add message
            if (!(boolean) Config.entries.get("onMessage").value) {
                this.addMessage(new LiteralText("").append(ChatUtilities.style).append(" ").append(time).append(ChatUtilities.style), 0);
            } else {
                this.addMessage(new LiteralText("").append(time.formatted(Formatting.DARK_GRAY, Formatting.ITALIC)).append(message), 0);
                return true;
            }
        }
        return false;
    }
}
