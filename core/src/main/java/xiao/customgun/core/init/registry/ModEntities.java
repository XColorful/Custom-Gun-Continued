package xiao.customgun.core.init.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.init.registry.IRegistrar;
import xiao.customgun.core.api.init.registry.IRegistryObject;
import xiao.customgun.core.api.minecraft.entity.CustomEntityType;
import xiao.customgun.core.entity.projectile.GunProjectile;

public class ModEntities {
    public static final IRegistrar<EntityType<?>> ENTITY_TYPES = CustomGun.getRegistrarFactory().createEntityTypes(CustomGun.MOD_ID);


    public static final IRegistryObject<EntityType<GunProjectile>> GUN_PROJECTILE = ENTITY_TYPES.registerEntity(CustomEntityType.GUN_PROJECTILE.getTagName(), GunProjectile.class,
            factory -> EntityType.Builder
                    .of(factory, MobCategory.MISC)
                    .sized(0.0625f, 0.0625f) // BlockBench模型尺寸
                    .noSummon()
                    .noSave()
                    // TODO 模组做完后可考虑修改
                    // 可见范围8区块 (128格)
                    .clientTrackingRange(8)
                    // 原版发包同步间隔4ticks (200ms)
                    .updateInterval(4)
    );
}
