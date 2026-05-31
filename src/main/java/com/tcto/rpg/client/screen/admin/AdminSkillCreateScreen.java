package com.tcto.rpg.client.screen.admin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class AdminSkillCreateScreen extends BaseAdminScreen {
    private EditBox skillField;
    private EditBox jobField;
    private EditBox targetField;
    private EditBox slotField;

    public AdminSkillCreateScreen() {
        super("Create Skill");
    }

    @Override
    protected void init() {
        setLines("스킬 생성/장착", "스킬 ID와 직업을 입력하고 해금/장착합니다.");
        int x = panelX();
        int y = panelY() + 26;
        skillField = editBox(x + 56, y, 104, "rage_strike");
        jobField = editBox(x + 56, y + 22, 104, "warrior");
        targetField = editBox(x + 56, y + 44, 104, currentPlayerName());
        slotField = editBox(x + 184, y + 44, 32, "0");
        addRenderableWidget(skillField);
        addRenderableWidget(jobField);
        addRenderableWidget(targetField);
        addRenderableWidget(slotField);
        addRenderableWidget(Button.builder(Component.literal("생성"), b -> runCommand("tctorpg template skill " + skillId() + " " + jobId())).bounds(x, y + 72, 52, 20).build());
        addRenderableWidget(Button.builder(Component.literal("해금"), b -> runCommand("tctorpg player unlockskill " + target() + " " + skillId())).bounds(x + 56, y + 72, 52, 20).build());
        addRenderableWidget(Button.builder(Component.literal("장착"), b -> runCommand("tctorpg player equipskill " + target() + " " + intValue(slotField, "0") + " " + skillId())).bounds(x + 112, y + 72, 52, 20).build());
        addRenderableWidget(commandButton("검증", "tctorpg validate skills", x + 168, y + 72, 52));
        addRenderableWidget(navButton("뒤로", new AdminSkillScreen(), x, y + 96, 52));
    }

    private String skillId() {
        return value(skillField, "rage_strike").toLowerCase();
    }

    private String jobId() {
        return value(jobField, "warrior").toLowerCase();
    }

    private String target() {
        return value(targetField, currentPlayerName());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int x = panelX();
        int y = panelY() + 30;
        drawLabel(guiGraphics, "스킬", x, y, 0xF2E8C9);
        drawLabel(guiGraphics, "직업", x, y + 22, 0xF2E8C9);
        drawLabel(guiGraphics, "대상", x, y + 44, 0xF2E8C9);
        drawLabel(guiGraphics, "슬롯", x + 162, y + 44, 0xF2E8C9);
    }
}
