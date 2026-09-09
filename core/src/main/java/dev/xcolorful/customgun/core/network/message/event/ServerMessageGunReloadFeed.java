package dev.xcolorful.customgun.core.network.message.event;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message.event._ServerMessageGunReloadFeed;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.util.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public record ServerMessageGunReloadFeed(int shooterId,
                                         ItemStack gunItem)
        implements IMessage<ServerMessageGunReloadFeed> {

    @Override
    public void encode(ServerMessageGunReloadFeed message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.shooterId);
        NetworkUtils.writeItem(buffer, message.gunItem);
    }

    public static ServerMessageGunReloadFeed decode(FriendlyByteBuf buffer) {
        int shooterId = buffer.readVarInt();
        ItemStack gunItem = NetworkUtils.readItem(buffer);
        return new ServerMessageGunReloadFeed(shooterId, gunItem);
    }

    @Override
    public void handle(ServerMessageGunReloadFeed message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> _ServerMessageGunReloadFeed.doClientEvent(message));
        }
    }
}
