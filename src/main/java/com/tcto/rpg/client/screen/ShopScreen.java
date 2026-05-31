package com.tcto.rpg.client.screen;

import net.minecraft.client.gui.GuiGraphics;

public class ShopScreen extends BaseRpgScreen {
    public ShopScreen() {
        super("Shop", "shop_screen_bg.png", 256, 180);
    }

    @Override
    protected void init() {
        int x = panelX() + 16;
        int y = panelY() + 144;
        String playerName = currentPlayerName();
        addRenderableWidget(commandButton("테스트 구매", "tctorpg player giveitem " + playerName + " blood_staff", x, y, 82));
        addRenderableWidget(commandButton("상점 검증", "tctorpg validate shops", x + 88, y, 72));
        addRenderableWidget(commandButton("리로드", "tctorpg reload shops", x + 166, y, 48));
        addRenderableWidget(closeButton("닫기", x + 218, y, 28));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int x = panelX() + 20;
        int y = panelY() + 34;
        drawLabel(guiGraphics, "상점: TCToRPG 테스트 상점", x, y, 0xF2E8C9);
        drawLabel(guiGraphics, "보유 재화: 서버 경제 시스템 연결 예정", x, y + 14, 0xB8D0FF);

        drawSlot(guiGraphics, x, y + 34, 44, "검", "100G");
        drawSlot(guiGraphics, x + 54, y + 34, 44, "지팡이", "120G");
        drawSlot(guiGraphics, x + 108, y + 34, 44, "물약", "30G");
        drawSlot(guiGraphics, x + 162, y + 34, 44, "재료", "15G");

        drawLabel(guiGraphics, "MVP에서는 버튼이 서버 명령으로 테스트 아이템을 지급합니다.", x, y + 92, 0xD8D8D8);
        drawLabel(guiGraphics, "정식 구매 조건/가격 검증은 서버 ShopService 단계에서 확장합니다.", x, y + 106, 0x8FA9B8);
    }
}
