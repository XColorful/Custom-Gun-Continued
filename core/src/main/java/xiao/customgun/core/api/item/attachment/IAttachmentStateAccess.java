package xiao.customgun.core.api.item.attachment;

import net.minecraft.world.item.ItemStack;

public interface IAttachmentStateAccess {

    /**
     * 获取tooltip掩码 (服务端处理数据，客户端读取)
     */
    boolean hasTooltipMask(ItemStack attachmentItem);
    int getTooltipMask(ItemStack attachmentItem);
    void setTooltipMask(ItemStack attachmentItem, int tooltipMask);
}
