package com.tcto.rpg.admin;

import net.minecraft.server.level.ServerPlayer;

public final class AdminManager {
    private AdminManager() {
    }

    public static void reloadPermissions() {
        AdminPermissionService.load();
    }

    public static AdminSession createSession(ServerPlayer player) {
        return new AdminSession(player.getUUID(), player.getGameProfile().getName(), AdminPermissionService.levelOf(player));
    }
}
