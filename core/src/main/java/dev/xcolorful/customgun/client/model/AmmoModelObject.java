/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.model;

import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry.NodeName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class AmmoModelObject extends ModelObject {

    /**
     * 展示框渲染原点定位组的路径
     */
    private @Nullable List<BedrockPart> fixedOriginPath;
    /**
     * 地面实体渲染原点定位组的路径
     */
    private @Nullable List<BedrockPart> groundOriginPath;
    /**
     * 第三人称手部实体渲染原点定位组的路径
     */
    private @Nullable List<BedrockPart> thirdPersonHandOriginPath;

    AmmoModelObject(@NotNull BedrockModel pojo) {
        super(pojo);
    }

    public static @Nullable AmmoModelObject fromPojo(BedrockModel pojo) {
        if (pojo == null) return null;
        AmmoModelObject instance = new AmmoModelObject(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }

    @Override public boolean resetCache() {
        if (!super.resetCache()) return false;

        {
            this.fixedOriginPath = this.getPath(this.modelMap_get(NodeName.FIXED_ORIGIN.getName()));
            this.groundOriginPath = this.getPath(this.modelMap_get(NodeName.GROUND_ORIGIN.getName()));
            this.thirdPersonHandOriginPath = this.getPath(this.modelMap_get(NodeName.THIRD_PERSON_HAND_ORIGIN.getName()));
        }
        
        return true;
    }
    
    // --------Getter--------

    public @Nullable List<BedrockPart> getFixedOriginPath() {
        return fixedOriginPath;
    }
    public @Nullable List<BedrockPart> getGroundOriginPath() {
        return groundOriginPath;
    }
    public @Nullable List<BedrockPart> getThirdPersonHandOriginPath() {
        return thirdPersonHandOriginPath;
    }

    // --------Deprecated--------

    @Deprecated(forRemoval = true) private static final String FIXED_ORIGIN_NODE = NodeName.FIXED_ORIGIN.getName();
    @Deprecated(forRemoval = true) private static final String GROUND_ORIGIN_NODE = NodeName.GROUND_ORIGIN.getName();
    @Deprecated(forRemoval = true) private static final String THIRD_PERSON_HAND_ORIGIN_NODE = NodeName.THIRD_PERSON_HAND_ORIGIN.getName();
}
