package dev.xcolorful.customgun.client.api.minecraft.stencil;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("1.21.6")
public interface IStencilOperator {

    void applyStencil(StencilState state);
}
