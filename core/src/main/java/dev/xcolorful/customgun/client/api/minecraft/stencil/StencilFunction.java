package dev.xcolorful.customgun.client.api.minecraft.stencil;

import org.lwjgl.opengl.GL11;

public enum StencilFunction {
    // ----AlphaFunction----
    NEVER(GL11.GL_NEVER),
    LESS(GL11.GL_LESS),
    EQUAL(GL11.GL_EQUAL),
    LEQUAL(GL11.GL_LEQUAL),
    GREATER(GL11.GL_GREATER),
    NOTEQUAL(GL11.GL_NOTEQUAL),
    GEQUAL(GL11.GL_GEQUAL),
    ALWAYS(GL11.GL_ALWAYS),
    ;

    public final int symbol;
    StencilFunction(int symbol) {
        this.symbol = symbol;
    }

    public static StencilFunction of(int symbol) {
        return switch (symbol) {
            case GL11.GL_NEVER -> NEVER;
            case GL11.GL_LESS -> LESS;
            case GL11.GL_EQUAL -> EQUAL;
            case GL11.GL_LEQUAL -> LEQUAL;
            case GL11.GL_GREATER -> GREATER;
            case GL11.GL_NOTEQUAL -> NOTEQUAL;
            case GL11.GL_GEQUAL -> GEQUAL;
            case GL11.GL_ALWAYS -> ALWAYS;
            default -> ALWAYS;
        };
    }
}
