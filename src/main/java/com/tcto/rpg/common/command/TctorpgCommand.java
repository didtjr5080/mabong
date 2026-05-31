package com.tcto.rpg.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.tcto.rpg.admin.AdminActionLogger;
import com.tcto.rpg.admin.AdminPermissionLevel;
import com.tcto.rpg.admin.AdminPermissionService;
import com.tcto.rpg.backup.BackupManager;
import com.tcto.rpg.backup.BackupMetadata;
import com.tcto.rpg.common.data.EquipmentDefinition;
import com.tcto.rpg.common.data.JobDefinition;
import com.tcto.rpg.common.data.QuestDefinition;
import com.tcto.rpg.common.data.RpgDataManager;
import com.tcto.rpg.common.data.SkillDefinition;
import com.tcto.rpg.docs.SchemaDocGenerator;
import com.tcto.rpg.server.boss.BossService;
import com.tcto.rpg.server.character.LevelingService;
import com.tcto.rpg.server.combat.RpgRuntimeState;
import com.tcto.rpg.server.combat.RpgRuntimeStateManager;
import com.tcto.rpg.server.data.CharacterData;
import com.tcto.rpg.server.data.PlayerRpgData;
import com.tcto.rpg.server.equipment.EquipmentRequirementChecker;
import com.tcto.rpg.server.equipment.EquipmentService;
import com.tcto.rpg.server.equipment.EquipmentSlot;
import com.tcto.rpg.server.monster.MonsterService;
import com.tcto.rpg.server.save.RpgSavedData;
import com.tcto.rpg.server.save.RpgSyncService;
import com.tcto.rpg.server.skill.SkillSlot;
import com.tcto.rpg.template.TemplateManager;
import com.tcto.rpg.validation.DataValidator;
import com.tcto.rpg.validation.ValidationError;
import com.tcto.rpg.validation.ValidationResult;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.nio.file.Path;
import java.nio.file.Files;

public final class TctorpgCommand {
    private static final Path DATA_ROOT = Path.of("data", "tctorpg", "rpg");
    private static final Path CONTENT_PACKS_ROOT = Path.of("content_packs");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private TctorpgCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tctorpg")
            .executes(ctx -> runHelp(ctx.getSource()))
            .then(Commands.literal("help")
                .executes(ctx -> runHelp(ctx.getSource())))
            .then(Commands.literal("admin")
                .requires(source -> can(source, AdminPermissionLevel.ADMIN))
                .executes(ctx -> runAdminHint(ctx.getSource())))
            .then(Commands.literal("ui")
                .requires(source -> can(source, AdminPermissionLevel.ADMIN))
                .executes(ctx -> runAdminHint(ctx.getSource())))
            .then(Commands.literal("validate")
                .requires(source -> can(source, AdminPermissionLevel.ADMIN))
                .executes(ctx -> runValidate(ctx.getSource(), null))
                .then(Commands.argument("category", StringArgumentType.word())
                    .executes(ctx -> runValidate(ctx.getSource(), StringArgumentType.getString(ctx, "category")))))
            .then(Commands.literal("reload")
                .requires(source -> can(source, AdminPermissionLevel.ADMIN))
                .executes(ctx -> runReload(ctx.getSource(), "all"))
                .then(Commands.argument("category", StringArgumentType.word())
                    .executes(ctx -> runReload(ctx.getSource(), StringArgumentType.getString(ctx, "category")))))
            .then(Commands.literal("template")
                .requires(source -> can(source, AdminPermissionLevel.DESIGNER))
                .then(Commands.argument("kind", StringArgumentType.word())
                    .then(Commands.argument("id", StringArgumentType.word())
                        .executes(ctx -> runTemplate(ctx.getSource(), StringArgumentType.getString(ctx, "kind"), StringArgumentType.getString(ctx, "id"), ""))
                        .then(Commands.argument("option", StringArgumentType.word())
                            .executes(ctx -> runTemplate(ctx.getSource(), StringArgumentType.getString(ctx, "kind"), StringArgumentType.getString(ctx, "id"), StringArgumentType.getString(ctx, "option")))))))
            .then(Commands.literal("backup")
                .requires(source -> can(source, AdminPermissionLevel.ADMIN))
                .then(Commands.literal("create")
                    .executes(ctx -> runBackupCreate(ctx.getSource())))
                .then(Commands.literal("list")
                    .executes(ctx -> runBackupList(ctx.getSource()))))
            .then(Commands.literal("docs")
                .requires(source -> can(source, AdminPermissionLevel.DESIGNER))
                .then(Commands.literal("generate")
                    .executes(ctx -> runDocsGenerate(ctx.getSource()))))
            .then(Commands.literal("item")
                .requires(source -> can(source, AdminPermissionLevel.DESIGNER))
                .then(Commands.literal("create")
                    .then(Commands.argument("spec", StringArgumentType.greedyString())
                        .executes(ctx -> runCreateItemSpec(ctx.getSource(), StringArgumentType.getString(ctx, "spec"))))))
            .then(Commands.literal("player")
                .requires(source -> can(source, AdminPermissionLevel.MODERATOR))
                .then(Commands.literal("info")
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> runPlayerInfo(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("setlevel")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                            .executes(ctx -> runSetLevel(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "level"))))))
                .then(Commands.literal("addexp")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                            .executes(ctx -> runAddExp(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("setjob")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("jobId", StringArgumentType.word())
                            .executes(ctx -> runSetJob(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), normalize(StringArgumentType.getString(ctx, "jobId")))))))
                .then(Commands.literal("setstat")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("statId", StringArgumentType.word())
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> runAddStat(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "statId"), IntegerArgumentType.getInteger(ctx, "amount")))))))
                .then(Commands.literal("addstatpoint")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                            .executes(ctx -> runAddStatPoint(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("sethp")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                            .executes(ctx -> runSetHp(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("addhp")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                            .executes(ctx -> runAddHp(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("setmana")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                            .executes(ctx -> runSetMana(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("addmana")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                            .executes(ctx -> runAddMana(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("setmaxmana")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                            .executes(ctx -> runSetMaxMana(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("unlockskill")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("skillId", StringArgumentType.word())
                            .executes(ctx -> runUnlockSkill(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), normalize(StringArgumentType.getString(ctx, "skillId")))))))
                .then(Commands.literal("equipskill")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("slotIndex", IntegerArgumentType.integer(0, 5))
                            .then(Commands.argument("skillId", StringArgumentType.word())
                                .executes(ctx -> runEquipSkill(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "slotIndex"), normalize(StringArgumentType.getString(ctx, "skillId"))))))))
                .then(Commands.literal("unequipskill")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("slotIndex", IntegerArgumentType.integer(0, 5))
                            .executes(ctx -> runUnequipSkill(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "slotIndex"))))))
                .then(Commands.literal("forceequipskill")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("slotIndex", IntegerArgumentType.integer(0, 5))
                            .then(Commands.argument("skillId", StringArgumentType.word())
                                .executes(ctx -> runForceEquipSkill(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "slotIndex"), normalize(StringArgumentType.getString(ctx, "skillId"))))))))
                .then(Commands.literal("giveitem")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("itemId", StringArgumentType.word())
                            .executes(ctx -> runGiveItem(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), normalize(StringArgumentType.getString(ctx, "itemId")))))))
                .then(Commands.literal("equipitem")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("slot", StringArgumentType.word())
                            .then(Commands.argument("itemId", StringArgumentType.word())
                                .executes(ctx -> runEquipItem(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "slot"), normalize(StringArgumentType.getString(ctx, "itemId"))))))))
                .then(Commands.literal("unequipitem")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("slot", StringArgumentType.word())
                            .executes(ctx -> runUnequipItem(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "slot"))))))
                .then(Commands.literal("startquest")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("questId", StringArgumentType.word())
                            .executes(ctx -> runStartQuest(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), normalize(StringArgumentType.getString(ctx, "questId")))))))
                .then(Commands.literal("completequest")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("questId", StringArgumentType.word())
                            .executes(ctx -> runCompleteQuest(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), normalize(StringArgumentType.getString(ctx, "questId")))))))
                .then(Commands.literal("resetquest")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("questId", StringArgumentType.word())
                            .executes(ctx -> runResetQuest(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), normalize(StringArgumentType.getString(ctx, "questId"))))))))
            .then(Commands.literal("spawnmob")
                .requires(source -> can(source, AdminPermissionLevel.TESTER))
                .then(Commands.argument("monsterId", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        MonsterService.spawn(player, normalize(StringArgumentType.getString(ctx, "monsterId")));
                        log(ctx.getSource(), "spawned monster " + StringArgumentType.getString(ctx, "monsterId"));
                        return 1;
                    })))
            .then(Commands.literal("spawnboss")
                .requires(source -> can(source, AdminPermissionLevel.TESTER))
                .then(Commands.argument("bossId", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        BossService.spawn(player, normalize(StringArgumentType.getString(ctx, "bossId")));
                        log(ctx.getSource(), "spawned boss " + StringArgumentType.getString(ctx, "bossId"));
                        return 1;
                    })))
            .then(Commands.literal("event")
                .requires(source -> can(source, AdminPermissionLevel.ADMIN))
                .then(Commands.literal("start")
                    .then(Commands.argument("eventId", StringArgumentType.word())
                        .executes(ctx -> runLoggedStub(ctx.getSource(), "started event " + StringArgumentType.getString(ctx, "eventId")))))
                .then(Commands.literal("stop")
                    .then(Commands.argument("eventId", StringArgumentType.word())
                        .executes(ctx -> runLoggedStub(ctx.getSource(), "stopped event " + StringArgumentType.getString(ctx, "eventId")))))
                .then(Commands.literal("list")
                    .executes(ctx -> runLoggedStub(ctx.getSource(), "listed events")))
                .then(Commands.literal("status")
                    .then(Commands.argument("eventId", StringArgumentType.word())
                        .executes(ctx -> runLoggedStub(ctx.getSource(), "checked event " + StringArgumentType.getString(ctx, "eventId"))))))
        );
    }

    private static int runHelp(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("TCToRPG commands:"), false);
        source.sendSuccess(() -> Component.literal("/tctorpg admin 또는 /tctorpg ui - 관리자 UI 안내. 실제 화면은 기본 O 키로 열립니다."), false);
        source.sendSuccess(() -> Component.literal("/tctorpg validate [category] - validate RPG JSON data"), false);
        source.sendSuccess(() -> Component.literal("/tctorpg reload [category] - reload server resources"), false);
        source.sendSuccess(() -> Component.literal("/tctorpg template <kind> <id> [option] - create content template"), false);
        source.sendSuccess(() -> Component.literal("/tctorpg backup create|list - manage backups"), false);
        source.sendSuccess(() -> Component.literal("/tctorpg docs generate - generate schema docs"), false);
        source.sendSuccess(() -> Component.literal("/tctorpg player info|setlevel|addexp|setjob|setstat|addstatpoint|unlockskill|equipskill|equipitem ..."), false);
        source.sendSuccess(() -> Component.literal("/tctorpg spawnmob <monsterId>, /tctorpg spawnboss <bossId>"), false);
        return 1;
    }

    private static int runAdminHint(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("[TCToRPG 관리자 UI]"), false);
        source.sendSuccess(() -> Component.literal("현재 빌드는 클라이언트 로컬 키바인드 방식입니다."), false);
        source.sendSuccess(() -> Component.literal("관리자 화면을 열려면 기본 키 O를 누르세요."), false);
        source.sendSuccess(() -> Component.literal("키 설정: 설정 -> 조작 -> TCToRPG -> screen_admin"), false);
        source.sendSuccess(() -> Component.literal("버튼 클릭 시 서버 명령이 실행됩니다."), false);
        log(source, "requested admin ui hint");
        return 1;
    }

    private static int runValidate(CommandSourceStack source, String category) {
        ValidationResult result = category == null
            ? new DataValidator().validateAll(DATA_ROOT, CONTENT_PACKS_ROOT)
            : new DataValidator().validateCategory(DATA_ROOT, category);
        source.sendSuccess(() -> Component.literal(result.summary()), false);
        for (ValidationError error : result.errors()) {
            source.sendFailure(Component.literal("ERROR " + error.file() + ": " + error.message()));
        }
        for (ValidationError warning : result.warnings()) {
            source.sendSuccess(() -> Component.literal("WARN " + warning.file() + ": " + warning.message()), false);
        }
        log(source, "executed /tctorpg validate" + (category == null ? "" : " " + category));
        return result.hasErrors() ? 0 : 1;
    }

    private static int runReload(CommandSourceStack source, String category) {
        source.getServer().reloadResources(source.getServer().getPackRepository().getSelectedIds());
        source.sendSuccess(() -> Component.literal("TCToRPG reload started: " + category), true);
        source.sendSuccess(() -> Component.literal("TCToRPG reload command sent to Minecraft resource manager."), false);
        source.sendSuccess(() -> Component.literal("If content pack data does not change, check content_packs/default and run validate."), false);
        source.sendSuccess(() -> Component.literal("Resource reload requested. Completion is handled asynchronously by Minecraft."), false);
        log(source, "executed /tctorpg reload " + category);
        return 1;
    }

    private static int runTemplate(CommandSourceStack source, String kind, String id, String option) {
        try {
            Path path = new TemplateManager(CONTENT_PACKS_ROOT).createTemplate(kind, id, option);
            RpgDataManager.INSTANCE.loadExternalContentPacks(CONTENT_PACKS_ROOT);
            source.sendSuccess(() -> Component.literal("Created template: " + path), true);
            source.sendSuccess(() -> Component.literal("Next: /tctorpg validate"), false);
            source.sendSuccess(() -> Component.literal("Next: /tctorpg reload all"), false);
            source.sendSuccess(() -> Component.literal("Job 적용 예: /tctorpg player setjob stone_0401 dark_knight"), false);
            source.sendSuccess(() -> Component.literal("Skill 해금 예: /tctorpg player unlockskill stone_0401 rage_strike"), false);
            source.sendSuccess(() -> Component.literal("Item 지급 예: /tctorpg player giveitem stone_0401 blood_staff"), false);
            log(source, "created template " + kind + " " + id);
            return 1;
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().startsWith("Template already exists:")) {
                source.sendSuccess(() -> Component.literal(ex.getMessage()), false);
                source.sendSuccess(() -> Component.literal("Next: /tctorpg validate"), false);
                source.sendSuccess(() -> Component.literal("Next: /tctorpg reload all"), false);
                log(source, "template already exists " + kind + " " + id);
                return 1;
            }
            source.sendFailure(Component.literal(ex.getMessage()));
            return 0;
        }
    }

    private static int runBackupCreate(CommandSourceStack source) {
        try {
            BackupMetadata metadata = new BackupManager(Path.of("backups")).create();
            source.sendSuccess(() -> Component.literal("Created backup: " + metadata.id()), true);
            log(source, "created backup " + metadata.id());
            return 1;
        } catch (Exception ex) {
            source.sendFailure(Component.literal(ex.getMessage()));
            return 0;
        }
    }

    private static int runBackupList(CommandSourceStack source) {
        try {
            for (BackupMetadata metadata : new BackupManager(Path.of("backups")).list()) {
                source.sendSuccess(() -> Component.literal(metadata.id() + " (" + metadata.sizeBytes() + " bytes)"), false);
            }
            log(source, "listed backups");
            return 1;
        } catch (Exception ex) {
            source.sendFailure(Component.literal(ex.getMessage()));
            return 0;
        }
    }

    private static int runDocsGenerate(CommandSourceStack source) {
        try {
            new SchemaDocGenerator().generate(Path.of("docs", "generated"));
            source.sendSuccess(() -> Component.literal("Generated docs in docs/generated"), true);
            log(source, "generated schema docs");
            return 1;
        } catch (Exception ex) {
            source.sendFailure(Component.literal(ex.getMessage()));
            return 0;
        }
    }

    private static int runCreateItemSpec(CommandSourceStack source, String spec) {
        String[] parts = spec.trim().split("\\s+");
        if (parts.length < 4) {
            source.sendFailure(Component.literal("[TCToRPG] Usage: /tctorpg item create <name or id> <slot> <level> <attack>"));
            return 0;
        }
        int attack;
        int level;
        try {
            attack = Integer.parseInt(parts[parts.length - 1]);
            level = Integer.parseInt(parts[parts.length - 2]);
        } catch (NumberFormatException ex) {
            source.sendFailure(Component.literal("[TCToRPG] Last two values must be level and attack numbers."));
            return 0;
        }
        String slot = parts[parts.length - 3].toLowerCase();
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 0; i < parts.length - 3; i++) {
            if (i > 0) {
                nameBuilder.append(' ');
            }
            nameBuilder.append(parts[i]);
        }
        String displayName = nameBuilder.toString().trim();
        String itemId = safeItemId(displayName, slot);
        return runCreateItem(source, itemId, displayName, slot, level, attack);
    }

    private static int runCreateItem(CommandSourceStack source, String itemId, String displayName, String slot, int level, int attack) {
        if (equipmentSlot(slot) == null) {
            source.sendFailure(Component.literal("[TCToRPG] Invalid slot: " + slot));
            source.sendFailure(Component.literal("Slots: weapon, offhand, helmet, chestplate, leggings, boots, accessory_1, accessory_2, accessory_3"));
            return 0;
        }
        if (level < 1) {
            source.sendFailure(Component.literal("[TCToRPG] Level must be at least 1."));
            return 0;
        }
        Path path = CONTENT_PACKS_ROOT.resolve("default").resolve("items").resolve(itemId + ".json");
        if (Files.exists(path)) {
            source.sendFailure(Component.literal("[TCToRPG] Item already exists: " + path));
            return 0;
        }
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(itemJson(itemId, displayName, slot, level, attack)));
            RpgDataManager.INSTANCE.loadExternalContentPacks(CONTENT_PACKS_ROOT);
            source.sendSuccess(() -> Component.literal("[TCToRPG] Created item: " + path), true);
            source.sendSuccess(() -> Component.literal("[TCToRPG] Item id: " + itemId + " / name: " + displayName), false);
            source.sendSuccess(() -> Component.literal("Next: /tctorpg validate items"), false);
            source.sendSuccess(() -> Component.literal("Use: /tctorpg player giveitem stone_0401 " + itemId), false);
            source.sendSuccess(() -> Component.literal("Use: /tctorpg player equipitem stone_0401 " + slot + " " + itemId), false);
            log(source, "created item " + itemId + " slot=" + slot + " level=" + level + " attack=" + attack);
            return 1;
        } catch (Exception ex) {
            source.sendFailure(Component.literal("[TCToRPG] Failed to create item: " + ex.getMessage()));
            return 0;
        }
    }

    private static JsonObject itemJson(String itemId, String displayName, String slot, int level, int attack) {
        JsonObject json = new JsonObject();
        json.addProperty("id", itemId);
        json.addProperty("name", displayName);
        json.addProperty("type", "equipment");
        json.addProperty("slot", slot);
        json.addProperty("weapon_type", slot.equals("weapon") ? "sword" : "");
        json.addProperty("rarity", "common");

        JsonObject requirements = new JsonObject();
        requirements.addProperty("level", level);
        requirements.add("jobs", new com.google.gson.JsonArray());
        requirements.addProperty("tier", 0);
        requirements.add("stats", new JsonObject());
        json.add("requirements", requirements);

        JsonObject stats = new JsonObject();
        if (slot.equals("weapon")) {
            stats.addProperty("physical_attack", attack);
        } else {
            stats.addProperty("defense", attack);
        }
        json.add("stats", stats);
        json.add("effects", new com.google.gson.JsonArray());
        return json;
    }

    private static int runPlayerInfo(CommandSourceStack source, ServerPlayer target) {
        CharacterData character = character(target);
        source.sendSuccess(() -> Component.literal(target.getGameProfile().getName()
            + " level=" + character.level()
            + " job=" + character.jobId()
            + " statPoints=" + character.statPoints()), false);
        log(source, "checked player " + target.getGameProfile().getName());
        return 1;
    }

    private static int runSetLevel(CommandSourceStack source, ServerPlayer target, int level) {
        CharacterData character = character(target);
        int before = character.level();
        if (level > before) {
            LevelingService.grantLevels(target, level - before);
        } else {
            character.setLevel(level);
            saveAndSync(target);
        }
        target.sendSystemMessage(Component.literal("[TCToRPG] Level set: " + level));
        source.sendSuccess(() -> Component.literal("[TCToRPG] Level set: " + target.getGameProfile().getName() + " -> " + level), true);
        log(source, "changed player " + target.getGameProfile().getName() + " level -> " + level);
        return 1;
    }

    private static int runAddExp(CommandSourceStack source, ServerPlayer target, int amount) {
        LevelingService.addExp(target, amount);
        log(source, "added exp " + amount + " to " + target.getGameProfile().getName());
        return 1;
    }

    private static int runSetJob(CommandSourceStack source, ServerPlayer target, String jobId) {
        JobDefinition job = RpgDataManager.INSTANCE.getJob(jobId);
        if (job == null) {
            source.sendFailure(Component.literal("[TCToRPG] Unknown job: " + plainId(jobId)));
            return 0;
        }
        CharacterData character = character(target);
        String before = character.jobId();
        character.setJobId(jobId);
        character.setJobTier(job.tier());
        refreshSkillsAfterJobChange(character);
        saveAndSync(target);
        target.sendSystemMessage(Component.literal("[TCToRPG] Job set: " + plainId(jobId)));
        target.sendSystemMessage(Component.literal("[TCToRPG] Skills refreshed for job: " + plainId(jobId)));
        source.sendSuccess(() -> Component.literal("[TCToRPG] Job set: " + target.getGameProfile().getName() + " -> " + plainId(jobId)), true);
        log(source, "changed player " + target.getGameProfile().getName() + " job " + before + " -> " + jobId);
        return 1;
    }

    private static int runAddStat(CommandSourceStack source, ServerPlayer target, String statId, int amount) {
        if (!isValidStat(statId)) {
            source.sendFailure(Component.literal("[TCToRPG] Unknown stat: " + statId));
            return 0;
        }
        CharacterData character = character(target);
        if (character.statPoints() < amount) {
            source.sendFailure(Component.literal("[TCToRPG] Not enough stat points: " + character.statPoints() + " / " + amount));
            return 0;
        }
        character.setStatPoints(character.statPoints() - amount);
        character.baseStats().add(statId, amount);
        saveAndSync(target);
        target.sendSystemMessage(Component.literal("[TCToRPG] Stat allocated: " + statId + " +" + amount));
        source.sendSuccess(() -> Component.literal("[TCToRPG] Stat allocated: " + target.getGameProfile().getName() + " " + statId + " +" + amount), true);
        log(source, "changed player " + target.getGameProfile().getName() + " stat " + statId + " +=" + amount);
        return 1;
    }

    private static int runAddStatPoint(CommandSourceStack source, ServerPlayer target, int amount) {
        CharacterData character = character(target);
        character.setStatPoints(character.statPoints() + amount);
        saveAndSync(target);
        target.sendSystemMessage(Component.literal("[TCToRPG] Stat points added: +" + amount));
        source.sendSuccess(() -> Component.literal("[TCToRPG] Stat points added: " + target.getGameProfile().getName() + " +" + amount), true);
        log(source, "added stat points " + amount + " to " + target.getGameProfile().getName());
        return 1;
    }

    private static int runSetHp(CommandSourceStack source, ServerPlayer target, int amount) {
        float hp = Math.max(1.0F, Math.min((float) amount, target.getMaxHealth()));
        target.setHealth(hp);
        RpgSyncService.syncAll(target);
        target.sendSystemMessage(Component.literal("[TCToRPG] HP set: " + (int) hp + "/" + (int) target.getMaxHealth()));
        source.sendSuccess(() -> Component.literal("[TCToRPG] HP set: " + target.getGameProfile().getName() + " -> " + (int) hp), true);
        log(source, "set hp " + target.getGameProfile().getName() + " -> " + (int) hp);
        return 1;
    }

    private static int runAddHp(CommandSourceStack source, ServerPlayer target, int amount) {
        return runSetHp(source, target, (int) target.getHealth() + amount);
    }

    private static int runSetMana(CommandSourceStack source, ServerPlayer target, int amount) {
        RpgRuntimeState runtime = RpgRuntimeStateManager.get(target);
        runtime.setMana(amount);
        RpgSyncService.syncAll(target);
        target.sendSystemMessage(Component.literal("[TCToRPG] MP set: " + runtime.mana() + "/" + runtime.maxMana()));
        source.sendSuccess(() -> Component.literal("[TCToRPG] MP set: " + target.getGameProfile().getName() + " -> " + runtime.mana()), true);
        log(source, "set mana " + target.getGameProfile().getName() + " -> " + runtime.mana());
        return 1;
    }

    private static int runAddMana(CommandSourceStack source, ServerPlayer target, int amount) {
        RpgRuntimeState runtime = RpgRuntimeStateManager.get(target);
        return runSetMana(source, target, runtime.mana() + amount);
    }

    private static int runSetMaxMana(CommandSourceStack source, ServerPlayer target, int amount) {
        RpgRuntimeState runtime = RpgRuntimeStateManager.get(target);
        runtime.setMaxMana(amount);
        runtime.setMana(Math.min(runtime.mana(), runtime.maxMana()));
        RpgSyncService.syncAll(target);
        target.sendSystemMessage(Component.literal("[TCToRPG] Max MP set: " + runtime.maxMana()));
        source.sendSuccess(() -> Component.literal("[TCToRPG] Max MP set: " + target.getGameProfile().getName() + " -> " + runtime.maxMana()), true);
        log(source, "set max mana " + target.getGameProfile().getName() + " -> " + runtime.maxMana());
        return 1;
    }

    private static int runUnlockSkill(CommandSourceStack source, ServerPlayer target, String skillId) {
        if (!can(source, AdminPermissionLevel.ADMIN)) {
            source.sendFailure(Component.literal("[TCToRPG] Skill unlock is admin-only. Players obtain skills from quest rewards."));
            return 0;
        }
        SkillDefinition skill = RpgDataManager.INSTANCE.getSkill(skillId);
        if (skill == null) {
            source.sendFailure(Component.literal("[TCToRPG] Unknown skill: " + plainId(skillId)));
            return 0;
        }
        CharacterData character = character(target);
        character.unlockedSkills().add(skillId);
        saveAndSync(target);
        target.sendSystemMessage(Component.literal("[TCToRPG] Skill unlocked: " + plainId(skillId)));
        source.sendSuccess(() -> Component.literal("[TCToRPG] Skill unlocked: " + target.getGameProfile().getName() + " -> " + plainId(skillId)), true);
        log(source, "unlocked skill " + skillId + " for " + target.getGameProfile().getName());
        return 1;
    }

    private static int runEquipSkill(CommandSourceStack source, ServerPlayer target, int slotIndex, String skillId) {
        SkillSlot slot = SkillSlot.fromIndex(slotIndex);
        if (slot == null) {
            source.sendFailure(Component.literal("[TCToRPG] Invalid skill slot: " + slotIndex));
            return 0;
        }
        SkillDefinition skill = RpgDataManager.INSTANCE.getSkill(skillId);
        if (skill == null) {
            source.sendFailure(Component.literal("[TCToRPG] Unknown skill: " + plainId(skillId)));
            return 0;
        }
        CharacterData character = character(target);
        if (!character.unlockedSkills().contains(skillId)) {
            source.sendFailure(Component.literal("[TCToRPG] Skill is not unlocked: " + plainId(skillId)));
            return 0;
        }
        if (!meetsSkillRequirements(character, skill)) {
            source.sendFailure(Component.literal("[TCToRPG] Skill requirements not met: " + plainId(skillId)));
            return 0;
        }
        if (!canEquipSkillInSlot(slot, skill)) {
            source.sendFailure(Component.literal("[TCToRPG] Skill cannot be equipped in slot " + slotIndex + ": " + plainId(skillId)));
            return 0;
        }
        character.equippedSkills().put(slot, skillId);
        saveAndSync(target);
        target.sendSystemMessage(Component.literal("[TCToRPG] Skill equipped: " + plainId(skillId) + " -> slot " + slotIndex));
        source.sendSuccess(() -> Component.literal("[TCToRPG] Skill equipped: " + target.getGameProfile().getName() + " slot " + slotIndex + " -> " + plainId(skillId)), true);
        log(source, "equipped skill " + skillId + " for " + target.getGameProfile().getName() + " slot " + slotIndex);
        return 1;
    }

    private static int runForceEquipSkill(CommandSourceStack source, ServerPlayer target, int slotIndex, String skillId) {
        if (!can(source, AdminPermissionLevel.ADMIN)) {
            source.sendFailure(Component.literal("[TCToRPG] Force equip is admin-only."));
            return 0;
        }
        SkillSlot slot = SkillSlot.fromIndex(slotIndex);
        if (slot == null) {
            source.sendFailure(Component.literal("[TCToRPG] Invalid skill slot: " + slotIndex));
            return 0;
        }
        SkillDefinition skill = RpgDataManager.INSTANCE.getSkill(skillId);
        if (skill == null) {
            source.sendFailure(Component.literal("[TCToRPG] Unknown skill: " + plainId(skillId)));
            return 0;
        }
        if (!canEquipSkillInSlot(slot, skill)) {
            source.sendFailure(Component.literal("[TCToRPG] Skill cannot be equipped in slot " + slotIndex + ": " + plainId(skillId)));
            return 0;
        }
        CharacterData character = character(target);
        character.unlockedSkills().add(skillId);
        character.equippedSkills().put(slot, skillId);
        saveAndSync(target);
        target.sendSystemMessage(Component.literal("[TCToRPG] Skill force-equipped: " + plainId(skillId) + " -> slot " + slotIndex));
        source.sendSuccess(() -> Component.literal("[TCToRPG] Skill force-equipped: " + target.getGameProfile().getName() + " slot " + slotIndex + " -> " + plainId(skillId)), true);
        log(source, "force-equipped skill " + skillId + " for " + target.getGameProfile().getName() + " slot " + slotIndex);
        return 1;
    }

    private static int runUnequipSkill(CommandSourceStack source, ServerPlayer target, int slotIndex) {
        SkillSlot slot = SkillSlot.fromIndex(slotIndex);
        if (slot == null) {
            source.sendFailure(Component.literal("[TCToRPG] Invalid skill slot: " + slotIndex));
            return 0;
        }
        CharacterData character = character(target);
        String removed = character.equippedSkills().remove(slot);
        saveAndSync(target);
        String removedName = removed == null || removed.isBlank() ? "empty" : plainId(removed);
        target.sendSystemMessage(Component.literal("[TCToRPG] Skill unequipped: slot " + slotIndex));
        source.sendSuccess(() -> Component.literal("[TCToRPG] Skill unequipped: " + target.getGameProfile().getName() + " slot " + slotIndex + " (" + removedName + ")"), true);
        log(source, "unequipped skill " + removedName + " for " + target.getGameProfile().getName() + " slot " + slotIndex);
        return 1;
    }

    private static int runGiveItem(CommandSourceStack source, ServerPlayer target, String itemId) {
        // Temporary MVP implementation: custom item registry is not wired to real ItemStack yet.
        // Give a visible vanilla iron sword and name it with the requested TCToRPG item id.
        ItemStack stack = new ItemStack(Items.IRON_SWORD);

        String plainId = itemId.contains(":") ? itemId.substring(itemId.indexOf(':') + 1) : itemId;
        EquipmentDefinition definition = RpgDataManager.INSTANCE.getEquipment(itemId);
        String displayName = definition == null ? plainId : definition.name();
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(displayName));

        boolean added = target.getInventory().add(stack);
        if (!added) {
            target.drop(stack, false);
        }

        saveAndSync(target);

        source.sendSuccess(() -> Component.literal("[TCToRPG] 아이템 지급: " + displayName + " (" + plainId + ") -> " + target.getGameProfile().getName()), true);
        target.sendSystemMessage(Component.literal("[TCToRPG] 아이템 지급: " + displayName));

        log(source, "gave visible item " + itemId + " to " + target.getGameProfile().getName());
        return 1;
    }

    private static int runEquipItem(CommandSourceStack source, ServerPlayer target, String rawSlot, String itemId) {
        EquipmentSlot slot = equipmentSlot(rawSlot);
        if (slot == null) {
            source.sendFailure(Component.literal("[TCToRPG] Invalid equipment slot: " + rawSlot));
            return 0;
        }
        EquipmentDefinition item = RpgDataManager.INSTANCE.getEquipment(itemId);
        if (item == null) {
            source.sendFailure(Component.literal("[TCToRPG] Unknown equipment data: " + plainId(itemId)));
            return 0;
        }
        if (!item.slot().equalsIgnoreCase(slot.tagKey())) {
            source.sendFailure(Component.literal("[TCToRPG] Slot mismatch: " + plainId(itemId) + " requires " + item.slot()));
            return 0;
        }
        CharacterData character = character(target);
        if (!EquipmentRequirementChecker.canEquip(character, item)) {
            source.sendFailure(Component.literal("[TCToRPG] Equipment requirements not met: " + plainId(itemId)));
            return 0;
        }
        if (!EquipmentService.tryEquip(character, slot, itemId)) {
            source.sendFailure(Component.literal("[TCToRPG] Equipment equip failed: " + plainId(itemId)));
            return 0;
        }
        saveAndSync(target);
        target.sendSystemMessage(Component.literal("[TCToRPG] Equipment equipped: " + plainId(itemId) + " -> " + slot.tagKey()));
        source.sendSuccess(() -> Component.literal("[TCToRPG] Equipment equipped: " + target.getGameProfile().getName() + " " + slot.tagKey() + " -> " + plainId(itemId)), true);
        log(source, "equipped item " + itemId + " for " + target.getGameProfile().getName() + " slot " + slot.tagKey());
        return 1;
    }

    private static int runUnequipItem(CommandSourceStack source, ServerPlayer target, String rawSlot) {
        EquipmentSlot slot = equipmentSlot(rawSlot);
        if (slot == null) {
            source.sendFailure(Component.literal("[TCToRPG] Invalid equipment slot: " + rawSlot));
            return 0;
        }
        CharacterData character = character(target);
        String removed = character.equipment().remove(slot);
        saveAndSync(target);
        String removedName = removed == null || removed.isBlank() ? "empty" : plainId(removed);
        target.sendSystemMessage(Component.literal("[TCToRPG] Equipment unequipped: " + slot.tagKey()));
        source.sendSuccess(() -> Component.literal("[TCToRPG] Equipment unequipped: " + target.getGameProfile().getName() + " " + slot.tagKey() + " (" + removedName + ")"), true);
        log(source, "unequipped item " + removedName + " for " + target.getGameProfile().getName() + " slot " + slot.tagKey());
        return 1;
    }

    private static int runStartQuest(CommandSourceStack source, ServerPlayer target, String questId) {
        String plainId = plainId(questId);
        QuestDefinition quest = RpgDataManager.INSTANCE.getQuest(questId);
        if (quest == null) {
            source.sendFailure(Component.literal("[TCToRPG] Unknown quest: " + plainId));
            return 0;
        }
        CharacterData character = character(target);
        if (character.completedQuests().contains(questId) && !quest.repeatable()) {
            source.sendFailure(Component.literal("[TCToRPG] Quest already completed: " + plainId));
            return 0;
        }
        character.activeQuests().add(questId);
        saveAndSync(target);
        target.sendSystemMessage(Component.literal("[TCToRPG] 퀘스트 시작: " + plainId));
        source.sendSuccess(() -> Component.literal("[TCToRPG] 퀘스트 시작: " + plainId + " -> " + target.getGameProfile().getName()), true);
        log(source, "started quest " + questId + " for " + target.getGameProfile().getName());
        return 1;
    }

    private static int runCompleteQuest(CommandSourceStack source, ServerPlayer target, String questId) {
        String plainId = plainId(questId);
        QuestDefinition quest = RpgDataManager.INSTANCE.getQuest(questId);
        if (quest == null) {
            source.sendFailure(Component.literal("[TCToRPG] Unknown quest: " + plainId));
            return 0;
        }
        CharacterData character = character(target);
        if (!character.activeQuests().contains(questId) && !quest.repeatable()) {
            source.sendFailure(Component.literal("[TCToRPG] Quest is not active: " + plainId));
            return 0;
        }
        character.activeQuests().remove(questId);
        character.completedQuests().add(questId);
        applyQuestRewards(source, target, character, quest);
        saveAndSync(target);
        target.sendSystemMessage(Component.literal("[TCToRPG] 퀘스트 완료: " + plainId));
        source.sendSuccess(() -> Component.literal("[TCToRPG] 퀘스트 완료: " + plainId + " -> " + target.getGameProfile().getName()), true);
        log(source, "completed quest " + questId + " for " + target.getGameProfile().getName());
        return 1;
    }

    private static int runResetQuest(CommandSourceStack source, ServerPlayer target, String questId) {
        String plainId = plainId(questId);
        CharacterData character = character(target);
        character.activeQuests().remove(questId);
        character.completedQuests().remove(questId);
        saveAndSync(target);
        target.sendSystemMessage(Component.literal("[TCToRPG] 퀘스트 초기화: " + plainId));
        source.sendSuccess(() -> Component.literal("[TCToRPG] 퀘스트 초기화: " + plainId + " -> " + target.getGameProfile().getName()), true);
        log(source, "reset quest " + questId + " for " + target.getGameProfile().getName());
        return 1;
    }

    private static int runLoggedStub(CommandSourceStack source, String action) {
        source.sendSuccess(() -> Component.literal("TCToRPG: " + action), true);
        log(source, action);
        return 1;
    }

    private static CharacterData character(ServerPlayer target) {
        return RpgSavedData.get(target.server).getPlayerData(target.getUUID()).getOrCreateSelectedCharacter();
    }

    private static void saveAndSync(ServerPlayer target) {
        RpgSavedData savedData = RpgSavedData.get(target.server);
        PlayerRpgData playerData = savedData.getPlayerData(target.getUUID());
        savedData.setDirty();
        RpgSyncService.syncAll(target, playerData);
    }

    private static void applyQuestRewards(CommandSourceStack source, ServerPlayer target, CharacterData character, QuestDefinition quest) {
        for (QuestDefinition.Reward reward : quest.rewards()) {
            String type = reward.type() == null ? "" : reward.type().toLowerCase();
            switch (type) {
                case "exp" -> {
                    int amount = Math.max(0, reward.amount());
                    if (amount > 0) {
                        LevelingService.addExp(target, amount);
                        target.sendSystemMessage(Component.literal("[TCToRPG] Quest reward EXP: +" + amount));
                    }
                }
                case "skill" -> grantQuestSkillReward(source, target, character, reward.id());
                case "item" -> {
                    if (reward.id() != null && !reward.id().isBlank()) {
                        int amount = Math.max(1, reward.amount());
                        for (int i = 0; i < amount; i++) {
                            runGiveItem(source, target, normalize(reward.id()));
                        }
                    }
                }
                case "stat_point", "stat_points" -> {
                    int amount = Math.max(1, reward.amount());
                    character.setStatPoints(character.statPoints() + amount);
                    target.sendSystemMessage(Component.literal("[TCToRPG] Quest reward stat points: +" + amount));
                }
                default -> {
                    if (!type.isBlank()) {
                        source.sendSuccess(() -> Component.literal("[TCToRPG] Unknown quest reward ignored: " + type), false);
                    }
                }
            }
        }
    }

    private static void grantQuestSkillReward(CommandSourceStack source, ServerPlayer target, CharacterData character, String rawSkillId) {
        if (rawSkillId == null || rawSkillId.isBlank()) {
            return;
        }
        String skillId = normalize(rawSkillId);
        SkillDefinition skill = RpgDataManager.INSTANCE.getSkill(skillId);
        if (skill == null) {
            source.sendFailure(Component.literal("[TCToRPG] Quest reward skill does not exist: " + plainId(skillId)));
            return;
        }
        if (!meetsSkillRequirements(character, skill)) {
            source.sendFailure(Component.literal("[TCToRPG] Quest reward skill requirements not met: " + plainId(skillId)));
            return;
        }
        if (character.unlockedSkills().add(skillId)) {
            target.sendSystemMessage(Component.literal("[TCToRPG] Quest reward skill unlocked: " + plainId(skillId)));
            log(source, "quest unlocked skill " + skillId + " for " + target.getGameProfile().getName());
        } else {
            target.sendSystemMessage(Component.literal("[TCToRPG] Quest reward skill already unlocked: " + plainId(skillId)));
        }
    }

    private static boolean can(CommandSourceStack source, AdminPermissionLevel level) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return AdminPermissionService.has(player, level);
        }
        return source.hasPermission(2);
    }

    private static void log(CommandSourceStack source, String action) {
        if (source.getEntity() instanceof ServerPlayer player) {
            AdminActionLogger.log(player, action);
        } else {
            AdminActionLogger.log("Console", action);
        }
    }

    private static boolean isValidStat(String statId) {
        return "str".equals(statId) || "vit".equals(statId) || "int".equals(statId) || "luk".equals(statId);
    }

    private static boolean meetsSkillRequirements(CharacterData character, SkillDefinition skill) {
        if (!hasJobRequirement(character.jobId(), skill.jobRequired())) {
            return false;
        }
        if (character.level() < skill.levelRequired()) {
            return false;
        }
        return character.jobTier() >= skill.tierRequired();
    }

    private static boolean hasJobRequirement(String currentJobId, String requiredJobId) {
        if (requiredJobId == null || requiredJobId.isBlank()) {
            return true;
        }
        String cursor = currentJobId;
        for (int depth = 0; depth < 8 && cursor != null && !cursor.isBlank(); depth++) {
            if (requiredJobId.equals(cursor)) {
                return true;
            }
            JobDefinition job = RpgDataManager.INSTANCE.getJob(cursor);
            cursor = job == null ? "" : job.parent();
        }
        return false;
    }

    private static boolean canEquipSkillInSlot(SkillSlot slot, SkillDefinition skill) {
        boolean ultimateSkill = skill.slotType().equalsIgnoreCase("ultimate");
        return ultimateSkill == (slot == SkillSlot.ULTIMATE);
    }

    private static void refreshSkillsAfterJobChange(CharacterData character) {
        character.equippedSkills().entrySet().removeIf(entry -> {
            SkillDefinition skill = RpgDataManager.INSTANCE.getSkill(entry.getValue());
            return skill == null || !meetsSkillRequirements(character, skill) || !canEquipSkillInSlot(entry.getKey(), skill);
        });

        for (SkillDefinition skill : RpgDataManager.INSTANCE.skills()) {
            if (meetsSkillRequirements(character, skill)) {
                character.unlockedSkills().add(skill.id());
            }
        }

        if (!character.equippedSkills().containsKey(SkillSlot.SLOT_1)) {
            firstUsableSkill(character, false).ifPresent(skill -> character.equippedSkills().put(SkillSlot.SLOT_1, skill.id()));
        }
        if (!character.equippedSkills().containsKey(SkillSlot.ULTIMATE)) {
            firstUsableSkill(character, true).ifPresent(skill -> character.equippedSkills().put(SkillSlot.ULTIMATE, skill.id()));
        }
    }

    private static java.util.Optional<SkillDefinition> firstUsableSkill(CharacterData character, boolean ultimate) {
        return RpgDataManager.INSTANCE.skills().stream()
            .filter(skill -> skill.slotType().equalsIgnoreCase(ultimate ? "ultimate" : "normal"))
            .filter(skill -> meetsSkillRequirements(character, skill))
            .sorted(java.util.Comparator.comparingInt(SkillDefinition::levelRequired).thenComparing(SkillDefinition::id))
            .findFirst();
    }

    private static EquipmentSlot equipmentSlot(String rawSlot) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.tagKey().equalsIgnoreCase(rawSlot) || slot.name().equalsIgnoreCase(rawSlot)) {
                return slot;
            }
        }
        return null;
    }

    private static String safeItemId(String displayName, String slot) {
        String id = displayName.toLowerCase()
            .replaceAll("[^a-z0-9_\\-]+", "_")
            .replaceAll("_+", "_")
            .replaceAll("^_+|_+$", "");
        if (id.isBlank()) {
            id = "custom_" + slot;
        }
        return nextAvailableItemId(id);
    }

    private static String nextAvailableItemId(String baseId) {
        String id = baseId;
        int index = 1;
        while (Files.exists(CONTENT_PACKS_ROOT.resolve("default").resolve("items").resolve(id + ".json"))) {
            id = baseId + "_" + index;
            index++;
        }
        return id;
    }

    private static String normalize(String id) {
        if (id.contains(":")) {
            return id;
        }
        return "tctorpg:" + id;
    }

    private static String plainId(String id) {
        return id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
    }
}
