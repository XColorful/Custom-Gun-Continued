/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.gun.IGunRuntime;
import dev.xcolorful.customgun.core.api.item.gun.BoltType;
import dev.xcolorful.customgun.core.api.item.gun.IGunDataAccess;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.minecraft.capability.IInventoryCapability;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IGun extends IGunRuntime, IAnimationItem,
        IGunDataAccess, IGunGetter,
        IPojoItem {

    // --------IGunAmmoDataAccess--------

    @Override
    default int consumeAmmoOnce(LivingEntity livingEntity, ItemStack gunItem, BoltType boltType) {
        if (PlannedRefactor.ON_CONSUME_AMMO) return 0;

        // 消耗子弹
        int consumedAmmo;
        final int DEFAULT_CONSUME_AMMO = 1; // 连续消耗子弹应改用burst模式
        if (this.useInventoryAmmo(gunItem)) {
            // 背包直读
            ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(livingEntity);
            if (!iLivingShooter.cgc$needCheckAmmo()) {
                // 不需要检查子弹
                consumedAmmo = DEFAULT_CONSUME_AMMO;
            } else if (this.useDummyAmmo(gunItem)) {
                // 虚拟备弹
                consumedAmmo = this.findAndExtractDummyAmmo(this, gunItem, DEFAULT_CONSUME_AMMO);
            } else {
                // 背包物品
                @Nullable IInventoryCapability inventoryCapability = CustomGun.getCapabilityProvider().getItemHandler(livingEntity, null);
                consumedAmmo = inventoryCapability == null ? DEFAULT_CONSUME_AMMO
                        : this.findAndExtractInventoryAmmo(inventoryCapability, this, gunItem, DEFAULT_CONSUME_AMMO);
            }
        } else if (boltType.useBarrelAmmo()) {
            // 消耗枪管子弹
            int barrelAmmo = this.getBarrelAmmoCount(gunItem);
            if (barrelAmmo > 0) {
                consumedAmmo = DEFAULT_CONSUME_AMMO;
                this.setBarrelAmmoCount(gunItem, barrelAmmo - consumedAmmo);
            } else {
                consumedAmmo = 0;
            }
        } else {
            // 消耗弹匣子弹
            consumedAmmo = consumeMagAmmoOnce(gunItem);
        }

        // 射击后自动上膛的boltType
        if (boltType.useBarrelAmmo() && boltType.autoBoltBarrelAmmo()) {
            this.boltBarrelAmmo(livingEntity, gunItem, boltType);
        }

        return consumedAmmo;
    }

    @Override
    default void unloadAmmo(LivingEntity livingEntity, ItemStack gunItem) {
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(livingEntity);
        this.retrieveAmmoFromGun(this, gunItem, iLivingShooter, livingEntity);
    }

    @Override
    default int boltBarrelAmmo(LivingEntity livingEntity, ItemStack gunItem, BoltType boltType) {
        if (!boltType.useBarrelAmmo()) return 0; // open bolt 不需要上膛

        if (this.hasBarrelAmmo(gunItem)) return 0; // 枪管已经有子弹

        int maxBarrelAmmo = PlannedRefactor.GET_MAX_BARREL_AMMO;
        if (maxBarrelAmmo <= 0) return 0;

        int consumedAmmo;
        if (this.useInventoryAmmo(gunItem)) {
            // 背包直读
            ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(livingEntity);
            if (!iLivingShooter.cgc$needCheckAmmo()) {
                // 不需要检查子弹
                consumedAmmo = maxBarrelAmmo;
            } else if (this.useDummyAmmo(gunItem)) {
                // 虚拟备弹
                consumedAmmo = this.findAndExtractDummyAmmo(this, gunItem, maxBarrelAmmo);
            } else {
                // 背包物品
                @Nullable IInventoryCapability inventoryCapability = CustomGun.getCapabilityProvider().getItemHandler(livingEntity, null);
                consumedAmmo = inventoryCapability == null ? maxBarrelAmmo
                        : this.findAndExtractInventoryAmmo(inventoryCapability, this, gunItem, maxBarrelAmmo);
            }
        } else {
            // 弹匣供弹
            consumedAmmo = consumeMagAmmoOnce(gunItem);
        }

        if (consumedAmmo <= 0) return 0;

        this.setBarrelAmmoCount(gunItem, consumedAmmo);
        return consumedAmmo;
    }

    // --------IPojoItem--------

    @Override
    default @NotNull Identifier getPojoLocation(ItemStack gunItem) {
        return this.getGunLocation(gunItem);
    }
    @Override
    default void setPojoLocation(ItemStack gunItem, Identifier gunLocation) {
        this.setGunLocation(gunItem, gunLocation);
    }
}
