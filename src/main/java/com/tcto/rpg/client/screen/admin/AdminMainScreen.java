package com.tcto.rpg.client.screen.admin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

public class AdminMainScreen extends BaseAdminScreen {
    private static final int SLOT = 34;
    private static final int GAP = 3;
    private Page page = Page.MAIN;
    private String selectedPlayer = "";

    public AdminMainScreen() {
        super("TCToRPG Admin Chest");
    }

    @Override
    protected void init() {
        setLines();
        if (selectedPlayer.isBlank()) {
            selectedPlayer = currentPlayerName();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int index = slotAt((int) mouseX, (int) mouseY);
        if (index < 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        Entry[] entries = entries();
        if (index >= entries.length || entries[index] == null) {
            return true;
        }

        Entry entry = entries[index];
        if (entry.selectPlayer != null && !entry.selectPlayer.isBlank()) {
            selectedPlayer = entry.selectPlayer;
            page = Page.PLAYER;
            return true;
        }
        if (entry.page != null) {
            page = entry.page;
            return true;
        }
        if (entry.close) {
            minecraft.setScreen(null);
            return true;
        }
        if (entry.command != null && !entry.command.isBlank()) {
            runCommand(entry.command.replace("{player}", selectedPlayerName()));
        }
        return true;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int x = gridX();
        int y = gridY();
        Entry[] entries = entries();
        for (int i = 0; i < 27; i++) {
            int sx = x + (i % 9) * (SLOT + GAP);
            int sy = y + (i / 9) * (SLOT + GAP);
            drawChestSlot(guiGraphics, sx, sy, i < entries.length ? entries[i] : null, i == slotAt(mouseX, mouseY));
        }
        guiGraphics.drawCenteredString(font, "Target: " + selectedPlayerName(), width / 2, y - 31, 0xB8D8FF);
        guiGraphics.drawCenteredString(font, title(page), width / 2, y - 18, 0xF2E8C9);
    }

    private void drawChestSlot(GuiGraphics guiGraphics, int x, int y, Entry entry, boolean hover) {
        int fill = hover ? 0xD02C2A22 : 0xCC11100D;
        guiGraphics.fill(x, y, x + SLOT, y + SLOT, fill);
        guiGraphics.hLine(x, x + SLOT, y, 0xFFB8A56E);
        guiGraphics.hLine(x, x + SLOT, y + SLOT, 0xFF3B3428);
        guiGraphics.vLine(x, y, y + SLOT, 0xFFB8A56E);
        guiGraphics.vLine(x + SLOT, y, y + SLOT, 0xFF3B3428);
        if (entry == null) {
            return;
        }
        guiGraphics.drawCenteredString(font, entry.icon, x + SLOT / 2, y + 5, entry.color);
        guiGraphics.drawCenteredString(font, entry.shortLabel, x + SLOT / 2, y + 21, 0xFFFFFF);
    }

    private int slotAt(int mouseX, int mouseY) {
        int x = gridX();
        int y = gridY();
        int localX = mouseX - x;
        int localY = mouseY - y;
        if (localX < 0 || localY < 0) {
            return -1;
        }
        int col = localX / (SLOT + GAP);
        int row = localY / (SLOT + GAP);
        if (col < 0 || col >= 9 || row < 0 || row >= 3) {
            return -1;
        }
        int insideX = localX % (SLOT + GAP);
        int insideY = localY % (SLOT + GAP);
        if (insideX >= SLOT || insideY >= SLOT) {
            return -1;
        }
        return row * 9 + col;
    }

    private int gridX() {
        return (width - (9 * SLOT + 8 * GAP)) / 2;
    }

    private int gridY() {
        return (height - (3 * SLOT + 2 * GAP)) / 2 + 20;
    }

    private Entry[] entries() {
        return switch (page) {
            case MAIN -> entries(
                page("P", "Player", Page.PLAYER, 0xFF90CAF9),
                page("I", "Item", Page.ITEM, 0xFFFFCC80),
                page("J", "Job", Page.JOB, 0xFFA5D6A7),
                page("S", "Skill", Page.SKILL, 0xFFCE93D8),
                page("T", "Stat", Page.STAT, 0xFFA5D6A7),
                page("Q", "Quest", Page.QUEST, 0xFFFFF59D),
                cmd("V", "Valid", "tctorpg validate", 0xFFE0E0E0),
                cmd("R", "Reload", "tctorpg reload all", 0xFF80DEEA),
                close("X", "Close")
            );
            case PLAYER -> entries(
                page("@", "Pick", Page.PLAYER_SELECT, 0xFFB8D8FF),
                cmd("?", "Info", "tctorpg player info {player}", 0xFFFFFFFF),
                cmd("50", "Lv50", "tctorpg player setlevel {player} 50", 0xFFFFF59D),
                cmd("H+", "HP100", "tctorpg player sethp {player} 100", 0xFFFF8A80),
                cmd("M+", "MP100", "tctorpg player setmana {player} 100", 0xFF90CAF9),
                page("J", "Job", Page.JOB, 0xFFA5D6A7),
                page("S", "Skill", Page.SKILL, 0xFFCE93D8),
                page("T", "Stat", Page.STAT, 0xFFA5D6A7),
                back()
            );
            case PLAYER_SELECT -> playerEntries();
            case ITEM -> entries(
                cmd("+", "Create", "tctorpg item create dev_sword weapon 30 1000", 0xFFFFCC80),
                cmd("ST", "Give", "tctorpg player giveitem {player} starter_sword", 0xFFB0BEC5),
                cmd("EQ", "Equip", "tctorpg player equipitem {player} weapon starter_sword", 0xFFB0BEC5),
                cmd("V", "Valid", "tctorpg validate items", 0xFFE0E0E0),
                cmd("R", "Reload", "tctorpg reload all", 0xFF80DEEA),
                back()
            );
            case STAT -> entries(
                cmd("+P", "Pt10", "tctorpg player addstatpoint {player} 10", 0xFFA5D6A7),
                cmd("STR", "STR1", "tctorpg player setstat {player} str 1", 0xFFFF8A80),
                cmd("VIT", "VIT1", "tctorpg player setstat {player} vit 1", 0xFFA5D6A7),
                cmd("INT", "INT1", "tctorpg player setstat {player} int 1", 0xFF90CAF9),
                cmd("LUK", "LUK1", "tctorpg player setstat {player} luk 1", 0xFFFFF59D),
                cmd("S10", "S10", "tctorpg player setstat {player} str 10", 0xFFFF8A80),
                cmd("V10", "V10", "tctorpg player setstat {player} vit 10", 0xFFA5D6A7),
                cmd("I10", "I10", "tctorpg player setstat {player} int 10", 0xFF90CAF9),
                cmd("L10", "L10", "tctorpg player setstat {player} luk 10", 0xFFFFF59D),
                back()
            );
            case JOB -> entries(
                page("W", "War", Page.JOB_WARRIOR, 0xFFFFCC80),
                page("G", "Gun", Page.JOB_GUN, 0xFF90CAF9),
                page("B", "Blood", Page.JOB_BLOOD, 0xFFFF8A80),
                page("R", "Rogue", Page.JOB_ROGUE, 0xFFA5D6A7),
                cmd("V", "Valid", "tctorpg validate jobs", 0xFFE0E0E0),
                back()
            );
            case JOB_WARRIOR -> entries(
                cmd("W", "War", "tctorpg player setjob {player} warrior", 0xFFFFCC80),
                cmd("K", "Kngt", "tctorpg player setjob {player} knight", 0xFFFFCC80),
                cmd("P", "Pal", "tctorpg player setjob {player} paladin", 0xFFFFF59D),
                cmd("B", "Bers", "tctorpg player setjob {player} berserker", 0xFFFF8A80),
                page("<", "Job", Page.JOB, 0xFFE0E0E0)
            );
            case JOB_GUN -> entries(
                cmd("G", "Gun", "tctorpg player setjob {player} gunslinger", 0xFF90CAF9),
                cmd("R", "Rang", "tctorpg player setjob {player} ranger", 0xFF90CAF9),
                cmd("S", "Snip", "tctorpg player setjob {player} sniper", 0xFF90CAF9),
                cmd("T", "Tech", "tctorpg player setjob {player} gun_technician", 0xFF80DEEA),
                page("<", "Job", Page.JOB, 0xFFE0E0E0)
            );
            case JOB_BLOOD -> entries(
                cmd("BM", "Blood", "tctorpg player setjob {player} blood_mage", 0xFFFF8A80),
                cmd("BS", "Sorc", "tctorpg player setjob {player} blood_sorcerer", 0xFFFF8A80),
                cmd("BK", "King", "tctorpg player setjob {player} blood_king", 0xFFFF8A80),
                cmd("VM", "Vamp", "tctorpg player setjob {player} vampire_mage", 0xFFCE93D8),
                page("<", "Job", Page.JOB, 0xFFE0E0E0)
            );
            case JOB_ROGUE -> entries(
                cmd("RG", "Rogue", "tctorpg player setjob {player} rogue", 0xFFA5D6A7),
                cmd("A", "Asn", "tctorpg player setjob {player} assassin", 0xFFA5D6A7),
                cmd("SL", "Shad", "tctorpg player setjob {player} shadow_lord", 0xFFB39DDB),
                cmd("PO", "Pois", "tctorpg player setjob {player} poisoner", 0xFFA5D6A7),
                page("<", "Job", Page.JOB, 0xFFE0E0E0)
            );
            case SKILL -> entries(
                page("W", "War", Page.SKILL_WARRIOR, 0xFFFFCC80),
                page("G", "Gun", Page.SKILL_GUN, 0xFF90CAF9),
                page("B", "Blood", Page.SKILL_BLOOD, 0xFFFF8A80),
                page("R", "Rogue", Page.SKILL_ROGUE, 0xFFA5D6A7),
                cmd("V", "Valid", "tctorpg validate skills", 0xFFE0E0E0),
                cmd("?", "Info", "tctorpg player info {player}", 0xFFFFFFFF),
                back()
            );
            case SKILL_WARRIOR -> entries(
                cmd("SJ", "Judge", "tctorpg player unlockskill {player} sword_judgement", 0xFFCE93D8),
                cmd("SU", "StrUp", "tctorpg player unlockskill {player} strength_up", 0xFFFF8A80),
                cmd("SB", "Shld", "tctorpg player unlockskill {player} shield_bash", 0xFFB0BEC5),
                cmd("SW", "Wave", "tctorpg player unlockskill {player} sword_wave", 0xFF90CAF9),
                cmd("HJ", "Holy", "tctorpg player unlockskill {player} holy_judgement", 0xFFFFF59D),
                cmd("RS", "Rage", "tctorpg player unlockskill {player} rage_strike", 0xFFFF8A80),
                cmd("BR", "BUlt", "tctorpg player unlockskill {player} berserker_rage", 0xFFFF5555),
                cmd("FG", "G-Ult", "tctorpg player forceequipskill {player} 5 berserker_rage", 0xFFFFFFFF),
                page("<", "Skill", Page.SKILL, 0xFFE0E0E0)
            );
            case SKILL_GUN -> entries(
                cmd("RF", "Rapid", "tctorpg player unlockskill {player} rapid_fire", 0xFF90CAF9),
                cmd("RO", "Roll", "tctorpg player unlockskill {player} roll", 0xFFA5D6A7),
                cmd("FO", "Focus", "tctorpg player unlockskill {player} focus", 0xFF90CAF9),
                cmd("HS", "Head", "tctorpg player unlockskill {player} headshot", 0xFFFF8A80),
                cmd("OB", "OUlt", "tctorpg player unlockskill {player} overclock_barrage", 0xFF80DEEA),
                cmd("FG", "G-Ult", "tctorpg player forceequipskill {player} 5 overclock_barrage", 0xFFFFFFFF),
                page("<", "Skill", Page.SKILL, 0xFFE0E0E0)
            );
            case SKILL_BLOOD -> entries(
                cmd("BC", "Chain", "tctorpg player unlockskill {player} blood_chain", 0xFFFF8A80),
                cmd("BS", "Spear", "tctorpg player unlockskill {player} blood_spear", 0xFFFF8A80),
                cmd("LS", "Steal", "tctorpg player unlockskill {player} lifesteal", 0xFFFF8A80),
                cmd("BR", "BRage", "tctorpg player unlockskill {player} blood_rage", 0xFFFF8A80),
                cmd("VE", "VUlt", "tctorpg player unlockskill {player} vampiric_eclipse", 0xFFCE93D8),
                cmd("FG", "G-Ult", "tctorpg player forceequipskill {player} 5 vampiric_eclipse", 0xFFFFFFFF),
                page("<", "Skill", Page.SKILL, 0xFFE0E0E0)
            );
            case SKILL_ROGUE -> entries(
                cmd("TC", "Cut", "tctorpg player unlockskill {player} throat_cut", 0xFFA5D6A7),
                cmd("ST", "Hide", "tctorpg player unlockskill {player} stealth", 0xFFB39DDB),
                cmd("IN", "Fear", "tctorpg player unlockskill {player} intimidation", 0xFFCE93D8),
                cmd("PK", "Pois", "tctorpg player unlockskill {player} poison_kill", 0xFFA5D6A7),
                cmd("SS", "Step", "tctorpg player unlockskill {player} shadow_step", 0xFFB39DDB),
                cmd("FG", "G-Ult", "tctorpg player forceequipskill {player} 5 shadow_step", 0xFFFFFFFF),
                page("<", "Skill", Page.SKILL, 0xFFE0E0E0)
            );
            case QUEST -> entries(
                cmd("S", "Start", "tctorpg player startquest {player} warrior_trial", 0xFFFFF59D),
                cmd("C", "Done", "tctorpg player completequest {player} warrior_trial", 0xFFFFF59D),
                cmd("0", "Reset", "tctorpg player resetquest {player} warrior_trial", 0xFFFFF59D),
                cmd("+", "Make", "tctorpg template quest warrior_trial job", 0xFFFFF59D),
                cmd("V", "Valid", "tctorpg validate quests", 0xFFE0E0E0),
                back()
            );
        };
    }

    private Entry[] playerEntries() {
        List<Entry> result = new ArrayList<>();
        if (minecraft != null && minecraft.getConnection() != null) {
            for (PlayerInfo info : minecraft.getConnection().getOnlinePlayers()) {
                String name = info.getProfile().getName();
                result.add(new Entry("P", shortLabel(name), null, null, false, 0xFFB8D8FF, name));
                if (result.size() >= 26) {
                    break;
                }
            }
        }
        result.add(back());
        return result.toArray(new Entry[0]);
    }

    private Entry[] entries(Entry... entries) {
        return entries;
    }

    private Entry cmd(String icon, String label, String command, int color) {
        return new Entry(icon, shortLabel(label), command, null, false, color, "");
    }

    private Entry page(String icon, String label, Page target, int color) {
        return new Entry(icon, shortLabel(label), null, target, false, color, "");
    }

    private Entry close(String icon, String label) {
        return new Entry(icon, shortLabel(label), null, null, true, 0xFFFF8A80, "");
    }

    private Entry back() {
        return page("<", "Back", Page.MAIN, 0xFFE0E0E0);
    }

    private String selectedPlayerName() {
        return selectedPlayer == null || selectedPlayer.isBlank() ? currentPlayerName() : selectedPlayer;
    }

    private String shortLabel(String label) {
        return label.length() > 5 ? label.substring(0, 5) : label;
    }

    private String title(Page page) {
        return switch (page) {
            case MAIN -> "Admin Menu";
            case PLAYER -> "Player";
            case PLAYER_SELECT -> "Select Player";
            case ITEM -> "Item";
            case JOB -> "Job Category";
            case JOB_WARRIOR -> "Job: Warrior";
            case JOB_GUN -> "Job: Gun";
            case JOB_BLOOD -> "Job: Blood";
            case JOB_ROGUE -> "Job: Rogue";
            case SKILL -> "Skill Category";
            case SKILL_WARRIOR -> "Skill: Warrior";
            case SKILL_GUN -> "Skill: Gun";
            case SKILL_BLOOD -> "Skill: Blood";
            case SKILL_ROGUE -> "Skill: Rogue";
            case STAT -> "Stat";
            case QUEST -> "Quest";
        };
    }

    private enum Page {
        MAIN,
        PLAYER,
        PLAYER_SELECT,
        ITEM,
        JOB,
        JOB_WARRIOR,
        JOB_GUN,
        JOB_BLOOD,
        JOB_ROGUE,
        SKILL,
        SKILL_WARRIOR,
        SKILL_GUN,
        SKILL_BLOOD,
        SKILL_ROGUE,
        STAT,
        QUEST
    }

    private record Entry(String icon, String shortLabel, String command, Page page, boolean close, int color, String selectPlayer) {
    }
}
