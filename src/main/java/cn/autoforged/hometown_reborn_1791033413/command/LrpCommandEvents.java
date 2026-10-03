package cn.autoforged.hometown_reborn_1791033413.command;

import cn.autoforged.hometown_reborn_1791033413.HometownReborn;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 在 FORGE bus 上注册「Loading resource pack」模组的 /lrp 指令（默认 bus = FORGE）。 */
@Mod.EventBusSubscriber(modid = HometownReborn.MOD_ID)
public final class LrpCommandEvents {

    private LrpCommandEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LrpCommand.register(event.getDispatcher());
    }
}
