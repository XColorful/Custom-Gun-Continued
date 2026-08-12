package dev.xcolorful.customgun.core.util;

import net.minecraft.util.Mth;

import java.awt.*;

public class ColorUtils {

    /**
     * @param rrggbb "RRGGBB" "0xRRGGBB" "#RRGGBB"
     */
    public static Color fromRRGGBBtoColor(String rrggbb) {
        return new Color(fromRRGGBBtoInt(rrggbb));
    }
    /**
     * @param rrggbb "RRGGBB" "0xRRGGBB" "#RRGGBB"
     */
    public static int fromRRGGBBtoInt(String rrggbb) {
        if (rrggbb == null) return 0xFFFFFF;

        int startIndex = 0;
        // 跳过前缀
        if (rrggbb.startsWith("#")) {
            startIndex = 1;
        } else if (rrggbb.startsWith("0x") || rrggbb.startsWith("0X")) {
            startIndex = 2;
        }

        try {
            int len = rrggbb.length();
            if (len - startIndex != 6) {
                return 0xFFFFFF; // 长度不符合 RRGGBB 要求
            }
            return Integer.parseInt(rrggbb, startIndex, len, 16);
        } catch (NumberFormatException e) {
            return 0xFFFFFF; // 包含非十六进制字符时返回默认白
        }
    }

    /**
     * @return "0xRRGGBB"
     */
    public static String fromIntTo0xRRGGBB(int colorInt) {
        return String.format("0x%06X", colorInt & 0xFFFFFF);
    }
    /**
     * @return "#RRGGBB"
     */
    public static String fromIntTo_RRGGBB(int colorInt) {
        return String.format("#%06X", colorInt & 0xFFFFFF);
    }
    /**
     * @return "#RRGGBB"
     */
    public static String fromColorTo_RRGGBB(Color color) {
        if (color == null) return "#FFFFFF";
        return fromIntTo_RRGGBB(color.getRGB());
    }

    public static int as8BitChannel(float value) {
        return Mth.floor(value * 255.0f);
    }

    public static class ARGB32 {

        public static int toAlpha(int argb32) {
            return argb32 >> 24;
        }
        public static int toRed(int argb32) {
            return argb32 >> 16 & 255;
        }
        public static int toGreen(int argb32) {
            return argb32 >> 8 & 255;
        }
        public static int toBlue(int argb32) {
            return argb32 & 255;
        }

        public static int of(float alpha, float red, float green, float blue) {
            return of(as8BitChannel(alpha), as8BitChannel(red), as8BitChannel(green), as8BitChannel(blue));
        }
        public static int of(int alpha, int red, int gree, int blue) {
            return alpha << 24 | red << 16 | gree << 8 | blue;
        }
    }
}
