package com.tcto.rpg.client.hud;

import com.tcto.rpg.TCToRPG;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class StatusEffectHudRenderer {
    private StatusEffectHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        List<String> effects = ClientRpgState.effects();
        if (effects.isEmpty()) {
            return;
        }
        int x = HudLayoutConfig.STATUS_X;
        int y = HudLayoutConfig.STATUS_Y;
        for (int i = 0; i < effects.size() && i < 6; i++) {
            ResourceLocation icon = iconFor(effects.get(i));
            if (icon != null) {
                guiGraphics.blit(icon, x + i * 18, y, 0, 0, 16, 16, 256, 256);
            }
        }
    }

    private static ResourceLocation iconFor(String effectId) {
        return switch (effectId) {
            case "bleed" -> texture("hud/icon_bleed.png");
            case "stun" -> texture("hud/icon_stun.png");
            case "poison" -> texture("hud/icon_poison.png");
            case "buff" -> texture("hud/icon_buff.png");
            case "debuff" -> texture("hud/icon_debuff.png");
            default -> null;
        };
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(TCToRPG.MODID, "textures/gui/" + path);
    }
}

