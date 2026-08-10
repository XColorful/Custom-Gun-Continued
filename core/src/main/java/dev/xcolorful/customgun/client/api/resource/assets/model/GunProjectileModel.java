package dev.xcolorful.customgun.client.api.resource.assets.model;

import dev.xcolorful.customgun.client.model.ModelObject;
import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock._GeometryModel;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry._Bone;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry._Description;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry.bone._Cube;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry.bone.cube._Uv;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry.bone.cube.uv._FaceUv;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GunProjectileModel {

    /**
     * @return 合法的基岩版模型POJO{@link BedrockModel}
     * @throws RuntimeException 如果构建不合法，会抛出异常，游戏启动时就能完成校验
     */
    public static @NotNull BedrockModel createBedrockModel() {
        BedrockModel bedrockModel = new BedrockModel(); {
            bedrockModel.setFormatVersion("1.12.0");

            List<_GeometryModel> geometryModels = new ArrayList<>(); {
                _GeometryModel geometryModel = new _GeometryModel(); {
                    _Description description = new _Description(); {
                        description.setIdentifier("geometry.customgun:gun_projectile");
                        description.setTextureWidth(16);
                        description.setTextureHeight(16);
                        description.setVisibleBoundsWidth(2);
                        description.setVisibleBoundsHeight(2.5f);
                        description.setVisibleBoundsOffset(new float[]{0, 0.75f, 0});
                    }
                    geometryModel.setDescription(description);

                    List<_Bone> bones = new ArrayList<>(); {
                        _Bone bone = new _Bone(); {
                            bone.setName("bb_main");
                            bone.setPivot(new float[]{0, 0, 0});
                            List<_Cube> cubes = new ArrayList<>(); {
                                _Cube cube = new _Cube(); {
                                    cube.setOrigin(new float[]{-8, 0, -8});
                                    cube.setSize(new float[]{16, 16, 16});
                                    _Uv uv = new _Uv(); {
                                        _FaceUv north = new _FaceUv(); {
                                            north.setUv(new float[]{0.25f, 0.25f});
                                            north.setUvSize(new float[]{0.5f, 0.5f});
                                        }
                                        uv.setNorth(north);

                                        _FaceUv east = new _FaceUv(); {
                                            east.setUv(new float[]{1.25f, 0.25f});
                                            east.setUvSize(new float[]{0.5f, 0.5f});
                                        }
                                        uv.setEast(east);

                                        _FaceUv south = new _FaceUv(); {
                                            south.setUv(new float[]{2.25f, 0.25f});
                                            south.setUvSize(new float[]{0.5f, 0.5f});
                                        }
                                        uv.setSouth(south);

                                        _FaceUv west = new _FaceUv(); {
                                            west.setUv(new float[]{3.25f, 0.25f});
                                            west.setUvSize(new float[]{0.5f, 0.5f});
                                        }
                                        uv.setWest(west);

                                        _FaceUv up = new _FaceUv(); {
                                            up.setUv(new float[]{4.75f, 0.75f});
                                            up.setUvSize(new float[]{-0.5f, -0.5f});
                                        }
                                        uv.setUp(up);

                                        _FaceUv down = new _FaceUv(); {
                                            down.setUv(new float[]{5.75f, 0.75f});
                                            down.setUvSize(new float[]{-0.5f, -0.5f});
                                        }
                                        uv.setDown(down);
                                    }
                                    cube.setUv(uv);
                                }
                                cubes.add(cube);
                            }
                            bone.setCubes(cubes);
                        }
                        bones.add(bone);
                    }
                    geometryModel.setBones(bones);
                }
                geometryModels.add(geometryModel);
            }
            bedrockModel.setGeometryModels(geometryModels);
        }

        bedrockModel.validate();
        if (!bedrockModel.isValid()) {
            throw new RuntimeException("GunProjectileModel: The " + BedrockModel.class.getSimpleName() + " is invalid! Check " + GunProjectileModel.class.getName());
        }
        return bedrockModel;
    }

    /**
     * @return 合法的基岩版模型实例{@link ModelObject}
     * @throws RuntimeException 如果构建不合法，会抛出异常，游戏启动时就能完成校验
     */
    public static @NotNull ModelObject createModelObject() {
        @NotNull BedrockModel pojo = createBedrockModel();
        ModelObject modelObject = ModelObject.fromPojo(pojo);
        if (modelObject == null) {
            throw new RuntimeException("GunProjectileModel: The " + ModelObject.class.getSimpleName() + " is null! Check " + GunProjectileModel.class.getName());
        }
        return modelObject;
    }
}
