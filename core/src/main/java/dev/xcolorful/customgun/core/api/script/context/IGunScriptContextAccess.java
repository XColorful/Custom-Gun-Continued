/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.script.context;

import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IGunScriptContextAccess {

    @Nullable ILivingShooter getILivingShooter();
    @Nullable LivingEntity getLivingShooter();

    IGun getIGun();
    ItemStack getGunItem();
    @Nullable GunIndexInstance getGunIndexInstance();

    @Deprecated
    @Nullable _LuaNbtAccessor getNbt();
}
