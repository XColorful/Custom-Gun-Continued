/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.entity.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.entity.LocalShooterProperty;
import dev.xcolorful.customgun.client.api.item.IAnimateGeoItem;
import dev.xcolorful.customgun.client.api.renderer.item.IAnimateGeoItemRenderer;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.api.sound.gun.GunSoundType;
import dev.xcolorful.customgun.client.config.SoundConfig;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.sound.SoundPlayManager;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.event.shooter.ShooterDrawEvent;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.config.GunConfig;
import dev.xcolorful.customgun.core.entity.shooter.modifier.ShooterGunModifierManager;
import dev.xcolorful.customgun.core.network.message.ClientMessagePlayerDrawGun;
import dev.xcolorful.customgun.core.util.SendUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class LocalShooterDraw extends LocalShooterAspect {

    /**
     * 原模组这个字段就没true过
     */
    private boolean readyToDraw = false;

    public LocalShooterDraw(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public boolean isReadyToDraw() {
        return this.readyToDraw;
    }
    public void setReadyToDraw(boolean readyToDraw) {
        this.readyToDraw = readyToDraw;
    }

    public void draw(ItemStack lastItem) {
        // 重置各个状态
        long currentTimeMillis = System.currentTimeMillis();
        this.localShooterProperty.resetProperty();
        // 初始化切枪时间戳
        if (this.localShooterProperty.clientDrawFinishTimestamp < 0) this.localShooterProperty.clientDrawFinishTimestamp = currentTimeMillis;

        // 锁上状态锁
        this.localShooterProperty.lockState(operator -> operator.cgc$getSynDrawCooldown() > 0);

        @Nullable IGun lastIGun = IGunGetter.fromItemStack(lastItem);
        ItemStack currentItem = this.localShooter.getMainHandItem();
        @Nullable IGun currentIGun = IGunGetter.fromItemStack(currentItem);

        // 计算 draw 时长和 putAway 时长
        long fromLastDrawFinishedMs = currentTimeMillis - this.localShooterProperty.clientDrawFinishTimestamp;
        if (fromLastDrawFinishedMs >= 0) { // draw结束 在 当前时间 之前 -> 当前没有一个未来的drawFinishTime -> 当前不在draw
            fromLastDrawFinishedMs = _updateDrawTime(currentTimeMillis, lastItem, lastIGun, fromLastDrawFinishedMs);
        }
        long putAwayTime = Math.abs(fromLastDrawFinishedMs);

        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter);
        CustomGun.getEventPoster().postCustomEvent(new ShooterDrawEvent(McLogicalSide.CLIENT,
                iLivingShooter, this.localShooter, lastItem, currentItem));

        SendUtils.sendMessageToServer(new ClientMessagePlayerDrawGun());

        // 异步放映收枪动画（切出动画）
        if (lastIGun != null) {
            this._doPutAway(lastItem, putAwayTime);
        }

        // 异步放映抬枪动画（切入动画）
        if (currentIGun != null) {
            this._doDraw(currentItem, putAwayTime);
            // 刷新配件数据
            ShooterGunModifierManager.postChangeEvent(this.localShooter, currentItem);
        }
    }
    private long _updateDrawTime(long currentTimeMillis, ItemStack lastItem, IGun lastGun, long drawTime) {
        @Nullable IAnimateGeoItemRenderer<?, ?> renderer = IAnimateGeoItem.cgc$getCustomRenderer(lastItem);
        if (renderer != null) {
            long putAwayTime = renderer.getPutAwayTime(lastItem);
            if (drawTime > putAwayTime) {
                drawTime = putAwayTime;
            }

            this.localShooterProperty.clientDrawFinishTimestamp = currentTimeMillis + drawTime;
        } else {
            drawTime = 0;
            this.localShooterProperty.clientDrawFinishTimestamp = currentTimeMillis;
        }
        return drawTime;
    }

     private void _doDraw(ItemStack currentItem, long putAwayTime) {
        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(currentItem);
        if (gunDisplayInstance == null) return;

        // 取消预定中的 draw 行为
        if (this.localShooterProperty.drawFuture != null) {
            this.localShooterProperty.drawFuture.cancel(false);
        }
        // 根据 put away time 预定 draw 行为（仅播放音效，状态机的初始化为了保证一致性已经移动）
//        this.localShooterProperty.drawFuture = LocalShooterProperty.SCHEDULED_EXECUTOR_SERVICE.schedule(() -> {
//            Minecraft.getInstance().submitAsync(() -> {
//                SoundPlayManager.get().stopCurrentSound();
//                SoundPlayManager.get().playGunSound();
//            }, putAwayTime, TimeUnit.MILLISECONDS);
//        });
     }
     private void _doPutAway(ItemStack lastItem, long putAwayTime) {
        @Nullable IAnimateGeoItemRenderer<?, ?> renderer = IAnimateGeoItem.cgc$getCustomRenderer(lastItem);
        if (renderer == null) return;

        renderer.tryExit(lastItem, putAwayTime);

        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(lastItem);
        if (gunDisplayInstance == null) return;

        SoundPlayManager.get().stopMainTrackSound();
        SoundPlayManager.get().playClientSound(gunDisplayInstance.getGunSound(GunSoundType.PUT_AWAY_SOUND),
                1.0f, 1.0f,
                this.localShooter, false,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(),
                true, SoundConfig.DEFAULT_SOUND_CONCURRENCY_LIMIT.get(),
                true);
     }
}
