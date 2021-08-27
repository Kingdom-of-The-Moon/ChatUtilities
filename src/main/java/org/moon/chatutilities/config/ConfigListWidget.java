package org.moon.chatutilities.config;

import org.moon.chatutilities.config.Config.ConfigEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class ConfigListWidget extends ElementListWidget<ConfigListWidget.Entry> {

    //screen
    private final ConfigScreen parent;

    //focused binding
    public KeyBinding focusedBinding;

    //text types
    public static final Predicate<String> ANY = s -> true;
    public static final Predicate<String> INT = s -> s.matches("^[\\-+]?[0-9]*$");
    public static final Predicate<String> FLOAT = s -> s.matches("[\\-+]?[0-9]*(\\.[0-9]+)?") || s.endsWith(".") || s.isEmpty();
    public static final Predicate<String> HEX_COLOR = s -> s.matches("^[#]?[0-9a-fA-F]{0,6}$");

    //accent color
    public static final Formatting ACCENT_COLOR = Formatting.LIGHT_PURPLE;

    public ConfigListWidget(ConfigScreen parent, MinecraftClient client) {
        super(client, parent.width + 45, parent.height, 43, parent.height - 32, 20);
        this.parent = parent;

        //category title
        this.addEntry(new CategoryEntry(new TranslatableText("chatUtilities.config.time")));

        //entries
        this.addEntry(new BooleanEntry(new TranslatableText("chatUtilities.config.time.seconds"), new TranslatableText("chatUtilities.config.time.seconds.tooltip"), Config.entries.get("showSeconds")));
        this.addEntry(new BooleanEntry(new TranslatableText("chatUtilities.config.time.message"), new TranslatableText("chatUtilities.config.time.message.tooltip"), Config.entries.get("onMessage")));
        this.addEntry(new BooleanEntry(new TranslatableText("chatUtilities.config.time.twelve"), new TranslatableText("chatUtilities.config.time.twelve.tooltip"), Config.entries.get("twelveHour")));
        this.addEntry(new BooleanEntry(new TranslatableText("chatUtilities.config.time.enable"), new TranslatableText("chatUtilities.config.time.enable.tooltip"), Config.entries.get("enableClock")));

        //category title
        this.addEntry(new CategoryEntry(new TranslatableText("chatUtilities.config.spam")));

        //entries
        this.addEntry(new BooleanEntry(new TranslatableText("chatUtilities.config.spam.enable"), new TranslatableText("chatUtilities.config.spam.enable.tooltip"), Config.entries.get("enableAntiSpam")));

        //category title
        this.addEntry(new CategoryEntry(new TranslatableText("chatUtilities.config.ping")));

        //entries
        this.addEntry(new InputEntry(new TranslatableText("chatUtilities.config.ping.regex"), new TranslatableText("chatUtilities.config.ping.regex.tooltip"), Config.entries.get("pingRegex"), ANY));
        this.addEntry(new InputEntry(new TranslatableText("chatUtilities.config.ping.bg"), new TranslatableText("chatUtilities.config.ping.bg.tooltip"), Config.entries.get("pingBgColor"), HEX_COLOR));
        this.addEntry(new InputEntry(new TranslatableText("chatUtilities.config.ping.sound"), new TranslatableText("chatUtilities.config.ping.sound.tooltip"), Config.entries.get("pingSoundId"), ANY));

        //category title
        this.addEntry(new CategoryEntry(new TranslatableText("chatUtilities.config.system")));

        //entries
        this.addEntry(new BooleanEntry(new TranslatableText("chatUtilities.config.system.enable"), new TranslatableText("chatUtilities.config.system.enable.tooltip"), Config.entries.get("systemEnabled")));
        this.addEntry(new BooleanEntry(new TranslatableText("chatUtilities.config.system.seconds"), new TranslatableText("chatUtilities.config.system.seconds.tooltip"), Config.entries.get("systemTwelveHour")));
        this.addEntry(new BooleanEntry(new TranslatableText("chatUtilities.config.system.twelve"), new TranslatableText("chatUtilities.config.system.twelve.tooltip"), Config.entries.get("systemShowSeconds")));
    }

    @Override
    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 15;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 150;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.children().forEach(entry -> {
            if (entry instanceof InputEntry inputEntry) {
                inputEntry.field.setTextFieldFocused(inputEntry.field.isMouseOver(mouseX, mouseY));
                if (inputEntry.field.isFocused())
                    inputEntry.field.setSelectionEnd(0);
            }
        });
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public class CategoryEntry extends Entry {
        //properties
        private final Text text;

        public CategoryEntry(Text text) {
            this.text = text;
        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            //render text
            TextRenderer textRenderer = ConfigListWidget.this.client.textRenderer;
            Text text = this.text;
            int textWidth = ConfigListWidget.this.client.textRenderer.getWidth(this.text);
            float xPos = (float) (ConfigListWidget.this.client.currentScreen.width / 2 - textWidth / 2);
            int yPos = y + entryHeight;
            textRenderer.draw(matrices, text, xPos, (float)(yPos - 9 - 1), 16777215);
        }

        @Override
        public boolean changeFocus(boolean lookForwards) {
            return false;
        }

        public List<? extends Element> children() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return Collections.emptyList();
        }
    }

    public class BooleanEntry extends Entry {
        //entry
        private final ConfigEntry<Boolean> config;

        //values
        private final Text display;
        private final Text tooltip;
        private final boolean initValue;

        //buttons
        private final ButtonWidget toggle;
        private final ButtonWidget reset;

        public BooleanEntry(Text display, Text tooltip, ConfigEntry<Boolean> config) {
            this.display = display;
            this.tooltip = tooltip;
            this.config = config;
            this.initValue = config.value;

            //toggle button
            this.toggle = new ButtonWidget(0, 0, 75, 20, this.display, (button) -> config.configValue = !config.configValue);

            //reset button
            this.reset = new ButtonWidget(0, 0, 50, 20, new TranslatableText("controls.reset"), (button) -> config.configValue = config.defaultValue);
        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            //text
            TextRenderer textRenderer = ConfigListWidget.this.client.textRenderer;
            int posY = y + entryHeight / 2;
            textRenderer.draw(matrices, this.display, (float) x, (float) (posY - 9 / 2), 16777215);

            //reset button
            this.reset.x = x + 250;
            this.reset.y = y;
            this.reset.active = this.config.configValue != this.config.defaultValue;
            this.reset.render(matrices, mouseX, mouseY, tickDelta);

            //toggle button
            this.toggle.x = x + 165;
            this.toggle.y = y;
            this.toggle.setMessage(new TranslatableText("gui." + (this.config.configValue ? "yes" : "no")));

            //if setting is changed
            if (this.config.configValue != this.initValue)
                this.toggle.setMessage(this.toggle.getMessage().shallowCopy().formatted(ACCENT_COLOR));

            this.toggle.render(matrices, mouseX, mouseY, tickDelta);

            //overlay text
            if (isMouseOver(mouseX, mouseY) && mouseX < x + 165) {
                matrices.push();
                matrices.translate(0, 0, 599);
                parent.renderTooltip(matrices, this.tooltip, mouseX, mouseY);
                matrices.pop();
            }
        }

        @Override
        public List<? extends Element> children() {
            return Arrays.asList(this.toggle, this.reset);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return Arrays.asList(this.toggle, this.reset);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.toggle.mouseClicked(mouseX, mouseY, button) || this.reset.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.toggle.mouseReleased(mouseX, mouseY, button) || this.reset.mouseReleased(mouseX, mouseY, button);
        }
    }

    public class EnumEntry extends Entry {
        //entry
        private final ConfigEntry<Integer> config;

        //values
        private final Text display;
        private final Text tooltip;
        private final int initValue;
        private final List<Text> states;

        //buttons
        private final ButtonWidget toggle;
        private final ButtonWidget reset;

        public EnumEntry(Text display, Text tooltip, ConfigEntry<Integer> config, List<Text> states) {
            this.display = display;
            this.tooltip = tooltip;
            this.config = config;
            this.initValue = config.value;
            this.states = states;

            //toggle button
            this.toggle = new ButtonWidget(0, 0, 75, 20, this.display, (button) -> config.configValue = (config.configValue + 1) % states.size());

            //reset button
            this.reset = new ButtonWidget(0, 0, 50, 20, new TranslatableText("controls.reset"), (button) -> config.configValue = config.defaultValue);
        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            //text
            TextRenderer textRenderer = ConfigListWidget.this.client.textRenderer;
            int posY = y + entryHeight / 2;
            textRenderer.draw(matrices, this.display, (float) x, (float)(posY - 9 / 2), 16777215);

            //reset button
            this.reset.x = x + 250;
            this.reset.y = y;
            this.reset.active = !this.config.configValue.equals(this.config.defaultValue);
            this.reset.render(matrices, mouseX, mouseY, tickDelta);

            //toggle button
            this.toggle.x = x + 165;
            this.toggle.y = y;
            this.toggle.setMessage(states.get(this.config.configValue));

            //if setting is changed
            if (this.config.configValue != this.initValue)
                this.toggle.setMessage(this.toggle.getMessage().shallowCopy().formatted(ACCENT_COLOR));

            this.toggle.render(matrices, mouseX, mouseY, tickDelta);

            //overlay text
            if (isMouseOver(mouseX, mouseY) && mouseX < x + 165) {
                matrices.push();
                matrices.translate(0, 0, 599);
                parent.renderTooltip(matrices, this.tooltip, mouseX, mouseY);
                matrices.pop();
            }
        }

        @Override
        public List<? extends Element> children() {
            return Arrays.asList(this.toggle, this.reset);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return Arrays.asList(this.toggle, this.reset);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.toggle.mouseClicked(mouseX, mouseY, button) || this.reset.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.toggle.mouseReleased(mouseX, mouseY, button) || this.reset.mouseReleased(mouseX, mouseY, button);
        }
    }

    public class InputEntry extends Entry {
        //entry
        private final ConfigEntry config;

        //values
        private final Text display;
        private final Text tooltip;
        private final Object initValue;

        //buttons
        private final TextFieldWidget field;
        private final ButtonWidget reset;

        public InputEntry(Text display, Text tooltip, ConfigEntry config, Predicate<String> validator) {
            this.display = display;
            this.tooltip = tooltip;
            this.config = config;
            this.initValue = config.value;

            //field
            this.field = new TextFieldWidget(ConfigListWidget.this.client.textRenderer, 0, 0, 70, 16, new LiteralText(config.configValue + ""));
            this.field.setMaxLength(128);
            this.field.setChangedListener((fieldText) -> config.configValue = fieldText);
            this.field.setText(config.configValue + "");
            this.field.setTextPredicate(validator);

            //reset button
            this.reset = new ButtonWidget(0, 0, 50, 20, new TranslatableText("controls.reset"), (button) -> {
                config.configValue = config.defaultValue;
                this.field.setText(config.configValue + "");
            });
        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            //text
            TextRenderer textRenderer = ConfigListWidget.this.client.textRenderer;
            int posY = y + entryHeight / 2;
            textRenderer.draw(matrices, this.display, (float) x, (float)(posY - 9 / 2), 16777215);

            //reset button
            this.reset.x = x + 250;
            this.reset.y = y;
            this.reset.active = !this.config.configValue.equals(this.config.defaultValue + "");
            this.reset.render(matrices, mouseX, mouseY, tickDelta);

            //text field
            this.field.y = y + 2;

            //focused size
            if (this.field.isFocused()) {
                this.field.setWidth(70 + 167);
                this.field.x = x;
            } else {
                //reset size
                this.field.setWidth(70);
                this.field.x = x + 167;

                //render overlay text
                if (isMouseOver(mouseX, mouseY) && mouseX < x + 165) {
                    matrices.push();
                    matrices.translate(0, 0, 599);
                    parent.renderTooltip(matrices, this.tooltip, mouseX, mouseY);
                    matrices.pop();
                }
            }

            //if setting is changed
            if (!this.config.configValue.equals(this.initValue + ""))
                try {
                    this.config.defaultValue.getClass().getConstructor(new Class[] {String.class}).newInstance(this.config.configValue);
                    this.field.setEditableColor(ACCENT_COLOR.getColorValue());
                } catch (Exception e) {
                    this.field.setEditableColor(Formatting.RED.getColorValue());
                }
            else
                this.field.setEditableColor(Formatting.WHITE.getColorValue());

            this.field.render(matrices, mouseX, mouseY, tickDelta);
        }

        @Override
        public List<? extends Element> children() {
            return Arrays.asList(this.field, this.reset);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return Arrays.asList(this.field, this.reset);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.field.mouseClicked(mouseX, mouseY, button) || this.reset.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.field.mouseReleased(mouseX, mouseY, button) || this.reset.mouseReleased(mouseX, mouseY, button);
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return super.keyPressed(keyCode, scanCode, modifiers) || this.field.keyPressed(keyCode, scanCode, modifiers);
        }

        public boolean charTyped(char chr, int modifiers) {
            return this.field.charTyped(chr, modifiers);
        }
    }

    public class KeyBindEntry extends Entry {
        //entry
        private final Config.ConfigEntry<Integer> config;

        //values
        private final Text display;
        private final Text tooltip;
        private final KeyBinding binding;

        //buttons
        private final ButtonWidget toggle;
        private final ButtonWidget reset;

        public KeyBindEntry(Text display, Text tooltip, ConfigEntry<Integer> config, KeyBinding binding) {
            this.display = display;
            this.tooltip = tooltip;
            this.config = config;
            this.binding = binding;

            //toggle button
            this.toggle = new ButtonWidget(0, 0, 75, 20, this.display, (button) -> focusedBinding = binding);

            //reset button
            this.reset = new ButtonWidget(0, 0, 50, 20, new TranslatableText("controls.reset"), (button) -> {
                binding.setBoundKey(binding.getDefaultKey());
                KeyBinding.updateKeysByCode();
            });
        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            //text
            TextRenderer textRenderer = ConfigListWidget.this.client.textRenderer;
            int posY = y + entryHeight / 2;
            textRenderer.draw(matrices, this.display, (float) x, (float) (posY - 9 / 2), 16777215);

            //reset button
            this.reset.x = x + 250;
            this.reset.y = y;
            this.reset.active = !this.binding.isDefault();
            this.reset.render(matrices, mouseX, mouseY, tickDelta);

            //toggle button
            this.toggle.x = x + 165;
            this.toggle.y = y;
            this.toggle.setMessage(this.binding.getBoundKeyLocalizedText());

            if (focusedBinding == this.binding) {
                this.toggle.setMessage((new LiteralText("> ")).append(this.toggle.getMessage().shallowCopy().formatted(ACCENT_COLOR)).append(" <").formatted(ACCENT_COLOR));
            }
            else if (!this.binding.isUnbound()) {
                for (KeyBinding key : MinecraftClient.getInstance().options.keysAll) {
                    if (key != this.binding && this.binding.equals(key)) {
                        this.toggle.setMessage(this.toggle.getMessage().shallowCopy().formatted(Formatting.RED));
                        break;
                    }
                }
            }

            this.toggle.render(matrices, mouseX, mouseY, tickDelta);

            //overlay text
            if (isMouseOver(mouseX, mouseY) && mouseX < x + 165) {
                matrices.push();
                matrices.translate(0, 0, 599);
                parent.renderTooltip(matrices, this.tooltip, mouseX, mouseY);
                matrices.pop();
            }
        }

        @Override
        public List<? extends Element> children() {
            return Arrays.asList(this.toggle, this.reset);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return Arrays.asList(this.toggle, this.reset);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.toggle.mouseClicked(mouseX, mouseY, button) || this.reset.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.toggle.mouseReleased(mouseX, mouseY, button) || this.reset.mouseReleased(mouseX, mouseY, button);
        }
    }

    public abstract static class Entry extends ElementListWidget.Entry<Entry> {
        public Entry() {}
    }
}