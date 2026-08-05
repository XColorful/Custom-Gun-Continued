/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets;

import dev.xcolorful.customgun.client.api.resource.assets.AssetsFolderType;
import dev.xcolorful.customgun.client.resource.assets.animation.BedrockAnimation;
import dev.xcolorful.customgun.client.resource.assets.animation.GltfAnimation;
import dev.xcolorful.customgun.core.api.resource.FileExtensionType;
import dev.xcolorful.customgun.core.api.resource.assets.AssetsFolderName;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.ResourcePojoManager;
import dev.xcolorful.customgun.core.util.JsonUtils;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.List;

/**
 * 目录名称{@link AssetsFolderType}
 */
public abstract class AnimationManager<T extends ResourcePojo<T>> extends ResourcePojoManager<T> {

    public AnimationManager(String subPrefix, String extension, JsonUtils.ReadFunction<T> fromJson) {
        super(PackType.CLIENT_RESOURCES, Arrays.asList(AssetsFolderType.ANIMATIONS.getFolderName() + "/" + subPrefix, AssetsFolderName.ANIMATIONS_OLD1 + "/" + subPrefix),
                extension, fromJson);
    }
    public AnimationManager(String extension, JsonUtils.ReadFunction<T> fromJson) {
        super(PackType.CLIENT_RESOURCES, Arrays.asList(AssetsFolderType.ANIMATIONS.getFolderName(), AssetsFolderName.ANIMATIONS_OLD1),
                extension, fromJson);
    }
    public AnimationManager(AssetsFolderType folderType, String extension, JsonUtils.ReadFunction<T> fromJson) {
        super(PackType.CLIENT_RESOURCES, List.of(folderType.getFolderName() + "/" + extension),
                extension, fromJson);
    }

    public static final class BedrockAnimationManager extends AnimationManager<BedrockAnimation> {
        @ApiStatus.Internal
        public BedrockAnimationManager() {
            super(FileExtensionType.BEDROCK_ANIMATION.getExtensionNameWithDot(),
                    BedrockAnimation::fromJson);
        }
    }

    public static final class GltfAnimationManager extends AnimationManager<GltfAnimation> {
        @ApiStatus.Internal
        public GltfAnimationManager() {
            super(FileExtensionType.GLTF.getExtensionNameWithDot(),
                    GltfAnimation::fromJson);
        }
    }

    public static class PlayerAnimationManager extends AnimationManager<BedrockAnimation> {
        @ApiStatus.Internal
        public PlayerAnimationManager() {
            super(AssetsFolderType.PLAYER_ANIMATOR,
                    FileExtensionType.BEDROCK_ANIMATION.getExtensionNameWithDot(),
                    BedrockAnimation::fromJson);
        }
    }
}
