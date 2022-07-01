package org.moon.chatutilities.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.SimpleOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.moon.chatutilities.config.ConfigScreen;
import net.minecraft.client.gui.screen.option.ChatOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatOptionsScreen.class)
public class ChatOptionsScreenMixin  extends SimpleOptionsScreen {

    public ChatOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title, SimpleOption<?>[] options) {
        super(parent, gameOptions, title, options);
    }

    @Override
    protected void initFooter() {
        this.addDrawableChild(new ButtonWidget(
                this.width - 105, this.height - 25,
                100, 20,
                Text.translatable("chatUtilities.gui.configbutton"),
                button -> this.client.setScreen(new ConfigScreen(this))));

        super.initFooter();
    }
}
