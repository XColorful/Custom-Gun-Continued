package dev.xcolorful.customgun.client.api.minecraft.pipeline;

import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.minecraft.pipeline.PipelineModifierTag;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("1.21.6")
public enum PipelineModifier implements ResourceTag.RegistryTag {
    NO_COLOR_WRITE(PipelineModifierTag.NO_COLOR_WRITE,
            (pipeline, name) ->
                    // [1.20.1, 1.21.6)
//                    null
                    // [1.21.6, 26.1.x)
//                    pipeline.toBuilder()
//                            .withLocation(name)
//                            .withColorWrite(false) // [1.21.6, 26.1.x)
//                            .withColorTargetState(new ColorTargetState( // [26.1.x, )
//                                    pipeline.getColorTargetState().blendFunction(),
//                                    pipeline.getColorTargetState().format(), // [26.2, )
//                                    ColorTargetState.WRITE_NONE
//                                    )
//                            )
//                            .build()
                    // [26.3, )
           {
               RenderPipeline.Builder builder = pipeline.toBuilder().withLocation(name);
               List<ColorTargetState> colorTargets = pipeline.getColorTargetStates();
               for (int i = 0; i < colorTargets.size(); i++) {
                   ColorTargetState colorTarget = colorTargets.get(i);
                   if (colorTarget != null) {
                       /*
                       26.3：既然一个颜色都不写出，混合也就没有意义，顺手把混合函数一起去掉
                       SubmitNodeCollection#submitCustomGeometry 按 RenderType#hasBlending() 把自定义几何分派到 solid / translucentCustomGeometry 两个阶段
                       而这两个阶段执行时机不同（executeSolid 先于 executeTranslucent）
                       目镜模板的 INVERT 圆本身不写颜色却带着 TRANSLUCENT 混合，于是被排进 translucent 阶段，晚于 solid 阶段的黑色遮罩执行，孔根本挖不出来 —— 表现为 4 倍镜目镜全黑
                       去掉混合后它会回到 solid 阶段，模板写入与模板测试重新处于同一阶段、顺序恢复
                       */
                       builder.withColorTargetState(i, new ColorTargetState(
                               Optional.empty(),
                               colorTarget.format(), // [26.2, )
                               ColorTargetState.WRITE_NONE));
                   }
               }
               return builder.build();
           }
    ),
    NO_DEPTH_WRITE(PipelineModifierTag.NO_DEPTH_WRITE,
            (pipeline, name) ->
                    // [1.20.1, 1.21.6)
//                    null
                    // [1.21.6, 26.1.x)
                    pipeline.toBuilder()
                            .withLocation(name)
//                            .withDepthWrite(false) // [1.21.6, 26.1.x)
                            .withDepthStencilState(Optional.ofNullable(pipeline.getDepthStencilState()) // [26.1.x, )
                                    .map(state -> new DepthStencilState(state.depthTest(),
                                                    false,
                                                    state.depthBiasScaleFactor(),
                                                    state.depthBiasConstant()
                                                    , state.stencilTest() // [26.3, ) 模板测试从 RenderPipeline 挪进了 DepthStencilState
                                            )
                                    )
                            )
                            .build()
    ),
    NO_DEPTH_TEST(PipelineModifierTag.NO_DEPTH_TEST,
            (pipeline, name) ->
                    // [1.20.1, 1.21.6)
//                    null
                    // [1.21.6, 26.1.x)
                    pipeline.toBuilder()
                            .withLocation(name)
//                            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST) // [1.21.6, 26.1.x)
                            .withDepthStencilState(Optional.empty()) // [26.1.x, )
                            .build()
    ),
    ;

    public final String typeName;
    public final String registryName;
    public final Identifier registryLocation;
    public final IPipelineModifier modifier;
    PipelineModifier(String name, IPipelineModifier modifier) {
        this.typeName = name;
        this.registryLocation = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, name));
        this.registryName = this.registryLocation.toString();
        this.modifier = modifier;
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getRegistryName() {
        return this.registryName;
    }
    @Override public Identifier getRegistryLocation() {
        return this.registryLocation;
    }

    public IPipelineModifier getModifier() {
        return this.modifier;
    }
}
