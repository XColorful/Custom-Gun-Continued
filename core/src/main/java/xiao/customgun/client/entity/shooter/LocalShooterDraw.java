/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.event.shooter.ShooterDrawEvent;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.item.gun.GunPropertyManager;
import xiao.customgun.core.network.message.ClientMessagePlayerDrawGun;
import xiao.customgun.core.util.SendUtils;

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
        // 重置各种参数
        long currentTimeMillis = System.currentTimeMillis();
        this._resetData(currentTimeMillis);

        IGun lastGun = IGunGetter.fromItemStack(lastItem);
        ItemStack currentItem = this.localShooter.getMainHandItem();
        IGun currentGun = IGunGetter.fromItemStack(currentItem);

        // 计算 draw 时长和 putAway 时长
        long drawTime = currentTimeMillis - this.localShooterProperty.clientDrawTimestamp;
        if (drawTime >= 0) {
            drawTime = _getDrawTime(currentTimeMillis, lastItem, lastGun, drawTime);
        }
        long putAwayTime = Math.abs(drawTime);

        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter);
        CustomGun.getEventPoster().postCustomEvent(new ShooterDrawEvent(McLogicalSide.CLIENT,
                iLivingShooter, this.localShooter, lastItem, currentItem));
        SendUtils.sendMessageToServer(new ClientMessagePlayerDrawGun());

        // 异步放映抬枪动画
        if (currentGun != null) {
            this.doDraw(currentItem, putAwayTime);
            // 刷新配件数据
            GunPropertyManager.postChangeEvent(this.localShooter, currentItem);
        }
    }
    private long _getDrawTime(long currentTimeMillis, ItemStack lastItem, IGun lastGun, long drawTime) {
        if (true) {
            // TODO AnimateGeoItemRenderer getPutAwayTime
            this.localShooterProperty.clientDrawTimestamp = currentTimeMillis + drawTime;
        } else {
            drawTime = 0;
            this.localShooterProperty.clientDrawTimestamp = currentTimeMillis;
        }
        return drawTime;
    }
    private void _resetData(long currentTimeMillis) {
        // 锁上状态锁
        this.localShooterProperty.lockState(operator -> operator.cgc$getSynDrawCooldown() > 0);
        // 重置客户端的 shoot 时间戳
        this.localShooterProperty.isShootRecorded = true;
        this.localShooterProperty.clientShootTimestamp = -1;
        this.localShooterProperty.chargeProgress = 0;
        // 重置客户端瞄准状态
        this.localShooterProperty.clientIsAiming = false;
        this.localShooterProperty.clientAimingProgress = 0;
        LocalShooterProperty.oldAimingProgress = 0;
        // 重置拉栓状态
        this.localShooterProperty.isBolting = false;
        // 更新切枪时间戳
        if (this.localShooterProperty.clientDrawTimestamp < 0) {
            this.localShooterProperty.clientDrawTimestamp = currentTimeMillis;
        }
    }

     private void doDraw(ItemStack currentItem, long putAwayTime) {
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
     private void doPutAway(ItemStack lastItem, long putAwayTime) {
        // TODO
     }
}
