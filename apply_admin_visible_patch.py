from pathlib import Path

ROOT = Path.cwd()
CMD = ROOT / "src/main/java/com/tcto/rpg/common/command/TctorpgCommand.java"

if not CMD.exists():
    raise SystemExit(f"파일을 찾을 수 없습니다: {CMD}")

text = CMD.read_text(encoding="utf-8")

# ---------------------------------------------------------------------
# 1. import 추가
# ---------------------------------------------------------------------
old_import = """import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
"""

new_import = """import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.nio.file.Path;
"""

if old_import in text:
    text = text.replace(old_import, new_import, 1)
    print("[PATCH] ItemStack/Items import 추가")
else:
    print("[SKIP] import 패턴을 찾지 못했습니다. 이미 수정됐을 수 있습니다.")

# ---------------------------------------------------------------------
# 2. /tctorpg admin 메시지 개선 + /tctorpg ui 별칭 추가
# ---------------------------------------------------------------------
old_admin_block = """            .then(Commands.literal("admin")
                .requires(source -> can(source, AdminPermissionLevel.ADMIN))
                .executes(ctx -> {
                    log(ctx.getSource(), "opened admin screen");
                    ctx.getSource().sendSuccess(() -> Component.literal("TCToRPG admin screen requested. Press O to open the local admin UI."), false);
                    return 1;
                }))
"""

new_admin_block = """            .then(Commands.literal("admin")
                .requires(source -> can(source, AdminPermissionLevel.ADMIN))
                .executes(ctx -> runAdminHint(ctx.getSource())))
            .then(Commands.literal("ui")
                .requires(source -> can(source, AdminPermissionLevel.ADMIN))
                .executes(ctx -> runAdminHint(ctx.getSource())))
"""

if old_admin_block in text:
    text = text.replace(old_admin_block, new_admin_block, 1)
    print("[PATCH] /tctorpg admin 개선 및 /tctorpg ui 추가")
else:
    print("[SKIP] admin block 패턴을 찾지 못했습니다. 이미 수정됐을 수 있습니다.")

# ---------------------------------------------------------------------
# 3. giveitem stub을 실제 임시 아이템 지급으로 변경
# ---------------------------------------------------------------------
old_giveitem = """                .then(Commands.literal("giveitem")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("itemId", StringArgumentType.word())
                            .executes(ctx -> runLoggedStub(ctx.getSource(), "gave item " + StringArgumentType.getString(ctx, "itemId") + " to " + EntityArgument.getPlayer(ctx, "player").getGameProfile().getName()))))))
"""

new_giveitem = """                .then(Commands.literal("giveitem")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("itemId", StringArgumentType.word())
                            .executes(ctx -> runGiveItem(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), normalize(StringArgumentType.getString(ctx, "itemId"))))))))
"""

if old_giveitem in text:
    text = text.replace(old_giveitem, new_giveitem, 1)
    print("[PATCH] giveitem 실제 지급 함수 연결")
else:
    print("[SKIP] giveitem block 패턴을 찾지 못했습니다. 이미 수정됐을 수 있습니다.")

# ---------------------------------------------------------------------
# 4. runHelp 문구 개선
# ---------------------------------------------------------------------
old_help_1 = """        source.sendSuccess(() -> Component.literal("/tctorpg admin - request admin UI, use O to open local UI"), false);
"""

new_help_1 = """        source.sendSuccess(() -> Component.literal("/tctorpg admin 또는 /tctorpg ui - 관리자 UI 안내. 실제 화면은 기본 O 키로 열립니다."), false);
"""

if old_help_1 in text:
    text = text.replace(old_help_1, new_help_1, 1)
    print("[PATCH] help admin 문구 개선")
else:
    print("[SKIP] help 문구 패턴을 찾지 못했습니다.")

# ---------------------------------------------------------------------
# 5. runAdminHint 함수 추가
# ---------------------------------------------------------------------
marker = """    private static int runValidate(CommandSourceStack source, String category) {
"""

insert = """    private static int runAdminHint(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§6[TCToRPG 관리자 UI]"), false);
        source.sendSuccess(() -> Component.literal("§e현재 빌드는 클라이언트 로컬 키바인드 방식입니다."), false);
        source.sendSuccess(() -> Component.literal("§f관리자 화면을 열려면 기본 키 §bO§f 를 누르세요."), false);
        source.sendSuccess(() -> Component.literal("§7키 설정: 설정 → 조작 → TCToRPG → screen_admin"), false);
        source.sendSuccess(() -> Component.literal("§7서버 명령만으로 클라이언트 Screen을 직접 여는 패킷은 아직 연결되지 않았습니다."), false);
        log(source, "requested admin ui hint");
        return 1;
    }

"""

if marker in text and "private static int runAdminHint(" not in text:
    text = text.replace(marker, insert + marker, 1)
    print("[PATCH] runAdminHint 추가")
else:
    print("[SKIP] runAdminHint 이미 존재하거나 marker 없음")

# ---------------------------------------------------------------------
# 6. runGiveItem 함수 추가
# ---------------------------------------------------------------------
marker2 = """    private static int runLoggedStub(CommandSourceStack source, String action) {
"""

insert2 = """    private static int runGiveItem(CommandSourceStack source, ServerPlayer target, String itemId) {
        // 1차 임시 구현:
        // 커스텀 아이템 레지스트리가 아직 실제 ItemStack과 연결되지 않았기 때문에
        // 운영자가 변화를 확인할 수 있도록 바닐라 철검을 지급한다.
        // 추후 itemId -> 실제 TCToRPG 커스텀 ItemStack 매핑으로 교체한다.
        ItemStack stack = new ItemStack(Items.IRON_SWORD);

        String plainId = itemId.contains(":") ? itemId.substring(itemId.indexOf(':') + 1) : itemId;
        stack.setHoverName(Component.literal("TCToRPG Item: " + plainId));

        boolean added = target.getInventory().add(stack);
        if (!added) {
            target.drop(stack, false);
        }

        saveAndSync(target);

        source.sendSuccess(() -> Component.literal("TCToRPG: 실제 인벤토리에 임시 아이템을 지급했습니다: " + plainId + " -> " + target.getGameProfile().getName()), true);
        target.sendSystemMessage(Component.literal("§a[TCToRPG] 아이템 지급: §f" + plainId));

        log(source, "gave visible item " + itemId + " to " + target.getGameProfile().getName());
        return 1;
    }

"""

if marker2 in text and "private static int runGiveItem(" not in text:
    text = text.replace(marker2, insert2 + marker2, 1)
    print("[PATCH] runGiveItem 추가")
else:
    print("[SKIP] runGiveItem 이미 존재하거나 marker 없음")

CMD.write_text(text, encoding="utf-8")
print("[DONE] TctorpgCommand.java 패치 완료")
