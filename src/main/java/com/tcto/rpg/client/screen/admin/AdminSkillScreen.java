package com.tcto.rpg.client.screen.admin;

public class AdminSkillScreen extends BaseAdminScreen {
    public AdminSkillScreen() { super("Admin Skills"); }

    @Override
    protected void init() {
        setLines("Skill tools", "Server validates unlock, cooldown and resources.");
        int x = panelX();
        int y = panelY() + 28;
        addRenderableWidget(commandButton("New Skill", "tctorpg template skill rage_strike warrior", x, y, 100));
        addRenderableWidget(commandButton("Validate", "tctorpg validate skills", x + 110, y, 100));
        addBackButton();
    }
}
