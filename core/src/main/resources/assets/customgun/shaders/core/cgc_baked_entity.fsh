#version 150

// moj_import注释行前面不能带井号，否则会被当成真指令导入
// [1.20.1, 1.21.6)--------
// moj_import <fog.glsl>
// [1.21.6, )
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
// --------

uniform sampler2D Sampler0;

// [1.20.1, 1.21.6)--------
// uniform vec4 ColorModulator;
// uniform float FogStart;
// uniform float FogEnd;
// uniform vec4 FogColor;
//
// in float vertexDistance;
// [1.21.6, )
in float sphericalVertexDistance;
in float cylindricalVertexDistance;
// --------

in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
//in vec4 normal; // 1.21.6移除

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;
    // [1.20.1, 1.21.6)
//     fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
    // [1.21.6, )
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance,
            FogEnvironmentalStart, FogEnvironmentalEnd,
            FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
