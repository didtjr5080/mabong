package com.tcto.rpg.common.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class ExpTable {
    private final Map<Integer, Integer> levelExp = new HashMap<>();

    public void loadFromJson(JsonObject json) {
        levelExp.clear();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            int level = Integer.parseInt(entry.getKey());
            int exp = entry.getValue().getAsInt();
            levelExp.put(level, exp);
        }
    }

    public int expForLevel(int level) {
        return levelExp.getOrDefault(level, 0);
    }
}

