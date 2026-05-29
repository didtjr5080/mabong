package com.tcto.rpg.template;

import com.google.gson.JsonObject;

public class SkillTemplateGenerator {
    public JsonObject generate(String id, String jobId) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", id);
        json.addProperty("job_id", emptyToDefault(jobId, "warrior"));
        json.addProperty("required_level", 1);
        json.addProperty("required_job_tier", 0);
        json.addProperty("damage_type", "physical");
        json.addProperty("base_damage", 10);
        json.addProperty("stat_scaling", "str");
        json.addProperty("stat_coefficient", 1.0);
        json.addProperty("cooldown_ticks", 100);
        json.addProperty("resource_type", "mp");
        json.addProperty("resource_cost", 5);
        json.addProperty("range", 3.0);
        json.addProperty("target_type", "single");
        json.addProperty("icon", id);
        return json;
    }

    private static String emptyToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
