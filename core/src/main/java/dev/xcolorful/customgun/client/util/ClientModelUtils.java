/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.util;

import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry._Bone;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry.bone._Cube;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ClientModelUtils {

    /**
     * 基岩版用的是度，Java 版用的是弧度，这个转换很简单
     */
    public static float rotation_BEtoJE(float degree) {
        return (float) (degree * Math.PI / 180);
    }

    /**
     * 基岩版的旋转中心计算方式和 Java 版不太一样，需要进行转换
     * <p>
     * 如果有父模型
     * <li>x，z 方向：本模型坐标 - 父模型坐标
     * <li>y 方向：父模型坐标 - 本模型坐标
     * <p>
     * 如果没有父模型
     * <li>x，z 方向不变
     * <li>y 方向：24 - 本模型坐标
     *
     * @param index 是 xyz 的哪一个，x 是 0，y 是 1，z 是 2
     */
    public static float pivot_BEtoJE(@NotNull Map<String, _Bone> indexBones, @NotNull _Bone bone, int index) {
        if (bone.getParent() != null) {
            if (index == 1) return indexBones.get(bone.getParent()).getPivot()[1] // y
                    - bone.getPivot()[1]; // y
            else return bone.getPivot()[index] - indexBones.get(bone.getParent()).getPivot()[index];
        } else {
            if (index == 1) return 24 - bone.getPivot()[1]; // y
            else return bone.getPivot()[index];
        }
    }
    public static float pivot_BEtoJE(_Bone parent, _Cube cube, int index) {
        if (index == 1) return parent.getPivot()[1] // y
                - cube.getPivot()[1]; // y
        else return cube.getPivot()[index] - parent.getPivot()[index];
    }


    /**
     * 基岩版和 Java 版本的方块起始坐标也不一致，Java 是相对坐标，而且 y 值方向不一致。
     * 基岩版是绝对坐标，而且 y 方向朝上。
     * 其实两者规律很简单，但是我找了一下午，才明白咋回事。
     * <li>如果是 x，z 轴，那么只需要方块起始坐标减去旋转点坐标
     * <li>如果是 y 轴，旋转点坐标减去方块起始坐标，再减去方块的 y 长度
     *
     * @param index 是 xyz 的哪一个，x 是 0，y 是 1，z 是 2
     */
    public static float origin_BEtoJE(_Bone bone, _Cube cube, int index) {
        if (index == 1) return bone.getPivot()[1] // y
                - cube.getOrigin()[1] // y
                - cube.getSize()[1]; // y
        return cube.getOrigin()[index] - bone.getPivot()[index];
    }
    public static float origin_BEtoJE(_Cube cube, int index) {
        if (index == 1) return cube.getPivot()[1] // y
                - cube.getOrigin()[1] // y
                - cube.getSize()[1];
        else return cube.getOrigin()[index] - cube.getPivot()[index];
    }
}
