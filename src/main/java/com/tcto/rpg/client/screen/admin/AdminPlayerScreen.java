package com.tcto.rpg.client.screen.admin;

public class AdminPlayerScreen extends BaseAdminScreen {
    public AdminPlayerScreen() { super("Admin Players"); }

    @Override
    protected void init() {
        String playerName = minecraft != null && minecraft.player != null ? minecraft.player.getGameProfile().getName() : "stone_0401";
        setLines("Player tools", "Target: " + playerName);
        int x = panelX();
        int y = panelY() + 28;
        addRenderableWidget(commandButton("Info", "tctorpg player info " + playerName, x, y, 68));
        addRenderableWidget(commandButton("Lv 10", "tctorpg player setlevel " + playerName + " 10", x + 76, y, 68));
        addRenderableWidget(commandButton("+100 EXP", "tctorpg player addexp " + playerName + " 100", x + 152, y, 68));
        addRenderableWidget(commandButton("Warrior", "tctorpg player setjob " + playerName + " warrior", x, y + 25, 68));
        addRenderableWidget(commandButton("+STR", "tctorpg player setstat " + playerName + " str 1", x + 76, y + 25, 68));
        addRenderableWidget(commandButton("Unlock", "tctorpg player unlockskill " + playerName + " sword_judgement", x + 152, y + 25, 68));
        addBackButton();
    }
}
