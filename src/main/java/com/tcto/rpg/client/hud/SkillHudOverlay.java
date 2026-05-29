package com.tcto.rpg.client.hud;

import com.tcto.rpg.common.config.ClientUiConfig;
import net.minecraft.client.gui.GuiGraphics;

public final class SkillHudOverlay {
    private SkillHudOverlay() {
    }

    public static void render(GuiGraphics guiGraphics) {
        if (!ClientUiConfig.ENABLE_SKILL_HUD.get()) {
            return;
        }
        int x = ClientUiConfig.SKILL_HUD_X.get();
        int y = ClientUiConfig.SKILL_HUD_Y.get();
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, "Skill HUD", x, y, 0xFFFFFF, true);
    }
}

