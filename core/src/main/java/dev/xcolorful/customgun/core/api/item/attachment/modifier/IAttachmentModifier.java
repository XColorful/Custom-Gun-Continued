package dev.xcolorful.customgun.core.api.item.attachment.modifier;

import dev.xcolorful.customgun.core.api.item.IItemModifier;
import dev.xcolorful.customgun.core.api.item.gun.modifier.IGunModifier;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;

/*
文档译名: 配件修饰工具 (XiaoColorful译)
 */
/**
 * 配件修饰工具
 */
public interface IAttachmentModifier<K, V> extends IItemModifier<AttachmentData, K, V>,
        IGunModifier<AttachmentData, K, V> {
}
