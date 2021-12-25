package org.moon.chatutilities.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.moon.chatutilities.ChatUtilities;
import org.moon.chatutilities.accessor.ChatHudLineAccess;
import org.moon.chatutilities.config.ConfigManager.Config;
import org.moon.chatutilities.data.ChatRenderContext;
import org.moon.chatutilities.data.ImageCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Stack;

import static net.minecraft.client.gui.DrawableHelper.drawTexture;
import static net.minecraft.client.gui.DrawableHelper.fill;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Unique private int spam = 1;
    @Unique private int queueSize = 0;

    @Unique private int x1;
    @Unique private int y1;
    @Unique private int x2;
    @Unique private int y2;
    @Unique private int color;

    @Unique private List<String> imageLinks;
    @Unique private int inARow = 0;
    @Unique private Stack<ChatRenderContext> renderContextStack = new Stack<>();
    @Unique private TextRenderer textRenderer;

    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private Deque<Text> messageQueue;
    @Shadow @Final private List<ChatHudLine<Text>> messages;
    @Shadow @Final private List<ChatHudLine<OrderedText>> visibleMessages;


    @Inject(at = @At("TAIL"), method = "addMessage(Lnet/minecraft/text/Text;IIZ)V")
    public void addMessage(Text message, int messageId, int timestamp, boolean refresh, CallbackInfo ci) {

    }

    @Redirect(at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V"), method = "addMessage(Lnet/minecraft/text/Text;IIZ)V")
    private void listAddStealer(List instance, int i, Object e) {
        if (e instanceof ChatHudLine chatHudLine && chatHudLine.getText() instanceof OrderedText && imageLinks != null) {
            inARow++;
            if (inARow >= 3) {
                ((ChatHudLineAccess) chatHudLine).setImageLinks(imageLinks);
                imageLinks = null;
                inARow = 0;
            }
        }

        instance.add(i, e);
    }

    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;)V", cancellable = true)
    public void addMessage(Text message, CallbackInfo ci) {
        boolean cancel = cut$onAddMessage(message);

        if ((boolean) Config.SHOW_IMAGES.value) {
            List<String> links = cut$imageLinks(message);
            boolean hasImage = links.size() > 0;
            if (hasImage) {
                LiteralText newMessage = new LiteralText("");

                for(Text entry : message.getWithStyle(message.getStyle())) {
                    String str = entry.getString();

                    for(String link : links) {
                        str = str.replace(link, "§8§i<image>§r");
                    }

                    newMessage.append(new LiteralText(str));
                }

                imageLinks = links;

                this.addMessage(newMessage.append("\n".repeat(links.size()*4)), 0);
                ci.cancel();
            }
        }

        if (cancel) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "queueMessage", cancellable = true)
    public void queueMessage(Text message, CallbackInfo ci) {
        //anti spam
        if ((boolean) Config.HAS_ANTI_SPAM.value && cut$antiSpam(message, !this.messageQueue.isEmpty()))
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
            String bgColorConfig = (String) Config.PING_BG_COLOUR.value;
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

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/OrderedText;FFI)I",
                    by = 1
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void postRenderDraw(MatrixStack matrices, int tickDelta, CallbackInfo ci, int i, int j, boolean bl, float f, int k, double d, double e, double g, double h, int l, int m, ChatHudLine<?> chatHudLine, int o, double p, int q, int r, int s, double t) {
        if ((boolean) Config.SHOW_IMAGES.value) {
            List<String> links = ((ChatHudLineAccess)chatHudLine).getImageLinks();
            if (links != null)
                renderContextStack.push(new ChatRenderContext(chatHudLine, links, s, h, (int)(255.0D * o * d)));
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void renderImages(MatrixStack matrices, int tickDelta, CallbackInfo ci) {

        while (!renderContextStack.isEmpty()) {
            RenderSystem.enableBlend();
            ChatRenderContext ctx = renderContextStack.pop();
            int i = 0;
            for (String curLink : ctx.links()) {
                ImageCache.ImageTexture tex = ImageCache.getOrLoadImage(curLink);
                RenderSystem.setShaderTexture(0, tex.getGlId());
                RenderSystem.setShaderColor(1,1,1,ctx.aa()/255f);

                int size = 32;
                int width = (int) (size * tex.ratio);

                matrices.push();
                matrices.translate(0,0,500f);
                int x = 4;
                int y = (int)(ctx.s() + ctx.h()) + (size+4)*i;
                drawTexture(matrices, x, y, 0, 0, 0, width, size, size, width);
                matrices.pop();

                /* @TODO Make overlay and click link work
                 * if (client.mouse.getX() >= x && client.mouse.getX() <= x + width && client.mouse.getY() >= y && client.mouse.getY() <= y + size) {
                 *                     if (client.mouse.wasLeftButtonClicked()) {
                 *                         if (this.client.options.chatLinksPrompt) {
                 *                             this.client.setScreen(new ConfirmChatLinkScreen(b -> {
                 *                                 if (b) Util.getOperatingSystem().open(URI.create(curLink));
                 *                             }, curLink, false));
                 *                         } else {
                 *                             Util.getOperatingSystem().open(URI.create(curLink));
                 *                         }
                 *                     }
                 *
                 *                     drawStringWithShadow(matrices, textRenderer, curLink, 0, 0, 0xFFFFFF);
                 *                 }
                 */


                i++;
            }
            RenderSystem.disableBlend();
        }

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
        boolean hasClock = (boolean) Config.HAS_CLOCK.value && (boolean) Config.CLOCK_ON_MESSAGE.value && !isQueue;

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

    private List<String> cut$imageLinks(Text message) {
        String[] tokens = message.getString().split(" ");

        ArrayList<String> urls = new ArrayList<>();
        for (String s : tokens) {
            if (s.matches("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")) {
                urls.add(s);
            }
        }

        return urls;
    }

    private Text cut$removeText(Text message, List<String> toRemove) {

        String str = message.getString();
        toRemove.forEach(s -> str.replace(s, ""));

        MutableText newMessage = Text.of(str).copy();

        message.getSiblings().forEach(sibling -> {
            newMessage.append(cut$removeText(sibling, toRemove));
        });

        return newMessage;
    }

    private boolean cut$onAddMessage(Text message) {

        /////////////
        //anti spam//
        /////////////

        if ((boolean) Config.HAS_ANTI_SPAM.value && messageQueue.size() >= queueSize && cut$antiSpam(message, false)) {
            return true;
        }

        /////////////
        //timestamp//
        /////////////

        //do nothing if clock is disabled
        if (!(boolean) Config.HAS_CLOCK.value)
            return true;

        //get current time
        LocalTime timeNow = LocalTime.now();

        //format time
        int hour = timeNow.getHour();
        int minute = timeNow.getMinute();
        int second = timeNow.getSecond();

        //12h
        if ((boolean) Config.TWELVE_HOURS.value) {
            hour -= hour >= 13 ? 12 : 0;
        }

        //define if should send time message
        boolean newTime =
                (boolean) Config.CLOCK_ON_MESSAGE.value ||
                        ((boolean) Config.SHOW_SECONDS.value && second != ChatUtilities.lastSecond) ||
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
            if ((boolean) Config.SHOW_SECONDS.value)
                time.append(new LiteralText(":" + String.format("%02d", second)));

            //close time text
            time.append("] ");

            //add message
            if (!(boolean) Config.CLOCK_ON_MESSAGE.value) {
                this.addMessage(new LiteralText("").append(ChatUtilities.STYLE).append(" ").append(time).append(ChatUtilities.STYLE), 0);
            } else {
                this.addMessage(new LiteralText("").append(time.formatted(Formatting.DARK_GRAY, Formatting.ITALIC)).append(message), 0);
                return true;
            }
        }
        return false;
    }
}
