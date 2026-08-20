package dev.xcolorful.customgun.client.api.gui;

import org.jetbrains.annotations.ApiStatus;

/**
 * 用于GUI尺寸计算
 * <ul>
 *     <li>该类以1920x1080下的像素为计算标准</li>
 * </ul>
 */
public class GuiSize {

    /**
     * 1920x1080p下，每1数值对应4像素
     */
    @ApiStatus.Internal public static final int _sizeToPixelRatio = 4;

    /**
     * 以原版聊天栏为标准
     */
    public static class Text {
        /**
         * 文本高32像素
         */
        @ApiStatus.Internal public static final int _textHeightPixel = 32;
        /**
         * 阴影在文字下方扩展2像素
         */
        @ApiStatus.Internal public static final int _textShadowExpansionPixel = 2;
        /**
         * 每行文本(含阴影)下方额外空2像素
         */
        @ApiStatus.Internal public static final int _textSpaceExpansionPixel = 2;

        public static final int TEXT_LINE_HEIGHT = (_textHeightPixel + _textShadowExpansionPixel + _textSpaceExpansionPixel) / _sizeToPixelRatio;

        /**
         * 原版物品tooltip每行间隔4像素
         */
        @ApiStatus.Internal public static final int _textLineSpacingPixel = 4;

        public static final int TEXT_LINE_SPACING = _textLineSpacingPixel / _sizeToPixelRatio;

        private Text() {}
    }

    /**
     * GUI物品尺寸 (装备栏)
     */
    public static class Item {
        /**
         * 图像高64像素
         */
        @ApiStatus.Internal public static final int _itemHeightPixel = 64;
        /**
         * 图像宽64像素
         */
        @ApiStatus.Internal public static final int _itemWidthPixel = 64;

        public static final int ITEM_HEIGHT = _itemHeightPixel / _sizeToPixelRatio;
        public static final int ITEM_WIDTH = _itemWidthPixel / _sizeToPixelRatio;

        private Item() {}
    }

    private GuiSize() {}
}
