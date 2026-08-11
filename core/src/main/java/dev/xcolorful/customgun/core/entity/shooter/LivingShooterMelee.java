/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.entity.shooter;

import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.gun.attack.IGunAttackRuntime;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.gun.GunDataAccessor;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.item.gun.MeleeType;
import dev.xcolorful.customgun.core.network.message.event.ServerMessageGunMelee;
import dev.xcolorful.customgun.core.resource.data.data.attachment._MeleeModifierData;
import dev.xcolorful.customgun.core.resource.data.data.gun.melee._DefaultMeleeData;
import dev.xcolorful.customgun.core.util.SendUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class LivingShooterMelee extends LivingShooterAspect {

    private final LivingShooterDraw drawGun;

    public LivingShooterMelee(LivingEntity livingShooter, ShooterProperty shooterProperty,
                              LivingShooterDraw drawGun) {
        super(livingShooter, shooterProperty);
        this.drawGun = drawGun;
    }

    public void prepareMelee() {
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) return;
        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        if ( // 2.1 检查状态锁
                // 检查是否在切枪
                drawGun.getDrawCooldown() > 0
                // 检查是否在拉栓
                || this.shooterProperty.isBolting
        ) return;
        long currentTimeMillis = System.currentTimeMillis();
        if ( // 2.2 检查状态
                // 近战冷却
                _getMeleeCooldownMs(currentTimeMillis, iGun, gunItem) > 0
        ) return;

        @Nullable IGunAttackRuntime.MeleePreparation meleePreparation;
        { // 3. IGunRuntime操作结果 -> Shooter状态
            meleePreparation = iGun.prepareMelee(iGun, gunItem, ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter), this.livingShooter);
            if (meleePreparation == null) {
                return;
            }
            this.shooterProperty.meleePreparationTick = meleePreparation.prepareTick();
            this.shooterProperty.preparingMeleeType = meleePreparation.meleeType();
        }

        this.shooterProperty.meleeTimestamp = currentTimeMillis;

        SendUtils.sendMessageToTrackingEntity(this.livingShooter,
                new ServerMessageGunMelee(this.livingShooter.getId(), gunItem));
    }

    public long getMeleeCooldownMs(long currentTimeMillis) {
        if (this.shooterProperty.currentGunItem == null) return -1;
        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return -1;

        return _getMeleeCooldownMs(currentTimeMillis, iGun, gunItem);
    }
    public long _getMeleeCooldownMs(long currentTimeMillis, IGun iGun, ItemStack gunItem) {
        @Nullable MeleeType meleeType = iGun.getGunMeleeType(gunItem);
        if (meleeType == null) return -1;

        @Nullable _DefaultMeleeData defaultMeleeData = GunDataAccessor._getGunDefaultMeleeData(iGun, gunItem);
        if (defaultMeleeData == null) return -1;

        @Nullable _MeleeModifierData meleeModifierData = null;
        switch (meleeType) {
            case BAYONET, STOCK -> {
                AttachmentCategory attachmentCategory = meleeType == MeleeType.BAYONET ? AttachmentCategory.MUZZLE : AttachmentCategory.STOCK;
                meleeModifierData = GunDataAccessor._getAttachmentMeleeModifierData(iGun, gunItem, attachmentCategory);

                // 指定配件，但是获取不到，提前返回
                // 默认的IGun返回meleeType说明有数据，但不保证重载
                if (meleeModifierData == null) return -1;
            }
            case PUSH -> {
            }
        }

        float cooldownSeconds = defaultMeleeData.getBaseCooldown() + (meleeModifierData != null ? meleeModifierData.getExtraCooldown() : 0);
        long coolDownMs = (long) (cooldownSeconds * 1000) - (currentTimeMillis - this.shooterProperty.meleeTimestamp);
        // 给 5 ms 的窗口时间，以平衡延迟
        return coolDownMs < WINDOW_TIME_MS ? 0 : coolDownMs;
    }

    public void scheduleTickMelee() {
        if (this.shooterProperty.meleePreparationTick < 0) return;

        if (this.shooterProperty.meleePreparationTick-- > 0) {
            // 暂无操作
        } else {
            if (this.shooterProperty.currentGunItem == null) return;

            ItemStack gunItem = this.shooterProperty.currentGunItem.get();
            @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
            if (iGun == null) return;

            @Nullable MeleeType preparingMeleeType = this.shooterProperty.preparingMeleeType;
            if (preparingMeleeType == null) return;

            { // 3. IGunRuntime操作结果 -> Shooter状态
                ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
                iGun.melee(this.shooterProperty, iGun, gunItem, iLivingShooter, this.livingShooter, preparingMeleeType);
            }
            this.shooterProperty.preparingMeleeType = null;
        }
    }
}
