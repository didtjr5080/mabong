package com.tcto.rpg.client.screen;

import com.tcto.rpg.client.screen.admin.AdminMainScreen;
import net.minecraft.client.Minecraft;

public final class ScreenDispatcher {
    private ScreenDispatcher() {
    }

    public static void open(String screenId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        switch (screenId) {
            case "character" -> minecraft.setScreen(new CharacterSelectScreen());
            case "stats" -> minecraft.setScreen(new StatScreen());
            case "skills" -> minecraft.setScreen(new SkillEquipScreen());
            case "equipment" -> minecraft.setScreen(new EquipmentScreen());
            case "job" -> minecraft.setScreen(new JobScreen());
            case "quest" -> minecraft.setScreen(new QuestScreen());
            case "shop" -> minecraft.setScreen(new ShopScreen());
            case "npc" -> minecraft.setScreen(new NpcDialogueScreen());
            case "admin" -> minecraft.setScreen(new AdminMainScreen());
            default -> minecraft.setScreen(new SimpleRpgScreen(screenId));
        }
    }
}

