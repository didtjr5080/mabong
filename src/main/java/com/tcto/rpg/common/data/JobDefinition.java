package com.tcto.rpg.common.data;

import com.google.gson.JsonObject;

public class JobDefinition {
    private final String id;
    private final String name;
    private final int tier;
    private final String parent;

    public JobDefinition(String id, String name, int tier, String parent) {
        this.id = id;
        this.name = name;
        this.tier = tier;
        this.parent = parent;
    }

    public static JobDefinition fromJson(String id, JsonObject json) {
        String name = json.has("name") ? json.get("name").getAsString() : id;
        int tier = json.has("tier") ? json.get("tier").getAsInt() : 0;
        String parent = json.has("parent") ? json.get("parent").getAsString() : "";
        return new JobDefinition(id, name, tier, parent);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public int tier() {
        return tier;
    }

    public String parent() {
        return parent;
    }
}

