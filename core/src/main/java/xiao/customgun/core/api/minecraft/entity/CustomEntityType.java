package xiao.customgun.core.api.minecraft.entity;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.resource.ResourceTag;

import java.util.HashMap;
import java.util.Map;

public enum CustomEntityType implements ResourceTag.RegistryTag {
    // Gun Entity
    GUN_PROJECTILE(CustomEntityTypeTag.GUN_PROJECTILE);

    public final String typeName;
    public final String registryName;
    public final ResourceLocation registryLocation;
    CustomEntityType(String name) {
        this.typeName = name;
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
