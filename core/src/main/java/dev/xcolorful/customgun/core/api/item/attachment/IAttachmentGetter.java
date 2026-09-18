package dev.xcolorful.customgun.core.api.item.attachment;

import dev.xcolorful.customgun.core.api.item.IAttachment;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IAttachmentGetter {

    static @Nullable IAttachment fromItemStack(@Nullable ItemStack attachmentItem) {
        if (attachmentItem == null) return null;
        return attachmentItem.getItem() instanceof IAttachment iAttachment ? iAttachment : null;
    }

    // --------Deprecated--------

    @Deprecated static @Nullable IAttachment getIAttachmentOrNull(@Nullable ItemStack attachmentItem) {
        return fromItemStack(attachmentItem);
    }
}
