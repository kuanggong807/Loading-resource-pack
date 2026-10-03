package cn.autoforged.hometown_reborn_1791033413.network;

import cn.autoforged.hometown_reborn_1791033413.client.ClientResourcePackHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 服务端 -> 客户端：指示目标客户端加载(load=true)或卸载(load=false)本地
 * resourcepacks 文件夹中名为 packName 的材质包。
 *
 * 客户端处理逻辑通过 DistExecutor 隔离，物理服务端不会加载 ClientResourcePackHandler。
 */
public class ResourcePackControlPacket {

    private final String packName;
    private final boolean load;

    public ResourcePackControlPacket(String packName, boolean load) {
        this.packName = packName;
        this.load = load;
    }

    public ResourcePackControlPacket(FriendlyByteBuf buffer) {
        this.packName = buffer.readUtf();
        this.load = buffer.readBoolean();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.packName);
        buffer.writeBoolean(this.load);
    }

    public static void handle(ResourcePackControlPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientResourcePackHandler.handle(message.packName, message.load));
    }
}
