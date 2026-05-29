package com.tcto.rpg.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tcto.rpg.admin.AdminActionLogger;
import com.tcto.rpg.admin.AdminPermissionLevel;
import com.tcto.rpg.admin.AdminPermissionService;
import com.tcto.rpg.backup.BackupManager;
import com.tcto.rpg.backup.BackupMetadata;
import com.tcto.rpg.docs.SchemaDocGenerator;
import com.tcto.rpg.server.boss.BossService;
import com.tcto.rpg.server.character.LevelingService;
import com.tcto.rpg.server.data.CharacterData;
import com.tcto.rpg.server.data.PlayerRpgData;
import com.tcto.rpg.server.monster.MonsterService;
import com.tcto.rpg.server.save.RpgSavedData;
import com.tcto.rpg.server.save.RpgSyncService;
import com.tcto.rpg.template.TemplateManager;
import com.tcto.rpg.validation.DataValidator;
import com.tcto.rpg.validation.ValidationError;
import com.tcto.rpg.validation.ValidationResult;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public final class TctorpgCommand {
    private static final Path DATA_ROOT = Path.of("data", "tctorpg", "rpg");
    private static final Path CONTENT_PACKS_ROOT = Path.of("content_packs");

    private TctorpgCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tctorpg")
            .executes(ctx -> runHelp(ctx.getSource()))
            .then(Commands.literal("help")
                .executes(ctx -> runHelp(ctx.getSource())))
            .then(Commands.literal("admin")
                .requires(source -> can(source, AdminPermissionLevel.ADMIN))
                .executes(ctx -> {
                    log(ctx.getSource(), "opened admin screen");
                    ctx.getSource().sendSuccess(() -> Component.literal("TCToRPG admin screen requested. Press O to open the local admin UI."), false);
                    return 1;
                }))
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
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ctx -> runAddStat(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "statId"), IntegerArgumentType.getInteger(ctx, "amount")))))))
                .then(Commands.literal("unlockskill")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("skillId", StringArgumentType.word())
                            .executes(ctx -> runUnlockSkill(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), normalize(StringArgumentType.getString(ctx, "skillId")))))))
                .then(Commands.literal("giveitem")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("itemId", StringArgumentType.word())
                            .executes(ctx -> runLoggedStub(ctx.getSource(), "gave item " + StringArgumentType.getString(ctx, "itemId") + " to " + EntityArgument.getPlayer(ctx, "player").getGameProfile().getName()))))))
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
        source.sendSuccess(() -> Component.literal("/tctorpg admin - request admin UI, use O to open local UI"), false);
        source.sendSuccess(() -> Component.literal("/tctorpg validate [category] - validate RPG JSON data"), false);
        source.sendSuccess(() -> Component.literal("/tctorpg reload [category] - reload server resources"), false);
        source.sendSuccess(() -> Component.literal("/tctorpg template <kind> <id> [option] - create content template"), false);
        source.sendSuccess(() -> Component.literal("/tctorpg backup create|list - manage backups"), false);
        source.sendSuccess(() -> Component.literal("/tctorpg docs generate - generate schema docs"), false);
        source.sendSuccess(() -> Component.literal("/tctorpg player info|setlevel|addexp|setjob|setstat|unlockskill ..."), false);
        source.sendSuccess(() -> Component.literal("/tctorpg spawnmob <monsterId>, /tctorpg spawnboss <bossId>"), false);
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
        log(source, "executed /tctorpg reload " + category);
        return 1;
    }

    private static int runTemplate(CommandSourceStack source, String kind, String id, String option) {
        try {
            Path path = new TemplateManager(CONTENT_PACKS_ROOT).createTemplate(kind, id, option);
            source.sendSuccess(() -> Component.literal("Created template: " + path), true);
            log(source, "created template " + kind + " " + id);
            return 1;
        } catch (Exception ex) {
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
        character.setLevel(level);
        saveAndSync(target);
        log(source, "changed player " + target.getGameProfile().getName() + " level -> " + level);
        return 1;
    }

    private static int runAddExp(CommandSourceStack source, ServerPlayer target, int amount) {
        LevelingService.addExp(target, amount);
        log(source, "added exp " + amount + " to " + target.getGameProfile().getName());
        return 1;
    }

    private static int runSetJob(CommandSourceStack source, ServerPlayer target, String jobId) {
        CharacterData character = character(target);
        String before = character.jobId();
        character.setJobId(jobId);
        saveAndSync(target);
        log(source, "changed player " + target.getGameProfile().getName() + " job " + before + " -> " + jobId);
        return 1;
    }

    private static int runAddStat(CommandSourceStack source, ServerPlayer target, String statId, int amount) {
        character(target).baseStats().add(statId, amount);
        saveAndSync(target);
        log(source, "changed player " + target.getGameProfile().getName() + " stat " + statId + " +=" + amount);
        return 1;
    }

    private static int runUnlockSkill(CommandSourceStack source, ServerPlayer target, String skillId) {
        character(target).unlockedSkills().add(skillId);
        saveAndSync(target);
        log(source, "unlocked skill " + skillId + " for " + target.getGameProfile().getName());
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

    private static String normalize(String id) {
        if (id.contains(":")) {
            return id;
        }
        return "tctorpg:" + id;
    }
}
