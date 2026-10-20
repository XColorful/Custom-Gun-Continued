package dev.xcolorful.customgun.core.network.message.shooter;

import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.network.MessageDirection;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public class C2SMessageShooterReload_cancel implements IMessage<C2SMessageShooterReload_cancel> {

    public C2SMessageShooterReload_cancel() {
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
    }

    public static C2SMessageShooterReload_cancel decode(FriendlyByteBuf buffer) {
        return new C2SMessageShooterReload_cancel();
    }

    @Override
    public void handle(Consumer<Runnable> handler, NetworkContext context) {
        if (context.direction() == MessageDirection.CLIENT_TO_SERVER) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer player)) {
                    return;
                }

                ILivingShooterGetter.cgc$fromLivingEntity(player).cgc$cancelReload();
            });
        }
    }
}