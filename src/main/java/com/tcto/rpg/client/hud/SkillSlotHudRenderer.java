package com.tcto.rpg.client.hud;

import com.tcto.rpg.TCToRPG;
import com.tcto.rpg.common.config.ClientUiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public final class SkillSlotHudRenderer {
    private static final String[] KEYS = {"R", "Z", "X", "C", "V", "G"};
    private static final String[] SLOT_KEYS = {"slot_1", "slot_2", "slot_3", "slot_4", "slot_5", "ultimate"};
    private static final Set<String> KNOWN_SKILL_ICONS = Set.of(
        "sword_judgement", "strength_up", "rage_strike", "shield_bash", "sword_wave", "holy_judgement",
        "berserker_rage",
        "rapid_fire", "roll", "focus", "headshot", "overclock_barrage",
        "blood_chain", "blood_spear", "lifesteal", "blood_rage", "vampiric_eclipse",
        "throat_cut", "stealth", "intimidation", "poison_kill", "shadow_step"
    );

    private SkillSlotHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int totalWidth = HudLayoutConfig.SKILL_SLOT_SIZE * 6 + HudLayoutConfig.SKILL_GAP * 5;
        int startX = (screenWidth - totalWidth) / 2;
        int y = ClientUiConfig.SKILL_HUD_Y.get() + 40;

        int[] slots = ClientRpgState.cooldownSlots();
        int[] remaining = ClientRpgState.cooldownTicks();
        for (int i = 0; i < 6; i++) {
            int x = startX + i * (HudLayoutConfig.SKILL_SLOT_SIZE + HudLayoutConfig.SKILL_GAP);
            String skillId = ClientRpgState.equippedSkillId(SLOT_KEYS[i]);
            ResourceLocation slotTexture = i == 5
                ? texture("hud/ultimate_slot.png")
                : texture("hud/skill_slot.png");
            if (skillId.isEmpty()) {
                slotTexture = texture("hud/skill_slot_locked.png");
            }
            guiGraphics.blit(slotTexture, x, y, 0, 0, HudLayoutConfig.SKILL_SLOT_SIZE, HudLayoutConfig.SKILL_SLOT_SIZE,
                HudLayoutConfig.SKILL_SLOT_SIZE, HudLayoutConfig.SKILL_SLOT_SIZE);
            if (!skillId.isEmpty() && hasSkillIcon(skillId)) {
                ResourceLocation icon = skillIcon(skillId);
                int iconSize = Math.max(12, HudLayoutConfig.SKILL_SLOT_SIZE - 8);
                int iconX = x + (HudLayoutConfig.SKILL_SLOT_SIZE - iconSize) / 2;
                int iconY = y + (HudLayoutConfig.SKILL_SLOT_SIZE - iconSize) / 2;
                guiGraphics.blit(icon, iconX, iconY, iconSize, iconSize, 0, 0, 32, 32, 32, 32);
            } else if (!skillId.isEmpty()) {
                guiGraphics.drawCenteredString(Minecraft.getInstance().font, "?", x + HudLayoutConfig.SKILL_SLOT_SIZE / 2, y + 10, 0xFFFF80A0);
            }
            guiGraphics.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font, KEYS[i], x + 10, y + 22, 0xFFFFFF);

            int cooldown = findCooldown(slots, remaining, i);
            CooldownRenderer.renderCooldown(guiGraphics, x, y, HudLayoutConfig.SKILL_SLOT_SIZE, cooldown,
                ClientUiConfig.SHOW_COOLDOWN_NUMBERS.get());
        }
    }

    private static int findCooldown(int[] slots, int[] remaining, int index) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == index) {
                return remaining[i];
            }
        }
        return 0;
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(TCToRPG.MODID, "textures/gui/" + path);
    }

    private static ResourceLocation skillIcon(String skillId) {
        String clean = skillId.contains(":") ? skillId.substring(skillId.indexOf(':') + 1) : skillId;
        return ResourceLocation.fromNamespaceAndPath(TCToRPG.MODID, "textures/gui/skills/" + clean + ".png");
    }

    private static boolean hasSkillIcon(String skillId) {
        String clean = skillId.contains(":") ? skillId.substring(skillId.indexOf(':') + 1) : skillId;
        return KNOWN_SKILL_ICONS.contains(clean);
    }
}

