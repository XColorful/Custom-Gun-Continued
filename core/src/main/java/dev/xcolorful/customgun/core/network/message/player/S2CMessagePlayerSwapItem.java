package dev.xcolorful.customgun.core.network.message.player;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message.player._S2CMessagePlayerSwapItem;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;

public class S2CMessagePlayerSwapItem implements IMessage<S2CMessagePlayerSwapItem> {

    public S2CMessagePlayerSwapItem() {
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
    }

    public static S2CMessagePlayerSwapItem decode(FriendlyByteBuf buffer) {
        return new S2CMessagePlayerSwapItem();
    }

    @Override
    public void handle(Consumer<Runnable> handler, NetworkContext context) {
        var message = this;
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> _S2CMessagePlayerSwapItem.doClientEvent(message));
        }
    }
}