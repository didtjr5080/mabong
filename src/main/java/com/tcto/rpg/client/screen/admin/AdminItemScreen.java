package com.tcto.rpg.client.screen.admin;

public class AdminItemScreen extends BaseAdminScreen {
    public AdminItemScreen() { super("Admin Items"); }

    @Override
    protected void init() {
        String playerName = minecraft != null && minecraft.player != null ? minecraft.player.getGameProfile().getName() : "stone_0401";
        setLines("아이템 도구", "인게임 UI로 장비 데이터를 만들고 지급/장착합니다.");
        int x = panelX();
        int y = panelY() + 28;
        addRenderableWidget(navButton("아이템 생성", new AdminItemCreateScreen(), x, y, 100));
        addRenderableWidget(commandButton("Starter 지급", "tctorpg player giveitem " + playerName + " starter_sword", x + 110, y, 110));
        addRenderableWidget(commandButton("Starter 장착", "tctorpg player equipitem " + playerName + " weapon starter_sword", x, y + 24, 100));
        addRenderableWidget(commandButton("검증", "tctorpg validate items", x + 110, y + 24, 110));
        addBackButton();
    }
}
