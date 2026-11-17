#version 330
#extension GL_ARB_separate_shader_objects : require

/*
静态烘焙网格的实体着色器（渲染重构B1-a）
26.3 起管线由 shaderc 编译为 SPIR-V：只认 #include <minecraft:...>，不再有 #moj_import，输入/输出必须带 location。

[26.2, )
与 vanilla 的 core/entity 的差别只有两处，都来自「顶点在模型空间」：

1. 位置与雾
   vanilla 的顶点已经由调用方 CPU 乘过姿态，ModelViewMat 只放模型视图，顶点坐标本身就是「世界对齐」的
   这里顶点是模型空间，绘制侧把「模型视图 × 姿态」写进 ModelViewMat，姿态本身写进 TextureMat，
   所以雾要按 TextureMat * Position 取样，而不是直接拿 Position

2. 法线
   vanilla 的 entity 着色器直接点乘顶点里的 Normal，光照方向是世界空间常量
   烘焙网格存的是模型空间法线，这里用 transpose(inverse(mat3(TextureMat))) 复现即时块 CPU 写的 IT(姿态)
   （不这么做时表现为整枪偏暗、转动时亮度不变化，2026-11-17 修）
   TextureMat 被占用只是这一条 draw 内的事：烘焙网格不使用纹理变换（glint 之类不进烘焙路径）
*/

#include <minecraft:light.glsl>
#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>
#include <minecraft:sample_lightmap.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV1;
layout(location = 4) in ivec2 UV2;
layout(location = 5) in vec3 Normal;

#ifndef NO_OVERLAY
uniform sampler2D Sampler1;
#endif

#ifndef EMISSIVE
uniform sampler2D Sampler2;
#endif

layout(location = 0) out float sphericalVertexDistance;
layout(location = 1) out float cylindricalVertexDistance;

#ifdef PER_FACE_LIGHTING
layout(location = 2) out vec4 vertexPerFaceColorBack;
layout(location = 3) out vec4 vertexPerFaceColorFront;
#else
layout(location = 2) out vec4 vertexColor;
#endif

#ifndef EMISSIVE
layout(location = 4) out vec4 lightMapColor;
#endif

#ifndef NO_OVERLAY
layout(location = 5) out vec4 overlayColor;
#endif

layout(location = 6) out vec2 texCoord0;

void main() {
    // 与即时块 CPU 写进顶点的坐标同空间：模型空间顶点乘实例姿态
    vec3 posedPosition = (TextureMat * vec4(Position, 1.0)).xyz;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    sphericalVertexDistance = fog_spherical_distance(posedPosition);
    cylindricalVertexDistance = fog_cylindrical_distance(posedPosition);

    // 原版 entity 着色器直接用顶点里的 Normal 点乘世界空间光照方向，即时块在 CPU 写的是 IT(姿态)
    vec3 normal = transpose(inverse(mat3(TextureMat))) * Normal;

#ifdef PER_FACE_LIGHTING
    vec2 light = minecraft_compute_light(Light0_Direction, Light1_Direction, normal);
    vertexPerFaceColorBack = minecraft_mix_light_separate(-light, Color);
    vertexPerFaceColorFront = minecraft_mix_light_separate(light, Color);
#elif defined(NO_CARDINAL_LIGHTING)
    vertexColor = Color;
#else
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, normal, Color);
#endif

#ifndef EMISSIVE
    lightMapColor = sample_lightmap(Sampler2, UV2);
#endif

#ifndef NO_OVERLAY
    overlayColor = texelFetch(Sampler1, UV1, 0);
#endif

    texCoord0 = UV0;
}
