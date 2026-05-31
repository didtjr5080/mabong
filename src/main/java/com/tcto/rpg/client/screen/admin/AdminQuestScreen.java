package com.tcto.rpg.client.screen.admin;

public class AdminQuestScreen extends BaseAdminScreen {
    public AdminQuestScreen() { super("Admin Quests"); }

    @Override
    protected void init() {
        String playerName = currentPlayerName();
        setLines("퀘스트 도구", "명령어를 직접 치지 않고 화면에서 퀘스트를 관리합니다.");
        int x = panelX();
        int y = panelY() + 28;
        addRenderableWidget(commandButton("시작", "tctorpg player startquest " + playerName + " warrior_trial", x, y, 70));
        addRenderableWidget(commandButton("완료", "tctorpg player completequest " + playerName + " warrior_trial", x + 76, y, 70));
        addRenderableWidget(commandButton("초기화", "tctorpg player resetquest " + playerName + " warrior_trial", x + 152, y, 68));
        addRenderableWidget(navButton("생성/관리", new AdminQuestCreateScreen(), x, y + 24, 70));
        addRenderableWidget(commandButton("검증", "tctorpg validate quests", x + 76, y + 24, 70));
        addRenderableWidget(commandButton("리로드", "tctorpg reload all", x + 152, y + 24, 68));
        addBackButton();
    }
}
