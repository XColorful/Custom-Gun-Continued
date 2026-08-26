/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.entity.shooter.modifier;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.modifier.AttachmentModifierType;
import dev.xcolorful.customgun.core.api.item.attachment.modifier.IAttachmentModifier;
import dev.xcolorful.customgun.core.api.item.gun.modifier.GunModifierType;
import dev.xcolorful.customgun.core.api.item.gun.modifier.IGunModifier;
import dev.xcolorful.customgun.core.api.item.gun.modifier.IGunModifierHolder;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void initAttachmentModifiers(@NotNull GunIndexInstance gunIndexInstance,
                                         @NotNull IGun iGun, @NotNull ItemStack gunItem) {
        GunData gunData = gunIndexInstance.getGunData();

        // base值设置
        for (AttachmentModifierType type : ATTACHMENT_MODIFIER_TYPES) {
            IAttachmentModifier modifier = type.getModifier();
            modifierType_values.put(type.getGunModifierType(), modifier.getBase(iGun, gunItem, gunData));
        }

        // 配件modifier base值
        Map<GunModifierType, List<Object>> attachmentModifiers = new HashMap<>(); {
            for (AttachmentModifierType type : ATTACHMENT_MODIFIER_TYPES) {
                attachmentModifiers.put(type.getGunModifierType(), new ArrayList<>());
            }

            for (AttachmentCategory category : ATTACHMENT_CATEGORIES) {
                if (category == AttachmentCategory.NONE) continue;

                var attachmentLocation = iGun.getAttachmentLocation(gunItem, category);
                if (ResourceTag.NULL_LOCATION.equals(attachmentLocation)) attachmentLocation = iGun.getBuiltinAttachmentLocation(gunItem, category);
                if (ResourceTag.NULL_LOCATION.equals(attachmentLocation)) continue;

                @Nullable Map<AttachmentModifierType, Object> modifiers = ResourceApi.getAttachmentModifiers(attachmentLocation);
                if (modifiers == null) continue;

                for (Map.Entry<AttachmentModifierType, Object> entry : modifiers.entrySet()) {
                    attachmentModifiers.get(entry.getKey().getGunModifierType())
                            .add(entry.getValue());
                }
            }
        }

        // 一次性计算并覆盖 base 值
        for (AttachmentModifierType type : ATTACHMENT_MODIFIER_TYPES) {
            List<Object> rawModifiers = attachmentModifiers.get(type.getGunModifierType());
            if (rawModifiers.isEmpty()) continue;

            @Nullable Object base = modifierType_values.get(type.getGunModifierType());
            if (base == null) continue;

            IAttachmentModifier modifier = type.getModifier();
            @Nullable Object evaluated = modifier.eval(rawModifiers, base);
            if (evaluated != null) {
                modifierType_values.put(type.getGunModifierType(), evaluated);
            }
        }
    }

    // --------Getter & Setter--------

    /**
     * <font color="red">内部方法</font>，外部应使用{@link dev.xcolorful.customgun.core.api.item.gun.modifier}包中各{@code I*Modifier}接口的{@code static getValue}作为入口
     * <ul>
     *     <li>各{@code I*Modifier}子接口已固定{@code K}与{@code V}，其{@code static getValue}签名保证类型安全——编译器通过子接口的返回类型验证左值{@code V}，错误类型在编译期即可发现</li>
     *     <li>直接调用本方法相当于绕过编译期检查，类型错误会推迟到运行时{@code ClassCastException}</li>
     *     <li>使用子接口{@code static getValue}时，若本API更新导致签名变化，外部模组只需重新编译即可发现所有不兼容调用点——无需等到运行时从日志排查</li>
     * </ul>
     * @param modifierType 提供Modifier实例的类型标识
     * @param modifierClass {@link IGunModifier}子接口类型，API层已通过子接口固定其{@code K}与{@code V}
     * @param <T> 资源数据源类型
     * @param <K> modifier数据源类型，由{@code modifierClass}决定
     * @param <V> 缓存值类型，由{@code modifierClass}决定
     * @return 对应Modifier缓存值，不匹配时返回{@code null}
     */
    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    public @Nullable <T extends ResourcePojo<T>, K, V> V getValue(IGunModifierHolder modifierType, Class<? extends IGunModifier> modifierClass) {
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
    @ApiStatus.Internal
    public <T extends ResourcePojo<T>, K, V> void setValue(IGunModifierHolder modifierType, Class<? extends IGunModifier> modifierClass, V value) {
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

    /**
     * 用于迁移提示用途，防止不认识IGunModifierHolder
     */
    @Deprecated(forRemoval = true)
    public @Nullable <K, V> V getCache(AttachmentModifierType modifierType, Class<? extends IAttachmentModifier<K, V>> modifierClass) {
        return this.getValue((IGunModifierHolder) modifierType, modifierClass);
    }
}
