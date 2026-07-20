/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.client.animation.statemachine.GunAnimationState;
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.resource.instance.data.ClientGunIndexInstance;
import xiao.customgun.client.sound.SoundPlayManager;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.event.shooter.ShooterMeleeEvent;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.attachment.AttachmentCategory;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.item.gun.MeleeType;
import xiao.customgun.core.network.message.ClientMessagePlayerMelee;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.attachment._MeleeModifierData;
import xiao.customgun.core.resource.data.data.gun.melee._DefaultMeleeData;
import xiao.customgun.core.util.SendUtils;

public final class LocalShooterMelee extends LocalShooterAspect {

    public LocalShooterMelee(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public void melee() {
        // 检查状态锁
        if (this.localShooterProperty.clientStateLock) return;

        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return;

        // 刺刀
        var muzzleLocation = iGun.getAttachmentLocation(gunItem, AttachmentCategory.MUZZLE);
        _MeleeModifierData muzzleMeleeModifier = _getMeleeModifierData(muzzleLocation);
        if (muzzleMeleeModifier != null) {
            this.doMelee(iGun, gunItem, gunDisplayInstance, MeleeType.BAYONET);
            return;
        }

        // 枪托
        var stockLocation = iGun.getAttachmentLocation(gunItem, AttachmentCategory.STOCK);
        _MeleeModifierData stockMeleeModifier = _getMeleeModifierData(stockLocation);
        if (stockMeleeModifier != null) {
            this.doMelee(iGun, gunItem, gunDisplayInstance, MeleeType.STOCK);
            return;
        }

        // 枪推
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable ClientGunIndexInstance clientGunIndexInstance = ClientResourceApi.getClientGunIndexInstance(gunLocation);
        if (clientGunIndexInstance == null) return;
        @Nullable _DefaultMeleeData defaultMeleeData = clientGunIndexInstance.getGunData().getMeleeData().getDefaultMeleeData();
        if (defaultMeleeData == null) return;

        MeleeType meleeType = defaultMeleeData.getMeleeType();
        switch (meleeType) {
            case STOCK -> this.doMelee(iGun, gunItem, gunDisplayInstance, MeleeType.STOCK);
            default -> this.doMelee(iGun, gunItem, gunDisplayInstance, MeleeType.PUSH);
        }
    }

    private void doMelee(IGun iGun, ItemStack gunItem,
                         GunDisplayInstance gunDisplayInstance, MeleeType meleeType) {
        switch (meleeType) {
            case BAYONET -> doMelee(iGun, gunItem, gunDisplayInstance, GunSoundType.MELEE_BAYONET, GunAnimationState.INPUT_BAYONET_MUZZLE);
            case STOCK -> doMelee(iGun, gunItem, gunDisplayInstance, GunSoundType.MELEE_STOCK, GunAnimationState.INPUT_BAYONET_STOCK);
            case PUSH -> doMelee(iGun, gunItem, gunDisplayInstance, GunSoundType.MELEE_PUSH, GunAnimationState.INPUT_BAYONET_PUSH);
            // 增加类型使此处强制编译不通过
        }
    }
    private void doMelee(IGun iGun, ItemStack gunItem,
                         GunDisplayInstance gunDisplayInstance,
                         GunSoundType gunSoundType, GunAnimationState gunAnimationState) {
        if (!prepareMelee(iGun, gunItem)) {
            return;
        }

        SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(gunSoundType),
                this.localShooter);
        // 发送执行近战的数据包，通知服务器
        SendUtils.sendMessageToServer(new ClientMessagePlayerMelee());
        // 动画状态机转移状态
        // TODO GunDisplayInstance AnimationStateMachine
    }
    private boolean prepareMelee(IGun iGun, ItemStack gunItem) {
        // 锁上状态锁
        this.localShooterProperty.lockState(operator -> operator.cgc$getSynMeleeCooldown() > 0);
        // 触发近战事件
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter);
        boolean canceled = CustomGun.getEventPoster().postCustomEvent(new ShooterMeleeEvent(McLogicalSide.CLIENT,
                iLivingShooter, this.localShooter, iGun, gunItem));
        return !canceled;
    }

    private static @Nullable _MeleeModifierData _getMeleeModifierData(Identifier attachmentLocation) {
        @Nullable var attachmentIndexInstance = ClientResourceApi.getClientAttachmentIndexInstance(attachmentLocation);
        if (attachmentIndexInstance == null) return null;
        AttachmentData attachmentData = attachmentIndexInstance.getAttachmentData();
        return attachmentData != null ? attachmentData.getMeleeModifier() : null;
    }
}
