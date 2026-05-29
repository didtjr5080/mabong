package com.tcto.rpg.client.screen.admin;

public class AdminItemScreen extends BaseAdminScreen {
    public AdminItemScreen() { super("Admin Items"); }

    @Override
    protected void init() {
        String playerName = minecraft != null && minecraft.player != null ? minecraft.player.getGameProfile().getName() : "stone_0401";
        setLines("Item tools", "Create templates or grant test items.");
        int x = panelX();
        int y = panelY() + 28;
        addRenderableWidget(commandButton("New Item", "tctorpg template item blood_staff weapon", x, y, 100));
        addRenderableWidget(commandButton("Give Sword", "tctorpg player giveitem " + playerName + " iron_greatsword", x + 110, y, 110));
        addBackButton();
    }
}
