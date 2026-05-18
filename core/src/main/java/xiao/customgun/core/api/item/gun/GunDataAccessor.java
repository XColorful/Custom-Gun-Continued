/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.client.resource.assets.display.AttachmentDisplay;
import xiao.customgun.core.api.item.GunTag;
import xiao.customgun.core.api.item.attachment.AttachmentCategory;
import xiao.customgun.core.api.item.attachment.AttachmentDataAccessor;
import xiao.customgun.core.util.NBTUtils;

public interface GunDataAccessor extends IGunDataGetter, IGunDataSetter {

    // --------IGunDataGetter--------

    @Override
    default FireModeType getFireModeType(ItemStack gunItem) {
        if (gunItem.isEmpty()) return null;
        FireModeType fireModeType = FireModeType.fromString(NBTUtils.getString(gunItem, GunTag.FIRE_MODE_TYPE));
        return fireModeType != null ? fireModeType : FireModeType.DEFAULT;
    }

    @Override
    default float getScopeZoomScale(ItemStack gunItem) {
        float zoomScale = 1.0F;
        var scopeLocation = this.getAttachmentLocation(gunItem, AttachmentCategory.SCOPE);
        @Nullable CompoundTag attachmentCustomDataTag = this.getAttachmentCustomDataTag(gunItem, AttachmentCategory.SCOPE);
        int scopeViewIndex = AttachmentDataAccessor.INSTANCE.getScopeViewIndex(attachmentCustomDataTag);
        if (CustomGun.getMcSide().isClientSide()) {
            /**
             * {@link AttachmentDisplay#getScopeZoomScale()}
             */
            float[] scopeZoomScale = new float[]{1.0F, 1.0F}; // TODO ClientResourceApi
            if (scopeZoomScale != null) {
                return scopeZoomScale[scopeViewIndex % scopeZoomScale.length];
            }
        }
        return zoomScale;
    }

    // --------IGunAttachmentDataGetter--------

//    1.20.1
//    枪械物品序列化字符串 (第一个字符从花括号开始)
//    {
//        "id": "customgun:gun",
//        "count": 1,
//        "tag": {
//            "attachment_scope": {
//                "attachment_id": ""
//            }
//        }
//    }
//    配件物品序列化字符串
//    {
//        "id": "customgun:attachment",
//        "count": 1,
//        "tag": {
//            "attachment_id": ""
//        }
//    }
//    1.21.1+
//    枪械物品序列化字符串
//    {
//        "id": "customgun:gun",
//        "components": {
//            "custom_data": {
//                "attachment_scope":{
//                    "attachment_id": ""
//                }
//            }
//        }
//    }
//    配件物品序列化字符串
//    {
//        "id": "customgun:attachment",
//        "components": {
//            "custom_data": {
//                "attachment_id": ""
//            }
//        }
//    }
//    1.21.1+将在卸载时手动设置NBT(NBTUtils封装的就已经是将Tag放进custom_data)
//    1.20.1里获取的tag直接视为custom_data，而不是将attachment_scope视为序列化字符串的Tag
//    即忽略"Count"/"count"和"id"，直接创建一个count:1,id:"customgun:attachment"的物品并借NBTUtils写到custom_data
    @Override
    default @Nullable CompoundTag getAttachmentCustomDataTag(ItemStack gunItem, AttachmentCategory attachmentCategory) {
        // 快的情况先排除
        @Nullable var customData = NBTUtils.getCustomData(gunItem);
        if (customData == null) return null;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);

        // 稍慢的检查
        if (!isAttachmentEnabled(gunItem, attachmentCategory)) {
            return null;
        }

        return NBTUtils.getCompoundTag(customDataTag, attachmentCategory.getTagName());
    }
}
