/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation;

import dev.xcolorful.customgun.client.animation.channel.AnimChannelContent;
import dev.xcolorful.customgun.client.api.animation.AnimationChannelType;
import dev.xcolorful.customgun.client.api.animation.interpolator.IInterpolator;
import dev.xcolorful.customgun.client.api.animation.listener.IAnimationListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ObjectAnimationChannel {

    public final AnimationChannelType type;
    private final List<IAnimationListener> listeners = new ArrayList<>();
    /**
     * 节点名称
     */
    public String node;
    /**
     * 这个轨道的内容，包括关键帧
     */
    public AnimChannelContent content;
    public IInterpolator<?> interpolator;
    /**
     * 此变量用于动画过渡，
     * 如果你不明白在做什么，请不要更改它
     * <br>
     * TODO ↑一个屎山项目之所以能如此混乱的原因是什么？都没个文档交流，搞防辞退编程呢？
     * <br>
     * 以为自己很精英吗？等后浪掀起来，干掉你连一秒钟都不要
     */
    boolean transitioning = false;

    public ObjectAnimationChannel(AnimationChannelType type) {
        this.type = type;
        this.content = new AnimChannelContent();
    }

    public ObjectAnimationChannel(AnimationChannelType type, AnimChannelContent content) {
        this.type = type;
        this.content = content;
    }

    public void addListener(IAnimationListener listener) {
        if (listener.getType().equals(type)) {
            listeners.add(listener);
        } else {
            throw new RuntimeException("trying to add wrong type of listener to channel.");
        }
    }

    public void removeListener(IAnimationListener listener) {
        listeners.remove(listener);
    }

    public void clearListeners() {
        listeners.clear();
    }

    public List<IAnimationListener> getListeners() {
        return listeners;
    }

    public float getEndTimeS() {
        if (content.keyframeTimeS.length == 0) {
            return 0;
        }
        return content.keyframeTimeS[content.keyframeTimeS.length - 1];
    }

    /**
     * 根据输入时间执行计算，并将结果通知所有 AnimationListener
     *
     * @param timeS 绝对时间（以秒为单位）
     */
    public void update(float timeS, boolean blend) {
        if (!transitioning) {
            float[] result = getResult(timeS);
            for (int i = 0; i < this.listeners.size(); i++) {
                IAnimationListener listener = this.listeners.get(i);
                listener.update(result, blend);
            }
        }
    }

    public float[] getResult(float timeS) {
        int indexFrom = computeIndex(timeS);
        int indexTo = Math.min(content.keyframeTimeS.length - 1, indexFrom + 1);
        float alpha = computeAlpha(timeS, indexFrom);
        return interpolator.interpolate(indexFrom, indexTo, alpha);
    }

    private int computeIndex(float timeS) {
        int index = Arrays.binarySearch(content.keyframeTimeS, timeS);
        if (index >= 0) {
            return index;
        }
        return Math.max(0, -index - 2);
    }

    private float computeAlpha(float timeS, int indexFrom) {
        if (timeS <= content.keyframeTimeS[0]) {
            return 0.0f;
        }
        if (timeS >= content.keyframeTimeS[content.keyframeTimeS.length - 1]) {
            return 1.0f;
        }
        float local = timeS - content.keyframeTimeS[indexFrom];
        float delta = content.keyframeTimeS[indexFrom + 1] - content.keyframeTimeS[indexFrom];
        return local / delta;
    }

}
