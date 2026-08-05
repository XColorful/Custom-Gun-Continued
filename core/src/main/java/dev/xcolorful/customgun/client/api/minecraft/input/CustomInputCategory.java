package dev.xcolorful.customgun.client.api.minecraft.input;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.minecraft.input.CustomInputCategoryTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum CustomInputCategory implements ICustomInputCategory {
    @Deprecated(forRemoval = true) CONFIG(CustomInputCategoryTag.CONFIG),
    PLAYER(CustomInputCategoryTag.PLAYER),
    SHOOTER(CustomInputCategoryTag.SHOOTER);

    public final String tagName;
    public final String categoryName;
    public final String registryName;
    public final Identifier registryLocation;
    public final Component categoryLang;
    CustomInputCategory(String category) {
        this(CustomInputCategoryTag.PREFIX, category);
    }
    CustomInputCategory(String prefix, String category) {
        this.tagName = category;
        this.categoryName = category;
        this.registryLocation = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, prefix + category));
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
    @Override public Identifier getRegistryLocation() {
        return this.registryLocation;
    }

    @Override
    public Component getCategoryLang() {
        return this.categoryLang;
    }

    private static final Map<String, ICustomInputCategory> CATEGORIES = new HashMap<>();
    @ApiStatus.Internal
    public static void registerInputCategory(ICustomInputCategory category) {
        CATEGORIES.put(category.getTagName(), category);
        CATEGORIES.put(category.getCategoryName(), category);
        CATEGORIES.put(category.getRegistryName(), category);
    }

    static {
        for (CustomInputCategory category : values()) {
            registerInputCategory(category);
        }
    }

    public static @Nullable ICustomInputCategory fromString(String name) {
        return name != null ? CATEGORIES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.categoryName;
    }
}
