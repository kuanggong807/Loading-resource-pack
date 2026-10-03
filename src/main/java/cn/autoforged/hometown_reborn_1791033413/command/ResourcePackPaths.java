package cn.autoforged.hometown_reborn_1791033413.command;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 材质包路径安全校验与存在性检查。
 * 严格限制在服务器运行目录下的 resourcepacks 文件夹内，
 * 拒绝任何包含路径分隔符或上跳（..）的名称，防止越权访问其他路径。
 */
public final class ResourcePackPaths {

    private ResourcePackPaths() {
    }

    /** 资源包根目录：<服务器运行目录>/resourcepacks */
    public static Path baseDirectory(MinecraftServer server) {
        return server.getFile("resourcepacks").toPath().toAbsolutePath().normalize();
    }

    /** 仅接受 resourcepacks 目录内的“简单名称”，禁止分隔符与上跳。 */
    public static boolean isSafeName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        if (name.indexOf('/') >= 0 || name.indexOf('\\') >= 0) {
            return false;
        }
        if (name.contains("..")) {
            return false;
        }
        return true;
    }

    /** 校验目标是 resourcepacks 内的 .zip 文件或含 pack.mcmeta 的文件夹。 */
    public static boolean exists(MinecraftServer server, String name) {
        if (!isSafeName(name)) {
            return false;
        }
        Path base = baseDirectory(server);

        Path zip = base.resolve(name.endsWith(".zip") ? name : name + ".zip").normalize();
        if (zip.startsWith(base) && Files.isRegularFile(zip)) {
            return true;
        }

        Path directory = base.resolve(name).normalize();
        return directory.startsWith(base)
                && Files.isDirectory(directory)
                && Files.isRegularFile(directory.resolve("pack.mcmeta"));
    }
}
