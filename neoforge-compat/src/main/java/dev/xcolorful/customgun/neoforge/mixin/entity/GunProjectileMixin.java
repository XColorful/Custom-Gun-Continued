package dev.xcolorful.customgun.neoforge.mixin.entity;

import dev.xcolorful.customgun.core.entity.projectile.GunProjectile;
import dev.xcolorful.customgun.neoforge.entity.projectile.NeoGunProjectile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.spongepowered.asm.mixin.Mixin;

/**
 * <ul>
 *     <li>让 {@link GunProjectile} 在 NeoForge 下实现 {@link IEntityWithComplexSpawn}</li>
 *     <li>以绕过原版 {@code ClientboundAddEntityPacket} 对初速度 ±3.9 的 clamp</li>
 *     <li>并同步客户端渲染与移动仿真所需的枪射物状态</li>
 * </ul>
 *
 * <ul>
 *     注:
 *     <li>服务端通过 {@code new GunProjectile(...)} 直接创建（不经过 EntityType 工厂）</li>
 *     <li>因此必须在 {@link GunProjectile} 上通过 Mixin 实现该接口，而非依赖 {@link NeoGunProjectile} 拦截注册</li>
 * </ul>
 */
@Mixin(GunProjectile.class)
public abstract class GunProjectileMixin extends Projectile implements IEntityWithComplexSpawn {

    public GunProjectileMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

//    @Override
//    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
//        return NetworkHooks.getEntitySpawningPacket((GunProjectile) (Object) this);
//    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        GunProjectile _this = (GunProjectile) (Object) this;
        _this.encodeInitialSyncData(buffer);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        GunProjectile _this = (GunProjectile) (Object) this;
        _this.decodeInitialSyncData(additionalData);
    }
}
