#version 330
#extension GL_ARB_separate_shader_objects : require

/*
静态烘焙网格的实体着色器（渲染重构B1-a）
26.3 起管线由 shaderc 编译为 SPIR-V：只认 #include <minecraft:...>，不再有 #moj_import，输入/输出必须带 location。
26.2 起绘制已改用「顶点副本 + submitCustomGeometry 回放」，本管线只为低版本保留；这里仍按烘焙网格的约定写：
顶点在模型空间，位置由 ModelViewMat 带入视图，法线由 CgcBaked 的 IT(姿态) 带入视图
（DynamicTransforms 的 ModelViewMat 是 mv × 姿态，直接拿它的逆转置会少一次摄像机旋转）。
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

uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

layout(location = 0) out float sphericalVertexDistance;
layout(location = 1) out float cylindricalVertexDistance;
layout(location = 2) out vec4 vertexColor;
layout(location = 3) out vec4 lightMapColor;
layout(location = 4) out vec4 overlayColor;
layout(location = 5) out vec2 texCoord0;

layout(std140) uniform CgcBaked {
    mat4 NormalMat;
};

void main() {
    vec3 viewPosition = (ModelViewMat * vec4(Position, 1.0)).xyz;
    gl_Position = ProjMat * vec4(viewPosition, 1.0);

    sphericalVertexDistance = fog_spherical_distance(viewPosition);
    cylindricalVertexDistance = fog_cylindrical_distance(viewPosition);

    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, mat3(NormalMat) * Normal, Color);
    lightMapColor = sample_lightmap(Sampler2, UV2);
    overlayColor = texelFetch(Sampler1, UV1, 0);
    texCoord0 = UV0;
}
