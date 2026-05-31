package com.tcto.rpg.client.screen;

import com.tcto.rpg.client.hud.ClientRpgState;
import net.minecraft.client.gui.GuiGraphics;

public class EquipmentScreen extends BaseRpgScreen {
    private static final String[][] SLOTS = {
        {"weapon", "무기"},
        {"offhand", "보조"},
        {"helmet", "투구"},
        {"chestplate", "갑옷"},
        {"leggings", "하의"},
        {"boots", "신발"}
    };

    public EquipmentScreen() {
        super("Equipment", "equipment_screen_bg.png", 256, 180);
    }

    @Override
    protected void init() {
        int x = panelX() + 16;
        int y = panelY() + 148;
        String playerName = currentPlayerName();
        addRenderableWidget(commandButton("지급", "tctorpg player giveitem " + playerName + " starter_sword", x, y, 42));
        addRenderableWidget(commandButton("장착", "tctorpg player equipitem " + playerName + " weapon starter_sword", x + 46, y, 46));
        addRenderableWidget(commandButton("해제", "tctorpg player unequipitem " + playerName + " weapon", x + 96, y, 42));
        addRenderableWidget(commandButton("검증", "tctorpg validate items", x + 142, y, 42));
        addRenderableWidget(closeButton("닫기", x + 188, y, 38));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int x = panelX() + 22;
        int y = panelY() + 36;
        drawLabel(guiGraphics, "장비 슬롯", x, y, 0xF2E8C9);
        drawLabel(guiGraphics, "서버 저장 장비 ID 표시", x + 70, y, 0xAFC2D0);

        int rowY = y + 20;
        for (int i = 0; i < SLOTS.length; i++) {
            String key = SLOTS[i][0];
            String label = SLOTS[i][1];
            String value = displayName(ClientRpgState.equipmentId(key));
            int currentY = rowY + i * 16;
            guiGraphics.fill(x, currentY - 2, x + 212, currentY + 12, 0x66050505);
            drawLabel(guiGraphics, label, x + 8, currentY, 0xD8D1B8);
            drawLabel(guiGraphics, value, x + 74, currentY, value.equals("비어 있음") ? 0xA8A8A8 : 0xFFFFFF);
        }
        drawLabel(guiGraphics, "장착은 서버 레벨/직업/스탯 조건을 검사합니다.", x, y + 122, 0x8FA9B8);
    }

    private static String displayName(String id) {
        if (id == null || id.isBlank()) {
            return "비어 있음";
        }
        String clean = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        return clean.length() > 22 ? clean.substring(0, 22) : clean;
    }
}
