package dev.xcolorful.customgun.neoforgeclient.minecraft.stencil;

import dev.xcolorful.customgun.client.api.minecraft.stencil.StencilFunction;

public class StencilFunctionHelper {

    public static StencilFunction convert(com.mojang.renderpearl.api.pipeline.CompareOp compareOp) {
        return switch (compareOp) {
            case NEVER_PASS -> StencilFunction.NEVER;
            case ALWAYS_PASS -> StencilFunction.ALWAYS;
            case LESS_THAN -> StencilFunction.LESS;
            case LESS_THAN_OR_EQUAL -> StencilFunction.LEQUAL;
            case EQUAL -> StencilFunction.EQUAL;
            case GREATER_THAN_OR_EQUAL -> StencilFunction.GEQUAL;
            case GREATER_THAN -> StencilFunction.GREATER;
            case NOT_EQUAL -> StencilFunction.NOTEQUAL;
        };
    }
    public static com.mojang.renderpearl.api.pipeline.CompareOp convert(StencilFunction stencilFunction) {
        return switch (stencilFunction) {
            case NEVER -> com.mojang.renderpearl.api.pipeline.CompareOp.NEVER_PASS;
            case LESS -> com.mojang.renderpearl.api.pipeline.CompareOp.LESS_THAN;
            case EQUAL -> com.mojang.renderpearl.api.pipeline.CompareOp.EQUAL;
            case LEQUAL -> com.mojang.renderpearl.api.pipeline.CompareOp.LESS_THAN_OR_EQUAL;
            case GREATER -> com.mojang.renderpearl.api.pipeline.CompareOp.GREATER_THAN;
            case NOTEQUAL -> com.mojang.renderpearl.api.pipeline.CompareOp.NOT_EQUAL;
            case GEQUAL -> com.mojang.renderpearl.api.pipeline.CompareOp.GREATER_THAN_OR_EQUAL;
            case ALWAYS -> com.mojang.renderpearl.api.pipeline.CompareOp.ALWAYS_PASS;
        };
    }
}
