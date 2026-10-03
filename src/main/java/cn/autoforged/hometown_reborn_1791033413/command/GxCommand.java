package cn.autoforged.hometown_reborn_1791033413.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * /gx 总指令体系（原「材质包加载」功能已拆分至独立模组「Loading resource pack」）。
 *
 * 设计要点：
 *  - 根指令 /gx：无参数时以黄色字体提示材质包指令已迁移至 /lrp。
 *  - 权限等级 2；指令不依赖玩家执行，命令方块可用。
 */
public final class GxCommand {

    public static final int PERMISSION_LEVEL = 2;

    private GxCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("gx")
                        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                        .executes(GxCommand::showRootUsage)
        );
    }

    private static int showRootUsage(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> usage(
                "材质包指令已拆分至独立模组「Loading resource pack」，请使用 /lrp",
                "/lrp resourcepack <目标> set <材质包名>"), false);
        source.sendSuccess(() -> usage(
                "卸载材质包，省略材质包名则卸载全部已加载材质包",
                "/lrp resourcepack <目标> remove [材质包名]"), false);
        return 1;
    }

    private static Component usage(String description, String command) {
        return Component.literal(description + "  -  用法：" + command).withStyle(ChatFormatting.YELLOW);
    }
}
