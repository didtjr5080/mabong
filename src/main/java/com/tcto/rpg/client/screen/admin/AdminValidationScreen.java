package com.tcto.rpg.client.screen.admin;

public class AdminValidationScreen extends BaseAdminScreen {
    public AdminValidationScreen() { super("Admin Validation"); }

    @Override
    protected void init() {
        setLines("Data validation", "Validate before reload. Reload is server-side.");
        int x = panelX();
        int y = panelY() + 28;
        addRenderableWidget(commandButton("Validate All", "tctorpg validate", x, y, 100));
        addRenderableWidget(commandButton("Reload All", "tctorpg reload", x + 110, y, 100));
        addRenderableWidget(commandButton("Docs", "tctorpg docs generate", x, y + 25, 100));
        addBackButton();
    }
}
