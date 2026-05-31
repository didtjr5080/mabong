package com.tcto.rpg.template;

import com.google.gson.JsonObject;

public class SkillTemplateGenerator {
    public JsonObject generate(String id, String jobId) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", id);
        json.addProperty("job_required", normalizeJob(emptyToDefault(jobId, "warrior")));
        json.addProperty("level_required", 1);
        json.addProperty("tier_required", 0);
        json.addProperty("slot_type", "normal");
        json.addProperty("cooldown_ticks", 100);
        JsonObject resource = new JsonObject();
        resource.addProperty("type", "mp");
        resource.addProperty("cost", 5);
        json.add("resource", resource);
        JsonObject damage = new JsonObject();
        damage.addProperty("type", "physical");
        damage.addProperty("base", 10);
        JsonObject scale = new JsonObject();
        scale.addProperty("attack", 1.0);
        scale.addProperty("magic", 0.0);
        damage.add("scale", scale);
        json.add("damage", damage);
        json.addProperty("range", 3.0);
        json.addProperty("target_type", "single");
        json.addProperty("icon", id);
        return json;
    }

    private static String emptyToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String normalizeJob(String jobId) {
        return jobId.contains(":") ? jobId : "tctorpg:" + jobId;
    }
}
