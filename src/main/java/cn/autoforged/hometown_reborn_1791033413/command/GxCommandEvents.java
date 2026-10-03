package cn.autoforged.hometown_reborn_1791033413.command;

import cn.autoforged.hometown_reborn_1791033413.HometownReborn;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 在 FORGE bus 上监听指令注册事件（默认 bus = FORGE）。 */
@Mod.EventBusSubscriber(modid = HometownReborn.MOD_ID)
public final class GxCommandEvents {

    private GxCommandEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        GxCommand.register(event.getDispatcher());
    }
}
