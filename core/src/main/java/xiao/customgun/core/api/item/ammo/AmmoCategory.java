package xiao.customgun.core.api.item.ammo;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;

import java.util.HashMap;
import java.util.Map;

public enum AmmoCategory implements ResourceTag.CategoryTag {
    AMMO(AmmoCategoryTag.AMMO,
            Component.translatable("customgun.ammocategory.ammo"));

    public final String categoryName;
    public final MutableComponent categoryLang;
    AmmoCategory(String name, MutableComponent lang) {
        this.categoryName = name;
        this.categoryLang = lang;
    }

    @Override public String getTagName() {
        return this.categoryName;
    }
    @Override public String getCategoryName() {
        return this.categoryName;
    }

    public final MutableComponent getCategoryLang() {
        return this.categoryLang;
    }

    private static final Map<String, AmmoCategory> AMMO_CATEGORIES = new HashMap<>();

    static {
        for (AmmoCategory ammoCategory : values()) {
            AMMO_CATEGORIES.put(ammoCategory.getCategoryName(), ammoCategory);
        }
    }

    public static @Nullable AmmoCategory fromString(String name) {
        return name != null ? AMMO_CATEGORIES.get(name) : null;
    }
    public String toString() {
        return this.categoryName;
    }
}
