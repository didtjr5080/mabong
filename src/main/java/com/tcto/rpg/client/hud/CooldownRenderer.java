package com.tcto.rpg.client.hud;

import com.tcto.rpg.TCToRPG;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class CooldownRenderer {
    private CooldownRenderer() {
    }

    public static void renderCooldown(GuiGraphics guiGraphics, int x, int y, int size, int ticks, boolean showNumbers) {
        if (ticks <= 0) {
            return;
        }
        guiGraphics.blit(texture("hud/skill_slot_cooldown.png"), x, y, 0, 0, size, size, 256, 256);
        if (showNumbers) {
            guiGraphics.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font, String.valueOf(ticks / 20),
                x + size / 2, y + size / 2 - 4, 0xFFFFFF);
        }
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(TCToRPG.MODID, "textures/gui/" + path);
    }
}

