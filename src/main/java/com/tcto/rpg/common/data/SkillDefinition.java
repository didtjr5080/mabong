package com.tcto.rpg.common.data;

import com.google.gson.JsonObject;

public class SkillDefinition {
    private final String id;
    private final String name;
    private final String jobRequired;
    private final int levelRequired;
    private final int tierRequired;
    private final String slotType;
    private final int cooldownTicks;
    private final String resourceType;
    private final int resourceCost;
    private final double damageBase;
    private final double scaleAttack;
    private final double scaleMagic;

    public SkillDefinition(String id, String name, String jobRequired, int levelRequired, int tierRequired,
                           String slotType, int cooldownTicks, String resourceType, int resourceCost,
                           double damageBase, double scaleAttack, double scaleMagic) {
        this.id = id;
        this.name = name;
        this.jobRequired = jobRequired;
        this.levelRequired = levelRequired;
        this.tierRequired = tierRequired;
        this.slotType = slotType;
        this.cooldownTicks = cooldownTicks;
        this.resourceType = resourceType;
        this.resourceCost = resourceCost;
        this.damageBase = damageBase;
        this.scaleAttack = scaleAttack;
        this.scaleMagic = scaleMagic;
    }

    public static SkillDefinition fromJson(String id, JsonObject json) {
        String name = json.has("name") ? json.get("name").getAsString() : id;
        String jobRequired = json.has("job_required") ? json.get("job_required").getAsString() : "";
        int levelRequired = json.has("level_required") ? json.get("level_required").getAsInt() : 1;
        int tierRequired = json.has("tier_required") ? json.get("tier_required").getAsInt() : 0;
        String slotType = json.has("slot_type") ? json.get("slot_type").getAsString() : "normal";
        int cooldownTicks = json.has("cooldown_ticks") ? json.get("cooldown_ticks").getAsInt() : 0;
        String resourceType = json.has("resource") ? json.getAsJsonObject("resource").get("type").getAsString() : "";
        int resourceCost = json.has("resource") ? json.getAsJsonObject("resource").get("cost").getAsInt() : 0;
        double damageBase = 0.0;
        double scaleAttack = 0.0;
        double scaleMagic = 0.0;
        if (json.has("damage")) {
            JsonObject damage = json.getAsJsonObject("damage");
            damageBase = damage.has("base") ? damage.get("base").getAsDouble() : 0.0;
            if (damage.has("scale")) {
                JsonObject scale = damage.getAsJsonObject("scale");
                scaleAttack = scale.has("attack") ? scale.get("attack").getAsDouble() : 0.0;
                scaleMagic = scale.has("magic") ? scale.get("magic").getAsDouble() : 0.0;
            }
        }
        return new SkillDefinition(id, name, jobRequired, levelRequired, tierRequired, slotType, cooldownTicks,
            resourceType, resourceCost, damageBase, scaleAttack, scaleMagic);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String jobRequired() {
        return jobRequired;
    }

    public int levelRequired() {
        return levelRequired;
    }

    public int tierRequired() {
        return tierRequired;
    }

    public String slotType() {
        return slotType;
    }

    public int cooldownTicks() {
        return cooldownTicks;
    }

    public String resourceType() {
        return resourceType;
    }

    public int resourceCost() {
        return resourceCost;
    }

    public double damageBase() {
        return damageBase;
    }

    public double scaleAttack() {
        return scaleAttack;
    }

    public double scaleMagic() {
        return scaleMagic;
    }
}

