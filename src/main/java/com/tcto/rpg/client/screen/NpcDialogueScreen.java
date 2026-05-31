package com.tcto.rpg.client.screen;

import net.minecraft.client.gui.GuiGraphics;

public class NpcDialogueScreen extends BaseRpgScreen {
    public NpcDialogueScreen() {
        super("NPC Dialogue", "npc_dialogue_bg.png", 320, 90);
    }

    @Override
    protected void init() {
        int x = panelX() + 220;
        int y = panelY() + 58;
        addRenderableWidget(commandButton("퀘스트", "tctorpg help", x, y, 54));
        addRenderableWidget(closeButton("닫기", x + 60, y, 40));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int x = panelX() + 18;
        int y = panelY() + 28;
        drawLabel(guiGraphics, "마을 관리자", x, y, 0xF2E8C9);
        drawLabel(guiGraphics, "서버 명령과 데이터 검증으로 RPG 콘텐츠를 관리할 수 있습니다.", x, y + 14, 0xFFFFFF);
        drawLabel(guiGraphics, "정식 NPC 대화/선택지는 상호작용 패킷 단계에서 연결합니다.", x, y + 28, 0x8FA9B8);
    }
}
