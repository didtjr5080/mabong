package com.tcto.rpg.client.hud;

import com.tcto.rpg.common.config.ClientUiConfig;
import net.minecraft.client.gui.GuiGraphics;

public final class RpgHudOverlay {
    private RpgHudOverlay() {
    }

    public static void render(GuiGraphics guiGraphics) {
        if (!ClientUiConfig.ENABLE_SKILL_HUD.get()) {
            return;
        }
        if (ClientUiConfig.SHOW_RESOURCE_BARS.get()) {
            ResourceBarRenderer.render(guiGraphics);
        }
        SkillSlotHudRenderer.render(guiGraphics);
        StatusEffectHudRenderer.render(guiGraphics);
        QuestTrackerHudRenderer.render(guiGraphics);
        BossHudRenderer.render(guiGraphics);
        FloatingDamageRenderer.render(guiGraphics);
        CustomChatHud.render(guiGraphics);
    }
}

