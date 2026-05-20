package xiao.customgun.core.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

public class ComponentUtils {

    public static MutableComponent unknownTranslatableKey() {
        return Component.translatable("customgun.unknown");
    }
    public static MutableComponent fromTranslatableKey(String lang) {
        if (lang == null) return unknownTranslatableKey();
        return Component.translatable(lang);
    }
    public static String toTranslatableKey(MutableComponent component) {
        if (component == null) return "";
        var content = component.getContents();
        return content instanceof TranslatableContents translatable ? translatable.getKey() : "";
    }
}
