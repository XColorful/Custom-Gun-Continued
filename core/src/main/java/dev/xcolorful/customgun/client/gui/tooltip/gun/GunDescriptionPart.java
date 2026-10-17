package dev.xcolorful.customgun.client.gui.tooltip.gun;

import dev.xcolorful.customgun.client.api.item.gun.GunTooltipMask;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.core.api.minecraft.Color64;
import dev.xcolorful.customgun.core.resource.data.index.GunIndex;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/*
描述内容，描述内容
，描述内容。
 */
public final class GunDescriptionPart extends AbstractTooltipPart implements GunTooltipPart {
    public static final GunDescriptionPart INSTANCE = new GunDescriptionPart();
    private GunDescriptionPart() {}

    /**
     * 扩展模组可以动态识别当前鼠标到边界的距离来调整
     */
    public static int getGunDescriptionLineWidth() {
        // mixin注入点
        return RenderConfig.GUN_DESCRIPTION_WIDTH.get();
    }
    /**
     * 改用{@link #getGunDescriptionLineWidth()}
     */
    @Deprecated(forRemoval = true) @ApiStatus.Internal public static volatile int _textLineWidth = 300;
    private static final Color64 _defaultDescriptionColor = Color64.fromChatFormatting(ChatFormatting.GRAY);

    @Override
    public void build(ClientGunTooltip.Context context, Font font) {
        @Nullable GunIndexInstance gunIndexInstance = context.gunIndexInstance;
        if (gunIndexInstance == null) return;

        List<FormattedCharSequence> desc; {
            GunIndex gunIndex = gunIndexInstance.getPojo();
            String tooltipLang = gunIndex.getTooltipLang();

            desc = font.split(Component.translatable(tooltipLang), getGunDescriptionLineWidth());
        }
        context.view.desc = desc;
        for (int i = 0; i < desc.size(); i++) {
            FormattedCharSequence sequence = desc.get(i);
            context.widenMaxWidth(font.width(sequence));
        }
    }

    @Override
    public int measureHeight(ClientGunTooltip.Context context) {
        if (!context.visibleParts.contains(GunTooltipMask.DESCRIPTION)) return 0;

        @Nullable List<FormattedCharSequence> desc = context.view.desc;
        if (desc == null || desc.isEmpty()) return 0;

        return
//                textLineSpacing +
                        desc.size() * textLineHeight;
    }

    @Override
    public void renderText(ClientGunTooltip.Context context,
                           int startX, int startY) {
        int currentX = startX;
        int currentY = startY;

        @Nullable List<FormattedCharSequence> desc = context.view.desc;
        if (desc != null) {
            /**
             * {@link GunTooltipMask}第一项前不加行间距，后面的不管
             */
//            yOffset += textLineSpacing;

            // 枪械描述
            for (int i = 0; i < desc.size(); i++) {
                // 单行枪械描述
                FormattedCharSequence sequence = desc.get(i);
                context.drawText(sequence,
                        currentX, currentY,
                        _defaultDescriptionColor.getRGB()
                );
                currentY += textLineHeight;
            }
        }
    }
}
