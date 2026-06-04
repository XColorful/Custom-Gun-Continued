/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.mixin.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

import static xiao.customgun.client.resource.assets.SoundManager.MOD_SOUNDS;
import static xiao.customgun.client.resource.assets.SoundManager.MOD_SOUNDS_OLD1;

@Mixin(targets = "net.minecraft.client.sounds.SoundManager$Preparations")
public class SoundManagerMixin {

    @Shadow
    private Map<ResourceLocation, Resource> soundCache;

    @Inject(method = "listResources", at = @At("TAIL"))
    private void cgc$onPrepareSounds(ResourceManager resourceManager, CallbackInfo ci) {
        Map<ResourceLocation, Resource> merged = new HashMap<>(this.soundCache);
        merged.putAll(MOD_SOUNDS.listMatchingResources(resourceManager));
        merged.putAll(MOD_SOUNDS_OLD1.listMatchingResources(resourceManager));
        this.soundCache = merged;
    }
}
