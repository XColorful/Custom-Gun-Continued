package dev.xcolorful.customgun.core.api.text.placeholder;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IPlaceholderParser {

    String getPlaceholderKey();

    /**
     * 解析注册的{@link #getPlaceholderKey()}占位符文本
     */
    @NotNull String parsePlaceholderKey(ItemStack itemStack);
}
