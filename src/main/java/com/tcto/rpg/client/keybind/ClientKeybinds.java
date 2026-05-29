package com.tcto.rpg.client.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import com.tcto.rpg.common.config.ClientUiConfig;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import java.util.ArrayList;
import java.util.List;

public final class ClientKeybinds {
    private static final String CATEGORY = "key.categories.tctorpg";

    private static KeyMapping skill1;
    private static KeyMapping skill2;
    private static KeyMapping skill3;
    private static KeyMapping skill4;
    private static KeyMapping skill5;
    private static KeyMapping ultimate;
    private static KeyMapping dash;
    private static KeyMapping screenSkills;
    private static KeyMapping screenStats;
    private static KeyMapping screenEquip;
    private static KeyMapping screenQuest;
    private static KeyMapping screenAdmin;

    private ClientKeybinds() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        skill1 = build("key.tctorpg.skill1", ClientUiConfig.DEFAULT_SKILL_1.get());
        skill2 = build("key.tctorpg.skill2", ClientUiConfig.DEFAULT_SKILL_2.get());
        skill3 = build("key.tctorpg.skill3", ClientUiConfig.DEFAULT_SKILL_3.get());
        skill4 = build("key.tctorpg.skill4", ClientUiConfig.DEFAULT_SKILL_4.get());
        skill5 = build("key.tctorpg.skill5", ClientUiConfig.DEFAULT_SKILL_5.get());
        ultimate = build("key.tctorpg.ultimate", ClientUiConfig.DEFAULT_ULTIMATE.get());
        dash = build("key.tctorpg.dash", ClientUiConfig.DEFAULT_DASH.get());
        screenSkills = build("key.tctorpg.screen_skills", ClientUiConfig.DEFAULT_SCREEN_SKILLS.get());
        screenStats = build("key.tctorpg.screen_stats", ClientUiConfig.DEFAULT_SCREEN_STATS.get());
        screenEquip = build("key.tctorpg.screen_equip", ClientUiConfig.DEFAULT_SCREEN_EQUIP.get());
        screenQuest = build("key.tctorpg.screen_quest", ClientUiConfig.DEFAULT_SCREEN_QUEST.get());
        screenAdmin = build("key.tctorpg.screen_admin", ClientUiConfig.DEFAULT_SCREEN_ADMIN.get());

        event.register(skill1);
        event.register(skill2);
        event.register(skill3);
        event.register(skill4);
        event.register(skill5);
        event.register(ultimate);
        event.register(dash);
        event.register(screenSkills);
        event.register(screenStats);
        event.register(screenEquip);
        event.register(screenQuest);
        event.register(screenAdmin);
    }

    public static List<Integer> consumeCastSlots() {
        List<Integer> slots = new ArrayList<>();
        if (skill1.consumeClick()) {
            slots.add(0);
        }
        if (skill2.consumeClick()) {
            slots.add(1);
        }
        if (skill3.consumeClick()) {
            slots.add(2);
        }
        if (skill4.consumeClick()) {
            slots.add(3);
        }
        if (skill5.consumeClick()) {
            slots.add(4);
        }
        if (ultimate.consumeClick()) {
            slots.add(5);
        }
        return slots;
    }

    public static List<String> consumeScreenRequests() {
        List<String> screens = new ArrayList<>();
        if (screenSkills.consumeClick()) {
            screens.add("skills");
        }
        if (screenStats.consumeClick()) {
            screens.add("stats");
        }
        if (screenEquip.consumeClick()) {
            screens.add("equipment");
        }
        if (screenQuest.consumeClick()) {
            screens.add("quest");
        }
        if (screenAdmin.consumeClick()) {
            screens.add("admin");
        }
        return screens;
    }

    public static boolean consumeDash() {
        return dash.consumeClick();
    }

    private static KeyMapping build(String name, String defaultKey) {
        return new KeyMapping(
            name,
            KeyConflictContext.IN_GAME,
            InputConstants.getKey(defaultKey),
            CATEGORY
        );
    }
}

