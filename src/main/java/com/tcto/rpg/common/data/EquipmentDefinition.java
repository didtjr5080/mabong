package com.tcto.rpg.common.data;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EquipmentDefinition {
    private final String id;
    private final String slot;
    private final int levelRequired;
    private final int tierRequired;
    private final Set<String> allowedJobs;
    private final Map<String, Integer> statRequirements;
    private final Map<String, Integer> stats;

    public EquipmentDefinition(String id, String slot, int levelRequired, int tierRequired,
                               Set<String> allowedJobs, Map<String, Integer> statRequirements, Map<String, Integer> stats) {
        this.id = id;
        this.slot = slot;
        this.levelRequired = levelRequired;
        this.tierRequired = tierRequired;
        this.allowedJobs = allowedJobs;
        this.statRequirements = statRequirements;
        this.stats = stats;
    }

    public static EquipmentDefinition fromJson(String id, JsonObject json) {
        String slot = json.has("slot") ? json.get("slot").getAsString() : "weapon";
        int level = 0;
        int tier = 0;
        Set<String> allowedJobs = new HashSet<>();
        Map<String, Integer> reqStats = new HashMap<>();
        if (json.has("requirements")) {
            JsonObject req = json.getAsJsonObject("requirements");
            level = req.has("level") ? req.get("level").getAsInt() : 0;
            tier = req.has("tier") ? req.get("tier").getAsInt() : 0;
            if (req.has("jobs")) {
                req.getAsJsonArray("jobs").forEach(element -> allowedJobs.add(element.getAsString()));
            }
            if (req.has("stats")) {
                JsonObject stats = req.getAsJsonObject("stats");
                for (String key : stats.keySet()) {
                    reqStats.put(key, stats.get(key).getAsInt());
                }
            }
        }
        Map<String, Integer> stats = new HashMap<>();
        if (json.has("stats")) {
            JsonObject statObj = json.getAsJsonObject("stats");
            for (String key : statObj.keySet()) {
                stats.put(key, statObj.get(key).getAsInt());
            }
        }
        return new EquipmentDefinition(id, slot, level, tier, allowedJobs, reqStats, stats);
    }

    public String id() {
        return id;
    }

    public String slot() {
        return slot;
    }

    public int levelRequired() {
        return levelRequired;
    }

    public int tierRequired() {
        return tierRequired;
    }

    public Set<String> allowedJobs() {
        return allowedJobs;
    }

    public Map<String, Integer> statRequirements() {
        return statRequirements;
    }

    public Map<String, Integer> stats() {
        return stats;
    }
}

