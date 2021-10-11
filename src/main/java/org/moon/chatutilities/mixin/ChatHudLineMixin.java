package org.moon.chatutilities.mixin;

import net.minecraft.client.gui.hud.ChatHudLine;
import org.moon.chatutilities.accessor.ChatHudLineAccess;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(ChatHudLine.class)
public class ChatHudLineMixin implements ChatHudLineAccess {

    private List<String> cut$imageLinks;

    @Override
    public void setImageLinks(List<String> images) {
        cut$imageLinks = images;
    }

    @Override
    public List<String> getImageLinks() {
        return cut$imageLinks;
    }
}
