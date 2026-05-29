package com.tcto.rpg.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientUiConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_SKILL_HUD;
    public static final ModConfigSpec.IntValue SKILL_HUD_X;
    public static final ModConfigSpec.IntValue SKILL_HUD_Y;
    public static final ModConfigSpec.BooleanValue SHOW_COOLDOWN_NUMBERS;
    public static final ModConfigSpec.BooleanValue SHOW_RESOURCE_BARS;

    public static final ModConfigSpec.ConfigValue<String> DEFAULT_SKILL_1;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_SKILL_2;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_SKILL_3;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_SKILL_4;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_SKILL_5;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_ULTIMATE;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_DASH;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_SCREEN_SKILLS;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_SCREEN_STATS;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_SCREEN_EQUIP;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_SCREEN_QUEST;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_SCREEN_ADMIN;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("hud");
        ENABLE_SKILL_HUD = builder.define("enable_skill_hud", true);
        SKILL_HUD_X = builder.defineInRange("skill_hud_x", 20, 0, 10000);
        SKILL_HUD_Y = builder.defineInRange("skill_hud_y", 180, 0, 10000);
        SHOW_COOLDOWN_NUMBERS = builder.define("show_cooldown_numbers", true);
        SHOW_RESOURCE_BARS = builder.define("show_resource_bars", true);
        builder.pop();

        builder.push("keybinds");
        DEFAULT_SKILL_1 = builder.define("default_skill_1", "key.keyboard.r");
        DEFAULT_SKILL_2 = builder.define("default_skill_2", "key.keyboard.z");
        DEFAULT_SKILL_3 = builder.define("default_skill_3", "key.keyboard.x");
        DEFAULT_SKILL_4 = builder.define("default_skill_4", "key.keyboard.c");
        DEFAULT_SKILL_5 = builder.define("default_skill_5", "key.keyboard.v");
        DEFAULT_ULTIMATE = builder.define("default_ultimate", "key.keyboard.g");
        DEFAULT_DASH = builder.define("default_dash", "key.keyboard.left.alt");
        DEFAULT_SCREEN_SKILLS = builder.define("default_screen_skills", "key.keyboard.k");
        DEFAULT_SCREEN_STATS = builder.define("default_screen_stats", "key.keyboard.j");
        DEFAULT_SCREEN_EQUIP = builder.define("default_screen_equip", "key.keyboard.b");
        DEFAULT_SCREEN_QUEST = builder.define("default_screen_quest", "key.keyboard.l");
        DEFAULT_SCREEN_ADMIN = builder.define("default_screen_admin", "key.keyboard.o");
        builder.pop();

        SPEC = builder.build();
    }

    private ClientUiConfig() {
    }
}

