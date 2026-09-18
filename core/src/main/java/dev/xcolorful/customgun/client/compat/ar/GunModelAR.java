package dev.xcolorful.customgun.client.compat.ar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GunModelAR {

    /**
     * @return 是否接管渲染
     */
    public static boolean render(PoseStack matrixStack,
                                 ItemDisplayContext transformType,
                                 RenderType renderType,
                                 int light, int overlay,
                                 ItemStack gunItem) {
        // mixin注入点
        return false;
    }
}
