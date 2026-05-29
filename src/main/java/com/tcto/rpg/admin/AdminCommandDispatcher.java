package com.tcto.rpg.admin;

public final class AdminCommandDispatcher {
    private AdminCommandDispatcher() {
    }

    public static boolean canExecute(AdminPermissionLevel actorLevel, AdminPermissionLevel requiredLevel) {
        return actorLevel.includes(requiredLevel);
    }
}
