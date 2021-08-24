package org.moon.chatutilities.mixin;

import org.moon.chatutilities.accessor.MinecraftClientAccess;
import org.moon.chatutilities.accessor.SleepingChatScreenAccess;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SleepingChatScreen.class)
public class SleepingChatScreenMixin extends ChatScreen implements SleepingChatScreenAccess {

    public SleepingChatScreenMixin(String originalChatText) {
        super(originalChatText);
    }

    @Override
    public String getChatFieldText() {
        return this.chatField.getText();
    }

    @Inject(at = @At("HEAD"), method = "stopSleeping")
    public void stopSleeping(CallbackInfo ci) {
        //keep chat open when cancelling sleep
        String s = this.chatField.getText();
        if (!s.isEmpty() && this.client != null) ((MinecraftClientAccess) this.client).keepSleepingChatOpen(s);
    }
}
