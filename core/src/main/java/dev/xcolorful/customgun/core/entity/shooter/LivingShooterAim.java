package dev.xcolorful.customgun.core.entity.shooter;

import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentNBTAccessor;
import dev.xcolorful.customgun.core.api.item.attachment.modifier.AttachmentModifierType;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.item.gun.modifier.IAdsModifier;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.instance.data.AttachmentIndexInstance;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class LivingShooterAim extends LivingShooterAspect {

    public LivingShooterAim(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    public void aim(boolean isAim) {
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) return;
        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            this.shooterProperty.isAiming = false;
            return;
        }

        this.shooterProperty.isAiming = isAim;
    }

    @ApiStatus.Internal private void _resetAiming() {
        this.shooterProperty.aimingProgress = 0;
        this.shooterProperty.aimingTimestamp = System.currentTimeMillis();
    }
    public void tickAimingProgress() {
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) {
            _resetAiming();
            return;
        }
        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            _resetAiming();
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();
        if ( // 2.2 检查状态
                // 正在收枪时不能瞄准
                currentTimeMillis < this.shooterProperty.drawFinishTimestamp
        ) {
            _resetAiming();
            return;
        }

        _doAiming(iGun, gunItem, currentTimeMillis);
    }
    private void _doAiming(IGun iGun, ItemStack gunItem, long currentTimeMillis) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) {
            _resetAiming();
            return;
        }

        GunData gunData = gunIndexInstance.getGunData();
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
        float alphaProgress = LivingShooterAim._getAlphaProgress(iLivingShooter, gunData, this.shooterProperty.isAiming, currentTimeMillis, this.shooterProperty.aimingTimestamp);

        _aimProgressCalculate(this.shooterProperty.isAiming, alphaProgress, currentTimeMillis);
    }
    @ApiStatus.Internal
    public static float _getAlphaProgress(ILivingShooter iLivingShooter,
                                          GunData gunData,
                                          boolean isAiming,
                                          long currentTimeMillis, long aimingTimestamp) {
        float aimTime = gunData.getAimTime();
        if (aimTime <= 0) {
            return isAiming ? 1 : 0;
        }

        @Nullable ShooterGunModifierCache cache = iLivingShooter.cgc$getGunModifierCache();
        if (cache != null) {
            Float _aimTime = IAdsModifier.getValue(cache, AttachmentModifierType.ADS);
            if (_aimTime != null) aimTime = _aimTime;
        }

        return (currentTimeMillis - aimingTimestamp + 1) / (aimTime * 1000);
    }
    private void _aimProgressCalculate(boolean isAiming,
                                       float alphaProgress, long currentTimeMillis) {
        float aimProgress = this.shooterProperty.aimingProgress + (isAiming ? alphaProgress : -alphaProgress);
        this.shooterProperty.aimingProgress = Mth.clamp(aimProgress, 0, 1f);

        this.shooterProperty.aimingTimestamp = currentTimeMillis;
    }

    public void zoom(int fromScopeViewIndex, int toScopeViewIndex) {
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) return;
        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        _doZoom(iGun, gunItem, fromScopeViewIndex, toScopeViewIndex);
    }
    private void _doZoom(IGun iGun, ItemStack gunItem,
                         int fromScopeViewIndex, int toScopeViewIndex) {
        final var SCOPE = AttachmentCategory.SCOPE;
        @Nullable CompoundTag scopeCustomDataTag = iGun.getAttachmentCustomDataTag(gunItem, SCOPE);
        if (scopeCustomDataTag == null) return;

        var scopeLocation = iGun.getAttachmentLocation(gunItem, SCOPE);
        @Nullable AttachmentIndexInstance attachmentIndexInstance = ResourceApi.getAttachmentIndexInstance(scopeLocation);
        if (attachmentIndexInstance == null) return;

//        // ↓旧实现
//        int scopeViewIndex = AttachmentNBTAccessor.INSTANCE.getScopeViewIndex(scopeCustomDataTag);
//        scopeViewIndex++;
//        zoomNumber = zoomNumber % (Integer.MAX_VALUE - 1); // 取模不能避免变负，原模组的注释有误
//        if (scopeViewIndex < 0) {
//            if (PlannedRefactor.MOVE_SCOPE_VIEW_INDEX_TO_CORE) {}
//            scopeViewIndex = 0;
//        }

        int scopeViewIndex = AttachmentNBTAccessor.INSTANCE.getScopeViewIndex(scopeCustomDataTag);
        if (scopeViewIndex != fromScopeViewIndex) { // 客户端还没收到更新就又缩放
            // 允许客户端快速滚轮，只是开镜散布判定跟客户端有点延迟
//            if (scopeViewIndex >= 0) {
//                // 服务端数据合法，强行再写入一次 (不提前return)
//                toScopeViewIndex = scopeViewIndex;
//            }
        }

        if (toScopeViewIndex < 0) {
            if (PlannedRefactor.MOVE_SCOPE_VIEW_INDEX_TO_CORE) {}
            toScopeViewIndex = 0;
        }

        { // 写入操作需要保存
            AttachmentNBTAccessor.INSTANCE.setScopeViewIndex(scopeCustomDataTag, toScopeViewIndex);
        }
        iGun.setAttachmentCustomDataTag(gunItem, SCOPE, scopeCustomDataTag);

        return;
    }
}
