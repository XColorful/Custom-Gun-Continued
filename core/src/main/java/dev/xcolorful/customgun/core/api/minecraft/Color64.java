package dev.xcolorful.customgun.core.api.minecraft;

import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 6-bit RGB 颜色 (共64色)
 */
public enum Color64 {
    /**
     * {@link ChatFormatting#BLACK}
     */
    _000000(0x00, 0x00, 0x00),
    _000055(0x00, 0x00, 0x55),
    /**
     * {@link ChatFormatting#DARK_BLUE}
     */
    _0000AA(0x00, 0x00, 0xAA),
    _0000FF(0x00, 0x00, 0xFF),
    _005500(0x00, 0x55, 0x00),
    _005555(0x00, 0x55, 0x55),
    _0055AA(0x00, 0x55, 0xAA),
    _0055FF(0x00, 0x55, 0xFF),
    /**
     * {@link ChatFormatting#DARK_GREEN}
     */
    _00AA00(0x00, 0xAA, 0x00),
    _00AA55(0x00, 0xAA, 0x55),
    /**
     * {@link ChatFormatting#DARK_AQUA}
     */
    _00AAAA(0x00, 0xAA, 0xAA),
    _00AAFF(0x00, 0xAA, 0xFF),
    _00FF00(0x00, 0xFF, 0x00),
    _00FF55(0x00, 0xFF, 0x55),
    _00FFAA(0x00, 0xFF, 0xAA),
    _00FFFF(0x00, 0xFF, 0xFF),

    _550000(0x55, 0x00, 0x00),
    _550055(0x55, 0x00, 0x55),
    _5500AA(0x55, 0x00, 0xAA),
    _5500FF(0x55, 0x00, 0xFF),
    _555500(0x55, 0x55, 0x00),
    /**
     * {@link ChatFormatting#DARK_GRAY}
     */
    _555555(0x55, 0x55, 0x55),
    _5555AA(0x55, 0x55, 0xAA),
    /**
     * {@link ChatFormatting#BLUE}
     */
    _5555FF(0x55, 0x55, 0xFF),
    _55AA00(0x55, 0xAA, 0x00),
    _55AA55(0x55, 0xAA, 0x55),
    _55AAAA(0x55, 0xAA, 0xAA),
    _55AAFF(0x55, 0xAA, 0xFF),
    _55FF00(0x55, 0xFF, 0x00),
    /**
     * {@link ChatFormatting#GREEN}
     */
    _55FF55(0x55, 0xFF, 0x55),
    _55FFAA(0x55, 0xFF, 0xAA),
    /**
     * {@link ChatFormatting#AQUA}
     */
    _55FFFF(0x55, 0xFF, 0xFF),

    /**
     * {@link ChatFormatting#DARK_RED}
     */
    _AA0000(0xAA, 0x00, 0x00),
    _AA0055(0xAA, 0x00, 0x55),
    /**
     * {@link ChatFormatting#DARK_PURPLE}
     */
    _AA00AA(0xAA, 0x00, 0xAA),
    _AA00FF(0xAA, 0x00, 0xFF),
    _AA5500(0xAA, 0x55, 0x00),
    _AA5555(0xAA, 0x55, 0x55),
    _AA55AA(0xAA, 0x55, 0xAA),
    _AA55FF(0xAA, 0x55, 0xFF),
    _AAAA00(0xAA, 0xAA, 0x00),
    _AAAA55(0xAA, 0xAA, 0x55),
    /**
     * {@link ChatFormatting#GRAY}
     */
    _AAAAAA(0xAA, 0xAA, 0xAA),
    _AAAAFF(0xAA, 0xAA, 0xFF),
    _AAFF00(0xAA, 0xFF, 0x00),
    _AAFF55(0xAA, 0xFF, 0x55),
    _AAFFAA(0xAA, 0xFF, 0xAA),
    _AAFFFF(0xAA, 0xFF, 0xFF),

    _FF0000(0xFF, 0x00, 0x00),
    _FF0055(0xFF, 0x00, 0x55),
    _FF00AA(0xFF, 0x00, 0xAA),
    _FF00FF(0xFF, 0x00, 0xFF),
    /**
     * {@link ChatFormatting#RED}
     */
    _FF5555(0xFF, 0x55, 0x55),
    _FF5500(0xFF, 0x55, 0x00),
    _FF55AA(0xFF, 0x55, 0xAA),
    /**
     * {@link ChatFormatting#LIGHT_PURPLE}
     */
    _FF55FF(0xFF, 0x55, 0xFF),
    /**
     * {@link ChatFormatting#GOLD}
     */
    _FFAA00(0xFF, 0xAA, 0x00),
    _FFAA55(0xFF, 0xAA, 0x55),
    _FFAAAA(0xFF, 0xAA, 0xAA),
    _FFAAFF(0xFF, 0xAA, 0xFF),
    /**
     * {@link ChatFormatting#YELLOW}
     */
    _FFFF55(0xFF, 0xFF, 0x55),
    _FFFF00(0xFF, 0xFF, 0x00),
    _FFFFAA(0xFF, 0xFF, 0xAA),
    /**
     * {@link ChatFormatting#WHITE}
     */
    _FFFFFF(0xFF, 0xFF, 0xFF);

    public final int r;
    public final int g;
    public final int b;
    Color64(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getR() {
        return r;
    }
    public int getG() {
        return g;
    }
    public int getB() {
        return b;
    }
    public int getRGB() {
        return r << 16 | g << 8 | b;
    }
    public int getARGB() {
        return 0xFF000000 | r << 16 | g << 8 | b;
    }
    public Color getColor() {
        return new Color(r, g, b);
    }

    private static final Map<Integer, Color64> rgbToColor64 = new HashMap<>();

    static {
        for (Color64 color64 : Color64.values()) {
            rgbToColor64.put(color64.getRGB(), color64);
            rgbToColor64.put(color64.getARGB(), color64);
        }
    }

    public static @NotNull Color64 fromChatFormatting(ChatFormatting chatFormatting) {
        if (chatFormatting == null) return _000000;

        @Nullable Integer rgb = chatFormatting.getColor();
        if (rgb != null) {
            Color64 color64 = rgbToColor64.get(rgb & 0xFFFFFF);
            if (color64 != null) {
                return color64;
            }
        }
        return _FFFFFF;
    }
}