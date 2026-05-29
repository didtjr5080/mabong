package com.tcto.rpg.client.screen.admin;

public class AdminBossScreen extends BaseAdminScreen {
    public AdminBossScreen() { super("Admin Bosses"); }

    @Override
    protected void init() {
        setLines("Boss tools", "Boss spawn and templates.");
        int x = panelX();
        int y = panelY() + 28;
        addRenderableWidget(commandButton("Spawn Boss", "tctorpg spawnboss abyss_lord", x, y, 100));
        addRenderableWidget(commandButton("New Boss", "tctorpg template boss forest_guardian", x + 110, y, 110));
        addRenderableWidget(commandButton("Validate", "tctorpg validate bosses", x, y + 25, 100));
        addBackButton();
    }
}
