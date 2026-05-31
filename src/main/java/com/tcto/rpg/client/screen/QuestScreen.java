package com.tcto.rpg.client.screen;

import com.tcto.rpg.client.hud.ClientRpgState;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class QuestScreen extends BaseRpgScreen {
    public QuestScreen() {
        super("Quest", "quest_screen_bg.png", 256, 180);
    }

    @Override
    protected void init() {
        int x = panelX() + 16;
        int y = panelY() + 144;
        String playerName = currentPlayerName();
        addRenderableWidget(commandButton("전직퀘 시작", "tctorpg player startquest " + playerName + " warrior_trial", x, y, 82));
        addRenderableWidget(commandButton("완료", "tctorpg player completequest " + playerName + " warrior_trial", x + 88, y, 52));
        addRenderableWidget(commandButton("검증", "tctorpg validate quests", x + 146, y, 52));
        addRenderableWidget(closeButton("닫기", x + 204, y, 38));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int x = panelX() + 20;
        int y = panelY() + 34;
        drawLabel(guiGraphics, "퀘스트 진행", x, y, 0xF2E8C9);
        drawLabel(guiGraphics, "진행 중", x, y + 16, 0xFFFFFF);
        List<String> active = ClientRpgState.activeQuests();
        if (active.isEmpty()) {
            drawLabel(guiGraphics, "진행 중인 퀘스트가 없습니다.", x, y + 30, 0xB8B8B8);
        } else {
            for (int i = 0; i < Math.min(3, active.size()); i++) {
                drawLabel(guiGraphics, "- " + active.get(i), x, y + 30 + i * 12, 0xD8D8D8);
            }
        }

        drawLabel(guiGraphics, "완료 기록", x, y + 66, 0xF2E8C9);
        List<String> completed = ClientRpgState.completedQuests();
        if (completed.isEmpty()) {
            drawLabel(guiGraphics, "완료한 퀘스트가 없습니다.", x, y + 80, 0xB8B8B8);
        } else {
            for (int i = 0; i < Math.min(4, completed.size()); i++) {
                drawLabel(guiGraphics, "- " + completed.get(i), x, y + 80 + i * 12, 0xFFFFFF);
            }
        }
        drawLabel(guiGraphics, "퀘스트 상태 저장/목표 추적은 서버 데이터 기준입니다.", x, y + 126, 0x8FA9B8);
    }
}
