/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.event.shooter.ShooterMeleeEvent;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.attachment.AttachmentCategory;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.network.message.event.ServerMessageGunMelee;
import xiao.customgun.core.resource.data.data.attachment._MeleeModifierData;
import xiao.customgun.core.resource.data.data.gun._MeleeData;
import xiao.customgun.core.resource.data.data.gun.melee._DefaultMeleeData;
import xiao.customgun.core.resource.instance.data.AttachmentIndexInstance;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;
import xiao.customgun.core.util.SendUtils;

public final class LivingShooterMelee extends LivingShooterAspect {

    private final LivingShooterDraw drawGun;

    public LivingShooterMelee(LivingEntity livingShooter, ShooterProperty shooterProperty,
                              LivingShooterDraw drawGun) {
        super(livingShooter, shooterProperty);
        this.drawGun = drawGun;
    }

    public void melee() {
        if (this.shooterProperty.currentGunItem == null) return;

        if (drawGun.getDrawCooldown() > 0 // 检查是否在切枪
                || this.shooterProperty.isBolting // 检查是否在拉栓
        ) return;

        long currentTimeMillis = System.currentTimeMillis();
        if (getMeleeCooldown(currentTimeMillis) > 0) return;

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return;

        // 触发近战事件
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
        if (CustomGun.getEventPoster().postCustomEvent(new ShooterMeleeEvent(McLogicalSide.SERVER,
                iLivingShooter, this.livingShooter, iGun, currentGunItem))) {
            return;
        }
        SendUtils.sendMessageToTrackingEntity(this.livingShooter,
                new ServerMessageGunMelee(this.livingShooter.getId(), currentGunItem));

        this.shooterProperty.meleeTimestamp = System.currentTimeMillis();

        // 刺刀
        var muzzleLocation = iGun.getAttachmentLocation(currentGunItem, AttachmentCategory.MUZZLE);
        _MeleeModifierData muzzleMeleeModifier = _getMeleeModifierData(muzzleLocation);
        if (muzzleMeleeModifier != null) {
            float prepTime = muzzleMeleeModifier.getDamageDelaySeconds();
            this.shooterProperty.meleePrepTickCount = (int) Math.max(0, prepTime * 20);
            return;
        }
        // 枪托
        var stockLocation = iGun.getAttachmentLocation(currentGunItem, AttachmentCategory.STOCK);
        _MeleeModifierData stockMeleeModifier = _getMeleeModifierData(stockLocation);
        if (stockMeleeModifier != null) {
            float prepTime = stockMeleeModifier.getDamageDelaySeconds();
            this.shooterProperty.meleePrepTickCount = (int) Math.max(0, prepTime * 20);
            return;
        }

        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(iGun.getGunLocation(currentGunItem));
        if (gunIndexInstance == null) return;

        _DefaultMeleeData defaultMeleeData = gunIndexInstance.getGunData().getMeleeData().getDefaultMeleeData();
        if (defaultMeleeData != null) {
            float prepTime = defaultMeleeData.getDamageDelaySeconds();
            this.shooterProperty.meleePrepTickCount = (int) Math.max(0, prepTime * 20);
        }
    }

    public void scheduleTickMelee() {
        if (this.shooterProperty.meleePrepTickCount > 0) {
            this.shooterProperty.meleePrepTickCount--;
        } else if (this.shooterProperty.meleePrepTickCount == 0) {
            this.shooterProperty.meleePrepTickCount = -1;
            if (this.shooterProperty.currentGunItem == null) return;

            ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
            IGun iGun = IGunGetter.fromItemStack(currentGunItem);
            if (iGun == null) return;

            iGun.melee(this.shooterProperty, iGun, currentGunItem, ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter), this.livingShooter);
        }
    }

    public long getMeleeCooldown(long currentTimeMillis) {
        if (this.shooterProperty.currentGunItem == null) return 0;

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return 0;

        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(iGun.getGunLocation(currentGunItem));
        if (gunIndexInstance == null) return -1;

        _MeleeData meleeData = gunIndexInstance.getGunData().getMeleeData();
        // 刺刀冷却
        var muzzleLocation = iGun.getAttachmentLocation(currentGunItem, AttachmentCategory.MUZZLE);
        _MeleeModifierData muzzleMeleeModifier = _getMeleeModifierData(muzzleLocation);
        if (muzzleMeleeModifier != null) {
            return _getTotalCooldownTime(currentTimeMillis, meleeData, muzzleMeleeModifier.getBaseCooldown());
        }
        // 枪托冷却
        var stockLocation = iGun.getAttachmentLocation(currentGunItem, AttachmentCategory.STOCK);
        _MeleeModifierData stockMeleeModifier = _getMeleeModifierData(stockLocation);
        if (stockMeleeModifier != null) {
            return _getTotalCooldownTime(currentTimeMillis, meleeData, stockMeleeModifier.getBaseCooldown());
        }

        _DefaultMeleeData defaultMeleeData = meleeData.getDefaultMeleeData();
        float defaultMeleeCooldownTime = defaultMeleeData != null
                ? defaultMeleeData.getBaseCooldown() : 0;
        return _getTotalCooldownTime(currentTimeMillis, meleeData, defaultMeleeCooldownTime);
    }

    private long _getTotalCooldownTime(long currentTimeMillis, _MeleeData meleeData, float extraCooldownTime) {
        float totalCooldownTime = meleeData.getMeleeCooldown() + extraCooldownTime;
        long coolDown = (long) (totalCooldownTime * 1000) - (currentTimeMillis - this.shooterProperty.meleeTimestamp);
        // 给 5 ms 的窗口时间，以平衡延迟
        return coolDown < WINDOW_TIME_MS ? 0 : coolDown;
    }

    private static @Nullable _MeleeModifierData _getMeleeModifierData(Identifier attachmentLocation) {
        @Nullable AttachmentIndexInstance attachmentIndexInstance = ResourceApi.getAttachmentIndexInstance(attachmentLocation);
        if (attachmentIndexInstance == null) return null;
        return attachmentIndexInstance.getAttachmentData().getMeleeModifier();
    }
}
