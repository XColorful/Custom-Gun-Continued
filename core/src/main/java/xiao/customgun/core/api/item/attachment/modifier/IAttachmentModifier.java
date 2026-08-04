/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.attachment.modifier;

import xiao.customgun.core.api.item.IItemModifier;
import xiao.customgun.core.api.item.gun.modifier.IGunModifier;
import xiao.customgun.core.resource.data.data.AttachmentData;

/*
文档译名: 配件修饰工具 (XiaoColorful译)
 */
/**
 * 配件修饰工具
 */
public interface IAttachmentModifier<K, V> extends IItemModifier<AttachmentData, K, V>,
        IGunModifier<AttachmentData, K, V> {
}
