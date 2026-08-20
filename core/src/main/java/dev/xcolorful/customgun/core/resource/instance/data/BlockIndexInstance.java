/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.instance.data;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.data.BlockData;
import dev.xcolorful.customgun.core.resource.data.index.BlockIndex;
import dev.xcolorful.customgun.core.resource.data.recipefilter.RecipeFilterData;
import dev.xcolorful.customgun.core.resource.instance.PojoInstance;
import net.minecraft.world.item.BlockItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockIndexInstance extends PojoInstance<BlockIndex> {

    private BlockItem blockItemCache;
    private BlockData blockDataCache;
    private RecipeFilterData recipeFilterDataCache;

    private BlockIndexInstance(@NotNull BlockIndex pojo) {
        super(pojo);
    }

    public static @Nullable BlockIndexInstance fromPojo(BlockIndex pojo) {
        if (pojo == null) return null;
        BlockIndexInstance instance = new BlockIndexInstance(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }

    @Override public boolean resetCache() {
        var pojo = this.getPojo();

        this.blockItemCache = CustomGun.getMcRegistry().getItem(pojo.getBlockLocation()) instanceof BlockItem blockItem ? blockItem : null;
        if (this.blockItemCache == null) {
            CustomGun.LOGGER.debug("BlockIndexInstance: BlockItem {} not found", pojo.getBlockLocation());
            return false;
        }

        this.blockDataCache = ResourceApi.getBlockData(pojo.getDataLocation());
        if (this.blockDataCache == null) {
            CustomGun.LOGGER.debug("BlockIndexInstance: BlockData {} not found", pojo.getDataLocation());
            return false;
        } else if (!this.blockDataCache.isValid()) {
            CustomGun.LOGGER.debug("BlockIndexInstance: BlockData {} not valid", pojo.getDataLocation());
            return false;
        }

        this.recipeFilterDataCache = ResourceApi.getRecipeFilterData(this.blockDataCache.getRecipeFilterLocation());
        if (this.recipeFilterDataCache == null) {
            CustomGun.LOGGER.debug("BlockIndexInstance: RecipeFilterData {} not found", this.blockDataCache.getRecipeFilterLocation());
            return false;
        } else if (!this.recipeFilterDataCache.isValid()) {
            CustomGun.LOGGER.debug("BlockIndexInstance: RecipeFilterData {} not valid", this.blockDataCache.getRecipeFilterLocation());
            return false;
        }

        return true;
    }
    @Override protected boolean isPojoValid() {
        if (!super.isPojoValid()) return false;

        var pojo = this.getPojo();

        // BlockIndex
        if (pojo.getSlotSort() > 65536) CustomGun.LOGGER.warn("BlockIndexInstance: BlockIndex slotSort {} > 65536", pojo.getSlotSort());

        return true;
    }

    // --------Getter--------

    public BlockItem getBlockItem() {
        return this.blockItemCache;
    }
    public BlockData getBlockData() {
        return this.blockDataCache;
    }
    public RecipeFilterData getRecipeFilterData() {
        return this.recipeFilterDataCache;
    }
}