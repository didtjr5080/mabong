package com.tcto.rpg.client.hud;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ClientRpgState {
    private static CompoundTag lastData = new CompoundTag();
    private static int[] cooldownSlots = new int[0];
    private static int[] cooldownTicks = new int[0];
    private static List<String> effects = new ArrayList<>();
    private static int hp = 100;
    private static int maxHp = 100;
    private static int mp = 100;
    private static int maxMp = 100;
    private static int stamina = 100;
    private static int maxStamina = 100;
    private static String bossName = "";
    private static int bossHp = 0;
    private static int bossMaxHp = 0;
    private static String patternWarning = "";
    private static int patternWarningTicks = 0;
    private static final List<FloatingDamage> floatingDamage = new ArrayList<>();

    private ClientRpgState() {
    }

    public static void updateFromServer(CompoundTag tag) {
        lastData = tag.copy();
        if (tag.contains("hud")) {
            CompoundTag hud = tag.getCompound("hud");
            updateResources(
                hud.getInt("hp"),
                hud.getInt("max_hp"),
                hud.getInt("mp"),
                hud.getInt("max_mp"),
                hud.getInt("stamina"),
                hud.getInt("max_stamina")
            );
        }
    }

    public static void updateCooldowns(int[] slots, int[] remaining) {
        cooldownSlots = slots;
        cooldownTicks = remaining;
    }

    public static void updateEffects(List<String> newEffects) {
        effects = new ArrayList<>(newEffects);
    }

    public static void updateStatusEffects(List<String> newEffects) {
        effects = new ArrayList<>(newEffects);
    }

    public static void updateResources(int hpValue, int maxHpValue, int mpValue, int maxMpValue,
                                       int staminaValue, int maxStaminaValue) {
        hp = hpValue;
        maxHp = maxHpValue;
        mp = mpValue;
        maxMp = maxMpValue;
        stamina = staminaValue;
        maxStamina = maxStaminaValue;
    }

    public static void updateBossHud(String name, int currentHp, int maxHpValue) {
        bossName = name;
        bossHp = currentHp;
        bossMaxHp = maxHpValue;
    }

    public static void updatePatternWarning(String text, int durationTicks) {
        patternWarning = text;
        patternWarningTicks = Math.max(0, durationTicks);
    }

    public static void tickClient() {
        if (patternWarningTicks > 0) {
            patternWarningTicks -= 1;
            if (patternWarningTicks == 0) {
                patternWarning = "";
            }
        }
        Iterator<FloatingDamage> iterator = floatingDamage.iterator();
        while (iterator.hasNext()) {
            FloatingDamage entry = iterator.next();
            entry.ticksLeft -= 1;
            if (entry.ticksLeft <= 0) {
                iterator.remove();
            }
        }
    }

    public static CompoundTag data() {
        return lastData;
    }

    public static int[] cooldownSlots() {
        return cooldownSlots;
    }

    public static int[] cooldownTicks() {
        return cooldownTicks;
    }

    public static List<String> effects() {
        return effects;
    }

    public static int hp() {
        return hp;
    }

    public static int maxHp() {
        return maxHp;
    }

    public static int mp() {
        return mp;
    }

    public static int maxMp() {
        return maxMp;
    }

    public static int stamina() {
        return stamina;
    }

    public static int maxStamina() {
        return maxStamina;
    }

    public static String bossName() {
        return bossName;
    }

    public static int bossHp() {
        return bossHp;
    }

    public static int bossMaxHp() {
        return bossMaxHp;
    }

    public static String patternWarning() {
        return patternWarning;
    }

    public static int patternWarningTicks() {
        return patternWarningTicks;
    }

    public static void addFloatingDamage(String text, int durationTicks) {
        floatingDamage.add(new FloatingDamage(text, Math.max(10, durationTicks)));
    }

    public static List<String> floatingDamageTexts() {
        List<String> texts = new ArrayList<>();
        for (FloatingDamage entry : floatingDamage) {
            texts.add(entry.text);
        }
        return texts;
    }

    public static String jobId() {
        CompoundTag character = selectedCharacterTag();
        return character != null ? character.getString("job") : "";
    }

    public static int level() {
        CompoundTag character = selectedCharacterTag();
        return character != null ? character.getInt("level") : 1;
    }

    public static String equippedSkillId(String slotKey) {
        CompoundTag character = selectedCharacterTag();
        if (character == null || !character.contains("equipped_skills")) {
            return "";
        }
        CompoundTag equipped = character.getCompound("equipped_skills");
        return equipped.getString(slotKey);
    }

    private static CompoundTag selectedCharacterTag() {
        if (!lastData.contains("selected_character") || !lastData.contains("characters")) {
            return null;
        }
        String selected = lastData.getString("selected_character");
        CompoundTag characters = lastData.getCompound("characters");
        return characters.contains(selected) ? characters.getCompound(selected) : null;
    }

    private static class FloatingDamage {
        private final String text;
        private int ticksLeft;

        private FloatingDamage(String text, int ticksLeft) {
            this.text = text;
            this.ticksLeft = ticksLeft;
        }
    }
}

