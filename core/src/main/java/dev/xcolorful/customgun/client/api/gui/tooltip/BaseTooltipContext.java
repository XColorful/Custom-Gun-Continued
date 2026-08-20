package dev.xcolorful.customgun.client.api.gui.tooltip;

import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public abstract class BaseTooltipContext<T extends BaseTooltipView> {

    /*
    原版是能在玩家悬浮在物品tooltip，且查看期间切换创造/非创造的时候动态更新，并且还需要鼠标位置跟改成创造模式后的物品栏位置重合才能做到
    鼠标移开后会重新创建(刷新)tooltip
    目前没调查渲染线程实时读是否会触发CME
     */
    public final boolean isAdvanced;
    public final boolean isCreative;

    public final @NotNull T view;
    private int maxWidth = 0;

    public BaseTooltipContext(@NotNull T view) {
        {
            Minecraft mc = Minecraft.getInstance();

            this.isAdvanced = mc.options.advancedItemTooltips;

            LocalPlayer localPlayer = mc.player;
            this.isCreative = localPlayer != null && localPlayer.isCreative();
        }

        this.view = view;
    }

    protected abstract void buildView();

    protected abstract int calculateHeight(Font font);

    // --------Getter & Setter--------

    public final int getHeight(Font font) {
        return this.calculateHeight(font);
    }
    public final int getMaxWidth() {
        return this.maxWidth;
    }

    public final void widenMaxWidth(int maxWidth) {
        this.maxWidth = Math.max(this.maxWidth, maxWidth);
    }

    // --------Cache--------

    public Font textFont = null;
    public GuiGraphics textGraphic = null;
    public Matrix4f textMatrix4f = null;
    public MultiBufferSource.BufferSource textBufferSource = null;
    @ApiStatus.Internal
    public void _clearTextCache() {
        this.textFont = null;
        this.textGraphic = null;
        this.textMatrix4f = null;
        this.textBufferSource = null;
    }

    public Font imageFont = null;
    public GuiGraphics imageGraphic = null;
    @ApiStatus.Internal
    public void _clearImageCache() {
        this.imageFont = null;
        this.imageGraphic = null;
    }

    // --------Compat--------
    // 跨版本适配层

    private static final int _backgroundRGB = 0;
    private static final int _packedLightCoords = ClientRenderUtils.LightTexture_.pack(15, 15);
    public void drawText(@NotNull Component component,
                         int currentX, int currentY,
                         int textRGB,
                         boolean hasTextShadow) {
        this.textFont.drawInBatch(component,
                currentX, currentY,
                textRGB,
                hasTextShadow,
                this.textMatrix4f,
                this.textBufferSource,
                Font.DisplayMode.NORMAL,
                _backgroundRGB,
                _packedLightCoords
        );
    }
    public void drawText(@NotNull FormattedCharSequence sequence,
                         int currentX, int currentY,
                         int textRGB,
                         boolean hasTextShadow) {
        this.textFont.drawInBatch(sequence,
                currentX, currentY,
                textRGB,
                hasTextShadow,
                this.textMatrix4f,
                this.textBufferSource,
                Font.DisplayMode.NORMAL,
                _backgroundRGB,
                _packedLightCoords
        );
    }

    public void drawItem(ItemStack itemStack,
                         int currentX, int currentY) {
        this.imageGraphic.renderItem(itemStack,
                currentX, currentY);
    }
}
