package dev.xcolorful.customgun.core.network.message.shooter;

import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.network.MessageDirection;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record C2SMessageShooterProne(boolean isProne)
        implements IMessage<C2SMessageShooterProne> {

    @Override
    public void encode(FriendlyByteBuf buffer) {
        var message = this;
        buffer.writeBoolean(message.isProne);
    }

    public static C2SMessageShooterProne decode(FriendlyByteBuf buffer) {
        return new C2SMessageShooterProne(buffer.readBoolean());
    }

    @Override
    public void handle(Consumer<Runnable> handler, NetworkContext context) {
        var message = this;
        if (context.direction() == MessageDirection.CLIENT_TO_SERVER) {
            handler.accept(() -> {
                @Nullable LivingEntity livingShooter = context.sender();
                if (livingShooter == null) return;

                ILivingShooterGetter.cgc$fromLivingEntity(livingShooter).cgc$prone(message.isProne);
            });
        }
    }
}