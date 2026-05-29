package com.tcto.rpg.client.screen.admin;

public class AdminQuestScreen extends BaseAdminScreen {
    public AdminQuestScreen() { super("Admin Quests"); }

    @Override
    protected void init() {
        setLines("Quest tools", "Create quest templates and validate data.");
        int x = panelX();
        int y = panelY() + 28;
        addRenderableWidget(commandButton("New Quest", "tctorpg template quest warrior_trial job", x, y, 110));
        addRenderableWidget(commandButton("Validate", "tctorpg validate quests", x + 120, y, 100));
        addBackButton();
    }
}
