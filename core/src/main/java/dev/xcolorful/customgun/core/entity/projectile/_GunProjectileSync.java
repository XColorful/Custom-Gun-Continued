/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.entity.projectile;

import dev.xcolorful.customgun.core.util.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class _GunProjectileSync {
    
    protected static void encodeInitialSyncData(GunProjectile _this, FriendlyByteBuf buffer) {
        Vec3 deltaMovement = _this.getDeltaMovement();
        buffer.writeDouble(deltaMovement.x);
        buffer.writeDouble(deltaMovement.y);
        buffer.writeDouble(deltaMovement.z);

        Vec3 shootPos = _this.getShootPos(_this);
        if (shootPos == null) shootPos = Vec3.ZERO;
        buffer.writeDouble(shootPos.x);
        buffer.writeDouble(shootPos.y);
        buffer.writeDouble(shootPos.z);

        buffer.writeInt(_this.getLifetimeTicks(_this));
        buffer.writeFloat(_this.getBulletSpeed(_this));
        buffer.writeFloat(_this.getGravity(_this));
        buffer.writeFloat(_this.getFriction(_this));
        buffer.writeInt(_this.getPierce(_this));
        buffer.writeBoolean(_this.getIsTracer(_this));

        NetworkUtils.writeResourceLocation(buffer, _this.getGunLocation(_this));
        NetworkUtils.writeResourceLocation(buffer, _this.getGunDisplayLocation(_this));
        NetworkUtils.writeResourceLocation(buffer, _this.getAmmoLocation(_this));

        Entity owner = _this.getOwner();
        buffer.writeInt(owner != null ? owner.getId() : 0);
    }
    protected static void decodeInitialSyncData(GunProjectile _this, FriendlyByteBuf buffer) {
        _this.setDeltaMovement(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble());

        _this.setShootPos(_this, new Vec3(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble()));

        // 直接逐项设置服务端计算好的值，避免客户端用 constructInitData 重新计算造成不一致
        _this.setLifetimeTicks(_this, buffer.readInt());
        _this.setBulletSpeed(_this, buffer.readFloat());
        _this.setGravity(_this, buffer.readFloat());
        _this.setFriction(_this, buffer.readFloat());
        _this.setPierce(_this, buffer.readInt());
        _this.setIsTracer(_this, buffer.readBoolean());

        _this.setGunLocation(_this, NetworkUtils.readResourceLocation(buffer));
        _this.setGunDisplayLocation(_this, NetworkUtils.readResourceLocation(buffer));
        _this.setAmmoLocation(_this, NetworkUtils.readResourceLocation(buffer));

        Entity owner = _this.level().getEntity(buffer.readInt());
        if (owner != null) {
            _this.setOwner(owner);
        }

        _this.rebuildCache();
    }
}
