package com.tcto.rpg.client.screen;

import com.tcto.rpg.TCToRPG;
import com.tcto.rpg.client.hud.ClientRpgState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillEquipScreen extends BaseRpgScreen {
    private static final String[] SLOT_KEYS = {"slot_1", "slot_2", "slot_3", "slot_4", "slot_5", "ultimate"};
    private static final String[] SLOT_LABELS = {"R", "Z", "X", "C", "V", "G"};
    private static final Map<String, String> JOB_PARENT = new HashMap<>();
    private static final Map<String, SkillMeta> SKILL_META = new HashMap<>();
    private final List<ClickRegion> skillRegions = new ArrayList<>();
    private final List<ClickRegion> slotRegions = new ArrayList<>();
    private final List<ClickRegion> commandRegions = new ArrayList<>();
    private String selectedSkill = "";
    private int selectedSlot = 0;
    private String lastStateSignature = "";

    static {
        JOB_PARENT.put("knight", "warrior");
        JOB_PARENT.put("paladin", "knight");
        JOB_PARENT.put("berserker", "knight");
        JOB_PARENT.put("ranger", "gunslinger");
        JOB_PARENT.put("sniper", "ranger");
        JOB_PARENT.put("gun_technician", "ranger");
        JOB_PARENT.put("blood_sorcerer", "blood_mage");
        JOB_PARENT.put("blood_king", "blood_sorcerer");
        JOB_PARENT.put("vampire_mage", "blood_sorcerer");
        JOB_PARENT.put("assassin", "rogue");
        JOB_PARENT.put("shadow_lord", "assassin");
        JOB_PARENT.put("poisoner", "assassin");

        skill("sword_judgement", "warrior", 1, 0, false, 100, "ST", 10, "빛의 검으로 전방을 강타합니다.");
        skill("strength_up", "warrior", 3, 0, false, 240, "ST", 8, "일시적으로 물리 공격력을 높입니다.");
        skill("shield_bash", "knight", 15, 1, false, 120, "ST", 14, "방패로 밀쳐 짧은 기절을 줍니다.");
        skill("sword_wave", "knight", 15, 1, false, 160, "ST", 20, "푸른 검기를 전방으로 날립니다.");
        skill("holy_judgement", "paladin", 30, 2, true, 600, "MP", 35, "성스러운 빛으로 큰 피해를 줍니다.");
        skill("rage_strike", "berserker", 30, 2, false, 140, "ST", 18, "분노를 실은 강한 베기 공격입니다.");
        skill("berserker_rage", "berserker", 30, 2, true, 560, "ST", 40, "광폭한 분노로 주변을 휩씁니다.");
        skill("rapid_fire", "gunslinger", 3, 0, false, 90, "ST", 12, "짧은 시간 동안 탄환을 연사합니다.");
        skill("roll", "gunslinger", 1, 0, false, 100, "ST", 10, "빠르게 회피 이동합니다.");
        skill("focus", "ranger", 15, 1, false, 220, "ST", 12, "집중하여 치명타 성능을 높입니다.");
        skill("headshot", "sniper", 30, 2, true, 520, "ST", 32, "정밀 조준으로 치명적인 사격을 합니다.");
        skill("overclock_barrage", "gun_technician", 30, 2, true, 540, "ST", 38, "과부하 장치로 탄환을 퍼붓습니다.");
        skill("blood_chain", "blood_mage", 3, 0, false, 130, "MP", 15, "피의 사슬로 적을 묶습니다.");
        skill("blood_spear", "blood_mage", 5, 0, false, 110, "MP", 18, "피로 만든 창을 관통시킵니다.");
        skill("lifesteal", "blood_sorcerer", 15, 1, false, 180, "MP", 22, "피해 일부를 생명력으로 흡수합니다.");
        skill("blood_rage", "blood_king", 30, 2, true, 600, "MP", 40, "혈기를 폭주시켜 전투력을 끌어올립니다.");
        skill("vampiric_eclipse", "vampire_mage", 30, 2, true, 580, "MP", 42, "흡혈의 월식으로 생명력을 빼앗습니다.");
        skill("throat_cut", "rogue", 5, 0, false, 100, "ST", 14, "급소를 베어 출혈을 유발합니다.");
        skill("stealth", "rogue", 3, 0, false, 260, "ST", 18, "그림자 속에 숨어 위협을 낮춥니다.");
        skill("intimidation", "assassin", 15, 1, false, 200, "ST", 16, "위압감으로 적을 약화시킵니다.");
        skill("poison_kill", "poisoner", 30, 2, true, 560, "ST", 34, "독 단검으로 강한 지속 피해를 줍니다.");
        skill("shadow_step", "shadow_lord", 30, 2, true, 500, "ST", 30, "그림자를 타고 순간 이동합니다.");
    }

    public SkillEquipScreen() {
        super("Skill Equip", "skill_screen_bg.png", 256, 180);
    }

    @Override
    protected void init() {
        List<String> unlocked = knownUnlockedSkills();
        String recommended = recommendedSkillForJob(ClientRpgState.jobId());
        selectedSkill = chooseInitialSkill(unlocked, recommended);
        rebuildRegions();
        lastStateSignature = stateSignature();
    }

    private void rebuildRegions() {
        skillRegions.clear();
        slotRegions.clear();
        commandRegions.clear();

        int x = panelX();
        int y = panelY();
        List<String> unlocked = knownUnlockedSkills();
        int count = Math.min(6, unlocked.size());
        for (int i = 0; i < count; i++) {
            String skillId = plainId(unlocked.get(i));
            int sx = x + 18 + i * 38;
            int sy = y + 88;
            skillRegions.add(new ClickRegion(sx, sy, 32, 32, skillId, -1, ""));
        }

        for (int i = 0; i < SLOT_LABELS.length; i++) {
            int sx = x + 18 + i * 38;
            int sy = y + 132;
            slotRegions.add(new ClickRegion(sx, sy, 32, 32, "", i, ""));
        }

        String playerName = currentPlayerName();
        String recommended = recommendedSkillForJob(ClientRpgState.jobId());
        String questId = "skill_" + recommended;
        commandRegions.add(new ClickRegion(x + 14, y + 166, 42, 14, "", -1,
            "tctorpg player startquest " + playerName + " " + questId));
        commandRegions.add(new ClickRegion(x + 62, y + 166, 52, 14, "", -1,
            "tctorpg player completequest " + playerName + " " + questId));
        commandRegions.add(new ClickRegion(x + 120, y + 166, 42, 14, "", -1, "clear"));
        commandRegions.add(new ClickRegion(x + 168, y + 166, 36, 14, "", -1,
            "tctorpg player info " + playerName));
        commandRegions.add(new ClickRegion(x + 210, y + 166, 36, 14, "", -1, "close"));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        for (ClickRegion region : skillRegions) {
            if (region.contains(mouseX, mouseY)) {
                if (button != 0) {
                    return true;
                }
                selectedSkill = region.skillId;
                return true;
            }
        }
        for (ClickRegion region : slotRegions) {
            if (region.contains(mouseX, mouseY)) {
                selectedSlot = region.slotIndex;
                if (button == 1) {
                    unequipSlot(region.slotIndex);
                } else {
                    equipSelected(region.slotIndex);
                }
                return true;
            }
        }
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        for (ClickRegion region : commandRegions) {
            if (region.contains(mouseX, mouseY)) {
                if ("close".equals(region.command)) {
                    minecraft.setScreen(null);
                } else if ("clear".equals(region.command)) {
                    unequipSlot(selectedSlot);
                } else {
                    runCommand(region.command);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void equipSelected(int slotIndex) {
        if (selectedSkill == null || selectedSkill.isBlank()) {
            return;
        }
        SkillMeta meta = SKILL_META.get(plainId(selectedSkill));
        if (meta != null && meta.ultimate != (slotIndex == 5)) {
            showLocalMessage(meta.ultimate ? "궁극기는 G 슬롯에만 장착할 수 있습니다." : "일반 스킬은 R/Z/X/C/V 슬롯에 장착하세요.");
            return;
        }
        if (meta != null && !canUse(plainId(ClientRpgState.jobId()), ClientRpgState.level(), ClientRpgState.jobTier(), meta)) {
            showLocalMessage("현재 직업/레벨/전직 조건이 맞지 않습니다.");
            return;
        }
        runCommand("tctorpg player equipskill " + currentPlayerName() + " " + slotIndex + " " + selectedSkill);
    }

    private void unequipSlot(int slotIndex) {
        runCommand("tctorpg player unequipskill " + currentPlayerName() + " " + slotIndex);
    }

    private void showLocalMessage(String message) {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.literal("[TCToRPG] " + message), false);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        refreshRegionsIfStateChanged();
        int x = panelX();
        int y = panelY();
        String jobId = plainId(ClientRpgState.jobId());
        String recommended = recommendedSkillForJob(ClientRpgState.jobId());

        drawLabel(guiGraphics, "직업 " + jobName(jobId), x + 18, y + 28, 0xF2E8C9);
        drawLabel(guiGraphics, "선택 " + shortText(skillName(selectedSkill), 12), x + 112, y + 28, 0xB8D8FF);
        drawSkillDetails(guiGraphics, selectedSkill, x + 18, y + 40);
        drawLabel(guiGraphics, "해금 스킬", x + 18, y + 78, 0xF2E8C9);

        for (ClickRegion region : skillRegions) {
            SkillMeta meta = SKILL_META.get(plainId(region.skillId));
            boolean usable = meta != null && canUse(jobId, ClientRpgState.level(), ClientRpgState.jobTier(), meta);
            drawSkillIcon(guiGraphics, region.x, region.y, region.skillId, region.skillId.equals(selectedSkill), usable);
        }
        if (skillRegions.isEmpty()) {
            drawLabel(guiGraphics, "아직 해금된 스킬이 없습니다. 퀘스트: " + recommended, x + 18, y + 92, 0xB8B8B8);
        }

        drawLabel(guiGraphics, "장착 슬롯", x + 18, y + 122, 0xF2E8C9);
        for (int i = 0; i < slotRegions.size(); i++) {
            ClickRegion region = slotRegions.get(i);
            String skill = plainId(ClientRpgState.equippedSkillId(SLOT_KEYS[i]));
            drawSlotIcon(guiGraphics, region.x, region.y, SLOT_LABELS[i], skill, i == selectedSlot);
        }

        drawCommand(guiGraphics, commandRegions.get(0), "퀘스트");
        drawCommand(guiGraphics, commandRegions.get(1), "완료");
        drawCommand(guiGraphics, commandRegions.get(2), "해제");
        drawCommand(guiGraphics, commandRegions.get(3), "정보");
        drawCommand(guiGraphics, commandRegions.get(4), "닫기");
    }

    private void refreshRegionsIfStateChanged() {
        String signature = stateSignature();
        if (!signature.equals(lastStateSignature)) {
            lastStateSignature = signature;
            if (!shouldKeepSelectedSkill()) {
                selectedSkill = chooseInitialSkill(knownUnlockedSkills(), recommendedSkillForJob(ClientRpgState.jobId()));
            }
            rebuildRegions();
        }
    }

    private void drawSkillDetails(GuiGraphics guiGraphics, String skillId, int x, int y) {
        SkillMeta meta = SKILL_META.get(plainId(skillId));
        if (meta == null) {
            drawLabel(guiGraphics, "스킬 정보가 없습니다.", x, y, 0xB8B8B8);
            return;
        }
        String slot = meta.ultimate ? "궁극기 G" : "일반 R-V";
        drawLabel(guiGraphics, "요구 " + jobName(meta.job) + " Lv" + meta.level + " T" + meta.tier + " / " + slot, x, y, 0xD8D8D8);
        drawLabel(guiGraphics, "쿨타임 " + cooldownText(meta.cooldownTicks) + " / 소모 " + resourceName(meta.resource) + " " + meta.cost, x, y + 12, 0xD8D8D8);
        drawLabel(guiGraphics, meta.description, x, y + 24, 0xC8D8FF);
    }

    private void drawSkillIcon(GuiGraphics guiGraphics, int x, int y, String skillId, boolean selected, boolean usable) {
        int border = selected ? 0xFFFFD166 : usable ? 0xFF8B8069 : 0xFF606060;
        guiGraphics.fill(x, y, x + 32, y + 32, usable ? 0xAA050505 : 0xCC101010);
        guiGraphics.hLine(x, x + 32, y, border);
        guiGraphics.hLine(x, x + 32, y + 32, 0xFF4A4337);
        guiGraphics.vLine(x, y, y + 32, border);
        guiGraphics.vLine(x + 32, y, y + 32, 0xFF4A4337);
        guiGraphics.blit(skillIcon(skillId), x + 6, y + 6, 20, 20, 0, 0, 32, 32, 32, 32);
        if (!usable) {
            guiGraphics.fill(x + 1, y + 1, x + 31, y + 31, 0x77000000);
            guiGraphics.drawCenteredString(font, "!", x + 16, y + 12, 0xFFFF5555);
        }
    }

    private void drawSlotIcon(GuiGraphics guiGraphics, int x, int y, String label, String skillId, boolean selected) {
        int border = selected ? 0xFFFFD166 : 0xFF8B8069;
        guiGraphics.fill(x, y, x + 32, y + 32, 0xAA050505);
        guiGraphics.hLine(x, x + 32, y, border);
        guiGraphics.hLine(x, x + 32, y + 32, 0xFF4A4337);
        guiGraphics.vLine(x, y, y + 32, border);
        guiGraphics.vLine(x + 32, y, y + 32, 0xFF4A4337);
        if (!skillId.isBlank() && SKILL_META.containsKey(plainId(skillId))) {
            guiGraphics.blit(skillIcon(skillId), x + 6, y + 6, 20, 20, 0, 0, 32, 32, 32, 32);
        } else if (!skillId.isBlank()) {
            guiGraphics.drawCenteredString(font, "?", x + 16, y + 13, 0xFFFF80A0);
        }
        guiGraphics.drawCenteredString(font, label, x + 16, y + 2, 0xFFFFFF);
    }

    private void drawCommand(GuiGraphics guiGraphics, ClickRegion region, String label) {
        guiGraphics.fill(region.x, region.y, region.x + region.w, region.y + region.h, 0xCC403D38);
        guiGraphics.hLine(region.x, region.x + region.w, region.y, 0xFFB8A56E);
        guiGraphics.hLine(region.x, region.x + region.w, region.y + region.h, 0xFF3B3428);
        guiGraphics.vLine(region.x, region.y, region.y + region.h, 0xFFB8A56E);
        guiGraphics.vLine(region.x + region.w, region.y, region.y + region.h, 0xFF3B3428);
        guiGraphics.drawCenteredString(font, label, region.x + region.w / 2, region.y + 3, 0xFFFFFF);
    }

    private static String chooseInitialSkill(List<String> unlocked, String recommended) {
        if (unlocked.contains("tctorpg:" + recommended) || unlocked.contains(recommended)) {
            return recommended;
        }
        String jobId = plainId(ClientRpgState.jobId());
        int level = ClientRpgState.level();
        int tier = ClientRpgState.jobTier();
        for (String skillId : unlocked) {
            SkillMeta meta = SKILL_META.get(plainId(skillId));
            if (meta != null && canUse(jobId, level, tier, meta)) {
                return plainId(skillId);
            }
        }
        if (!unlocked.isEmpty()) {
            return plainId(unlocked.get(0));
        }
        return recommended;
    }

    private boolean shouldKeepSelectedSkill() {
        if (!containsSkill(knownUnlockedSkills(), selectedSkill)) {
            return false;
        }
        SkillMeta meta = SKILL_META.get(plainId(selectedSkill));
        return meta != null && canUse(plainId(ClientRpgState.jobId()), ClientRpgState.level(), ClientRpgState.jobTier(), meta);
    }

    private static boolean containsSkill(List<String> skills, String skillId) {
        String plain = plainId(skillId);
        for (String value : skills) {
            if (plainId(value).equals(plain)) {
                return true;
            }
        }
        return false;
    }

    private static String stateSignature() {
        return plainId(ClientRpgState.jobId()) + "|"
            + ClientRpgState.level() + "|"
            + ClientRpgState.jobTier() + "|"
            + ClientRpgState.unlockedSkills() + "|"
            + ClientRpgState.equippedSkillId("slot_1") + "|"
            + ClientRpgState.equippedSkillId("slot_2") + "|"
            + ClientRpgState.equippedSkillId("slot_3") + "|"
            + ClientRpgState.equippedSkillId("slot_4") + "|"
            + ClientRpgState.equippedSkillId("slot_5") + "|"
            + ClientRpgState.equippedSkillId("ultimate");
    }

    private static List<String> knownUnlockedSkills() {
        List<String> result = new ArrayList<>();
        for (String rawSkill : ClientRpgState.unlockedSkills()) {
            String skillId = plainId(rawSkill);
            SkillMeta meta = SKILL_META.get(skillId);
            if (meta != null) {
                result.add(skillId);
            }
        }
        String jobId = plainId(ClientRpgState.jobId());
        int level = ClientRpgState.level();
        int tier = ClientRpgState.jobTier();
        result.sort(Comparator
            .comparing((String skillId) -> !canUse(jobId, level, tier, SKILL_META.get(skillId)))
            .thenComparing(skillId -> skillName(skillId)));
        return result;
    }

    private static String skillName(String skillId) {
        return switch (plainId(skillId)) {
            case "sword_judgement" -> "검의 심판";
            case "strength_up" -> "근력 강화";
            case "shield_bash" -> "방패 강타";
            case "sword_wave" -> "검기 방출";
            case "holy_judgement" -> "성심판";
            case "rage_strike" -> "분노의 일격";
            case "berserker_rage" -> "광폭화";
            case "rapid_fire" -> "난사";
            case "roll" -> "구르기";
            case "focus" -> "집중";
            case "headshot" -> "헤드샷";
            case "overclock_barrage" -> "과부하 난사";
            case "blood_chain" -> "피의 사슬";
            case "blood_spear" -> "블러드 스피어";
            case "lifesteal" -> "흡혈";
            case "blood_rage" -> "혈액 폭주";
            case "vampiric_eclipse" -> "흡혈 월식";
            case "throat_cut" -> "목긋기";
            case "stealth" -> "은신";
            case "intimidation" -> "협박";
            case "poison_kill" -> "독살";
            case "shadow_step" -> "그림자 이동";
            default -> plainId(skillId);
        };
    }

    private static String jobName(String jobId) {
        return switch (plainId(jobId)) {
            case "warrior" -> "전사";
            case "knight" -> "기사";
            case "paladin" -> "팔라딘";
            case "berserker" -> "버서커";
            case "gunslinger" -> "총잡이";
            case "ranger" -> "레인저";
            case "sniper" -> "스나이퍼";
            case "gun_technician" -> "건 테크니션";
            case "blood_mage" -> "혈마법사";
            case "blood_sorcerer" -> "혈술사";
            case "blood_king" -> "혈왕";
            case "vampire_mage" -> "흡혈 마법사";
            case "rogue" -> "도적";
            case "assassin" -> "암살자";
            case "shadow_lord" -> "그림자 군주";
            case "poisoner" -> "독술사";
            default -> plainId(jobId);
        };
    }

    private static String resourceName(String resource) {
        return "MP".equalsIgnoreCase(resource) ? "마나" : "기력";
    }

    private static String shortText(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value == null ? "" : value;
        }
        return value.substring(0, Math.max(1, maxLength - 1)) + ".";
    }

    private static boolean canUse(String jobId, int level, int tier, SkillMeta meta) {
        return level >= meta.level && tier >= meta.tier && hasJob(jobId, meta.job);
    }

    private static boolean hasJob(String currentJob, String requiredJob) {
        if (requiredJob == null || requiredJob.isBlank()) {
            return true;
        }
        String cursor = currentJob;
        for (int depth = 0; depth < 8 && cursor != null && !cursor.isBlank(); depth++) {
            if (requiredJob.equals(cursor)) {
                return true;
            }
            cursor = JOB_PARENT.getOrDefault(cursor, "");
        }
        return false;
    }

    private static String recommendedSkillForJob(String rawJobId) {
        String job = plainId(rawJobId);
        return switch (job) {
            case "warrior" -> "sword_judgement";
            case "knight" -> "shield_bash";
            case "paladin" -> "holy_judgement";
            case "berserker" -> "berserker_rage";
            case "gunslinger" -> "rapid_fire";
            case "gun_technician" -> "overclock_barrage";
            case "ranger" -> "focus";
            case "sniper" -> "headshot";
            case "blood_mage" -> "blood_spear";
            case "blood_sorcerer" -> "lifesteal";
            case "vampire_mage" -> "vampiric_eclipse";
            case "blood_king" -> "blood_rage";
            case "rogue" -> "throat_cut";
            case "assassin" -> "intimidation";
            case "shadow_lord" -> "shadow_step";
            case "poisoner" -> "poison_kill";
            default -> "sword_judgement";
        };
    }

    private static String plainId(String id) {
        if (id == null || id.isBlank()) {
            return "";
        }
        return id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
    }

    private static ResourceLocation skillIcon(String skillId) {
        return ResourceLocation.fromNamespaceAndPath(TCToRPG.MODID, "textures/gui/skills/" + plainId(skillId) + ".png");
    }

    private static boolean isUltimateSkill(String skillId) {
        SkillMeta meta = SKILL_META.get(plainId(skillId));
        return meta != null && meta.ultimate;
    }

    private static String cooldownText(int ticks) {
        if (ticks <= 0) {
            return "0s";
        }
        return String.format("%.1fs", ticks / 20.0);
    }

    private static void skill(String id, String job, int level, int tier, boolean ultimate, int cooldownTicks, String resource, int cost, String description) {
        SKILL_META.put(id, new SkillMeta(job, level, tier, ultimate, cooldownTicks, resource, cost, description));
    }

    private record ClickRegion(int x, int y, int w, int h, String skillId, int slotIndex, String command) {
        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        }
    }

    private record SkillMeta(String job, int level, int tier, boolean ultimate, int cooldownTicks, String resource, int cost, String description) {
    }
}
