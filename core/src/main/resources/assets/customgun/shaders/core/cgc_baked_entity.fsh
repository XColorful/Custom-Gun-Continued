#version 330
#extension GL_ARB_separate_shader_objects : require

/*
静态烘焙网格的实体着色器（渲染重构B1-a）
26.3 起管线的着色器由 shaderc 编译为 SPIR-V：#include <minecraft:...> 取代 #moj_import，输入/输出必须带 location。
26.2 起绘制已改用「顶点副本 + submitCustomGeometry 回放」，本管线只为低版本保留。
*/

#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

layout(location = 0) in float sphericalVertexDistance;
layout(location = 1) in float cylindricalVertexDistance;
layout(location = 2) in vec4 vertexColor;
layout(location = 3) in vec4 lightMapColor;
layout(location = 4) in vec4 overlayColor;
layout(location = 5) in vec2 texCoord0;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance,
            FogEnvironmentalStart, FogEnvironmentalEnd,
            FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
