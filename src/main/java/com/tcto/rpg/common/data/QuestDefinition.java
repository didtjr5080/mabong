package com.tcto.rpg.common.data;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class QuestDefinition {
    private final String id;
    private final String name;
    private final String type;
    private final boolean repeatable;
    private final List<Reward> rewards;

    public QuestDefinition(String id, String name, String type, boolean repeatable, List<Reward> rewards) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.repeatable = repeatable;
        this.rewards = List.copyOf(rewards);
    }

    public static QuestDefinition fromJson(String id, JsonObject json) {
        String name = json.has("name") ? json.get("name").getAsString() : id;
        String type = json.has("type") ? json.get("type").getAsString() : "normal";
        boolean repeatable = json.has("repeatable") && json.get("repeatable").getAsBoolean();
        List<Reward> rewards = new ArrayList<>();
        if (json.has("rewards") && json.get("rewards").isJsonArray()) {
            for (var element : json.getAsJsonArray("rewards")) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject reward = element.getAsJsonObject();
                String rewardType = reward.has("type") ? reward.get("type").getAsString() : "";
                String rewardId = "";
                if (reward.has("skill_id")) {
                    rewardId = reward.get("skill_id").getAsString();
                } else if (reward.has("item_id")) {
                    rewardId = reward.get("item_id").getAsString();
                } else if (reward.has("id")) {
                    rewardId = reward.get("id").getAsString();
                }
                int amount = reward.has("amount") ? reward.get("amount").getAsInt() : 1;
                rewards.add(new Reward(rewardType, rewardId, amount));
            }
        }
        return new QuestDefinition(id, name, type, repeatable, rewards);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public boolean repeatable() {
        return repeatable;
    }

    public List<Reward> rewards() {
        return rewards;
    }

    public record Reward(String type, String id, int amount) {
    }
}
