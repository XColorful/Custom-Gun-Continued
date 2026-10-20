package dev.xcolorful.customgun.core.network.message.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message.shooter._S2CMessageShooterRefit_refresh;
import dev.xcolorful.customgun.core.api.common.McSide;
import dev.xcolorful.customgun.core.api.network.MessageDirection;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;

public class S2CMessageShooterRefit_refresh implements IMessage<S2CMessageShooterRefit_refresh> {

    public S2CMessageShooterRefit_refresh() {
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
    }

    public static S2CMessageShooterRefit_refresh decode(FriendlyByteBuf buffer) {
        return new S2CMessageShooterRefit_refresh();
    }

    @Override
    public void handle(Consumer<Runnable> handler, NetworkContext context) {
        if (context.direction() == MessageDirection.SERVER_TO_CLIENT) {
            handler.accept(() -> CustomGun.getSideExecutor().executeOn(McSide.CLIENT, () -> _S2CMessageShooterRefit_refresh::updateScreen
            ));
        }
    }
}