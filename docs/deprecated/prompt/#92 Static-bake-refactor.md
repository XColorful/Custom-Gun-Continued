> #92 提示词

# 

```
读`./docs/architecture/client/render/PLAN.md`，完成`TODO.md`B1-a 静态烘焙
- 可以根据需要将任务拆解给多Agent执行
```

# 

Now I have the full picture. I'll implement B1-a. Starting with the bake infrastructure.

Implementation compiles. Now recording the design and acceptance notes in the docs.

B1-a 静态烘焙已落地，代码按“状态段 + 几何段”拆分：

**实现**
- 新增 [bake/](core/src/main/java/dev/xcolorful/customgun/client/model/bake/)：`BakedMesh`（模型空间持久 VBO + `drawWithShader`）、`StaticBake`（取缓存/烘焙/绘制入口）、`StaticBakeCache`（LRU 32 项 / 32 MiB，随资源重载释放）、`DiscardVertexConsumer`。
- [BedrockPart.java](core/src/main/java/dev/xcolorful/customgun/client/model/bedrock/BedrockPart.java) / [FunctionalBedrockPart.java](core/src/main/java/dev/xcolorful/customgun/client/model/bedrock/FunctionalBedrockPart.java)：新增状态段 `renderStatus`（跑 functional 回调、定可见性、累加节点指纹）与几何段 `renderGeometry`（只发顶点，不跑回调），无回调子树走纯指纹快速路径。
- [ModelObject.java:193](core/src/main/java/dev/xcolorful/customgun/client/model/ModelObject.java:193)：非第一人称走烘焙，第一人称保持原路径；顺带删掉 1.20.1 上纯开销的双重 `NodeTransform` 快照（保留 `[26.2, )` 注释）。
- [_AttachmentModelRender.java:217](core/src/main/java/dev/xcolorful/customgun/client/model/_AttachmentModelRender.java:217)：瞄具那组 pass（镜身/目镜环）按节点路径烘焙成子网格，绘制矩阵取施加路径前的姿态，路径变换烘进顶点。
- 失效：节点全变换字段指纹 + RenderType/光照/颜色；F12 采集期间强制重建；`_AssetsInstanceManager#clear` 在渲染线程释放全部网格。

**安全网**（画错不如退回即时管线）：第一人称、非等比缩放姿态、functional 回调直接发射几何（状态段用 `DiscardVertexConsumer` 前后差值检测）都会回退。

**探针**：新增 `Static bake` 计时、`Bake rebuilds`、`Distinct RenderTypes`（B2 下限）。模板“每帧一清”没有做——静态场景的瞄具窗口不写模板值，多枪共用模板缓冲时改成一清会变语义，收益仅 0.059 ms/帧，留给 B2；`GL_TIME_ELAPSED` 区间查询会与整帧查询嵌套，也未做。三点都写进了 [b1a-static-bake.md](docs/architecture/client/render/refactor/b1a-static-bake.md)，含验收清单与已知光影风险。

`./gradlew --offline build` 通过。**游戏内画面、光影（Oculus）与缓存失效尚未验证**，需要按文档清单实机跑一遍；另有一个独立代码评审仍在跑，结果出来我会回来处理。

The review surfaced two claims I need to verify against the 1.20.1 shader/level-render sources before changing anything.

The reviewer's key finding is verified against the 1.20.1 shader sources. Let me check whether newer versions behave the same before proposing options.

复核否掉了 B1 的核心前提，因此我把烘焙路径默认关掉了（`StaticBake.ENABLED = false`），当前行为与重构前完全一致。

**证据**（1.20.1 / 1.20.2 / 1.20.4 / 1.21.1 的 `rendertype_entity_cutout.vsh` 一致）：

```glsl
vertexDistance = fog_distance(ModelViewMat, IViewRotMat * Position, FogShape);
vertexColor    = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color);
```

- 光照直接用**原始 `Normal`**，不乘 `ModelViewMat`。原版实体渲染在 CPU 端用 `PoseStack.Pose#normal()` 把法线转到**视图空间**再写顶点；烘焙网格存的是模型空间法线，实例矩阵只作用于位置 → 旋转过的展示框/掉落物/第三人称光照错误。
- 雾用 `IViewRotMat * Position`，要求 `Position` 已是视图空间；模型空间坐标代入后雾距错误。
- 这不是 1.20.1 独有，**B2 的世界空间合并依赖同一条假设**，所以是方案层问题。26.2+ 的 `DynamicTransforms` 管线是否消费矩阵还没核对。

**本次已完成的实现**（保留，默认不生效）：状态段/几何段拆分、`BakedMesh`/`StaticBakeCache`、瞄具 pass 子网格烘焙、资源重载释放、F12 强制重建、三个新探针；缓存键/失效条件、退回即时管线的三种情况都写进了 [b1a-static-bake.md](docs/architecture/client/render/refactor/b1a-static-bake.md)。另外**移除 1.20.1 双重节点快照**是独立收益（multi 实测 0.135 ms/帧），不受这个问题影响。

**复核还指出并已处理/登记**：着色器在 `setupRenderState` 之前取（已修）；`DiscardVertexConsumer` 单例重入（已改为前后差值，不再 reset）；光照值进缓存键会让不同位置的展示框分裂成多份网格、可能触发 LRU 抖动（已登记，路线定了再处理）；Iris/Oculus 绕过缓冲源（原有风险，已在文档）。

三条路线：
- **A 自定义 shader**：位置/法线/雾都按实例矩阵算，片元照抄 `entity_cutout`。保住“零逐帧顶点”，B2 也可行；代价是 `core` 不能直接 import Forge 的 shader 注册事件（要走 compat 层），且光影包不会映射自定义程序，需要“有光影退回即时管线”的开关（现有 `IrisCompat.isUsingRenderPack` 只有外部 shader-compat 附加模组存在时才有值）。
- **B CPU 回放**：缓存模型空间顶点，每帧按姿态写入 `BufferSource`，省掉场景图遍历与逐顶点分配，画面与原版完全一致、光影安全；但去不掉上传与每顶点乘法，multi CGC 大概只能降 20–40%，到不了 0.85 ms。
- **C 维持现状**：打开开关做实机对照，看光照/雾偏差能否接受（今天就可看，但 B2 沿用同一假设）。

`./gradlew --offline build` 通过；游戏内未验证。

# 

```
最终方案是需要在光影下也能吃到优化
- 如果最终重构完会在光影下性能没有根本提升，则不优先选择
- 如果需要加Oculus/Iris接口，则在IrisCompat补接口，在CGC Shader Compat里mixin接上
- 如果需要对Oculus/Iris mixin，则写在Shader compat里
- CGC和Compat配合，可以Compat改用CGC本地依赖
- 如果要加Forge事件注册，可以参考ClientRenderRegistry
- 如果没有合适方案，再提出
```

# 

The user's constraints need concrete facts about how Oculus/Iris route custom draws. Let me read the project's prior findings and inspect both shader-compat repos' model of integration.

The shader-side facts are now clear: Oculus swaps shaders at `GameRenderer.getRendertype*Shader()`, and its pack shaders consume the model-view matrix for both position and normal — so a per-instance matrix is *correct* under a pack, while the no-pack vanilla shader needs our own program. I'll implement both paths.

The user's guidance points at `ClientRenderRegistry` as the registration pattern. I'll refactor the shader hook to that shape instead of a separate provider service.

The GLSL has a type error (`mat3 * mat4`). Fixing it before the build ships.

方案已经按“光影下也要吃到优化”落地，核心结论：

**Oculus/Iris 下的路径不需要回退。** 我核对了 Oculus 源码：光影包起作用时，`GameRenderer.getRendertype*Shader()` 被注入，Oculus 会把该 `RenderType` 的 shader 换成光影包的实体 program（`ENTITIES_CUTOUT_DIFFUSE`，按 shadow/hand/block-entity/entities 阶段分支）；`ExtendedShader.apply()` 用 `drawWithShader` 传进去的 `ModelViewMat` 计算 `NormalMat = inverse-transpose(ModelViewMat)`。也就是说**光影包的着色器本来就把实例矩阵同时用于位置、法线和雾**，我们只需在 `setupRenderState()` 之后取 `RenderSystem.getShader()` 即可。

**无光影**则用新注册的 core shader `customgun:cgc_baked_entity`：
- 位置：`ProjMat * ModelViewMat * Position`
- 法线：`inverse-transpose(mat3(ModelViewMat)) * Normal`（等比缩放下与原版 `PoseStack.Pose#normal()` 逐位一致）
- 雾：`fog_distance(mat4(IViewRotMat) * ModelViewMat, Position)`，与 `entity_cutout` 的世界对齐口径一致
- 片元着色器逐行照抄 `entity_cutout`

**接线方式**（按你指定的 `ClientRenderRegistry` 约定，core 不 import Forge）：
- core：`ICoreShaderRegistrar` + `ClientRenderRegistry#onRegisterShaders`，`BakedEntityRenderState` 保存最新 `ShaderInstance`；
- forge-compat：`ForgeCoreShaderRegistration` 监听 `RegisterShadersEvent`，每次资源重载重新注册；
- `StaticBake#resolveEntityShader()` 每次绘制时查询（重载会换实例）。

**顶点格式**：开光影包时 Iris 的 `BufferBuilder` 混入会把 `NEW_ENTITY` 扩成 `IrisVertexFormats.ENTITY`。缓存键已加入光影状态，并且 `BakedMesh` 记录烘焙时**实际**格式，绘制前与当前状态比对，不一致（刚切包）只让这一帧退回即时管线，下一帧重新烘焙。

**cgccshader 1.20.1 不需要改**：`IrisCompat.isUsingRenderPack` 的 mixin 已经存在，1.20.1 也没有 26.x 的 `RenderPipeline` 映射表，不需要 `assignPipeline`。没装 cgccshader 的 Oculus 用户会走格式不一致的保护分支——画面正确但没有优化，这条已写进文档。

**26.3 的计划**（阶段 4，未实现）：走自定义 `RenderPipeline` + `GpuDevice.createBuffer`/`PreparedRenderType.drawFromBuffer`，并在 `IrisCompat.registerRenderPipelines` 注入点由 26.3 的 cgccshader 调 `IrisApi.assignPipeline(pipeline, IrisProgram.ENTITIES)`；还需处理 OIT，以及 26.3 `entity.vsh` 用原始 `Position` 算雾的问题（标准实体管线把模型矩阵烘进了顶点）。

`./gradlew --offline build` 通过，shader 资源已打进 jar。游戏内（尤其开光影、旋转展示框的光照、远处雾）仍需按 [b1a-static-bake.md](docs/architecture/client/render/refactor/b1a-static-bake.md) 的清单验收；另有一个独立复核仍在跑，结果出来我会回来处理。

The review found a real fog bug (cylinder mode zeroes Y in model space) plus a few robustness gaps. Fixing them now.

独立复核回来了，找到一个真实缺陷并已修复，另外几处健壮性问题也一并处理了。

**复核发现与修复**

1. **圆柱雾（户外默认）算错** — 我原来的 `fog_distance(mat4(IViewRotMat) * ModelViewMat, Position, FogShape)` 会在**模型空间**里执行 shape=1 的“清 Y”，而原版是在相机旋转抵消后的世界对齐坐标里清 Y。现在先算 `IViewRotMat * ModelViewMat * Position`，再用单位矩阵交给 `fog_distance`，两种雾形状都与 `entity_cutout` 一致。
2. **着色器注册失败会崩** — `ShaderInstance` 会把编译/JSON 错误包成运行时异常，原来只 catch `IOException` 接不住；现在改为 catch `Exception`，失败只记日志并退回即时管线。
3. **超大网格静默不画** — 网格超过 32 MiB 预算时会在 `put` 里被立刻淘汰并关闭，而 `draw` 对已关闭网格返回成功。现在淘汰至少保留一个条目，且 `draw` 对已关闭网格返回失败 → 退回即时管线。
4. **光照键导致缓存抖动** — 条目上限从 32 提到 128（内存仍由 32 MiB 兜底），满配不同配置的枪不再反复重烘；跨光照的二级缓存仍留给后续。
5. **功能节点缓存过期** — `setFunctionalRenderer` 在首次渲染后再挂回调时，祖先的 `containsFunctional` 缓存会过期导致回调永不执行；新增 `invalidateFunctionalCache()` 并在设置处调用。
6. `setupRenderState()` 移进 `try`，异常时不再漏掉 `clearRenderState()`；另外复核确认 `inverse-transpose(ModelViewMat)` 对**任意仿射姿态**（含非等比缩放）都与原版 `PoseStack.Pose#normal()` 等价，所以删掉了原来那个不必要的“非等比缩放退回”分支，烘焙覆盖面反而变宽。

**复核确认没问题的部分**：着色器注册时序（mod 构造期挂监听、每次重载重建、`volatile` 读取）、Oculus 路径的 `getRendertypeEntityCutoutShader` 注入与 `ExtendedShader` 的 `NormalMat` 推导、`useExtendedVertexFormat` 与 `isUsingRenderPack` 的等价性、模板窗口与 setup/clear 平衡。

**仍然成立的限制**（都已写进 [b1a-static-bake.md](docs/architecture/client/render/refactor/b1a-static-bake.md)）：没装 `cgccshader` 的 Oculus 用户会自动退回即时管线（画面正确、无优化）；OptiFine 光影不在目标内；退回路径会让 functional 回调在同一帧执行两次，第三方 addon 需要幂等。

`./gradlew --offline build` 通过，shader 资源已打进 jar。剩下的是实机验收：重点看旋转展示框/掉落物的光照、远处展示框的圆柱雾、开关光影包的 A/B，以及 `Bake rebuilds` 稳态是否为 0。
