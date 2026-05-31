package com.tcto.rpg.client.screen.admin;

public class AdminSkillScreen extends BaseAdminScreen {
    public AdminSkillScreen() { super("Admin Skills"); }

    @Override
    protected void init() {
        String playerName = currentPlayerName();
        setLines("스킬 도구", "명령어를 직접 치지 않고 화면에서 스킬을 관리합니다.");
        int x = panelX();
        int y = panelY() + 28;
        addRenderableWidget(commandButton("Warrior 적용", "tctorpg player setjob " + playerName + " warrior", x, y, 100));
        addRenderableWidget(commandButton("심판 해금", "tctorpg player unlockskill " + playerName + " sword_judgement", x + 110, y, 100));
        addRenderableWidget(commandButton("R 장착", "tctorpg player equipskill " + playerName + " 0 sword_judgement", x, y + 24, 100));
        addRenderableWidget(navButton("스킬 생성/장착", new AdminSkillCreateScreen(), x + 110, y + 24, 100));
        addRenderableWidget(commandButton("검증", "tctorpg validate skills", x, y + 48, 100));
        addBackButton();
    }
}
