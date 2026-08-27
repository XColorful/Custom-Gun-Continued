package dev.xcolorful.customgun.client.api.minecraft.stencil;

import org.lwjgl.opengl.GL11;

public enum StencilOperation {
    // ----BlendingFactorDest----
    ZERO(GL11.GL_ZERO),

    // ----LogicOp----
    INVERT(GL11.GL_INVERT),

    // ----StencilOp----
    KEEP(GL11.GL_KEEP),
    REPLACE(GL11.GL_REPLACE),
    INCR(GL11.GL_INCR),
    DECR(GL11.GL_DECR),
    ;

    public final int symbol;
    StencilOperation(int symbol) {
        this.symbol = symbol;
    }

    public static StencilOperation of(int symbol) {
        return switch (symbol) {
            case GL11.GL_ZERO -> ZERO;
            case GL11.GL_INVERT -> INVERT;
            case GL11.GL_KEEP -> KEEP;
            case GL11.GL_REPLACE -> REPLACE;
            case GL11.GL_INCR -> INCR;
            case GL11.GL_DECR -> DECR;
            default -> KEEP;
        };
    }
}
