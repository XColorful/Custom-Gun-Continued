package dev.xcolorful.customgun.core.api.resource.data.tag;

import dev.xcolorful.customgun.CustomGun;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public enum BypassBulletType {
    /**
     * 暂未使用，保留枚举order
     */
    @Deprecated BLOCK(Registries.BLOCK),
    ENTITY(Registries.ENTITY_TYPE);

    public final ResourceKey<? extends Registry<?>> tagKey;
    public final TagKey<?> armorEnchantment;
    public final TagKey<?> invulnerabilityEnchantment;
    <T> BypassBulletType(ResourceKey<? extends Registry<T>> registryKey) {
        this.tagKey = registryKey;
        this.armorEnchantment = TagKey.create(registryKey,
                CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s/%s",
                        CustomGun.MOD_ID,
                        BypassBulletTag.BYPASS_BULLET,
                        BypassBulletTag.ARMOR_ENCHANTMENT)));
        this.invulnerabilityEnchantment = TagKey.create(registryKey,
                CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s/%s",
                        CustomGun.MOD_ID,
                        BypassBulletTag.BYPASS_BULLET,
                        BypassBulletTag.INVULNERABILITY_ENCHANTMENT)));
    }

    @SuppressWarnings("unchecked")
    public <T> TagKey<T> getArmorEnchantment() {
        return (TagKey<T>) armorEnchantment;
    }
    @SuppressWarnings("unchecked")
    public <T> TagKey<T> getInvulnerabilityEnchantment() {
        return (TagKey<T>) invulnerabilityEnchantment;
    }
}
