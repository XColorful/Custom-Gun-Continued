package xiao.customgun.client.api.minecraft.input;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.minecraft.input.CustomInputCategoryTag;
import xiao.customgun.core.api.resource.ResourceTag;

import java.util.HashMap;
import java.util.Map;

public enum CustomInputCategory implements ResourceTag.CategoryTag, ResourceTag.RegistryTag {
    CONFIG(CustomInputCategoryTag.CONFIG),
    PLAYER(CustomInputCategoryTag.PLAYER),
    SHOOTER(CustomInputCategoryTag.SHOOTER);

    public final String tagName;
    public final String categoryName;
    public final String registryName;
    public final ResourceLocation registryLocation;
    public final Component categoryLang;
    CustomInputCategory(String category) {
        this(CustomInputCategoryTag.PREFIX, category);
    }
    CustomInputCategory(String prefix, String category) {
        this.tagName = category;
        this.categoryName = category;
        this.registryLocation = CustomGun.getMcRegistry().createResourceLocation(prefix + category);
        this.registryName = registryLocation.toString();
        this.categoryLang = Component.translatable(this.registryLocation.getPath());
    }
    @Override public String getTagName() {
        return this.tagName;
    }
    @Override public String getCategoryName() {
        return this.categoryName;
    }
    @Override public String getRegistryName() {
        return this.registryName;
    }
    @Override public ResourceLocation getRegistryLocation() {
        return this.registryLocation;
    }

    public Component getCategoryLang() {
        return this.categoryLang;
    }

    private static final Map<String, CustomInputCategory> CATEGORIES = new HashMap<>();

    static {
        for (CustomInputCategory category : values()) {
            CATEGORIES.put(category.tagName, category);
            CATEGORIES.put(category.categoryName, category);
            CATEGORIES.put(category.registryName, category);
        }
    }

    public static @Nullable CustomInputCategory fromString(String name) {
        return name != null ? CATEGORIES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.categoryName;
    }
}
