package xiao.customgun.forge.init.registry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import xiao.customgun.core.entity.projectile.GunProjectile;
import xiao.customgun.core.init.registry.ModEntities;
import xiao.customgun.forge.entity.projectile.ForgeGunProjectile;

import java.util.HashMap;
import java.util.Map;

/**
 * 处理需要hack的{@link ModEntities}注册类型
 */
public class ForgeModEntities {

    private static final Map<Class<? extends Entity>, EntityType.EntityFactory<? extends Entity>> FORGE_ENTITY_FACTORY_MAP = new HashMap<>();
    private static <C extends Entity, F extends C> void _put(Class<C> coreClass, EntityType.EntityFactory<F> factory) {
        FORGE_ENTITY_FACTORY_MAP.put(coreClass, factory);
    }

    static {
        _put(GunProjectile.class, ForgeGunProjectile::new);
    }


    @SuppressWarnings("unchecked")
    public static <E extends Entity> EntityType.EntityFactory<E> getForgeFactory(Class<E> clazz) {
        if (FORGE_ENTITY_FACTORY_MAP.containsKey(clazz)) {
            return (EntityType.EntityFactory<E>) FORGE_ENTITY_FACTORY_MAP.get(clazz);
        }

        throw new IllegalArgumentException("No Forge entity mapping registered for class: " + clazz.getName());
    }
}
