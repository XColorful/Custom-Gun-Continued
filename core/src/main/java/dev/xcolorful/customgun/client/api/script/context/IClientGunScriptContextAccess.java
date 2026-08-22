/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.script.context;

import dev.xcolorful.customgun.client.api.entity.ILocalShooter;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.script.context._LuaNbtAccessor;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IClientGunScriptContextAccess {

    @Nullable ILocalShooter getILocalShooter();
    @Nullable LocalPlayer getLocalShooter();

    @Nullable Entity getCameraShooter();

    @Nullable IGun getIGun();
    ItemStack getGunItem();
    @Nullable GunIndexInstance getGunIndexInstance();
    @Nullable GunDisplayInstance getGunDisplayInstance();

    float getWalkDistAnchor();
    float getPartialTicks();

    @Deprecated
    @Nullable _LuaNbtAccessor getNbt();

    void setWalkDistAnchor(float value);
}
