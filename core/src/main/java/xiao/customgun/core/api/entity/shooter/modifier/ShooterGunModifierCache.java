/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.entity.shooter.modifier;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.attachment.modifier.AttachmentModifierType;
import xiao.customgun.core.api.item.attachment.modifier.IAttachmentModifier;
import xiao.customgun.core.util.ClassUtils;

/*
文档译名: 射手枪械修饰缓存 (XiaoColorful译)
- Cache后缀还是决定保留，以区分持久化保存的属性和临时计算值
 */
public class ShooterGunModifierCache {

    public ShooterGunModifierCache() {
    }

    private final ClassUtils.ArrayMap<AttachmentModifierType, AttachmentModifier_Value<?, ?>> attachmentModifier_values = new ClassUtils.ArrayMap<>(AttachmentModifier_Value::getType);

    private static class AttachmentModifier_Value<K, V> {
        public final AttachmentModifierType type;
        public final IAttachmentModifier<K, V> modifier;
        public @Nullable V value;
        public AttachmentModifier_Value(AttachmentModifierType type, IAttachmentModifier<K, V> modifier) {
            this.type = type;
            this.modifier = modifier;
        }
        public AttachmentModifierType getType() {
            return this.type;
        }
    }
}
