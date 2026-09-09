/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message.shooter._S2CMessageShooterRefit_refresh;
import dev.xcolorful.customgun.core.api.common.McSide;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;

public class S2CMessageShooterRefit_refresh implements IMessage<S2CMessageShooterRefit_refresh> {

    public S2CMessageShooterRefit_refresh() {
    }

    @Override
    public void encode(S2CMessageShooterRefit_refresh message, FriendlyByteBuf buffer) {
    }

    public static S2CMessageShooterRefit_refresh decode(FriendlyByteBuf buffer) {
        return new S2CMessageShooterRefit_refresh();
    }

    @Override
    public void handle(S2CMessageShooterRefit_refresh message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> CustomGun.getSideExecutor().executeOn(McSide.CLIENT, () -> _S2CMessageShooterRefit_refresh::updateScreen
            ));
        }
    }
}