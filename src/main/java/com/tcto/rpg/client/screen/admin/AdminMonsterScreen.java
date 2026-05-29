package com.tcto.rpg.client.screen.admin;

public class AdminMonsterScreen extends BaseAdminScreen {
    public AdminMonsterScreen() { super("Admin Monsters"); }

    @Override
    protected void init() {
        setLines("Monster tools", "Spawn tests run through server commands.");
        int x = panelX();
        int y = panelY() + 28;
        addRenderableWidget(commandButton("Spawn Boar", "tctorpg spawnmob wild_boar", x, y, 100));
        addRenderableWidget(commandButton("New Monster", "tctorpg template monster cave_spider ruined_mine", x + 110, y, 110));
        addRenderableWidget(commandButton("Validate", "tctorpg validate monsters", x, y + 25, 100));
        addBackButton();
    }
}
