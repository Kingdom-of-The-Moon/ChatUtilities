package net.dreemurr.chattimestamp.mixin;

import net.dreemurr.chattimestamp.accessor.MinecraftClientAccess;
import net.dreemurr.chattimestamp.accessor.SleepingChatScreenAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements MinecraftClientAccess {

    @Shadow @Nullable public Screen currentScreen;
    @Shadow @Nullable public ClientPlayerEntity player;

    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo ci) {
        //keep chat open when waking up
        if (this.currentScreen != null && this.currentScreen instanceof SleepingChatScreen && !this.player.isSleeping()) {
            String s = ((SleepingChatScreenAccess) this.currentScreen).getChatFieldText();

            if (s.isEmpty()) setScreen(null);
            else keepSleepingChatOpen(s);
        }
    }

    @Override
    public void keepSleepingChatOpen(String text) {
        this.openChatScreen(text);
    }

    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @Shadow protected abstract void openChatScreen(String text);
}
