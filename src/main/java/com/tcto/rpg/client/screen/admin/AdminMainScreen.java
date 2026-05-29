package com.tcto.rpg.client.screen.admin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class AdminMainScreen extends BaseAdminScreen {
    public AdminMainScreen() {
        super("TCToRPG Admin");
    }

    @Override
    protected void init() {
        setLines("Server-authoritative admin tools", "All actions are checked on server.");
        int x = (width - 220) / 2;
        int y = (height - 110) / 2 + 24;
        addRenderableWidget(navButton("Players", new AdminPlayerScreen(), x, y, 70));
        addRenderableWidget(navButton("Data", new AdminValidationScreen(), x + 75, y, 70));
        addRenderableWidget(navButton("Backup", new AdminBackupScreen(), x + 150, y, 70));
        addRenderableWidget(navButton("Items", new AdminItemScreen(), x, y + 25, 70));
        addRenderableWidget(navButton("Jobs", new AdminJobScreen(), x + 75, y + 25, 70));
        addRenderableWidget(navButton("Skills", new AdminSkillScreen(), x + 150, y + 25, 70));
        addRenderableWidget(navButton("Monsters", new AdminMonsterScreen(), x, y + 50, 70));
        addRenderableWidget(navButton("Bosses", new AdminBossScreen(), x + 75, y + 50, 70));
        addRenderableWidget(navButton("Quests", new AdminQuestScreen(), x + 150, y + 50, 70));
        addRenderableWidget(navButton("Dungeons", new AdminDungeonScreen(), x, y + 75, 70));
        addRenderableWidget(navButton("Shops", new AdminShopScreen(), x + 75, y + 75, 70));
        addRenderableWidget(navButton("Events", new AdminEventScreen(), x + 150, y + 75, 70));
    }
}
