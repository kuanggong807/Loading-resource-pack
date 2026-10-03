package cn.autoforged.hometown_reborn_1791033413.command;

import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 记录本模组通过 /gx 指令为各玩家加载的材质包（服务端权威状态，仅服务端线程访问）。
 * 用于 remove 不带材质包参数时精确卸载本模组管理过的内容，
 * 避免误伤玩家自行启用的其他资源包。
 */
public final class ResourcePackManager {

    private static final Map<UUID, Set<String>> LOADED = new HashMap<>();

    private ResourcePackManager() {
    }

    public static void markLoaded(ServerPlayer player, String packName) {
        LOADED.computeIfAbsent(player.getUUID(), key -> new LinkedHashSet<>()).add(packName);
    }

    public static boolean markUnloaded(ServerPlayer player, String packName) {
        Set<String> loaded = LOADED.get(player.getUUID());
        return loaded != null && loaded.remove(packName);
    }

    public static Set<String> clearLoaded(ServerPlayer player) {
        Set<String> loaded = LOADED.remove(player.getUUID());
        return loaded == null ? Collections.emptySet() : loaded;
    }
}
