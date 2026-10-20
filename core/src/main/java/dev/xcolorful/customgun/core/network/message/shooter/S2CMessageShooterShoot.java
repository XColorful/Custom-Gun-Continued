package dev.xcolorful.customgun.core.network.message.shooter;

import dev.xcolorful.customgun.client.network.message.shooter._S2CMessageShooterShoot;
import dev.xcolorful.customgun.core.api.network.MessageDirection;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.util.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public record S2CMessageShooterShoot(int shooterId,
                                     ItemStack gunItem)
        implements IMessage<S2CMessageShooterShoot> {

    @Override
    public void encode(FriendlyByteBuf buffer) {
        var message = this;
        buffer.writeVarInt(message.shooterId);
        NetworkUtils.writeItem(buffer, message.gunItem);
    }

    public static S2CMessageShooterShoot decode(FriendlyByteBuf buffer) {
        int shooterId = buffer.readVarInt();
        ItemStack gunItem = NetworkUtils.readItem(buffer);
        return new S2CMessageShooterShoot(shooterId, gunItem);
    }

    @Override
    public void handle(Consumer<Runnable> handler, NetworkContext context) {
        var message = this;
        if (context.direction() == MessageDirection.SERVER_TO_CLIENT) {
            handler.accept(() -> _S2CMessageShooterShoot.doClientEvent(message));
        }
    }
}