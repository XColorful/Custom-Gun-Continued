package dev.xcolorful.customgun.core.init.registry;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.init.registry.IRegistrar;
import dev.xcolorful.customgun.core.api.init.registry.IRegistryObject;
import dev.xcolorful.customgun.core.api.minecraft.entity.CustomEntityType;
import dev.xcolorful.customgun.core.entity.projectile.GunProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static final IRegistrar<EntityType<?>> ENTITY_TYPES = CustomGun.getRegistrarFactory().createEntityTypes(CustomGun.MOD_ID);


    public static final IRegistryObject<EntityType<GunProjectile>> GUN_PROJECTILE = ENTITY_TYPES.register(CustomEntityType.GUN_PROJECTILE.getRegistryLocation().getPath(), () -> EntityType.Builder
            .<GunProjectile>of(GunProjectile::new, MobCategory.MISC)
                    .sized(0.0625f, 0.0625f) // BlockBench模型尺寸
                    .noSummon()
                    .noSave()
                    // 可见范围20区块 (320格)
                    .clientTrackingRange(20)
                    // 原版发包同步间隔4ticks (200ms)
                    .updateInterval(4)
                    // 不接收速度更新，否则跟本地计算冲突
                    .setShouldReceiveVelocityUpdates(false)
            .build(CustomEntityType.GUN_PROJECTILE.getRegistryLocation().getPath())
    );
}
