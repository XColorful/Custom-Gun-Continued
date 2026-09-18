package dev.xcolorful.customgun.core.api.item;

import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentDataAccess;
import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentGetter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IAttachment extends IAttachmentDataAccess, IAttachmentGetter,
        IPojoItem {

    @Override
    default @NotNull ResourceLocation getPojoLocation(ItemStack attachmentItem) {
        return this.getAttachmentLocation(attachmentItem);
    }
    @Override
    default void setPojoLocation(ItemStack attachmentItem, ResourceLocation attachmentLocation) {
        this.setAttachmentLocation(attachmentItem, attachmentLocation);
    }
}
