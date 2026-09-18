package dev.xcolorful.customgun.client.animation.channel;

import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

public class SoundChannelContent {

    public double[] keyframeTimeS;
    public ResourceLocation[] keyframeSoundName;

    public SoundChannelContent() {
    }

    public SoundChannelContent(SoundChannelContent source) {
        if (source.keyframeTimeS != null) {
            this.keyframeTimeS = Arrays.copyOf(source.keyframeTimeS, source.keyframeTimeS.length);
        }
        if (source.keyframeSoundName != null) {
            this.keyframeSoundName = Arrays.copyOf(source.keyframeSoundName, source.keyframeSoundName.length);
        }
    }
}
