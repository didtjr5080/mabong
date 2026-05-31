package com.tcto.rpg.client.screen.admin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class AdminItemCreateScreen extends BaseAdminScreen {
    private EditBox idField;
    private EditBox slotField;
    private EditBox levelField;
    private EditBox attackField;

    public AdminItemCreateScreen() {
        super("Create Item");
    }

    @Override
    protected void init() {
        setLines("아이템 생성", "ID, 슬롯, 요구 레벨, 공격력/방어력을 입력하세요.");
        int x = panelX();
        int y = panelY() + 28;

        idField = editBox(x + 68, y, 120, "custom_sword");
        slotField = editBox(x + 68, y + 22, 120, "weapon");
        levelField = editBox(x + 68, y + 44, 120, "1");
        attackField = editBox(x + 68, y + 66, 120, "8");

        addRenderableWidget(idField);
        addRenderableWidget(slotField);
        addRenderableWidget(levelField);
        addRenderableWidget(attackField);
        addRenderableWidget(Button.builder(Component.literal("생성"), button -> runCommand(buildCommand())).bounds(x, y + 90, 54, 20).build());
        addRenderableWidget(commandButton("검증", "tctorpg validate items", x + 60, y + 90, 54));
        addRenderableWidget(commandButton("리로드", "tctorpg reload all", x + 120, y + 90, 54));
        addRenderableWidget(navButton("뒤로", new AdminItemScreen(), x + 180, y + 90, 40));
    }

    private String buildCommand() {
        return "tctorpg item create " + value(idField, "custom_sword")
            + " " + value(slotField, "weapon").toLowerCase()
            + " " + intValue(levelField, "1")
            + " " + intValue(attackField, "8");
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int x = panelX();
        int y = panelY() + 32;
        drawLabel(guiGraphics, "ID", x, y, 0xF2E8C9);
        drawLabel(guiGraphics, "슬롯", x, y + 22, 0xF2E8C9);
        drawLabel(guiGraphics, "레벨", x, y + 44, 0xF2E8C9);
        drawLabel(guiGraphics, "수치", x, y + 66, 0xF2E8C9);
        drawLabel(guiGraphics, "슬롯: weapon, offhand, helmet, chestplate, leggings, boots", x, y + 114, 0x8FA9B8);
    }
}
