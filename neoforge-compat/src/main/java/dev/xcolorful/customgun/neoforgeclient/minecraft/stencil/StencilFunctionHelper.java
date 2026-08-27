package dev.xcolorful.customgun.neoforgeclient.minecraft.stencil;

import dev.xcolorful.customgun.client.api.minecraft.stencil.StencilFunction;

public class StencilFunctionHelper {

    public static StencilFunction convert(net.neoforged.neoforge.client.stencil.StencilFunction stencilFunction) {
        return switch (stencilFunction) {
            case NEVER -> StencilFunction.NEVER;
            case ALWAYS -> StencilFunction.ALWAYS;
            case LESS -> StencilFunction.LESS;
            case LEQUAL -> StencilFunction.LEQUAL;
            case EQUAL -> StencilFunction.EQUAL;
            case GEQUAL -> StencilFunction.GEQUAL;
            case GREATER -> StencilFunction.GREATER;
            case NOTEQUAL -> StencilFunction.NOTEQUAL;
        };
    }
    public static net.neoforged.neoforge.client.stencil.StencilFunction convert(StencilFunction stencilFunction) {
        return switch (stencilFunction) {
            case NEVER -> net.neoforged.neoforge.client.stencil.StencilFunction.NEVER;
            case LESS -> net.neoforged.neoforge.client.stencil.StencilFunction.LESS;
            case EQUAL -> net.neoforged.neoforge.client.stencil.StencilFunction.EQUAL;
            case LEQUAL -> net.neoforged.neoforge.client.stencil.StencilFunction.LEQUAL;
            case GREATER -> net.neoforged.neoforge.client.stencil.StencilFunction.GREATER;
            case NOTEQUAL -> net.neoforged.neoforge.client.stencil.StencilFunction.NOTEQUAL;
            case GEQUAL -> net.neoforged.neoforge.client.stencil.StencilFunction.GEQUAL;
            case ALWAYS -> net.neoforged.neoforge.client.stencil.StencilFunction.ALWAYS;
        };
    }
}
