package com.tcto.rpg.client.screen.admin;

public class AdminDungeonScreen extends BaseAdminScreen {
    public AdminDungeonScreen() { super("Admin Dungeons"); }

    @Override
    protected void init() {
        setLines("Dungeon tools", "Create dungeon templates and validate data.");
        int x = panelX();
        int y = panelY() + 28;
        addRenderableWidget(commandButton("New Dungeon", "tctorpg template dungeon ruined_mine", x, y, 110));
        addRenderableWidget(commandButton("Validate", "tctorpg validate dungeons", x + 120, y, 100));
        addBackButton();
    }
}
