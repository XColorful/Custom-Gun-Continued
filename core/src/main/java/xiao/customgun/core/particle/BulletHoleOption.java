/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import xiao.customgun.core.api.minecraft.particle.CustomParticleType;
import xiao.customgun.core.api.particle.BulletHoleOptionTag;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.init.registry.ModParticles;
import xiao.customgun.core.util.NetworkUtils;

public record BulletHoleOption(Direction direction, BlockPos pos,
                               String ammoLocation, String gunDisplayLocation, String gunLocation)
        implements ParticleOptions {

    public BulletHoleOption(int dir, long pos,
                            String ammoLocation, String gunDisplayLocation, String gunLocation) {
        this(Direction.values()[dir], BlockPos.of(pos), ammoLocation, gunDisplayLocation, gunLocation);
    }

    @Override
    public ParticleType<?> getType() {
        return ModParticles.BULLET_HOLE_PARTICLE_TYPE;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.direction);
        buffer.writeBlockPos(this.pos);
        NetworkUtils.writeUtf(buffer, this.ammoLocation);
        NetworkUtils.writeUtf(buffer, this.gunDisplayLocation);
        NetworkUtils.writeUtf(buffer, this.gunLocation);
    }

    public static BulletHoleOption fromNetwork(FriendlyByteBuf buffer) {
        Direction direction = buffer.readEnum(Direction.class);
        BlockPos pos = buffer.readBlockPos();
        var ammoLocation = NetworkUtils.readUtf(buffer);
        var gunDisplayLocation = NetworkUtils.readUtf(buffer);
        var gunLocation = NetworkUtils.readUtf(buffer);
        return new BulletHoleOption(direction, pos, ammoLocation, gunDisplayLocation, gunLocation);
    }

    @Override
    public String writeToString() {
        return CustomParticleType.BULLET_HOLE.getRegistryName() + " " + this.direction.getName();
    }

    // --------Mod particle--------

    @SuppressWarnings("deprecation")
    public static final ParticleOptions.Deserializer<BulletHoleOption> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public BulletHoleOption fromCommand(ParticleType<BulletHoleOption> particleType, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            int dir = reader.readInt();
            reader.expect(' ');
            long pos = reader.readLong();
            reader.expect(' ');
            var ammoLocation = reader.readString();
            reader.expect(' ');
            var gunDisplayLocation = reader.readString();
            reader.expect(' ');
            var gunLocation = reader.readString();
            return new BulletHoleOption(dir, pos, ammoLocation, gunDisplayLocation, gunLocation);
        }

        @Override
        public BulletHoleOption fromNetwork(ParticleType<BulletHoleOption> particleType, FriendlyByteBuf buffer) {
            return BulletHoleOption.fromNetwork(buffer);
        }
    };

    public static final Codec<BulletHoleOption> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.INT.fieldOf(BulletHoleOptionTag.DIRECTION).forGetter(option -> option.direction.ordinal()),
                    Codec.LONG.fieldOf(BulletHoleOptionTag.POSITION).forGetter(option -> option.pos.asLong()),
                    Codec.STRING.fieldOf(BulletHoleOptionTag.AMMO_LOCATION).forGetter(option -> option.ammoLocation),
                    Codec.STRING.fieldOf(BulletHoleOptionTag.GUN_DISPLAY_LOCATION).forGetter(option -> option.gunDisplayLocation),
                    Codec.STRING.optionalFieldOf(BulletHoleOptionTag.GUN_LOCATION, ResourceTag.nullLocation).forGetter(option -> option.gunLocation)
            ).apply(builder, BulletHoleOption::new));
}
