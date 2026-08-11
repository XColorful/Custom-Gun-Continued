/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.model;

import dev.xcolorful.customgun.client.api.model.bedrock.IBedrockRenderer;
import dev.xcolorful.customgun.client.model.bedrock.BedrockCubeBox;
import dev.xcolorful.customgun.client.model.bedrock.BedrockCubePerFace;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock._GeometryModel;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry._Bone;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry._Description;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry.bone._Cube;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry.bone.cube._Uv;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.xcolorful.customgun.client.util.ClientModelUtils.*;

public class _ModelLoader {

    protected static void loadNewModel(ModelObject _this, BedrockModel pojo) {
        List<_GeometryModel> geometryModels = pojo.getGeometryModels();
        assert !geometryModels.isEmpty();
        _GeometryModel geometryModel = geometryModels.get(0);
        geometryModel.deco();

        _Description description = geometryModel.getDescription();

        { // Deprecated
            float[] offset = description.getVisibleBoundsOffset();
            _this.offset = new Vec3(offset[0], offset[1], offset[2]);
            _this.width = description.getVisibleBoundsWidth() / 2f;
            _this.height = description.getVisibleBoundsHeight() / 2f;
            _this.hasSize = true;
        }

        {
            @Nullable List<_Bone> bones = geometryModel.getBones();
            if (bones == null) return;

            { // 往 indexBones 里面注入数据，为后续坐标转换做参考
                for (int i = 0; i < bones.size(); i++) {
                    _Bone bone = bones.get(i);
                    // 塞索引，这是给后面坐标转换用的
                    _this.indexBones.putIfAbsent(bone.getName(), bone);
                    // 塞入新建的空 BedrockPart 实例
                    // 因为后面添加 parent 需要，所以先塞空对象，然后二次遍历再进行数据存储
                    _this.modelMap_putIfAbsent(bone.getName(), IBedrockRenderer.of(new BedrockPart(bone.getName())));
                }
            }

            { // 开始往 ModelRenderer 实例里面塞数据
                // 材质的长度、宽度
                int textureWidth = description.getTextureWidth();
                int textureHeight = description.getTextureHeight();

                for (int i = 0; i < bones.size(); i++) {
                    _Bone bone = bones.get(i);
                    String name = bone.getName(); // 骨骼名称
                    float @Nullable [] rotation = bone.getRotation(); // 旋转
                    @Nullable String parent = bone.getParent(); // 父骨骼的名称
                    BedrockPart model = _this.modelMap_get(name).getModelRenderer(); // 塞进 HashMap 里面的模型对象

                    // 镜像参数
                    model.mirror = bone.getMirror();
                    // 旋转点
                    model.setPos(pivot_BEtoJE(_this.indexBones, bone, 0),
                            pivot_BEtoJE(_this.indexBones, bone, 1),
                            pivot_BEtoJE(_this.indexBones, bone, 2));
                    // 设置旋转角度
                    if (rotation != null) {
                        _this.setRotationAngle(model, rotation_BEtoJE(rotation[0]), rotation_BEtoJE(rotation[1]), rotation_BEtoJE(rotation[2]));
                    }
                    // 进行父骨骼绑定
                    if (parent != null) {
                        BedrockPart _parent = _this.modelMap_get(parent).getModelRenderer();
                        _parent.addChild(model);
                        model.setParent(_parent);
                    } else {
                        // 没有父骨骼的模型才进行渲染
                        _this.shouldRender.add(model);
                        model.setParent(null);
                    }

                    @Nullable List<_Cube> cubes = bone.getCubes();
                    if (cubes == null) continue;
                    for (int j = 0; j < cubes.size(); j++) {
                        _Cube cube = cubes.get(j);

                        _Uv uv = cube.getUv();
                        float[] size = cube.getSize();
                        float[] cubeRotation = cube.getRotation();
                        boolean mirror = cube.getMirror();
                        float inflate = cube.getInflate();

                        if (cubeRotation == null) {
                            // 当做普通 cube 存入
                            float @Nullable [] _uv = uv.getUv();
                            if (_uv != null) {
                                model.cubes.add(new BedrockCubeBox(_uv[0], _uv[1],
                                        origin_BEtoJE(bone, cube, 0), origin_BEtoJE(bone, cube, 1), origin_BEtoJE(bone, cube, 2),
                                        size[0], size[1], size[2], inflate,
                                        mirror,
                                        textureWidth, textureHeight)
                                );
                            } else {
                                model.cubes.add(new BedrockCubePerFace(origin_BEtoJE(bone, cube, 0), origin_BEtoJE(bone, cube, 1), origin_BEtoJE(bone, cube, 2),
                                        size[0], size[1], size[2], inflate,
                                        textureWidth, textureHeight,
                                        uv)
                                );
                            }
                        } else {
                            // 创建 Cube ModelRender
                            BedrockPart bedrockPart = new BedrockPart(null);
                            bedrockPart.setPos(pivot_BEtoJE(bone, cube, 0), pivot_BEtoJE(bone, cube, 1), pivot_BEtoJE(bone, cube, 2));
                            _this.setRotationAngle(bedrockPart, rotation_BEtoJE(cubeRotation[0]), rotation_BEtoJE(cubeRotation[1]), rotation_BEtoJE(cubeRotation[2]));
                            float @Nullable [] _uv = uv.getUv();
                            if (_uv != null) {
                                bedrockPart.cubes.add(new BedrockCubeBox(_uv[0], _uv[1],
                                        origin_BEtoJE(cube, 0), origin_BEtoJE(cube, 1), origin_BEtoJE(cube, 2),
                                        size[0], size[1], size[2], inflate,
                                        mirror,
                                        textureWidth, textureHeight)
                                );
                            } else {
                                bedrockPart.cubes.add(new BedrockCubePerFace(origin_BEtoJE(cube, 0), origin_BEtoJE(cube, 1), origin_BEtoJE(cube, 2),
                                        size[0], size[1], size[2], inflate,
                                        textureWidth, textureHeight,
                                        uv)
                                );
                            }

                            // 添加进父骨骼中
                            model.addChild(bedrockPart);
                        }
                    }
                }
            }
        }
    }

    protected static void loadLegacyModel(ModelObject _this, BedrockModel pojo) {
    }
}
