package xiao.customgun.client.api.gui.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public interface TooltipPart<T extends BaseTooltipContext<?>> {

    void build(T context);

    int measureHeight(T context);

    void renderText(T context,
                    Font font, int pX, int pY,
                    Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource);

    void renderImage(T context,
                     Font font, int pX, int pY,
                     int width, int height,
                     GuiGraphics guiGraphics);
}
