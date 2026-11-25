package dev.xcolorful.customgun.core.item.attachment;

import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentDataAccessor;
import dev.xcolorful.customgun.core.api.minecraft.item.ItemType;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.gui.tooltip.attachment.AttachmentTooltip;
import dev.xcolorful.customgun.core.init.registry.ModItems;
import dev.xcolorful.customgun.core.resource.data.index.AttachmentIndex;
import dev.xcolorful.customgun.core.resource.instance.data.AttachmentIndexInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AttachmentItem extends Item implements IAttachment, AttachmentDataAccessor {

    protected AttachmentItem(Properties properties) {
        super(properties);
    }
    public AttachmentItem() {
        this(ModItems.CUSTOM_ITEM_PROPERTY.apply(ItemType.ATTACHMENT.getRegistryLocation()));
    }

    // --------Item--------

    @Override
    public @NotNull Component getName(@NotNull ItemStack attachmentItem) {
        var attachmentLocation = this.getAttachmentLocation(attachmentItem);
        @Nullable AttachmentIndexInstance attachmentIndexInstance = ResourceApi.getAttachmentIndexInstance(attachmentLocation);
        if (attachmentIndexInstance == null) return super.getName(attachmentItem);

        AttachmentIndex attachmentIndex = attachmentIndexInstance.getPojo();
        return Component.translatable(attachmentIndex.getNameLang());
    }

    /**
     * 获取供客户端使用的 Tooltip 信息
     */
    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack attachmentItem) {
        return Optional.ofNullable(AttachmentTooltip.fromItem(attachmentItem));
    }
}
