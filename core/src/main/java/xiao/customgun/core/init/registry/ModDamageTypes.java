/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.init.registry;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import xiao.customgun.core.api.minecraft.damage.CustomDamageType;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> BULLET = CustomDamageType.BULLET.resourceKey;
    public static final ResourceKey<DamageType> PIERCER = CustomDamageType.PIERCER.resourceKey;
    public static final ResourceKey<DamageType> BREAKER = CustomDamageType.BREAKER.resourceKey;
    public static final ResourceKey<DamageType> OVERRIDER = CustomDamageType.OVERRIDER.resourceKey;

    public static final TagKey<DamageType> BULLET_DAMAGE = TagKey.create(Registries.DAMAGE_TYPE, CustomDamageType.Tag.BULLET_DAMAGE);
    public static final TagKey<DamageType> PIERCE_DAMAGE = TagKey.create(Registries.DAMAGE_TYPE, CustomDamageType.Tag.PIERCE_DAMAGE);
    public static final TagKey<DamageType> BYPASS_DAMAGE = TagKey.create(Registries.DAMAGE_TYPE, CustomDamageType.Tag.BYPASS_DAMAGE);

    /**
     * @param directEntity 出伤工具 (子弹)
     * @param causingEntity 使用工具的实体 (枪手)
     */
    public static DamageSource createDamage(RegistryAccess registryAccess, CustomDamageType customDamageType,
                                            Entity directEntity, Entity causingEntity) {
        var damageType = registryAccess
                .registryOrThrow(Registries.DAMAGE_TYPE) // .lookupOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(customDamageType.resourceKey); // .getOrThrow(customDamageType.resourceKey)
        return new DamageSource(damageType,
                directEntity, causingEntity);
    }

    // --------Deprecated--------

    @Deprecated public static final ResourceKey<DamageType> BULLET_IGNORE_ARMOR = PIERCER;
    @Deprecated public static final ResourceKey<DamageType> BULLET_VOID = BREAKER;
    @Deprecated public static final ResourceKey<DamageType> BULLET_VOID_IGNORE_ARMOR = OVERRIDER;
    @Deprecated public static final TagKey<DamageType> BULLETS_TAG = BULLET_DAMAGE;
    @Deprecated
    public static class Sources {
        public static DamageSource bullet(RegistryAccess access, Entity bullet, Entity shooter, boolean ignoreArmor) {
            return createDamage(access, CustomDamageType.of(ignoreArmor, false), bullet, shooter);
        }

        public static DamageSource bulletVoid(RegistryAccess access, Entity bullet, Entity shooter, boolean ignoreArmor) {
            return createDamage(access, CustomDamageType.of(ignoreArmor, true), bullet, shooter);
        }
    }
}
