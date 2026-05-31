package com.tcto.rpg.client.screen.admin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class AdminPlayerScreen extends BaseAdminScreen {
    private EditBox playerField;
    private EditBox levelField;
    private EditBox jobField;
    private EditBox statField;
    private EditBox skillField;
    private EditBox itemField;

    public AdminPlayerScreen() {
        super("Admin Players");
    }

    @Override
    protected void init() {
        setLines("플레이어 관리", "닉네임과 값을 입력하고 버튼으로 적용합니다.");
        int x = panelX();
        int y = panelY() + 26;
        String playerName = currentPlayerName();

        playerField = editBox(x + 52, y, 92, playerName);
        levelField = editBox(x + 178, y, 38, "50");
        jobField = editBox(x + 52, y + 22, 92, "warrior");
        statField = editBox(x + 178, y + 22, 38, "10");
        skillField = editBox(x + 52, y + 44, 92, "sword_judgement");
        itemField = editBox(x + 52, y + 66, 92, "custom_weapon");

        addRenderableWidget(playerField);
        addRenderableWidget(levelField);
        addRenderableWidget(jobField);
        addRenderableWidget(statField);
        addRenderableWidget(skillField);
        addRenderableWidget(itemField);

        addRenderableWidget(Button.builder(Component.literal("정보"), button -> runCommand("tctorpg player info " + player())).bounds(x + 150, y + 44, 66, 20).build());
        addRenderableWidget(Button.builder(Component.literal("레벨 적용"), button -> runCommand("tctorpg player setlevel " + player() + " " + intValue(levelField, "1"))).bounds(x, y + 90, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("직업 적용"), button -> runCommand("tctorpg player setjob " + player() + " " + value(jobField, "warrior"))).bounds(x + 74, y + 90, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+P"), button -> runCommand("tctorpg player addstatpoint " + player() + " " + intValue(statField, "1"))).bounds(x + 148, y + 90, 42, 20).build());
        addRenderableWidget(navButton("뒤로", new AdminMainScreen(), x + 194, y + 90, 34));

        addRenderableWidget(Button.builder(Component.literal("스킬 해금"), button -> runCommand("tctorpg player unlockskill " + player() + " " + value(skillField, "sword_judgement"))).bounds(x + 150, y + 66, 66, 20).build());
        addRenderableWidget(Button.builder(Component.literal("아이템 지급"), button -> runCommand("tctorpg player giveitem " + player() + " " + value(itemField, "starter_sword"))).bounds(x + 150, y + 22, 66, 20).build());
    }

    private String player() {
        return value(playerField, currentPlayerName());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int x = panelX();
        int y = panelY() + 30;
        drawLabel(guiGraphics, "대상", x, y, 0xF2E8C9);
        drawLabel(guiGraphics, "레벨", x + 148, y, 0xF2E8C9);
        drawLabel(guiGraphics, "직업", x, y + 22, 0xF2E8C9);
        drawLabel(guiGraphics, "스탯", x + 148, y + 22, 0xF2E8C9);
        drawLabel(guiGraphics, "스킬", x, y + 44, 0xF2E8C9);
        drawLabel(guiGraphics, "아이템", x, y + 66, 0xF2E8C9);
    }
}
