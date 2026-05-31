package com.tcto.rpg.client.screen;

import com.tcto.rpg.client.hud.ClientRpgState;
import net.minecraft.client.gui.GuiGraphics;

public class JobScreen extends BaseRpgScreen {
    public JobScreen() {
        super("Job / Advancement", "job_screen_bg.png", 256, 180);
    }

    @Override
    protected void init() {
        int x = panelX() + 16;
        int y = panelY() + 144;
        String playerName = currentPlayerName();
        addRenderableWidget(commandButton("전사", "tctorpg player setjob " + playerName + " warrior", x, y, 48));
        addRenderableWidget(commandButton("거너", "tctorpg player setjob " + playerName + " gunslinger", x + 52, y, 48));
        addRenderableWidget(commandButton("혈법", "tctorpg player setjob " + playerName + " blood_mage", x + 104, y, 48));
        addRenderableWidget(commandButton("도적", "tctorpg player setjob " + playerName + " rogue", x + 156, y, 48));
        addRenderableWidget(closeButton("닫기", x + 208, y, 34));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int x = panelX() + 20;
        int y = panelY() + 34;
        drawLabel(guiGraphics, "현재 직업: " + fallback(ClientRpgState.jobId(), "미선택"), x, y, 0xF2E8C9);
        drawLabel(guiGraphics, "레벨: " + ClientRpgState.level() + "   전직 단계: " + ClientRpgState.jobTier(), x, y + 14, 0xFFFFFF);

        drawLabel(guiGraphics, "직업 트리", x, y + 38, 0xD8D1B8);
        drawLabel(guiGraphics, "warrior -> knight -> paladin / berserker", x, y + 52, 0xFFFFFF);
        drawLabel(guiGraphics, "gunslinger -> ranger -> sniper / gun_technician", x, y + 64, 0xFFFFFF);
        drawLabel(guiGraphics, "blood_mage -> blood_sorcerer -> blood_king / vampire_mage", x, y + 76, 0xFFFFFF);
        drawLabel(guiGraphics, "rogue -> assassin -> shadow_lord / poisoner", x, y + 88, 0xFFFFFF);

        drawLabel(guiGraphics, "Lv.10 1차, Lv.25 2차 전직 기준으로 확장 예정", x, y + 112, 0x8FA9B8);
    }

    private static String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
