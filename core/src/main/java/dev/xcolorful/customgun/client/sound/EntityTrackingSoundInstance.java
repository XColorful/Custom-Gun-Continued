/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.sound;

import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class EntityTrackingSoundInstance extends ResourceSoundInstance implements TickableSoundInstance {

    protected final WeakReference<Entity> entityRef;

    public EntityTrackingSoundInstance(SoundEvent event, SoundSource source, RandomSource random,
                                       @Nullable Identifier soundLocation, @Nullable Identifier soundPath,
                                       float volume, float pitch,
                                       @NotNull Entity entity, boolean relative,
                                       float soundDistance) {
        super(event, source, random, soundLocation, soundPath, volume, pitch, entity, relative, soundDistance);
        this.entityRef = new WeakReference<>(entity);
    }

    @Override
    public void tick() {
        Entity entity = this.entityRef.get();
        if (entity == null || entity.isRemoved() || entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying()) {
            this.setStop();
            return;
        }
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }
}
