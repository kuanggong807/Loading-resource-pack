package cn.autoforged.hometown_reborn_1791033413.command;

import cn.autoforged.hometown_reborn_1791033413.network.ModNetwork;
import cn.autoforged.hometown_reborn_1791033413.network.ResourcePackControlPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Set;

/**
 * 「Loading resource pack」独立模组的指令入口：/lrp resourcepack 目标选择器 set/remove 材质包名。
 *
 * 设计要点（对应需求）：
 *  - 根指令 /lrp：无参数时列出 resourcepack 二级用法，每条末尾追加该指令用法，黄色字体。
 *  - /lrp resourcepack：无参数时列出 set / remove 用法。
 *  - /lrp resourcepack <目标> set <材质包名>：为指定目标加载 resourcepacks 内材质包。
 *  - /lrp resourcepack <目标> remove [材质包名]：卸载指定/全部已加载材质包。
 *  - 权限等级 2；指令不依赖玩家执行，命令方块可用（目标用实体选择器）。
 *  - 材质包来源严格限定在运行目录的 resourcepacks 文件夹内。
 *
 * 本功能由原 /gx resourcepack 拆分而来，后端加载逻辑（ResourcePackPaths /
 * ResourcePackManager / ModNetwork / ResourcePackControlPacket）复用不变。
 */
public final class LrpCommand {

    public static final int PERMISSION_LEVEL = 2;

    private LrpCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("lrp")
                        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                        .executes(LrpCommand::showRootUsage)
                        .then(Commands.literal("resourcepack")
                                .executes(LrpCommand::showResourcePackUsage)
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("pack", StringArgumentType.string())
                                                        .executes(LrpCommand::setResourcePack)))
                                        .then(Commands.literal("remove")
                                                .executes(context -> removeResourcePack(context, false))
                                                .then(Commands.argument("pack", StringArgumentType.string())
                                                        .executes(context -> removeResourcePack(context, true)))))
                        )
        );
    }

    private static int showRootUsage(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> usage(
                "为指定目标加载 resourcepacks 文件夹中的材质包（目标：玩家选择器；材质包名：resourcepacks 内 .zip 文件名或文件夹名）",
                "/lrp resourcepack <目标> set <材质包名>"), false);
        source.sendSuccess(() -> usage(
                "为指定目标卸载材质包，省略材质包名则卸载本指令为其加载的全部材质包",
                "/lrp resourcepack <目标> remove [材质包名]"), false);
        return 1;
    }

    private static int showResourcePackUsage(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> usage(
                "加载材质包（目标：玩家选择器；材质包名：resourcepacks 内 .zip 文件名或文件夹名）",
                "/lrp resourcepack <目标> set <材质包名>"), false);
        source.sendSuccess(() -> usage(
                "卸载材质包，省略材质包名则卸载全部已加载材质包",
                "/lrp resourcepack <目标> remove [材质包名]"), false);
        return 1;
    }

    private static int setResourcePack(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
        String packName = StringArgumentType.getString(context, "pack");

        if (!ResourcePackPaths.isSafeName(packName)) {
            source.sendFailure(failure("加载失败：材质包名称非法，仅允许 resourcepacks 文件夹内的名称，禁止包含路径分隔符或上级目录"));
            return 0;
        }
        if (!ResourcePackPaths.exists(source.getServer(), packName)) {
            source.sendFailure(failure("加载失败：resourcepacks 文件夹中不存在材质包 \"" + packName + "\""));
            return 0;
        }

        for (ServerPlayer player : targets) {
            ResourcePackManager.markLoaded(player, packName);
            ModNetwork.sendToPlayer(new ResourcePackControlPacket(packName, true), player);
        }

        int count = targets.size();
        source.sendSuccess(() -> success("已为 " + count + " 个目标加载材质包 \"" + packName + "\""), true);
        return count;
    }

    private static int removeResourcePack(CommandContext<CommandSourceStack> context, boolean specificPack) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
        String packName = specificPack ? StringArgumentType.getString(context, "pack") : null;

        if (packName != null && !ResourcePackPaths.isSafeName(packName)) {
            source.sendFailure(failure("卸载失败：材质包名称非法，仅允许 resourcepacks 文件夹内的名称"));
            return 0;
        }

        int affected = 0;
        for (ServerPlayer player : targets) {
            if (packName != null) {
                if (ResourcePackManager.markUnloaded(player, packName)) {
                    ModNetwork.sendToPlayer(new ResourcePackControlPacket(packName, false), player);
                    affected++;
                }
            } else {
                Set<String> loaded = ResourcePackManager.clearLoaded(player);
                if (!loaded.isEmpty()) {
                    for (String name : loaded) {
                        ModNetwork.sendToPlayer(new ResourcePackControlPacket(name, false), player);
                    }
                    affected++;
                }
            }
        }

        if (affected == 0) {
            String reason = packName == null
                    ? "卸载失败：目标未通过 /lrp 加载任何材质包"
                    : "卸载失败：目标未加载材质包 \"" + packName + "\"";
            source.sendFailure(failure(reason));
            return 0;
        }

        String message = packName == null
                ? "已为 " + affected + " 个目标卸载全部已加载材质包"
                : "已为 " + affected + " 个目标卸载材质包 \"" + packName + "\"";
        source.sendSuccess(() -> success(message), true);
        return affected;
    }

    private static Component usage(String description, String command) {
        return Component.literal(description + "  -  用法：" + command).withStyle(ChatFormatting.YELLOW);
    }

    private static Component success(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GREEN);
    }

    private static Component failure(String text) {
        return Component.literal(text).withStyle(ChatFormatting.RED);
    }
}
