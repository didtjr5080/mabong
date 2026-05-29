package com.tcto.rpg.client.screen.admin;

import com.tcto.rpg.client.screen.BaseRpgScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class BaseAdminScreen extends BaseRpgScreen {
    private final List<String> lines = new ArrayList<>();

    protected BaseAdminScreen(String title) {
        super(title, "admin_screen_bg.png", 256, 180);
    }

    protected int panelX() {
        return (width - 220) / 2;
    }

    protected int panelY() {
        return (height - 110) / 2;
    }

    protected Button commandButton(String label, String command, int x, int y, int width) {
        return Button.builder(Component.literal(label), button -> runCommand(command)).bounds(x, y, width, 20).build();
    }

    protected Button navButton(String label, BaseAdminScreen screen, int x, int y, int width) {
        return Button.builder(Component.literal(label), button -> minecraft.setScreen(screen)).bounds(x, y, width, 20).build();
    }

    protected void addBackButton() {
        addRenderableWidget(navButton("Back", new AdminMainScreen(), panelX(), panelY() + 100, 70));
    }

    protected void setLines(String... values) {
        lines.clear();
        lines.addAll(List.of(values));
    }

    protected void runCommand(String command) {
        if (minecraft == null || minecraft.player == null || minecraft.player.connection == null) {
            return;
        }
        minecraft.player.connection.sendCommand(command.startsWith("/") ? command.substring(1) : command);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int x = panelX();
        int y = panelY() + 2;
        for (String line : lines) {
            guiGraphics.drawString(font, line, x, y, 0xD8D8D8, false);
            y += 11;
        }
    }
}
