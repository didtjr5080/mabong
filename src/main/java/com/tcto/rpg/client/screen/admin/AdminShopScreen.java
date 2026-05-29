package com.tcto.rpg.client.screen.admin;

public class AdminShopScreen extends BaseAdminScreen {
    public AdminShopScreen() { super("Admin Shops"); }

    @Override
    protected void init() {
        setLines("Shop tools", "Shop editing is a second-phase feature.");
        int x = panelX();
        int y = panelY() + 28;
        addRenderableWidget(commandButton("Validate", "tctorpg validate shops", x, y, 100));
        addRenderableWidget(commandButton("Reload Shops", "tctorpg reload shops", x + 110, y, 110));
        addBackButton();
    }
}
