/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.init.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.init.registry.IRegistrar;
import xiao.customgun.core.api.init.registry.IRegistryObject;
import xiao.customgun.core.api.minecraft.particle.CustomParticleType;
import xiao.customgun.core.particle.BulletHoleOption;

public class ModParticles {
    public static final IRegistrar<ParticleType<?>> PARTICLE_TYPES = CustomGun.getRegistrarFactory().createParticleTypes(CustomGun.MOD_ID);


    public static final ModParticleType<BulletHoleOption> BULLET_HOLE_PARTICLE_TYPE = new ModParticleType<>(false, BulletHoleOption.DESERIALIZER, BulletHoleOption.CODEC);
    public static final IRegistryObject<ParticleType<BulletHoleOption>> BULLET_HOLE = PARTICLE_TYPES.register(CustomParticleType.BULLET_HOLE.getTagName(), () ->
            BULLET_HOLE_PARTICLE_TYPE);


    public static class ModParticleType<T extends ParticleOptions> extends ParticleType<T> {
        private final Codec<T> codec;

        @SuppressWarnings("deprecation")
        public ModParticleType(boolean overrideLimiter,
                               ParticleOptions.Deserializer<T> deserializer,
                               Codec<T> codec) {
            super(overrideLimiter, deserializer);
            this.codec = codec;
        }

        @Override
        public @NotNull Codec<T> codec() {
            return this.codec;
        }
    }
}
