package org.moon.chatutilities.data;

import net.minecraft.client.gui.hud.ChatHudLine;

import java.util.List;

public record ChatRenderContext(ChatHudLine chatHudLine, List<String> links, double s, double h, int aa) {}
