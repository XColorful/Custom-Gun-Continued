package dev.xcolorful.customgun.core.text.placeholder;

import dev.xcolorful.customgun.core.api.text.placeholder.IPlaceholderParser;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class _PlaceholderParser {

    /**
     * <ul>
     *     <li>不把反斜杠当转义处理，即{@code "\%"}</li>
     *     <li>对{@code "%%"}解析为{@code "%"}</li>
     * </ul>
     *
     */
    protected static @NotNull String parse(PlaceholderManager _this, @NotNull String text, ItemStack itemStack) {
        int length = text.length();
        if (text.indexOf('%') < 0) {
            // 没有"%"直接返回
            return text;
        }

        StringBuilder result = null;
        int index = 0;
        while (index < length) {
            int start = text.indexOf('%', index);
            if (start < 0) {
                // 缺少第一个"%"
                if (result != null) {
                    result.append(text, index, length);
                }
                break;
            }

            int end = text.indexOf('%', start + 1);
            if (end < 0) {
                // 缺少第二个"%"
                if (result != null) {
                    // 前面已经构造过，把剩余的都填进builder
                    result.append(text, index, length);
                }
                break;
            }

            // ----上面逻辑已经检测到两个%%----

            // 初始化builder
            if (result == null) result = new StringBuilder(length);

            // 把上一次结束到这次开始的添加进builder
            result.append(text, index, start);

            if (end == start + 1) {
                // 两个"%"连续，替换为"%"
                result.append('%');
            } else {
                // 两个"%"之间有内容，找parser解析
                String placeholder = text.substring(start + 1, end);
                IPlaceholderParser parser = _this.parsers.mapGet(placeholder);
                if (parser != null) {
                    // 用parser解析
                    result.append(parser.parsePlaceholderKey(itemStack));
                } else {
                    // 没有parser就保留源文本
                    result.append(text, start, end + 1);
                }
            }

            index = end + 1;
        }

        return result == null ? text // 没有遇到两个"%" (没解析过)
                : result.toString();
    }
}
