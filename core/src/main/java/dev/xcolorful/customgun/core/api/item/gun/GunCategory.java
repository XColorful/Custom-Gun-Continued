package dev.xcolorful.customgun.core.api.item.gun;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum GunCategory implements ResourceTag.CategoryTag {
    /**
     * 手枪
     */
    PISTOL(GunCategoryTag.PISTOL,
            Component.translatable("customgun.guncategory.pistol")),
    /**
     * 突击步枪
     */
    AR(GunCategoryTag.AR, GunCategoryTag.AR_OLD1,
            Component.translatable("customgun.guncategory.ar")),
    /**
     * 精确射手步枪
     */
    DMR(GunCategoryTag.DMR,
            Component.translatable("customgun.guncategory.dmr")),
    /**
     * 狙击步枪
     */
    SR(GunCategoryTag.SR, GunCategoryTag.SR_OLD1,
            Component.translatable("customgun.guncategory.sr")),
    /**
     * 霰弹枪
     */
    SHOTGUN(GunCategoryTag.SHOTGUN,
            Component.translatable("customgun.guncategory.shotgun")),
    /**
     * 冲锋枪
     */
    SMG(GunCategoryTag.SMG,
            Component.translatable("customgun.guncategory.smg")),
    /**
     * 机枪
     */
    MG(GunCategoryTag.MG,
            Component.translatable("customgun.guncategory.mg")),
    /**
     * 火箭筒
     */
    RPG(GunCategoryTag.RPG,
            Component.translatable("customgun.guncategory.rpg")),
    /**
     * 自定义
     */
    CUSTOM(GunCategoryTag.CUSTOM,
            Component.translatable("customgun.guncategory.custom"));

    public final String categoryName;
    public final String categoryNameOld;
    public final MutableComponent categoryLang;
    GunCategory(String name, MutableComponent lang) {
        this(name, null, lang);
    }
    GunCategory(String name, String nameOld, MutableComponent lang) {
        this.categoryName = name;
        this.categoryNameOld = nameOld;
        this.categoryLang = lang;
    }

    @Override public String getTagName() {
        return this.categoryName;
    }
    @Override public String getCategoryName() {
        return this.categoryName;
    }

    /**
     * 调用方手动copy，不然会缓存旧{@link Language}
     */
    public final MutableComponent getCategoryLang() {
        return this.categoryLang;
    }

    private static final Map<String, GunCategory> GUN_CATEGORIES = new HashMap<>();

    static {
        for (GunCategory type : values()) {
            GUN_CATEGORIES.put(type.categoryName, type);
            GUN_CATEGORIES.put(type.categoryName.toUpperCase(Locale.ENGLISH), type);
            if (type.categoryNameOld != null) {
                GUN_CATEGORIES.put(type.categoryNameOld, type);
            }
        }
    }

    public static @Nullable GunCategory fromString(String name) {
        return name != null ? GUN_CATEGORIES.get(name) : null;
    }
    public String toString() {
        return this.categoryName;
    }
}