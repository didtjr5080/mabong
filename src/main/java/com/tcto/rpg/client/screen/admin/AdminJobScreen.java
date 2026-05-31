package com.tcto.rpg.client.screen.admin;

public class AdminJobScreen extends BaseAdminScreen {
    public AdminJobScreen() { super("Admin Jobs"); }

    @Override
    protected void init() {
        String playerName = currentPlayerName();
        setLines("직업 도구", "명령어를 직접 치지 않고 화면에서 직업을 관리합니다.");
        int x = panelX();
        int y = panelY() + 28;
        addRenderableWidget(navButton("직업 생성/적용", new AdminJobCreateScreen(), x, y, 105));
        addRenderableWidget(commandButton("Warrior 적용", "tctorpg player setjob " + playerName + " warrior", x + 112, y, 105));
        addRenderableWidget(commandButton("검증", "tctorpg validate jobs", x, y + 24, 105));
        addRenderableWidget(commandButton("리로드", "tctorpg reload all", x + 112, y + 24, 105));
        addBackButton();
    }
}
