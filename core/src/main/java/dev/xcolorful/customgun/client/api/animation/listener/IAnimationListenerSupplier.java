package dev.xcolorful.customgun.client.api.animation.listener;

import dev.xcolorful.customgun.client.api.animation.AnimationChannelType;

import javax.annotation.Nullable;

public interface IAnimationListenerSupplier {

    @Nullable
    IAnimationListener supplyListeners(String nodeName,
                                       AnimationChannelType type);
}
