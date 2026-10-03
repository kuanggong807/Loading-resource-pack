package cn.autoforged.hometown_reborn_1791033413.network;

import cn.autoforged.hometown_reborn_1791033413.HometownReborn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 单一 SimpleChannel。当前只承载「服务端 -> 客户端」的材质包控制包：
 * 服务端指令校验通过后，通知目标客户端在本地 resourcepacks 文件夹加载/卸载材质包。
 */
public final class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";
    private static int nextId = 0;
    private static SimpleChannel instance;

    private ModNetwork() {
    }

    public static void register() {
        SimpleChannel channel = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(HometownReborn.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals);

        channel.messageBuilder(ResourcePackControlPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ResourcePackControlPacket::encode)
                .decoder(ResourcePackControlPacket::new)
                .consumerMainThread(ResourcePackControlPacket::handle)
                .add();

        instance = channel;
    }

    public static void sendToPlayer(ResourcePackControlPacket message, ServerPlayer player) {
        if (instance == null || player == null || player.connection == null) {
            return;
        }
        instance.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
