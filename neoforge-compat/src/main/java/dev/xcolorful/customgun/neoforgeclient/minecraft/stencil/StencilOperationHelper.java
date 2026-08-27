package dev.xcolorful.customgun.neoforgeclient.minecraft.stencil;

import dev.xcolorful.customgun.client.api.minecraft.stencil.StencilOperation;

public class StencilOperationHelper {

    public static StencilOperation convert(net.neoforged.neoforge.client.stencil.StencilOperation stencilOperation) {
        return switch (stencilOperation) {
            case KEEP -> StencilOperation.KEEP;
            case ZERO -> StencilOperation.ZERO;
            case REPLACE -> StencilOperation.REPLACE;
            case INCR -> StencilOperation.INCR;
            case DECR -> StencilOperation.DECR;
            case INVERT -> StencilOperation.INVERT;
            default -> StencilOperation.KEEP;
        };
    }

    public static net.neoforged.neoforge.client.stencil.StencilOperation convert(StencilOperation stencilOperation) {
        return switch (stencilOperation) {
            case ZERO -> net.neoforged.neoforge.client.stencil.StencilOperation.ZERO;
            case INVERT -> net.neoforged.neoforge.client.stencil.StencilOperation.INVERT;
            case KEEP -> net.neoforged.neoforge.client.stencil.StencilOperation.KEEP;
            case REPLACE -> net.neoforged.neoforge.client.stencil.StencilOperation.REPLACE;
            case INCR -> net.neoforged.neoforge.client.stencil.StencilOperation.INCR;
            case DECR -> net.neoforged.neoforge.client.stencil.StencilOperation.DECR;
        };
    }
}