/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.entity.shooter.modifier;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.IItemModifier;
import xiao.customgun.core.api.item.attachment.AttachmentCategory;
import xiao.customgun.core.api.item.attachment.modifier.AttachmentModifierType;
import xiao.customgun.core.api.item.attachment.modifier.IAttachmentModifier;
import xiao.customgun.core.api.item.gun.modifier.GunModifierType;
import xiao.customgun.core.api.item.gun.modifier.IGunModifier;
import xiao.customgun.core.api.item.gun.modifier.IGunModifierHolder;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

import java.util.HashMap;
import java.util.Map;

/*
文档译名: 射手枪械修饰缓存 (XiaoColorful译)
- Cache后缀还是决定保留，以区分持久化保存的属性和临时计算值
 */
public final class ShooterGunModifierCache {

    private final Map<GunModifierType, Object> modifierType_values;

    private ShooterGunModifierCache() {
        this.modifierType_values = new HashMap<>();
    }
    public static ShooterGunModifierCache of(@NotNull GunIndexInstance gunIndexInstance,
                                             @NotNull IGun iGun, @NotNull ItemStack gunItem) {
        ShooterGunModifierCache cache = new ShooterGunModifierCache();
        cache.initAttachmentModifiers(gunIndexInstance, iGun, gunItem);
        return cache;
    }

    private static final AttachmentModifierType[] ATTACHMENT_MODIFIER_TYPES = AttachmentModifierType.values();
    private static final AttachmentCategory[] ATTACHMENT_CATEGORIES = AttachmentCategory.values();
    private void initAttachmentModifiers(@NotNull GunIndexInstance gunIndexInstance,
                                         @NotNull IGun iGun, @NotNull ItemStack gunItem) {
        GunData gunData = gunIndexInstance.getGunData();

        // base值设置
        for (AttachmentModifierType type : ATTACHMENT_MODIFIER_TYPES) {
            IAttachmentModifier<?, ?> modifier = type.getModifier();
            modifierType_values.put(type.getGunModifierType(), modifier.getBase(iGun, gunItem, gunData));
        }

        // Attachment modifier
        for (AttachmentCategory category : ATTACHMENT_CATEGORIES) {
            var attachmentLocation = iGun.getAttachmentLocation(gunItem, category);
            // TODO
        }
    }

    // --------Getter & Setter--------

    /**
     * <ol>
     *     <li>{@link IGunModifier}{@code <T, K, V>}继承{@link IItemModifier}{@code <T, K, V>}</li>
     *     <li>
     *         {@link IGunModifierHolder#getGunModifier()}返回的Modifier实例，与对应的{@link IGunModifier}子接口共同满足同一泛型约束，
     *         Java编译器会检查继承路径上的泛型参数一致性
     *     </li>
     *     <li>
     *         在满足API约束时，{@link Class#isInstance(Object)}可验证Modifier实例类型，并由{@link IGunModifier}子接口约束{@code V}
     *     </li>
     * </ol>
     *
     * @param modifierType 提供Modifier实例的类型标识
     * @param modifierClass {@link IGunModifier}子接口类型，API层已固定其{@code K}与{@code V}泛型参数
     * @return 对应Modifier缓存值
     */
    @SuppressWarnings("unchecked")
    public @Nullable <T extends ResourcePojo<T>, K, V> V getValue(IGunModifierHolder modifierType, Class<? extends IGunModifier<T, K, V>> modifierClass) {
        if (!modifierClass.isInstance(modifierType.getGunModifier())) {
            CustomGun.LOGGER.error("ShooterGunModifierCache: Failed to get modifier value, modifier type {} with modifier {} does not implement {}",
                    modifierType,
                    modifierType.getGunModifier().getClass().getName(),
                    modifierClass.getName()
            );
            return null;
        }
        @Nullable Object value = modifierType_values.get(modifierType.getGunModifierType());
        return value != null ? (V) value : null;
    }
    /**
     * 设计同{@link #getValue}
     */
    public <T extends ResourcePojo<T>, K, V> void setValue(IGunModifierHolder modifierType, Class<? extends IGunModifier<T, K, V>> modifierClass, V value) {
        if (!modifierClass.isInstance(modifierType.getGunModifier())) {
            CustomGun.LOGGER.error("ShooterGunModifierCache: Failed to set modifier value, modifier type {} with modifier {} does not implement {}",
                    modifierType,
                    modifierType.getGunModifier().getClass().getName(),
                    modifierClass.getName()
            );
            return;
        }
        modifierType_values.put(modifierType.getGunModifierType(), value);
    }

    // --------Deprecated--------

    /**
     * @deprecated 如果使用场景没有枪械，就不需要这个缓存了，直接设置null就行
     */
    @Deprecated
    public static ShooterGunModifierCache empty() {
        return new ShooterGunModifierCache();
    }
}
