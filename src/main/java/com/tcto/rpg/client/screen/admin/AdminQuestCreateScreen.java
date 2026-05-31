package com.tcto.rpg.client.screen.admin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class AdminQuestCreateScreen extends BaseAdminScreen {
    private EditBox questField;
    private EditBox typeField;
    private EditBox targetField;

    public AdminQuestCreateScreen() {
        super("Create Quest");
    }

    @Override
    protected void init() {
        setLines("퀘스트 생성/진행", "퀘스트 ID를 입력하고 시작/완료/초기화합니다.");
        int x = panelX();
        int y = panelY() + 34;
        questField = editBox(x + 58, y, 116, "warrior_trial");
        typeField = editBox(x + 58, y + 22, 116, "job");
        targetField = editBox(x + 58, y + 44, 116, currentPlayerName());
        addRenderableWidget(questField);
        addRenderableWidget(typeField);
        addRenderableWidget(targetField);
        addRenderableWidget(Button.builder(Component.literal("생성"), b -> runCommand("tctorpg template quest " + questId() + " " + value(typeField, "normal"))).bounds(x, y + 72, 42, 20).build());
        addRenderableWidget(Button.builder(Component.literal("시작"), b -> runCommand("tctorpg player startquest " + target() + " " + questId())).bounds(x + 46, y + 72, 42, 20).build());
        addRenderableWidget(Button.builder(Component.literal("완료"), b -> runCommand("tctorpg player completequest " + target() + " " + questId())).bounds(x + 92, y + 72, 42, 20).build());
        addRenderableWidget(Button.builder(Component.literal("초기화"), b -> runCommand("tctorpg player resetquest " + target() + " " + questId())).bounds(x + 138, y + 72, 52, 20).build());
        addRenderableWidget(navButton("뒤로", new AdminQuestScreen(), x, y + 96, 52));
    }

    private String questId() {
        return value(questField, "warrior_trial").toLowerCase();
    }

    private String target() {
        return value(targetField, currentPlayerName());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int x = panelX();
        int y = panelY() + 38;
        drawLabel(guiGraphics, "퀘스트", x, y, 0xF2E8C9);
        drawLabel(guiGraphics, "종류", x, y + 22, 0xF2E8C9);
        drawLabel(guiGraphics, "대상", x, y + 44, 0xF2E8C9);
    }
}
