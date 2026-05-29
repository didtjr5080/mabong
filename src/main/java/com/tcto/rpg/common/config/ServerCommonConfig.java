package com.tcto.rpg.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ServerCommonConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_LOG;
    public static final ModConfigSpec.IntValue DEFAULT_CHARACTER_SLOTS;
    public static final ModConfigSpec.IntValue MAX_SKILL_SLOTS;
    public static final ModConfigSpec.IntValue MAX_ACCESSORY_SLOTS;

    public static final ModConfigSpec.IntValue MAX_LEVEL;
    public static final ModConfigSpec.IntValue STAT_POINTS_PER_LEVEL;
    public static final ModConfigSpec.IntValue BONUS_POINTS_EVERY_10_LEVELS;

    public static final ModConfigSpec.BooleanValue ENABLE_CUSTOM_DAMAGE;
    public static final ModConfigSpec.DoubleValue MINIMUM_DAMAGE;
    public static final ModConfigSpec.DoubleValue BASE_CRIT_DAMAGE_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue ENABLE_EVASION;
    public static final ModConfigSpec.BooleanValue ENABLE_VANILLA_DAMAGE_CANCEL;

    public static final ModConfigSpec.BooleanValue REQUIREMENTS_USE_BASE_STATS_ONLY;
    public static final ModConfigSpec.BooleanValue ALLOW_NEGATIVE_STATS;

    public static final ModConfigSpec.IntValue GLOBAL_COOLDOWN_TICKS;
    public static final ModConfigSpec.BooleanValue ALLOW_SKILL_CAST_WHILE_STUNNED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");
        ENABLE_DEBUG_LOG = builder.define("enable_debug_log", false);
        DEFAULT_CHARACTER_SLOTS = builder.defineInRange("default_character_slots", 4, 1, 16);
        MAX_SKILL_SLOTS = builder.defineInRange("max_skill_slots", 6, 1, 10);
        MAX_ACCESSORY_SLOTS = builder.defineInRange("max_accessory_slots", 3, 0, 6);
        builder.pop();

        builder.push("leveling");
        MAX_LEVEL = builder.defineInRange("max_level", 50, 1, 200);
        STAT_POINTS_PER_LEVEL = builder.defineInRange("stat_points_per_level", 3, 0, 20);
        BONUS_POINTS_EVERY_10_LEVELS = builder.defineInRange("bonus_points_every_10_levels", 5, 0, 50);
        builder.pop();

        builder.push("combat");
        ENABLE_CUSTOM_DAMAGE = builder.define("enable_custom_damage", true);
        MINIMUM_DAMAGE = builder.defineInRange("minimum_damage", 1.0, 0.0, 1000.0);
        BASE_CRIT_DAMAGE_MULTIPLIER = builder.defineInRange("base_crit_damage_multiplier", 1.5, 1.0, 10.0);
        ENABLE_EVASION = builder.define("enable_evasion", true);
        ENABLE_VANILLA_DAMAGE_CANCEL = builder.define("enable_vanilla_damage_cancel", true);
        builder.pop();

        builder.push("equipment");
        REQUIREMENTS_USE_BASE_STATS_ONLY = builder.define("requirements_use_base_stats_only", true);
        ALLOW_NEGATIVE_STATS = builder.define("allow_negative_stats", true);
        builder.pop();

        builder.push("skills");
        GLOBAL_COOLDOWN_TICKS = builder.defineInRange("global_cooldown_ticks", 10, 0, 200);
        ALLOW_SKILL_CAST_WHILE_STUNNED = builder.define("allow_skill_cast_while_stunned", false);
        builder.pop();

        SPEC = builder.build();
    }

    private ServerCommonConfig() {
    }
}

