package dev.xcolorful.customgun.neoforge.init.registry;

import dev.xcolorful.customgun.core.entity.projectile.GunProjectile;
import dev.xcolorful.customgun.core.init.registry.ModEntities;
import dev.xcolorful.customgun.neoforge.entity.projectile.NeoGunProjectile;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

/**
 * 处理需要hack的{@link ModEntities}注册类型
 */
public class NeoModEntities {

    private static final Map<Class<? extends Entity>, EntityType.EntityFactory<? extends Entity>> NEO_ENTITY_FACTORY_MAP = new HashMap<>();
    private static <C extends Entity, F extends C> void _put(Class<C> coreClass, EntityType.EntityFactory<F> factory) {
        NEO_ENTITY_FACTORY_MAP.put(coreClass, factory);
    }

    static {
        _put(GunProjectile.class, NeoGunProjectile::new);
    }


    @SuppressWarnings("unchecked")
    public static <E extends Entity> EntityType.EntityFactory<E> getNeoFactory(Class<E> clazz) {
        if (NEO_ENTITY_FACTORY_MAP.containsKey(clazz)) {
            return (EntityType.EntityFactory<E>) NEO_ENTITY_FACTORY_MAP.get(clazz);
        }

        throw new IllegalArgumentException("No NeoForge entity mapping registered for class: " + clazz.getName());
    }
}
