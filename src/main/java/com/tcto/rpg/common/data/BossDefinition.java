package com.tcto.rpg.common.data;

import com.google.gson.JsonObject;

public class BossDefinition {
    private final String id;
    private final String name;
    private final String entityType;
    private final int level;

    public BossDefinition(String id, String name, String entityType, int level) {
        this.id = id;
        this.name = name;
        this.entityType = entityType;
        this.level = level;
    }

    public static BossDefinition fromJson(String id, JsonObject json) {
        String name = json.has("name") ? json.get("name").getAsString() : id;
        String entityType = json.has("entity_type") ? json.get("entity_type").getAsString() : "minecraft:wither";
        int level = json.has("level") ? json.get("level").getAsInt() : 1;
        return new BossDefinition(id, name, entityType, level);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String entityType() {
        return entityType;
    }

    public int level() {
        return level;
    }
}

