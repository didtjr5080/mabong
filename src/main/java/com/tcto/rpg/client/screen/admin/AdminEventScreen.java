package com.tcto.rpg.client.screen.admin;

public class AdminEventScreen extends BaseAdminScreen {
    public AdminEventScreen() { super("Admin Events"); }

    @Override
    protected void init() {
        setLines("Event tools", "Start/stop calls are logged server-side.");
        int x = panelX();
        int y = panelY() + 28;
        addRenderableWidget(commandButton("Start", "tctorpg event start weekend_exp_boost", x, y, 68));
        addRenderableWidget(commandButton("Stop", "tctorpg event stop weekend_exp_boost", x + 76, y, 68));
        addRenderableWidget(commandButton("List", "tctorpg event list", x + 152, y, 68));
        addRenderableWidget(commandButton("New Event", "tctorpg template event warrior_week class_boost", x, y + 25, 100));
        addRenderableWidget(commandButton("Validate", "tctorpg validate events", x + 110, y + 25, 100));
        addBackButton();
    }
}
