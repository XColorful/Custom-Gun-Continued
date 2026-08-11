package dev.xcolorful.customgun.core.text.placeholder;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.McSide;
import dev.xcolorful.customgun.core.api.text.placeholder.IPlaceholderManager;
import dev.xcolorful.customgun.core.api.text.placeholder.IPlaceholderParser;
import dev.xcolorful.customgun.core.util.ClassUtils;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderManager implements IPlaceholderManager {
    public static final PlaceholderManager INSTANCE = new PlaceholderManager();

    final ClassUtils.ArrayMap<String, IPlaceholderParser> parsers = new ClassUtils.ArrayMap<>(IPlaceholderParser::getPlaceholderKey);

    protected PlaceholderManager() {
    }

    public static void init(McSide mcSide) {
    }

    // --------IPlaceholderManager--------

    @Override
    public boolean register(IPlaceholderParser parser) {
        String placeholder = parser.getPlaceholderKey();
        if (this.parsers.containsKey(placeholder)) {
            CustomGun.LOGGER.warn("PlaceholderManager: parser for {} already exists", placeholder);
            return false;
        } else if (placeholder.contains("%")) {
            CustomGun.LOGGER.warn("PlaceholderManager: placeholder cannot contains '%'");
            return false;
        }
        parsers.put(placeholder, parser);
        CustomGun.LOGGER.debug("PlaceholderManager: parser for {} registered", placeholder);
        return true;
    }

    @Override
    public boolean unregister(IPlaceholderParser parser) {
        @Nullable IPlaceholderParser previous = this.parsers.mapGet(parser.getPlaceholderKey());
        if (previous == null) {
            CustomGun.LOGGER.warn("PlaceholderManager: parser for {} not registered", parser.getPlaceholderKey());
            return false;
        } else if (!previous.equals(parser)) {
            CustomGun.LOGGER.warn("PlaceholderManager: not the same parser for {}", parser.getPlaceholderKey());
            return false;
        }

        this.parsers.remove(parser.getPlaceholderKey());
        return true;
    }

    @Override
    public @NotNull String parse(@NotNull String text, ItemStack itemStack) {
        return _PlaceholderParser.parse(this, text, itemStack);
    }

    // --------IPlaceholderParser--------

    @Override
    public String getPlaceholderKey() {
        return "%%";
    }

    @Override
    public @NotNull String parsePlaceholderKey(ItemStack itemStack) {
        return "%";
    }
}
