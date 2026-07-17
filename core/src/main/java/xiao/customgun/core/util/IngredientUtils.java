package xiao.customgun.core.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public class IngredientUtils {

    /**
     * {@code Ingredient.of(TagKey<Item>)} 在 1.21.4+ 已被移除<br/>
     * 高版本 {@code Ingredient} 由 {@code HolderSet<Item>} 构成，
     * 需先将 tag 经注册表解析为 HolderSet（如 {@code BuiltInRegistries.ITEM.getOrThrow(tagKey)}），
     * 再通过 {@code Ingredient.of(HolderSet<Item>)} 构造<br/>
     * 参数 {@link TagKey} 全版本签名一致，各版本分支仅需替换方法体
     */
    public static Ingredient of(TagKey<Item> tagKey) {
        return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(tagKey));
    }
}
