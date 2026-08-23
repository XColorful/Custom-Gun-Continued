package dev.xcolorful.customgun.forgeclient.mixin.item;

import dev.xcolorful.customgun.client.api.item.IItemBEWLR;
import dev.xcolorful.customgun.client.renderer.item.AttachmentItemRenderer;
import dev.xcolorful.customgun.core.item.attachment.AttachmentItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AttachmentItem.class)
public abstract class AttachmentItemMixin extends Item implements IItemBEWLR {

    public AttachmentItemRenderer cgc$renderer;

    public AttachmentItemMixin(Properties properties) {
        super(properties);
    }

    // --------IItemBEWLR--------

    @Override
    public AttachmentItemRenderer cgc$getBEWLR() {
        if (this.cgc$renderer == null) this.cgc$renderer = new AttachmentItemRenderer();

        return this.cgc$renderer;
    }
}
