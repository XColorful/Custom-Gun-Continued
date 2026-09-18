package dev.xcolorful.customgun.client.api.model;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.client.model.AttachmentModelObject;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 原模组的{@link AttachmentModelObject#render}在高版本参数有变，用接口来把变更从 类 提升到 api
 */
public interface IAttachmentModelObjectRender {

    void render(PoseStack matrixStack,
                ItemDisplayContext transformType,
                RenderType renderType,
                int light, int overlay,
                ItemStack gunItem, @Nullable ItemStack attachmentItem);

    // --------Deprecated--------
}
