package com.tcto.rpg.client.hud;

import com.tcto.rpg.common.config.ClientUiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class CustomChatHud {
    private static final int MAX_HISTORY = 80;
    private static final Deque<ChatLine> LINES = new ArrayDeque<>();

    private CustomChatHud() {
    }

    public static void add(Component message) {
        if (message == null) {
            return;
        }
        String text = message.getString();
        if (text == null || text.isBlank()) {
            return;
        }
        LINES.addLast(new ChatLine(text, 240));
        while (LINES.size() > MAX_HISTORY) {
            LINES.removeFirst();
        }
    }

    public static void tick() {
        for (ChatLine line : LINES) {
            if (line.ticksLeft > 0) {
                line.ticksLeft--;
            }
        }
    }

    public static void render(GuiGraphics guiGraphics) {
        if (!ClientUiConfig.ENABLE_CUSTOM_CHAT.get() || LINES.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int x = ClientUiConfig.CHAT_X.get();
        int y = ClientUiConfig.CHAT_Y.get();
        int width = ClientUiConfig.CHAT_WIDTH.get();
        int maxLines = ClientUiConfig.CHAT_LINES.get();
        int lineHeight = 10;

        List<String> visible = visibleLines(font, width - 8, maxLines);
        if (visible.isEmpty()) {
            return;
        }

        int height = visible.size() * lineHeight + 6;
        guiGraphics.fill(x, y, x + width, y + height, 0xAA050505);
        guiGraphics.hLine(x, x + width, y, 0x883F86FF);
        guiGraphics.hLine(x, x + width, y + height, 0x66000000);

        int textY = y + 4;
        for (String line : visible) {
            guiGraphics.drawString(font, line, x + 4, textY, 0xEDEDED, false);
            textY += lineHeight;
        }
    }

    private static List<String> visibleLines(Font font, int maxWidth, int maxLines) {
        List<String> result = new ArrayList<>();
        Object[] values = LINES.toArray();
        for (int i = values.length - 1; i >= 0 && result.size() < maxLines; i--) {
            ChatLine line = (ChatLine) values[i];
            if (line.ticksLeft <= 0) {
                continue;
            }
            result.add(0, trimToWidth(font, line.text, maxWidth));
        }
        return result;
    }

    private static String trimToWidth(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String suffix = "...";
        int end = text.length();
        while (end > 0 && font.width(text.substring(0, end) + suffix) > maxWidth) {
            end--;
        }
        return text.substring(0, Math.max(0, end)) + suffix;
    }

    private static final class ChatLine {
        private final String text;
        private int ticksLeft;

        private ChatLine(String text, int ticksLeft) {
            this.text = text;
            this.ticksLeft = ticksLeft;
        }
    }
}
