package dev.xcolorful.customgun.core.api.resource.data.tag;

import dev.xcolorful.customgun.CustomGun;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public enum RaytraceType {
    BLOCK(Registries.BLOCK),
    ENTITY(Registries.ENTITY_TYPE);

    public final ResourceKey<? extends Registry<?>> tagKey;
    public final TagKey<?> projectilePassthrough;
    <T> RaytraceType(ResourceKey<? extends Registry<T>> registryKey) {
        this.tagKey = registryKey;
        this.projectilePassthrough = TagKey.create(registryKey,
                CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s/%s",
                        CustomGun.MOD_ID,
                        RaytraceTag.RAYTRACE,
                        RaytraceTag.PROJECTILE_PASSTHROUGH)));
    }

    @SuppressWarnings("unchecked")
    public <T> TagKey<T> getProjectilePassthrough() {
        return (TagKey<T>) projectilePassthrough;
    }
}
