package com.tcto.rpg.client.screen.admin;

public class AdminJobScreen extends BaseAdminScreen {
    public AdminJobScreen() { super("Admin Jobs"); }

    @Override
    protected void init() {
        setLines("Job tools", "Template generation is written to content_packs/default.");
        int x = panelX();
        int y = panelY() + 28;
        addRenderableWidget(commandButton("New Job", "tctorpg template job dark_knight", x, y, 100));
        addRenderableWidget(commandButton("Validate", "tctorpg validate jobs", x + 110, y, 100));
        addBackButton();
    }
}
