package net.dreemurr.chattimestamp.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class ConfigScreen extends Screen {

    public Screen parentScreen;
    private ConfigListWidget configListWidget;

    public ConfigScreen(Screen parentScreen) {
        super(new TranslatableText("chatTimeStamp.gui.configTitle"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();

        this.addDrawableChild(new ButtonWidget(this.width / 2 - 154, this.height - 29, 150, 20, new TranslatableText("gui.cancel"), (buttonWidgetx) -> {
            Config.discardConfig();
            this.client.setScreen(parentScreen);
        }));

        this.addDrawableChild(new ButtonWidget(this.width / 2 + 4, this.height - 29, 150, 20, new TranslatableText("gui.done"), (buttonWidgetx) -> {
            Config.copyConfig();
            Config.saveConfig();
            this.client.setScreen(parentScreen);
        }));

        this.configListWidget = new ConfigListWidget(this, this.client);
        this.addSelectableChild(this.configListWidget);
    }

    @Override
    public void onClose() {
        Config.copyConfig();
        Config.saveConfig();
        this.client.setScreen(parentScreen);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        //background
        this.renderBackgroundTexture(0);

        //list
        this.configListWidget.render(matrices, mouseX, mouseY, delta);

        //buttons
        super.render(matrices, mouseX, mouseY, delta);

        //screen title
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 12, 16777215);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (configListWidget.focusedBinding != null) {
            configListWidget.focusedBinding.setBoundKey(InputUtil.Type.MOUSE.createFromCode(button));
            configListWidget.focusedBinding = null;

            KeyBinding.updateKeysByCode();

            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (configListWidget.focusedBinding != null) {
            configListWidget.focusedBinding.setBoundKey(keyCode == 256 ? InputUtil.UNKNOWN_KEY: InputUtil.fromKeyCode(keyCode, scanCode));
            configListWidget.focusedBinding = null;

            KeyBinding.updateKeysByCode();

            return true;
        }
        else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}