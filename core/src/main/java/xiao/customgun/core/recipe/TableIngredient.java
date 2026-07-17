/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.resource.data.recipe.recipe._TableIngredientData;
import xiao.customgun.core.resource.data.recipe.recipe.ingredient._IngredientFilterData;
import xiao.customgun.core.util.IngredientUtils;

import java.util.ArrayList;
import java.util.List;

public record TableIngredient(Ingredient ingredient, int count) {

    public static List<TableIngredient> fromPojo(List<_TableIngredientData> pojos) {
        List<TableIngredient> tableIngredients = new ArrayList<>();
        for (_TableIngredientData pojo : pojos) {
            tableIngredients.add(new TableIngredient(
                    TableIngredient.fromPojo(pojo.getIngredientFilter()),
                    pojo.getIngredientCount())
            );
        }
        return tableIngredients;
    }

    public static Ingredient fromPojo(_IngredientFilterData pojo) {
        var itemFilterLocation = pojo.getItemFilterLocation();
        var tagFilterLocation = pojo.getTagFilterLocation();

        // item优先
        if (itemFilterLocation != null && !ResourceTag.NULL_LOCATION.equals(itemFilterLocation)) {
            Item item = CustomGun.getMcRegistry().getItem(itemFilterLocation);
            if (item != null) return Ingredient.of(item);
            CustomGun.LOGGER.warn("TableIngredient: Item {} not found", itemFilterLocation);
        }
        // 其次tag
        if (tagFilterLocation != null) {
            return IngredientUtils.of(TagKey.create(Registries.ITEM, tagFilterLocation));
        }

        return Ingredient.of();
    }
}
