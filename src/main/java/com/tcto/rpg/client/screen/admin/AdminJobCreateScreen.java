package com.tcto.rpg.client.screen.admin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class AdminJobCreateScreen extends BaseAdminScreen {
    private EditBox idField;
    private EditBox targetField;

    public AdminJobCreateScreen() {
        super("Create Job");
    }

    @Override
    protected void init() {
        setLines("직업 생성/적용", "직업 ID를 입력하고 생성 또는 대상에게 적용합니다.");
        int x = panelX();
        int y = panelY() + 34;
        idField = editBox(x + 58, y, 120, "dark_knight");
        targetField = editBox(x + 58, y + 24, 120, currentPlayerName());
        addRenderableWidget(idField);
        addRenderableWidget(targetField);
        addRenderableWidget(Button.builder(Component.literal("생성"), b -> runCommand("tctorpg template job " + jobId())).bounds(x, y + 58, 52, 20).build());
        addRenderableWidget(Button.builder(Component.literal("적용"), b -> runCommand("tctorpg player setjob " + target() + " " + jobId())).bounds(x + 56, y + 58, 52, 20).build());
        addRenderableWidget(commandButton("검증", "tctorpg validate jobs", x + 112, y + 58, 52));
        addRenderableWidget(commandButton("리로드", "tctorpg reload all", x + 168, y + 58, 52));
        addRenderableWidget(navButton("뒤로", new AdminJobScreen(), x, y + 84, 52));
    }

    private String jobId() {
        return value(idField, "dark_knight").toLowerCase();
    }

    private String target() {
        return value(targetField, currentPlayerName());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int x = panelX();
        int y = panelY() + 38;
        drawLabel(guiGraphics, "직업 ID", x, y, 0xF2E8C9);
        drawLabel(guiGraphics, "대상", x, y + 24, 0xF2E8C9);
    }
}
