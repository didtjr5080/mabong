package com.tcto.rpg.common.data;

import com.google.gson.JsonObject;

public class MonsterDefinition {
    private final String id;
    private final String entityType;
    private final int level;
    private final int exp;

    public MonsterDefinition(String id, String entityType, int level, int exp) {
        this.id = id;
        this.entityType = entityType;
        this.level = level;
        this.exp = exp;
    }

    public static MonsterDefinition fromJson(String id, JsonObject json) {
        String entityType = json.has("entity_type") ? json.get("entity_type").getAsString() : "minecraft:zombie";
        int level = json.has("level") ? json.get("level").getAsInt() : 1;
        int exp = json.has("exp") ? json.get("exp").getAsInt() : 0;
        return new MonsterDefinition(id, entityType, level, exp);
    }

    public String id() {
        return id;
    }

    public String entityType() {
        return entityType;
    }

    public int level() {
        return level;
    }

    public int exp() {
        return exp;
    }
}

