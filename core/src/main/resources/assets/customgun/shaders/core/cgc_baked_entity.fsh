#version 330

/*
静态烘焙网格的实体着色器（渲染重构B1-a）

[26.2, )
与 vanilla 的 core/entity 保持一致，包括 PER_FACE_LIGHTING / ALPHA_CUTOUT / EMISSIVE / NO_OVERLAY 分支：
枪体走 entityCutout（PER_FACE_LIGHTING + ALPHA_CUTOUT），镜片走 entityTranslucent，两条管线的 define 都会带进来
唯一被替换的是法线空间，见 cgc_baked_entity.vsh
*/

// moj_import注释行前面不能带井号，否则会被当成真指令导入
// [1.20.1, 1.21.6)--------
// moj_import <fog.glsl>
// [1.21.6, )
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
// --------

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;

#ifdef PER_FACE_LIGHTING
in vec4 vertexPerFaceColorBack;
in vec4 vertexPerFaceColorFront;
#else
in vec4 vertexColor;
#endif

#ifndef EMISSIVE
in vec4 lightMapColor;
#endif

#ifndef NO_OVERLAY
in vec4 overlayColor;
#endif

in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
#ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
#endif

#ifdef PER_FACE_LIGHTING
    vec4 faceVertexColor = gl_FrontFacing ? vertexPerFaceColorFront : vertexPerFaceColorBack;
#else
    vec4 faceVertexColor = vertexColor;
#endif

    color *= faceVertexColor * ColorModulator;
#ifndef NO_OVERLAY
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
#endif
#ifndef EMISSIVE
    color *= lightMapColor;
#endif

    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance,
            FogEnvironmentalStart, FogEnvironmentalEnd,
            FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
