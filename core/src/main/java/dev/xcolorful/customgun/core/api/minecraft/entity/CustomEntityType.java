package dev.xcolorful.customgun.core.api.minecraft.entity;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum CustomEntityType implements ResourceTag.RegistryTag {
    // Gun Entity
    GUN_PROJECTILE(CustomEntityTypeTag.GUN_PROJECTILE, CustomEntityTypeTag.GUN_PROJECTILE_OLD1),

    /*
    放在扩展模组里更好
     */
    @Deprecated TARGET_MINECART(CustomEntityTypeTag.TARGET_MINECART, null);

    public final String typeName;
    public final String typeNameOld;
    public final String registryName;
    public final ResourceLocation registryLocation;
    CustomEntityType(String name, @Nullable String nameOld) {
        this.typeName = name;
        this.typeNameOld = nameOld;
        this.registryName = String.format("%s:%s", CustomGun.MOD_ID, this.typeName);
        this.registryLocation = CustomGun.getMcRegistry().createResourceLocation(this.registryName);
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getRegistryName() {
        return this.registryName;
    }
    @Override public ResourceLocation getRegistryLocation() {
        return this.registryLocation;
    }

    private static final Map<String, CustomEntityType> ENTITY_TYPES = new HashMap<>();

    static {
        for (CustomEntityType type : CustomEntityType.values()) {
            ENTITY_TYPES.put(type.typeName, type);
            if (type.typeNameOld != null) ENTITY_TYPES.put(type.typeNameOld, type);
            ENTITY_TYPES.put(type.registryName, type);
        }
    }

    public static @Nullable CustomEntityType fromString(String name) {
        return name != null ? ENTITY_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
