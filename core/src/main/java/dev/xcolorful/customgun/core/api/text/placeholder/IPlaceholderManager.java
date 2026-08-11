package dev.xcolorful.customgun.core.api.text.placeholder;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IPlaceholderManager extends IPlaceholderParser {

    boolean register(IPlaceholderParser parser);

    boolean unregister(IPlaceholderParser parser);

    /**
     * 解析带 "%" 的字符串
     */
    @NotNull String parse(@NotNull String text, ItemStack itemStack);
}
