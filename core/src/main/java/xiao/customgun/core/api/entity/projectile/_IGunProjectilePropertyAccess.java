package xiao.customgun.core.api.entity.projectile;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.GunProjectileProperty;

/**
 * 专供第三方脚本（如 KubeJS）调用的属性访问接口，模组内部严禁使用 (会循环调用)
 * <p>
 * 推荐脚本先缓存 {@link IGunProjectileGetter#fromEntity} 和 {@link GunProjectileProperty#fromString}，避免反复调用便利方法
 * <p>
 * 由于 JavaScript 的数字默认为 Double，直接传入会导致 Java 泛型捕获失败并抛出 ClassCastException
 * <p>
 * 在 KubeJS 侧设置整型属性时，<b>必须</b>使用 <code>java(value).asInt()</code> 显式指定类型
 */
public interface _IGunProjectilePropertyAccess {

    @Deprecated(forRemoval = false)
    default <T> T getProperty(IGunProjectileDataAccess gunProjectileDataAccess, Entity gunProjectile, GunProjectileProperty property) {
        if (property == null) return null;
        return property.get(gunProjectileDataAccess, gunProjectile);
    }
    @Deprecated(forRemoval = false)
    default <T> void setProperty(IGunProjectileDataAccess gunProjectileDataAccess, Entity gunProjectile, GunProjectileProperty property, T value) {
        if (property == null) return;
        property.set(gunProjectileDataAccess, gunProjectile, value);
    }

    @Deprecated(forRemoval = false)
    default <T> T getProperty(Entity gunProjectile, String property) {
        @Nullable IGunProjectileDataAccess gunProjectileDataAccess = IGunProjectileGetter.fromEntity(gunProjectile);
        if (gunProjectileDataAccess == null) return null;
        return getProperty(gunProjectileDataAccess, gunProjectile, GunProjectileProperty.fromString(property));
    }
    @Deprecated(forRemoval = false)
    default <T> void setProperty(Entity gunProjectile, String property, T value) {
        @Nullable IGunProjectileDataAccess gunProjectileDataAccess = IGunProjectileGetter.fromEntity(gunProjectile);
        if (gunProjectileDataAccess == null) return;
        setProperty(gunProjectileDataAccess, gunProjectile, GunProjectileProperty.fromString(property), value);
    }

    // --------Deprecated--------

    @Deprecated default <T> T modifyProperty(Entity gunProjectile, String property, T value) {
        setProperty(gunProjectile, property, value);
        return getProperty(gunProjectile, property);
    }
}