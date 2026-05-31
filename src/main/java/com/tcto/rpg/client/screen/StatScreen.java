package com.tcto.rpg.client.screen;

import com.tcto.rpg.client.hud.ClientRpgState;
import net.minecraft.client.gui.GuiGraphics;

public class StatScreen extends BaseRpgScreen {
    public StatScreen() {
        super("Stats / Job", "stat_screen_bg.png", 256, 180);
    }

    @Override
    protected void init() {
        int x = panelX() + 16;
        int y = panelY() + 142;
        String playerName = currentPlayerName();
        addRenderableWidget(commandButton("+10P", "tctorpg player addstatpoint " + playerName + " 10", x, y, 42));
        addRenderableWidget(commandButton("+STR", "tctorpg player setstat " + playerName + " str 1", x + 46, y, 42));
        addRenderableWidget(commandButton("+VIT", "tctorpg player setstat " + playerName + " vit 1", x + 92, y, 42));
        addRenderableWidget(commandButton("+INT", "tctorpg player setstat " + playerName + " int 1", x + 138, y, 42));
        addRenderableWidget(commandButton("+LUK", "tctorpg player setstat " + playerName + " luk 1", x + 184, y, 42));
        addRenderableWidget(closeButton("닫기", panelX() + 210, panelY() + 118, 34));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int x = panelX() + 18;
        int y = panelY() + 34;

        drawLabel(guiGraphics, "직업: " + fallback(ClientRpgState.jobId(), "미선택"), x, y, 0xF2E8C9);
        drawLabel(guiGraphics, "레벨: " + ClientRpgState.level() + "   EXP: " + ClientRpgState.exp(), x, y + 14, 0xFFFFFF);
        drawLabel(guiGraphics, "전직 단계: " + ClientRpgState.jobTier() + "   스탯 포인트: " + ClientRpgState.statPoints(), x, y + 28, 0xFFFFFF);

        int statY = y + 50;
        drawLabel(guiGraphics, "STR 힘: " + ClientRpgState.baseStat("str"), x, statY, 0xFFB0A0);
        drawLabel(guiGraphics, "VIT 체력: " + ClientRpgState.baseStat("vit"), x, statY + 14, 0xB8FFB0);
        drawLabel(guiGraphics, "INT 지력: " + ClientRpgState.baseStat("int"), x + 112, statY, 0xA8C8FF);
        drawLabel(guiGraphics, "LUK 운: " + ClientRpgState.baseStat("luk"), x + 112, statY + 14, 0xFFE89A);

        drawLabel(guiGraphics, "파생: HP " + ClientRpgState.maxHp() + " / MP " + ClientRpgState.maxMp(), x, statY + 38, 0xD7D7D7);
        drawLabel(guiGraphics, "서버 동기화 기준으로 표시됩니다.", x, statY + 52, 0x8FA9B8);
    }

    private static String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
