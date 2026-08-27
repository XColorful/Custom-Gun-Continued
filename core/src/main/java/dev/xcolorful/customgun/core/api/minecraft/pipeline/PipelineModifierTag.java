package dev.xcolorful.customgun.core.api.minecraft.pipeline;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("1.21.6")
public class PipelineModifierTag {

    public static final String NO_COLOR_WRITE = "no_color_write";
    public static final String NO_DEPTH_WRITE = "no_depth_write";
    public static final String NO_DEPTH_TEST = "no_depth_test";

    private PipelineModifierTag() {}
}
