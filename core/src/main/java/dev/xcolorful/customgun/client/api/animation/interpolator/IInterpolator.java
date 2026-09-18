package dev.xcolorful.customgun.client.api.animation.interpolator;

import dev.xcolorful.customgun.client.animation.channel.AnimChannelContent;

public interface IInterpolator<T extends IInterpolator<T>> {

    InterpolatorType getType();

    void compile(AnimChannelContent content);

    float[] interpolate(int indexFrom, int indexTo, float alpha);

    T clone();
}
