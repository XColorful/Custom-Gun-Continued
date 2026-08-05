/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.xcolorful.customgun.client.resource.assets.SoundManager.MOD_SOUNDS_LISTER;

public class ResourceSoundInstance extends AbstractSoundInstance {

    private final float soundDistance;
    private @Nullable ResourceSound redirectedSound;
    private boolean stopped;

    // AbstractSoundInstance
    private final boolean canPlay;
    @Override public boolean canPlaySound() {
        return this.canPlay;
    }

    // ResourceSound
    private final @Nullable ResourceLocation soundLocation;
    private @Nullable ResourceLocation soundPath;

    public ResourceSoundInstance(SoundEvent event, SoundSource source, RandomSource random,
                                 @Nullable ResourceLocation soundLocation, @Nullable ResourceLocation soundPath,
                                 boolean canPlay, float volume, float pitch,
                                 double x, double y, double z, boolean relative,
                                 float soundDistance) {
        super(event, source, random);
        this.soundDistance = soundDistance;
        this.canPlay = canPlay;
        this.volume = volume;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.attenuation = Attenuation.NONE;
        this.relative = relative;
        this.soundLocation = soundLocation;
        this.soundPath = soundPath;
    }
    public ResourceSoundInstance(SoundEvent event, SoundSource source, RandomSource random,
                                 @Nullable ResourceLocation soundLocation, @Nullable ResourceLocation soundPath,
                                 float volume, float pitch,
                                 @NotNull Entity entity, boolean relative,
                                 float soundDistance) {
        this(event, source, random, soundLocation, soundPath, !entity.isSilent(), volume, pitch, 0, 0, 0, relative, soundDistance);
        if (!relative) {
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                this.volume = volume * (1.0f - Math.min(1.0f, (float) Math.sqrt(player.distanceToSqr(x, y, z)) / soundDistance));
                this.volume *= this.volume;
            }
        }
    }

    @Override
    public WeighedSoundEvents resolve(SoundManager soundManager) {
        WeighedSoundEvents events = super.resolve(soundManager);
        var path = this.getSoundPath();
        if (path != null) {
            this.sound = this.redirectedSound = new ResourceSound(this.soundPath, path, super.getSound());
        } else {
            this.redirectedSound = null;
        }
        return events;
    }

    @Override
    public Sound getSound() {
        return redirectedSound == null ? super.getSound() : redirectedSound;
    }

    public final void setStop() {
        Minecraft.getInstance().getSoundManager().stop(this);
        this.stopped = true;
    }

    public static @Nullable ResourceLocation getPathLocation(@Nullable ResourceLocation soundLocation) {
        if (soundLocation == null) return null;
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        for (int i = 0; i < MOD_SOUNDS_LISTER.size(); i++) {
            var path = MOD_SOUNDS_LISTER.get(i).idToFile(soundLocation);
            if (manager.getResource(path).isPresent()) return path;
        }
        return null;
    }

    // --------Getter--------

    public boolean isStopped() {
        return this.stopped;
    }
    public float getSoundDistance() {
        return this.soundDistance;
    }
    public final @Nullable ResourceLocation getSoundLocation() {
        return this.soundLocation;
    }
    public final @Nullable ResourceLocation getSoundPath() {
        if (this.soundPath == null) this.soundPath = getPathLocation(this.soundLocation);
        return this.soundPath;
    }

    // --------Deprecated--------

    @Deprecated public @Nullable ResourceLocation getRegistryName() {
        return this.soundLocation;
    }
}
