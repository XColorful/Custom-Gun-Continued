/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation;

import dev.xcolorful.customgun.client.animation.channel.SoundChannelContent;
import dev.xcolorful.customgun.client.animation.controller.AnimController;
import dev.xcolorful.customgun.client.animation.gltf.GltfStructure;
import dev.xcolorful.customgun.client.animation.interpolator.CompositeInterpolator;
import dev.xcolorful.customgun.client.api.animation.AnimationChannelType;
import dev.xcolorful.customgun.client.api.animation.interpolator.LerpMode;
import dev.xcolorful.customgun.client.api.animation.listener.IAnimationListenerSupplier;
import dev.xcolorful.customgun.client.resource.assets.animation.BedrockAnimation;
import dev.xcolorful.customgun.client.resource.assets.animation.bedrock._Animation;
import dev.xcolorful.customgun.client.resource.assets.animation.bedrock.animation._Bone;
import dev.xcolorful.customgun.client.resource.assets.animation.bedrock.animation._SoundEffects;
import dev.xcolorful.customgun.client.resource.assets.animation.bedrock.animation.bone._KeyFrame;
import dev.xcolorful.customgun.core.util.MathUtil;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnimationHelper {

    public static AnimController createControllerFromGltf(@NotNull GltfStructure structure, @NotNull IAnimationListenerSupplier supplier) {
        // TODO
        return null;
    }

    public static AnimController createControllerFromBedrock(BedrockAnimation animationFile, IAnimationListenerSupplier supplier) {
        return new AnimController(createAnimationFromBedrock(animationFile), supplier);
    }

    public static @NotNull List<ObjectAnimation> createAnimationFromBedrock(BedrockAnimation animationFile) {
        List<ObjectAnimation> result = new ArrayList<>();
        for (Map.Entry<String, _Animation> entry : animationFile.getAnimations().entrySet()) {
            ObjectAnimation objectAnimation = new ObjectAnimation(entry.getKey());
            _Animation animation = entry.getValue();
            if (animation.getBones() != null) {
                for (Map.Entry<String, _Bone> _entry : animation.getBones().entrySet()) {
                    _Bone bone = _entry.getValue();
                    Double2ObjectRBTreeMap<_KeyFrame> translationKeyframes = bone.getPosition();
                    Double2ObjectRBTreeMap<_KeyFrame> rotationKeyframes = bone.getRotation();
                    Double2ObjectRBTreeMap<_KeyFrame> scaleKeyframes = bone.getScale();
                    if (translationKeyframes != null) {
                        ObjectAnimationChannel translationChannel = new ObjectAnimationChannel(AnimationChannelType.TRANSLATION);
                        translationChannel.node = _entry.getKey();
                        translationChannel.interpolator = new CompositeInterpolator();
                        // 将位移数据转移进 AnimationChannel
                        _writeBedrockTranslation(translationChannel, translationKeyframes);
                        translationChannel.interpolator.compile(translationChannel.content);
                        objectAnimation.addChannel(translationChannel);
                    }
                    if (rotationKeyframes != null) {
                        ObjectAnimationChannel rotationChannel = new ObjectAnimationChannel(AnimationChannelType.ROTATION);
                        rotationChannel.node = _entry.getKey();
                        rotationChannel.interpolator = new CompositeInterpolator();
                        // 将旋转数据转移进 AnimationChannel
                        _writeBedrockRotation(rotationChannel, rotationKeyframes);
                        rotationChannel.interpolator.compile(rotationChannel.content);
                        objectAnimation.addChannel(rotationChannel);
                    }
                    if (scaleKeyframes != null) {
                        ObjectAnimationChannel scaleChannel = new ObjectAnimationChannel(AnimationChannelType.SCALE);
                        scaleChannel.node = _entry.getKey();
                        scaleChannel.interpolator = new CompositeInterpolator();
                        // 将缩放数据转移进 AnimationChannel
                        _writeBedrockScale(scaleChannel, scaleKeyframes);
                        scaleChannel.interpolator.compile(scaleChannel.content);
                        objectAnimation.addChannel(scaleChannel);
                    }
                }
            }

            // 将声音数据转移到 ObjectAnimation 中
            _SoundEffects soundEffectKeyframes = animation.getSoundEffects();
            if (soundEffectKeyframes != null) {
                ObjectAnimationSoundChannel soundChannel = new ObjectAnimationSoundChannel();
                soundChannel.content = new SoundChannelContent();
                int keyframeNum = soundEffectKeyframes.getKeyframes().size();
                soundChannel.content.keyframeTimeS = new double[keyframeNum];
                soundChannel.content.keyframeSoundName = new ResourceLocation[keyframeNum];
                int i = 0;
                for (Map.Entry<Double, ResourceLocation> _entry : soundEffectKeyframes.getKeyframes().double2ObjectEntrySet()) {
                    soundChannel.content.keyframeTimeS[i] = _entry.getKey();
                    soundChannel.content.keyframeSoundName[i] = _entry.getValue();
                    i++;
                }
                objectAnimation.setSoundChannel(soundChannel);
            }
            result.add(objectAnimation);
        }
        return result;
    }

    private static void _writeBedrockTranslation(ObjectAnimationChannel animationChannel, Double2ObjectRBTreeMap<_KeyFrame> keyframesMap) {
        // 基岩版动画中储存的动画数据为相对值，而本模组动画系统使用的是绝对值，所以需要叠加初始值
        // 此处就是在获取动画数据的初始值
        animationChannel.content.keyframeTimeS = new float[keyframesMap.size()];
        animationChannel.content.values = new float[keyframesMap.size()][];
        animationChannel.content.lerpModes = new LerpMode[keyframesMap.size()];
        int index = 0;
        for (Double2ObjectMap.Entry<_KeyFrame> entry : keyframesMap.double2ObjectEntrySet()) {
            // 写入关键帧时间
            animationChannel.content.keyframeTimeS[index] = (float) entry.getDoubleKey();
            // 写入关键帧数值
            _KeyFrame keyframe = entry.getValue();
            if (keyframe.getPre() != null || keyframe.getPost() != null) {
                if (keyframe.getPre() != null && keyframe.getPost() != null) {
                    animationChannel.content.values[index] = new float[6];
                    Vector3f pre = new Vector3f(keyframe.getPre());
                    Vector3f post = new Vector3f(keyframe.getPost());
                    pre.mul(1 / 16f, 1 / 16f, 1 / 16f);
                    post.mul(1 / 16f, 1 / 16f, 1 / 16f);
                    readVector3fToArray(animationChannel.content.values[index], pre, 0);
                    readVector3fToArray(animationChannel.content.values[index], post, 3);
                } else if (keyframe.getPre() != null) {
                    animationChannel.content.values[index] = new float[3];
                    Vector3f pre = new Vector3f(keyframe.getPre());
                    pre.mul(1 / 16f, 1 / 16f, 1 / 16f);
                    readVector3fToArray(animationChannel.content.values[index], pre, 0);
                } else {
                    animationChannel.content.values[index] = new float[3];
                    Vector3f post = new Vector3f(keyframe.getPost());
                    post.mul(1 / 16f, 1 / 16f, 1 / 16f);
                    readVector3fToArray(animationChannel.content.values[index], post, 0);
                }
            } else if (keyframe.getData() != null) {
                animationChannel.content.values[index] = new float[3];
                Vector3f data = new Vector3f(keyframe.getData());
                data.mul(1 / 16f, 1 / 16f, 1 / 16f);
                readVector3fToArray(animationChannel.content.values[index], data, 0);
            }
            // 写入关键帧插值类型
            @Nullable LerpMode lerpMode = keyframe.getLerpMode();
            animationChannel.content.lerpModes[index] = lerpMode != null ? lerpMode : LerpMode.LINEAR;
            index++;
        }
    }

    private static void _writeBedrockRotation(ObjectAnimationChannel animationChannel, Double2ObjectRBTreeMap<_KeyFrame> keyframesMap) {
        animationChannel.content.keyframeTimeS = new float[keyframesMap.size()];
        animationChannel.content.values = new float[keyframesMap.size()][];
        animationChannel.content.lerpModes = new LerpMode[keyframesMap.size()];
        int index = 0;
        for (Double2ObjectMap.Entry<_KeyFrame> entry : keyframesMap.double2ObjectEntrySet()) {
            // 写入关键帧时间
            animationChannel.content.keyframeTimeS[index] = (float) entry.getDoubleKey();
            // 写入关键帧数值
            _KeyFrame keyframe = entry.getValue();
            if (keyframe.getPre() != null || keyframe.getPost() != null) {
                if (keyframe.getPre() != null && keyframe.getPost() != null) {
                    animationChannel.content.values[index] = new float[6];
                    Vector3f pre = new Vector3f(keyframe.getPre());
                    Vector3f post = new Vector3f(keyframe.getPost());
                    MathUtil.toAngle(pre);
                    MathUtil.toAngle(post);
                    animationChannel.content.values[index][0] = pre.x();
                    animationChannel.content.values[index][1] = pre.y();
                    animationChannel.content.values[index][2] = pre.z();
                    animationChannel.content.values[index][3] = post.x();
                    animationChannel.content.values[index][4] = post.y();
                    animationChannel.content.values[index][5] = post.z();
                } else if (keyframe.getPre() != null) {
                    animationChannel.content.values[index] = new float[3];
                    Vector3f pre =  new Vector3f(keyframe.getPre());
                    MathUtil.toAngle(pre);
                    animationChannel.content.values[index][0] = pre.x();
                    animationChannel.content.values[index][1] = pre.y();
                    animationChannel.content.values[index][2] = pre.z();
                } else {
                    animationChannel.content.values[index] = new float[3];
                    Vector3f post =  new Vector3f(keyframe.getPost());
                    MathUtil.toAngle(post);
                    animationChannel.content.values[index][0] = post.x();
                    animationChannel.content.values[index][1] = post.y();
                    animationChannel.content.values[index][2] = post.z();
                }
            } else if (keyframe.getData() != null) {
                animationChannel.content.values[index] = new float[3];
                Vector3f data =  new Vector3f(keyframe.getData());
                MathUtil.toAngle(data);
                animationChannel.content.values[index][0] = data.x();
                animationChannel.content.values[index][1] = data.y();
                animationChannel.content.values[index][2] = data.z();
            }
            @Nullable LerpMode lerpMode = keyframe.getLerpMode();
            animationChannel.content.lerpModes[index] = lerpMode == LerpMode.CATMULL_ROM ? lerpMode : LerpMode.LINEAR;
            index++;
        }
    }

    private static void _writeBedrockScale(ObjectAnimationChannel animationChannel, Double2ObjectRBTreeMap<_KeyFrame> keyframesMap) {
        animationChannel.content.keyframeTimeS = new float[keyframesMap.size()];
        animationChannel.content.values = new float[keyframesMap.size()][];
        animationChannel.content.lerpModes = new LerpMode[keyframesMap.size()];
        int index = 0;
        for (Double2ObjectMap.Entry<_KeyFrame> entry : keyframesMap.double2ObjectEntrySet()) {
            // 写入关键帧时间
            animationChannel.content.keyframeTimeS[index] = (float) entry.getDoubleKey();
            // 写入关键帧数值
            _KeyFrame keyframe = entry.getValue();
            if (keyframe.getPre() != null || keyframe.getPost() != null) {
                if (keyframe.getPre() != null && keyframe.getPost() != null) {
                    animationChannel.content.values[index] = new float[6];
                    float[] pre = keyframe.getPre();
                    float[] post = keyframe.getPost();
                    readVector3fToArray(animationChannel.content.values[index], pre, 0);
                    readVector3fToArray(animationChannel.content.values[index], post, 3);
                } else if (keyframe.getPre() != null) {
                    animationChannel.content.values[index] = new float[3];
                    float[] pre = keyframe.getPre();
                    readVector3fToArray(animationChannel.content.values[index], pre, 0);
                } else {
                    animationChannel.content.values[index] = new float[3];
                    float[] post = keyframe.getPost();
                    readVector3fToArray(animationChannel.content.values[index], post, 0);
                }
            } else if (keyframe.getData() != null) {
                animationChannel.content.values[index] = new float[3];
                float[] data = keyframe.getData();
                readVector3fToArray(animationChannel.content.values[index], data, 0);
            }
            // 写入关键帧插值类型
            @Nullable LerpMode lerpMode = keyframe.getLerpMode();
            animationChannel.content.lerpModes[index] = lerpMode != null ? lerpMode : LerpMode.LINEAR;
            index++;
        }
    }

    private static void readVector3fToArray(float[] array, float[] vector3f, int offset) {
        array[offset] = vector3f[0];
        array[offset + 1] = vector3f[1];
        array[offset + 2] = vector3f[2];
    }
    private static void readVector3fToArray(float[] array, Vector3f vector3f, int offset) {
        array[offset] = vector3f.x();
        array[offset + 1] = vector3f.y();
        array[offset + 2] = vector3f.z();
    }

    // --------Deprecated--------

    @Deprecated(forRemoval = true) private static Vector3f toAngle(Vector3f vector3f) {
        return MathUtil.toAngle(vector3f);
    }
}
