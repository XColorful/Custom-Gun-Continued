#version 150

/*
静态烘焙网格的实体着色器（渲染重构B1-a）

[1.20.1, 1.21,6)
与 vanilla 的 rendertype_entity_cutout 的差别只有两处，都来自「顶点在模型空间、由 ModelViewMat 带入视图空间」：
    1. 光照：vanilla 的 Normal 属性已经是视图空间法线，着色器直接用它点乘视图空间光照方向
        这里顶点是模型空间法线，用 ModelViewMat 的逆转置把它带到同一空间
        等比缩放下与 vanilla 逐位等价
        1.21.1 起 ModelViewMat 只放调用方姿态，IT(ModelViewMat) 才与即时块 CPU 写的 IT(姿态) 同口径
    2. 雾：vanilla 用 IViewRotMat * Position（Position 已是视图空间），先在相机旋转抵消后的坐标系里取样
        这里把 ModelViewMat 一起乘进去，得到同样的世界对齐坐标，再交给 fog_distance
        注意必须在乘完之后才做 shape=1 的「清 Y」，否则清掉的是模型空间的 Y
        1.21.1 起
            fog_distance 不再接矩阵，也没有 IViewRotMat
            绘制侧的 ModelViewMat 只放调用方姿态（RenderSystem 的 modelview 由 CPU 并进 ProjMat，见 BakedMesh#draw），
            这里改乘 CgcViewMat 还原真实视图坐标，再交给 fog_distance

[1.21.6, )
与 vanilla 的 core/entity 的差别只有两处，都来自「顶点在模型空间、由 ModelViewMat 带入视图空间」：
    1. 光照：vanilla 的 Normal 属性已经是视图空间法线，着色器直接用它点乘视图空间光照方向
        这里顶点是模型空间法线，用 ModelViewMat 的逆转置把它带到同一空间
        等比缩放下与 vanilla 逐位等价
    2. 雾：vanilla 直接对原始顶点坐标取样，那套顶点已经由模型矩阵带到视图空间
        这里先把顶点乘进 ModelViewMat，再交给 fog_*_distance
*/

// moj_import注释行前面不能带井号，否则会被当成真指令导入
// [1.20.1, 1.21.6)--------
// moj_import <light.glsl>
// moj_import <fog.glsl>
// [1.21.6, )
#moj_import <minecraft:light.glsl>
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
// --------

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

// [1.20.1, 1.21.6)--------
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
// [1.20.1, 1.21.1)
uniform mat3 IViewRotMat;
// [1.21.1, )
// uniform mat4 CgcViewMat; // 绘制时 RenderSystem 的 modelview，只用来把顶点还原成真实视图坐标（雾）
uniform int FogShape;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out float vertexDistance;
// [1.21.6, )
out float sphericalVertexDistance;
out float cylindricalVertexDistance;
// --------

out vec4 vertexColor;
out vec4 lightMapColor;
out vec4 overlayColor;
out vec2 texCoord0;

void main() {
    // [1.20.1, 1.21.6)--------
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    // [1.20.1, 1.21.1)
    /*
    雾必须先在「世界对齐」坐标系里算：原版是 fog_distance(ModelViewMat, IViewRotMat * Position)
    其中 IViewRotMat * Position 消除了相机旋转，shape=1 时 zero 掉的是世界 Y
    这里把 ModelViewMat 乘进去之后再清 Y，否则清掉的是模型空间的 Y
     */
    vec3 worldAlignedPosition = (mat4(IViewRotMat) * ModelViewMat * vec4(Position, 1.0)).xyz;
    vertexDistance = fog_distance(mat4(1.0), worldAlignedPosition, FogShape);
    // [1.21.1, )
    /*
    雾要在真实视图坐标里取样：ModelViewMat 是调用方姿态，再乘 CgcViewMat（RenderSystem 的 modelview）才是视图位置
     */
//     vec3 viewPosition = (CgcViewMat * ModelViewMat * vec4(Position, 1.0)).xyz;
//     vertexDistance = fog_distance(viewPosition, FogShape);

    // [1.21.6, )
//     vec3 viewPosition = (ModelViewMat * vec4(Position, 1.0)).xyz;
//     gl_Position = ProjMat * vec4(viewPosition, 1.0);
//
//     sphericalVertexDistance = fog_spherical_distance(viewPosition);
//     cylindricalVertexDistance = fog_cylindrical_distance(viewPosition);

    // [1.21.6, )
    vec3 viewPosition = (ModelViewMat * vec4(Position, 1.0)).xyz;
    gl_Position = ProjMat * vec4(viewPosition, 1.0);

    sphericalVertexDistance = fog_spherical_distance(viewPosition);
    cylindricalVertexDistance = fog_cylindrical_distance(viewPosition);
    // --------

    mat3 normalMatrix = mat3(transpose(inverse(ModelViewMat)));
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, normalMatrix * Normal, Color);

    lightMapColor = texelFetch(Sampler2, UV2 / 16, 0);
    overlayColor = texelFetch(Sampler1, UV1, 0);
    texCoord0 = UV0;
//     normal = ProjMat * ModelViewMat * vec4(Normal, 0.0); // 1.21.6移除
}
