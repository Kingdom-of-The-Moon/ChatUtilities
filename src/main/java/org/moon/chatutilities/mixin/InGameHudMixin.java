package org.moon.chatutilities.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.moon.chatutilities.config.Config;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalTime;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(at = @At("TAIL"), method = "render")
    public void render(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (this.client.options.hudHidden || !(boolean) Config.entries.get("systemEnabled").value)
            return;

        //get current time
        LocalTime timeNow = LocalTime.now();

        //format time
        int hour = timeNow.getHour();
        int minute = timeNow.getMinute();
        int second = timeNow.getSecond();

        //12h
        if ((boolean) Config.entries.get("systemTwelveHour").value)
            hour -= hour >= 13 ? 12 : 0;

        LiteralText time = new LiteralText(String.format("%02d", hour) + ":" + String.format("%02d", minute));

        //add seconds if enabled
        if ((boolean) Config.entries.get("systemShowSeconds").value)
            time.append(new LiteralText(":" + String.format("%02d", second)));

        int timeSize = this.getTextRenderer().getWidth(time);

        DrawableHelper.fill(matrices, this.client.getWindow().getScaledWidth() - timeSize - 4, this.client.getWindow().getScaledHeight() - 25, this.client.getWindow().getScaledWidth() - 1, this.client.getWindow().getScaledHeight() - 14, this.client.options.getTextBackgroundColor(0.8f));
        this.getTextRenderer().draw(matrices, time, this.client.getWindow().getScaledWidth() - timeSize - 2, this.client.getWindow().getScaledHeight() - 23, 0xffffff);
    }
}
