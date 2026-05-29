package com.tcto.rpg.client.screen.admin;

public class AdminBackupScreen extends BaseAdminScreen {
    public AdminBackupScreen() { super("Admin Backup"); }

    @Override
    protected void init() {
        setLines("Backup tools", "Backups include data, content packs, config and logs.");
        int x = panelX();
        int y = panelY() + 28;
        addRenderableWidget(commandButton("Create", "tctorpg backup create", x, y, 100));
        addRenderableWidget(commandButton("List", "tctorpg backup list", x + 110, y, 100));
        addBackButton();
    }
}
