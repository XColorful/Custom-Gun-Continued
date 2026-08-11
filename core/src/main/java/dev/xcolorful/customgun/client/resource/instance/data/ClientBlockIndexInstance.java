/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.instance.data;

import com.mojang.math.Transformation;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.model.ModelObject;
import dev.xcolorful.customgun.client.resource.assets.display.BlockDisplay;
import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import dev.xcolorful.customgun.core.resource.data.index.BlockIndex;
import dev.xcolorful.customgun.core.resource.instance.PojoInstance;
import dev.xcolorful.customgun.core.util.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ClientBlockIndexInstance extends PojoInstance<BlockIndex> {

    private ModelObject blockModelInstance;

    private BlockDisplay blockDisplayCache;

    private ClientBlockIndexInstance(@NotNull BlockIndex pojo) {
        super(pojo);
    }

    public static @Nullable ClientBlockIndexInstance fromPojo(BlockIndex pojo) {
        if (pojo == null) return null;
        ClientBlockIndexInstance instance = new ClientBlockIndexInstance(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }

    @Override public boolean resetCache() {
        this.blockDisplayCache = ClientResourceApi.getBlockDisplay(this.getPojo().getDisplayIndexLocation());
        if (this.blockDisplayCache == null) {
            CustomGun.LOGGER.debug("ClientBlockIndexInstance: BlockDisplay {} not found", this.getPojo().getDisplayIndexLocation());
            return false;
        }

        BedrockModel bedrockModel = ClientResourceApi.getBedrockModel(this.blockDisplayCache.getModelLocation());
        if (bedrockModel == null) {
            CustomGun.LOGGER.debug("ClientBlockIndexInstance: BedrockModel {} not found", this.blockDisplayCache.getModelLocation());
            return false;
        }
        this.blockModelInstance = ModelObject.fromPojo(bedrockModel);
        if (this.blockModelInstance == null) {
            CustomGun.LOGGER.debug("ClientBlockIndexInstance: Failed to create ModelObject {}", this.blockDisplayCache.getModelLocation());
            return false;
        }

        return true;
    }
    @Override protected boolean isPojoValid() {
        var pojo = this.getPojo();
        if (!pojo.isValid()) return false;
        if (!resetCache()) return false;

        return true;
    }

    // --------Getter--------

    public ModelObject getBlockModelInstance() {
        return this.blockModelInstance;
    }

    // --------Deprecated--------

    @Deprecated public ModelObject getModel() {
        return getBlockModelInstance();
    }
    @Deprecated public String getName() {
        return ComponentUtils.toTranslatableKey(this.getPojo().getNameLang());
    }
    @Deprecated public String getTooltipKey() {
        return ComponentUtils.toTranslatableKey(this.getPojo().getTooltipLang());
    }
    @Deprecated public @Nullable ResourceLocation getTexture() {
        return this.blockDisplayCache.getTextureLocation();
    }
    @Deprecated public Transformation getTransforms() {
        return this.blockDisplayCache.getItemTransforms();
    }
}
