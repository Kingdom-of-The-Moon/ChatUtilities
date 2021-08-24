package org.moon.chatutilities.mixin;

import org.moon.chatutilities.config.ConfigScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ChatOptionsScreen;
import net.minecraft.client.gui.screen.option.NarratorOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Option;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatOptionsScreen.class)
public class ChatOptionsScreenMixin extends NarratorOptionsScreen {

    public ChatOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title, Option[] options) {
        super(parent, gameOptions, title, options);
    }

    @Override
    protected void initFooter() {
        this.addDrawableChild(new ButtonWidget(
                this.width - 105, this.height - 25,
                100, 20,
                new TranslatableText("chatUtilities.gui.configButton"),
                button -> this.client.setScreen(new ConfigScreen(this))));

        super.initFooter();
    }
}
