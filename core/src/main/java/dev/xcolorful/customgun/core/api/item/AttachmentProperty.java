/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item;

import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentDataAccess;
import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentStateAccess;
import dev.xcolorful.customgun.core.api.item.attachment._IAttachmentPropertyAccess;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public enum AttachmentProperty implements ResourceTag {
    // IAttachmentDataAccess
    ATTACHMENT_LOCATION(AttachmentPropertyTag.ATTACHMENT_LOCATION,
            IAttachmentDataAccess::getAttachmentLocation,
            IAttachmentDataAccess::setAttachmentLocation),
    ATTACHMENT_CATEGORY(AttachmentPropertyTag.ATTACHMENT_CATEGORY,
            IAttachmentDataAccess::getAttachmentCategory,
            IAttachmentDataAccess::setAttachmentCategory),

    SCOPE_VIEW_INDEX(AttachmentPropertyTag.SCOPE_VIEW_INDEX,
            IAttachmentDataAccess::getScopeViewIndex,
            IAttachmentDataAccess::setScopeViewIndex),
    LASER_COLOR(AttachmentPropertyTag.LASER_COLOR,
            IAttachmentDataAccess::getLaserColorInt,
            IAttachmentDataAccess::setLaserColorInt),

    // IAttachmentStateAccess
    TOOLTIP_MASK(AttachmentPropertyTag.TOOLTIP_MASK,
            IAttachmentStateAccess::getTooltipMask,
            IAttachmentStateAccess::setTooltipMask);

    public final String propertyName;
    private final BiFunction<IAttachmentDataAccess, ItemStack, ?> getter;
    private final TriConsumer<IAttachmentDataAccess, ItemStack, ?> setter;
    <T> AttachmentProperty(final String name, @Nullable BiFunction<IAttachmentDataAccess, ItemStack, T> getter, @Nullable TriConsumer<IAttachmentDataAccess, ItemStack, T> setter) {
        this.propertyName = name;
        this.getter = getter;
        this.setter = setter;
    }

    @Override public String getTagName() {
        return this.propertyName;
    }

    private static final Map<String, AttachmentProperty> PROPERTY_TYPE = new HashMap<>();

    static {
        for (AttachmentProperty property : AttachmentProperty.values()) {
            PROPERTY_TYPE.put(property.propertyName, property);
        }
    }

    public static @Nullable AttachmentProperty fromString(String name) {
        return name != null ? PROPERTY_TYPE.get(name) : null;
    }

    @Override
    public String toString() {
        return this.propertyName;
    }

    /**
     * {@link _IAttachmentPropertyAccess}
     */
    @SuppressWarnings("unchecked")
    public <T> T get(IAttachmentDataAccess access, ItemStack stack) {
        if (this.getter == null) {
            throw new UnsupportedOperationException("Property '" + this.propertyName + "' does not support read operations.");
        }
        return (T) this.getter.apply(access, stack);
    }
    /**
     * 设置配件属性
     * <p>
     * <b>注意：</b>若通过脚本引擎间接调用此方法，请务必保证传递的 {@code value} 类型与该属性期望的 Java 类型完全一致
     * <p>
     * 例如整型属性（如 SCOPE_VIEW_INDEX）必须传入 {@link java.lang.Integer}，否则会导致底层 Setter 强转崩溃
     */
    @SuppressWarnings("unchecked")
    public <T> void set(IAttachmentDataAccess access, ItemStack stack, T value) {
        if (this.setter == null) {
            throw new UnsupportedOperationException("Property '" + this.propertyName + "' does not support write operations.");
        }
        ((TriConsumer<IAttachmentDataAccess, ItemStack, T>) this.setter).accept(access, stack, value);
    }
}
