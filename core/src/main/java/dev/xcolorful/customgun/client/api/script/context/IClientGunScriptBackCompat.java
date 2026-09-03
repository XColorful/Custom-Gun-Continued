/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.script.context;

import dev.xcolorful.customgun.client.api.entity.ILocalShooter;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.client.model.GunModelObject;
import dev.xcolorful.customgun.client.renderer.model.ShellRender;
import dev.xcolorful.customgun.client.resource.assets.display.GunDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.gun._ShellEjectionParam;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.util.ClientInputUtils;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShootState;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.script.context.IGunScriptBackCompat;
import dev.xcolorful.customgun.core.api.script.context._LuaNbtAccessor;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import dev.xcolorful.customgun.core.util.EntityUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface IClientGunScriptBackCompat extends IClientGunScriptContextAccess, IGunScriptBackCompat {

    // --------IGunScriptContextAccess--------

    @Override
    default @Nullable LivingEntity getLivingShooter() {
        return this.getCameraShooter() instanceof LivingEntity livingShooter ? livingShooter : null;
    }
    @Override
    default @Nullable ILivingShooter getILivingShooter() {
        @Nullable LivingEntity livingShooter = this.getLivingShooter();
        return livingShooter != null ? ILivingShooterGetter.cgc$fromLivingEntity(livingShooter) : null;
    }

    @Deprecated(forRemoval = true)
    default <T> Optional<T> processGunData(BiFunction<IGun, GunDisplayInstance, T> processor) {
        @Nullable IGun iGun = this.getIGun();
        @Nullable GunDisplayInstance gunDisplayInstance = this.getGunDisplayInstance();
        if (iGun != null && gunDisplayInstance != null) {
            return Optional.ofNullable(processor.apply(iGun, gunDisplayInstance));
        } else {
            return Optional.empty();
        }
    }
    @Deprecated(forRemoval = true)
    default <T> Optional<T> processGunOperator(Function<ILocalShooter, T> processor) {
        @Nullable ILocalShooter iLocalShooter = this.getILocalShooter();
        if (iLocalShooter != null) {
            return Optional.ofNullable(processor.apply(iLocalShooter));
        } else {
            return Optional.empty();
        }
    }
    @Deprecated(forRemoval = true)
    default <T> Optional<T> processRemoveGunOperator(Function<ILivingShooter, T> processor) {
        @Nullable Entity cameraShooter = this.getCameraShooter();
        @Nullable ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromEntity(cameraShooter);
        if (iLivingShooter != null) {
            return Optional.ofNullable(processor.apply(iLivingShooter));
        } else {
            return Optional.empty();
        }
    }
    @Deprecated(forRemoval = true)
    default <T> Optional<T> processCameraEntity(Function<Entity, T> processor) {
        @Nullable Entity cameraShooter = this.getCameraShooter();
        if (cameraShooter != null) {
            return Optional.ofNullable(processor.apply(cameraShooter));
        } else {
            return Optional.empty();
        }
    }

    // --------IGunScriptBackCompat--------

    @Override
    default boolean isAiming() {
        @Nullable ILocalShooter iLocalShooter = this.getILocalShooter();
        return iLocalShooter != null && iLocalShooter.cgc$isAim();
    }

    @Override
    default float getAimingProgress() {
        @Nullable ILocalShooter iLocalShooter = this.getILocalShooter();
        if (iLocalShooter == null) return 0;
        return iLocalShooter.cgc$getRenderAimingProgress(this.getPartialTicks());
    }

    @Override
    default float getChargeProgress() {
        @Nullable ILocalShooter iLocalShooter = this.getILocalShooter();
        return iLocalShooter != null ? iLocalShooter.cgc$getChargeProgress() : 0;
    }

    // --------IClientGunScriptBackCompat--------

    /**
     * 获取枪膛内是否有子弹
     * @return 枪膛内是否有子弹.如果是开膛待击的枪械，则此方法返回 false
     */
    default boolean hasBulletInBarrel() {
        return this.hasAmmoInBarrel();
    }
    default boolean isOverheat() {
        return this.isOverheatLocked();
    }

    /**
     * 调整射击间隔。(仅在客户端表现)
     * @param alpha 需要加上或减少的射击间隔，单位为毫秒。正数即增加射击间隔，负数则是减少。
     */
    default void adjustClientShootInterval(long alpha) {
        @Nullable ILocalShooter iLocalShooter = this.getILocalShooter();
        if (iLocalShooter == null) return;
        iLocalShooter.cgc$getLocalShooterProperty().clientShootTimestamp += alpha; // not volatile
    }

    default long getShootCooldown() {
        @Nullable ILocalShooter iLocalShooter = this.getILocalShooter();
        return iLocalShooter != null ? iLocalShooter.cgc$getShootCooldown() : 0;
    }

    default boolean isInputUp() {
        @Nullable LocalPlayer localShooter = this.getLocalShooter();
        return ClientInputUtils.Key.forward(localShooter);
    }
    default boolean isInputDown() {
        @Nullable LocalPlayer localShooter = this.getLocalShooter();
        return ClientInputUtils.Key.backward(localShooter);
    }
    default boolean isInputLeft() {
        @Nullable LocalPlayer localShooter = this.getLocalShooter();
        return ClientInputUtils.Key.left(localShooter);
    }
    default boolean isInputRight() {
        @Nullable LocalPlayer localShooter = this.getLocalShooter();
        return ClientInputUtils.Key.right(localShooter);
    }
    default boolean isInputJumping() {
        @Nullable LocalPlayer localShooter = this.getLocalShooter();
        return ClientInputUtils.Key.jump(localShooter);
    }
    default boolean isInputProne() {
        @Nullable LocalPlayer localShooter = this.getLocalShooter();
        return ShootState.of(localShooter).contains(ShootState.PRONE);
    }

    default boolean isOnGround() {
        @Nullable Entity entity = this.getCameraShooter();
        return entity != null && entity.onGround();
    }
    default boolean isCrouching() {
        @Nullable Entity entity = this.getCameraShooter();
        return entity != null && entity.isCrouching();
    }
    default boolean shouldTilting() {
        if (!this.isCrouching() || RenderConfig.DISABLE_GUN_TILTING.get()) return false;
        boolean enableTilting; {
            @Nullable GunIndexInstance gunIndexInstance = getGunIndexInstance();
            if (gunIndexInstance == null) return false;
            GunData gunData = gunIndexInstance.getGunData();
            enableTilting = gunData.getEnableTilting();
        }
        return enableTilting;
    }

    default void anchorWalkDist() {
        @Nullable Entity cameraShooter = this.getCameraShooter();
        if (cameraShooter == null) return;
        float moveDist = EntityUtils.getMoveDist(cameraShooter);
        this.setWalkDistAnchor(moveDist);
    }
    default float getWalkDist() {
        @Nullable Entity cameraShooter = this.getCameraShooter();
        if (cameraShooter == null) return 0;
        float moveDist = EntityUtils.getMoveDist(cameraShooter);
        return moveDist - this.getWalkDistAnchor();
    }

    default void popShellFrom(int index) {
        @Nullable GunDisplayInstance gunDisplayInstance = this.getGunDisplayInstance();
        if (gunDisplayInstance == null) return;

        float[] randomVelocity; {
            GunDisplay gunDisplay = gunDisplayInstance.getPojo();
            @Nullable _ShellEjectionParam shellEjectionParam = gunDisplay.getShellEjectionParam();
            if (shellEjectionParam != null) {
                randomVelocity = shellEjectionParam.getRandomizeVelocity();
            } else {
                randomVelocity = new float[]{2.5f, 1.5f, 0.25f};
            }
        }

        { // 高模抛壳
            @Nullable GunModelObject gunModelObject = gunDisplayInstance.getGunModel();
            if (gunModelObject != null) {
                @Nullable ShellRender shellRender = gunModelObject.getShellRender(index);
                if (shellRender != null) {
                    shellRender.addShell(randomVelocity);
                }
            }
        }

        { // 低模抛壳
            @Nullable GunModelObject gunModelObject = gunDisplayInstance.getGunModelLod();
            if (gunModelObject != null) {
                @Nullable ShellRender shellRender = gunModelObject.getShellRender(index);
                if (shellRender != null) {
                    shellRender.addShell(randomVelocity);
                }
            }
        }
    }

    /**
     * 获取在枪械 display 中声明的状态机参数
     * @return 状态机参数表
     */
    default LuaTable getStateMachineParams() {
        @Nullable GunDisplayInstance gunDisplayInstance = this.getGunDisplayInstance();
        if (gunDisplayInstance == null) return new LuaTable();

        return gunDisplayInstance.getAnimStateMachineParams();
    }

    /**
     * 获取当前枪械物品的 NBT 数据访问器。<br/>
     * 注意，你不应该在客户端侧修改 NBT 数据，这可能会导致与服务端的数据不一致。<br/>
     * 你应该确保在状态机脚本内仅进行读操作
     * ↑为什么不直接用{@link #getIGun()}{@link #getGunItem()}呢？
     * @return NBT 数据访问器
     */
    default _LuaNbtAccessor getNbtAccessor() {
        return this.getNbt();
    }

    /**
     * 获取当前枪械的蓄力触发阈值(仅hold模式有效)
     * @return 当前枪械的蓄力触发阈值
     */
    default float getChargeThreshold() {
        return this.getFireThreshold();
    }

    /**
     * 获取当前是否正在蓄力
     * @return 当前是否正在蓄力
     */
    default boolean isCharging() {
        @Nullable ILocalShooter iLocalShooter = this.getILocalShooter();
        return iLocalShooter != null && iLocalShooter.cgc$isCharging();
    }

    // --------Deprecated--------

    @Deprecated(forRemoval = false) default long getShootCoolDown() {
        return this.getShootCooldown();
    }
    @Deprecated(forRemoval = false) default boolean isCrawl() {
        return this.isInputProne();
    }
    @Deprecated(forRemoval = false) default boolean shouldSlide() {
        return this.shouldTilting();
    }
}
