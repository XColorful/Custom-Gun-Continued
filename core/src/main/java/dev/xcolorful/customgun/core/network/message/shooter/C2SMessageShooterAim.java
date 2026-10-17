package dev.xcolorful.customgun.core.network.message.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public record C2SMessageShooterAim(boolean isAim)
        implements IMessage<C2SMessageShooterAim> {

    @Override
    public void encode(FriendlyByteBuf buffer) {
        var message = this;
        buffer.writeBoolean(message.isAim);
    }

    public static C2SMessageShooterAim decode(FriendlyByteBuf buffer) {
        return new C2SMessageShooterAim(buffer.readBoolean());
    }

    @Override
    public void handle(Consumer<Runnable> handler, NetworkContext context) {
        var message = this;
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer player)) {
                    return;
                }

                ILivingShooterGetter.cgc$fromLivingEntity(player).cgc$aim(message.isAim);
            });
        }
    }
}