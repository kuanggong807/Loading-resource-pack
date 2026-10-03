package cn.autoforged.hometown_reborn_1791033413.client;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端专用：根据服务端指令在本地 resourcepacks 文件夹里启用/停用材质包。
 * pack id 由 FolderRepositorySource 生成为 "file/<文件名>"（zip 含 .zip 后缀）。
 */
public final class ClientResourcePackHandler {

    private ClientResourcePackHandler() {
    }

    public static void handle(String packName, boolean load) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        PackRepository repository = minecraft.getResourcePackRepository();
        repository.reload();

        List<String> selected = new ArrayList<>(repository.getSelectedIds());
        String matched = findPackId(repository, packName);

        if (load) {
            if (matched != null && !selected.contains(matched)) {
                selected.add(matched);
                repository.setSelected(selected);
                minecraft.reloadResourcePacks();
            }
            return;
        }

        boolean changed;
        if (matched != null) {
            changed = selected.remove(matched);
        } else {
            changed = selected.removeIf(id -> matches(id, packName));
        }
        if (changed) {
            repository.setSelected(selected);
            minecraft.reloadResourcePacks();
        }
    }

    private static String findPackId(PackRepository repository, String packName) {
        for (String id : repository.getAvailableIds()) {
            if (matches(id, packName)) {
                return id;
            }
        }
        return null;
    }

    private static boolean matches(String id, String packName) {
        if (id == null || packName == null || packName.isBlank()) {
            return false;
        }
        String base = id;
        int slash = base.lastIndexOf('/');
        if (slash >= 0) {
            base = base.substring(slash + 1);
        }
        return base.equals(packName)
                || base.equals(packName + ".zip")
                || (packName.endsWith(".zip") && base.equals(packName));
    }
}
