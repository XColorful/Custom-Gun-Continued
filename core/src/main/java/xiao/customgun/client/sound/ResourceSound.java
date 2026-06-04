/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.sound;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.SampledFloat;

public class ResourceSound extends Sound {

    /**
     * 原版强制使用 {@link Sound#SOUND_LISTER}
     * <p>
     * 实际路径已在构造函数传入，由 {@link FileToIdConverter#idToFile} 而来
     */
    protected ResourceLocation pathLocation;
    @Override public final ResourceLocation getPath() {
        return this.pathLocation;
    }

    public ResourceSound(ResourceLocation soundLocation, ResourceLocation pathLocation,
                         SampledFloat volume, SampledFloat pitch,
                         int weight, Sound.Type type,
                         boolean stream, boolean preload, int attenuationDistance) {
        super(soundLocation.toString(), volume, pitch, weight, type, stream, preload, attenuationDistance);
        this.pathLocation = pathLocation;
    }
    public ResourceSound(ResourceLocation soundLocation, ResourceLocation pathLocation,
                         Sound template) {
        this(soundLocation, pathLocation,
                template.getVolume(), template.getPitch(),
                template.getWeight(), template.getType(),
                template.shouldStream(), template.shouldPreload(), template.getAttenuationDistance());
    }
}
