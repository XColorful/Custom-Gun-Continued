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
- core：`IShaderRegistrar` + `ClientRenderRegistry#onRegisterShaders`，`BakedEntityRenderState` 保存最新 `ShaderInstance`；
- forge-compat：`ForgeClientRenderRegistry` 监听 `RegisterShadersEvent`，每次资源重载重新注册；
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

# 
---

```
读`./docs/architecture/client/render/PLAN.md`和`#92 Static-bake-refactor.md`第1-121行

针对刚才的commit的变动内容，目前测试：
- 开光影后旋转展示框，枪纹理的阴影有更新
- 物品掉落物实体的阴影会随旋转更新
- （不用写报告）目前跑了一个26.35.11.1-4/b1-a-1/，只有Afterburner的统计，帧数应该是有优化了
- 在multi空手场景按F12后的3.96秒以内会崩溃，日志在D:\Temp-File\下hs_err_pid7128.log，debug.log

需要修复探针问题，文档其他的人工测试等修完再测
```

# 

Now the bake method itself.

探针崩溃已定位并修复。

**证据链**

- [hs_err_pid7128.log](D:/Temp-File/hs_err_pid7128.log) 里不是普通堆 OOM：`Failed to commit metaspace`，同时 `current process commit charge: 25817M`、`AvailPageFile size 2M`、堆只用了 1.25G。也就是说进程提交了 25.8 GB 原生内存，把页面文件吃光了。
- 20 GB 的原生内存只能来自逐帧分配且不释放的路径。核对 1.20.1 的 `BufferBuilder`（[BufferBuilder.java:41](file:///tmp/bbtmp/com/mojang/blaze3d/vertex/BufferBuilder.java)）后确认：构造时 `MemoryTracker.m_182527_(capacity * 6)` 直接 `malloc`，整条释放路径（`m_231179_` → `m_85730_`）只重置计数，**从不归还内存**；原版靠 `BufferSource` 每 RenderType 复用一个实例来规避。
- 我们每帧强制重建时每次都 `new BufferBuilder(1 MiB)` = 每次泄漏 6 MiB。multi 场景每帧数次烘焙、几百 FPS，3.96 秒就够累积到二十几 GB。第一次崩溃前的真实异常大概率是 `MemoryTracker` 的 `OutOfMemoryError("Failed to allocate …")`（发生在实体渲染中），随后生成 CrashReport 时要加载类、metaspace 提交 64 KB 失败，JVM 直接 abort——与 hs_err 的栈完全对得上。

**修复**

[BakedMesh.java](core/src/main/java/dev/xcolorful/customgun/client/model/bake/BakedMesh.java:67) 改为按烘焙嵌套深度复用 `BufferBuilder`（深度池，正常流程深度恒为 1），每次烘焙不再新建原生缓冲。复用后 `begin` → `endOrDiscardIfEmpty` → `VertexBuffer.upload`（内部 release 归零）正是原版 `BufferSource` 的用法，语义不变。顺带说明：这个泄漏在非 F12 下同样存在（每次缓存未命中泄漏 6 MiB），所以修复对正常游玩也有效。

文档 [b1a-static-bake.md](docs/architecture/client/render/refactor/b1a-static-bake.md:69) 的几何段补了这条约束与实测数据。`./gradlew --offline build` 通过，改动未提交，等你按清单实测。

# 

```
开关光影:
- 枪配件、瞄具、开镜都没问题
- 满配枪放展示框旋转、物品实体掉落物旋转都没问题
- 枪换配件立即更新

等帧数跑到之前-multi空手最高时（手机拍照，FPS和RAM一直在波动，只看MEM）：
- 842FPS, RAM 9032MB, MEM 933MB
- F3+T后连拍两张：723FPS, RAM 9086MB, MEM 932MB; 772FPS, RAM 8980MB, MEM 892MB
- MEM稳定后：837FPS, RAM 8947MB, MEM 875MB
- 再F3+T后拍照：770FPS, RAM 8916MB, MEM 893MB
- MEM稳定后：815FPS, RAM 8937MB, MEM 875MB

另外还跑了份26.35.11-4/b1-a-2/做开光影时baseline
- 26.35.11.1-4要写报告阶段性验收一下
```

# 

分析脚本跑通，数据已算出。现在写批次报告。

明白，先按文档原有风格把内容压到最少，并去掉跨文档重复。

已按 `chain/Home.md` 的密度重写了文档，并去掉了跨文档重复。

**报告**（[测试数据计算报告.md](docs/architecture/client/render/benchmark/26.35.11.1-4/测试数据计算报告.md)，69 行）：只保留数据来源、四步结果表、重构前后对照表、光影三行、功能验收、崩溃修复、结论与异常；推导中的解释性文字删掉，数值只在表里出现一次。数据仍由同目录的 [analyze.py](docs/architecture/client/render/benchmark/26.35.11.1-4/analyze.py) 生成，不手工录入。

**去重**：

- [benchmark/Home.md](docs/architecture/client/render/benchmark/Home.md)：批次表加一行；分工只写「回答什么问题」，数值与结论全部指向报告。
- [TODO.md](docs/architecture/client/render/TODO.md)：B1-a 只留三行——验收通过、待补项、已修复的 F12 崩溃。
- [b1a-static-bake.md](docs/architecture/client/render/refactor/b1a-static-bake.md)：渲染流程里加一句 `BufferBuilder` 复用约束；验收清单顶部一行状态 + 报告链接，不再复述结果。

核心结论（详细在报告里）：无光影 multi 9 把裸枪整帧 5.449 → 1.174 ms、单枪 −93.3%；开光影时空展示框已 6.68 ms，9 把枪只加 0.51 ms，CPU 侧收益在帧时间上看不见，需要一次 `StaticBake.ENABLED = false` 的同场景 A/B 才能定论。

代码改动（`BakedMesh` 复用 `BufferBuilder`）与文档都未提交，等你 review。

另外我把「文档要精炼、同一数据/结论只在一处写全」记进了项目记忆，后续写报告会先对照 `chain/Home.md` 的密度。

# 

[fork1](#fork1)
[fork2](#fork2)

# fork1

```
第一次benchmark测过tacz-multi同场景，明显有触发tacz静态优化，需要在-4报告补上当前阶段跟tacz的对比分析
```

# 

已在 [测试数据计算报告.md](docs/architecture/client/render/benchmark/26.35.11.1-4/测试数据计算报告.md) 补了第 3 节「与 TaCZ 1.2.0 对照」，数值由 [analyze.py](docs/architecture/client/render/benchmark/26.35.11.1-4/analyze.py) 直接解析 `26.35.11.1/tacz1-multi (1).txt` 得出：

| multi 9 把 ak47 | 无手持 | + 第一人称 |
|---|---:|---:|
| TaCZ 1.2.0 | 1.992 ms (502.0 FPS) | 1.185 ms (844.2 FPS) |
| CGC 重构前 | 5.449 ms | 6.533 ms |
| CGC B1-a | 1.174 ms | 1.795 ms |

需要注意的一点：TaCZ 这两个窗口是**反的**——去掉手持枪后的无手持段反而慢 0.81 ms，所以至少有一个窗口不可用（该批 multi 每段只有 1 次重复）。因此报告只取了能站得住的结论：B1-a 之后静态展示从 4.60 降到 0.31 ms，CGC 的 multi 整帧已和 TaCZ 同处 1.2–2.0 ms 区间；按无手持列 CGC 是 TaCZ 的 0.59×，按持枪列是 1.52×，方向相反，具体差额要 TaCZ 端在同场景重测（固定顺序、≥3 次）才能定。

顺带发现 `chain/scenes-and-static.md` 里的 **0.039 ms/枪** 是把「TaCZ multi 持枪 1.185」减「标准场景持枪 1.540」取绝对值再除以 9 得来的——这两个不是同一场景，严格说不成立，报告里已标注它只能作量级参考。改动都未提交。

# 

```
现在重测了tacz的数据，并且之前tacz的测试可能有ak47没拆枪托导致性能略差的问题，更新相应报告
- 目前估计还有潜在的常数优化或没涉及的点
```

# 

新数据到了（TaCZ 1.2.0 / +Accelerated Rendering 各 4 次，协议与 CGC 一致）。我先把它并入分析脚本和报告。

新 TaCZ 数据已并入，报告和相关文档已更新。

**新数据**（同场景、同 4 步协议、各 4 次）：

| 项 | CGC B1-a | TaCZ 1.2.0 | TaCZ + AR |
|---|---:|---:|---:|
| 无光影·空手 | 1.174 ms | **1.106 ms** | 1.279 ms |
| 无光影·持枪 | **1.795 ms** | 1.994 ms | 2.026 ms |
| 开光影·空手 | 7.193 ms | 7.160 ms | 7.187 ms |
| 开光影·持枪 | 7.474 ms | 7.447 ms | 7.436 ms |

结论：静态展示 CGC 只比 TaCZ 慢 0.067 ms（+6.1%），按空展示框基线折算单枪 0.034 vs 0.027 ms；重构前是 TaCZ 的 4.9×。差距剩下的是一个尚未归因的常数项——现在没有 draw 数/模板窗口探针，无法判断它来自 draw、模板还是状态切换，正是你说的「潜在的常数优化或没涉及的点」。持枪 CGC 反而快 0.2 ms；开光影三端完全同水平，再次印证 GPU 主导。

**文档更新**：

- [26.35.11.1-4 报告](docs/architecture/client/render/benchmark/26.35.11.1-4/测试数据计算报告.md)：第 3 节换成同批 TaCZ 数据，表头与结论同步；[analyze.py](docs/architecture/client/render/benchmark/26.35.11.1-4/analyze.py) 负责解析三端，图已重生成。
- 旧批次 [26.35.11.1 报告](docs/architecture/client/render/benchmark/26.35.11.1/测试数据计算报告.md) 加了一行修订说明：TaCZ multi 只有 1 次、步骤顺序相反、ak47 可能未拆枪托，已被 -4 取代。
- [benchmark/Home.md](docs/architecture/client/render/benchmark/Home.md)：-4 的批次范围扩大到三端，分工写明取代关系。
- [chain/Home.md](docs/architecture/client/render/chain/Home.md) 与 [scenes-and-static.md](docs/architecture/client/render/chain/scenes-and-static.md)：TaCZ 由 0.039 改为 0.027 ms/枪，13.6 倍改为约 19 倍，旧的 844.2/1.185 锚点换成 904.0/1.106 与 501.6/1.994。
- [reference-tacz-1.2.0.md](docs/architecture/client/render/refactor/reference-tacz-1.2.0.md) 的 V1 与 [scheme-d](docs/architecture/client/render/refactor/scheme-d-culling-lod-parallel.md) 的参照值同步更新。

代码（`BakedMesh` 的 `BufferBuilder` 复用）和全部文档改动都未提交。

# 

```
我改StaticBake额外做了个AB测试26.35.11-4/b1-a-3/，更新报告
```

# 

A/B 数据已经拿到，它推翻了我之前“光影下 GPU 主导、收益不可见”的推断。我先把 A/B 并入脚本和报告。

A/B 数据已并入，报告新增第 4 节。

**结果**（`b1-a-3`，`StaticBake#ENABLED = false`）：

| 场景 | n | 帧时间 |
|---|---:|---:|
| 空展示框（无烘焙） | 3 | 6.679 ± 0.039 ms |
| 9 把枪·烘焙关 | 4 | **23.949 ± 3.288 ms** |
| 9 把枪·B1-a | 4 | **7.193 ± 0.039 ms** |
| 9 把枪 + 第一人称·B1-a | 4 | 7.474 ± 0.010 ms |

关键结论：

- **光影下烘焙是 3.3× 收益**（23.9 → 7.19 ms）。烘焙关时帧时间远高于空展示框的 6.68 ms，说明重构前在光影下是 **CPU 受限**，不是 GPU 受限；烘焙后才贴近 GPU 下限。
- 之前报告的推断——「CPU 成本 5.45 ms 低于 GPU 6.68 ms，所以光影下收益不可见」——**作废**。错误在于拿无光影的 CPU 数值跨光影外推：Oculus 下立即路径的 CPU 成本高得多。报告里已明确写出这一点，避免后续再踩。
- 同一 A/B 的无光影对照为 5.485 ms，与重构前的 5.449 ms 差 0.7%，证明两次构建只差这一个开关。
- 开光影段波动较大（34.9–46.9 FPS，sd 3.29 ms，CPU 受限时对系统负载更敏感），最慢一次仍比烘焙后慢 4.0×，不影响结论。

**同步更新**：[analyze.py](docs/architecture/client/render/benchmark/26.35.11.1-4/analyze.py) 解析四个分组并重画了图（A 面板加入「烘焙关」，B 面板改为光影下 A/B 与三端对比）；[TODO.md](docs/architecture/client/render/TODO.md) 的「光影 A/B 待补」改为已完成并记录 23.9 → 7.19 ms，数据清单里对应的待办也勾掉；[benchmark/Home.md](docs/architecture/client/render/benchmark/Home.md) 的 -4 行与分工补上 A/B 构建和结论。

全部改动未提交，等你 review。

# 

```
雾的对照怎么做?
```

# 

雾的对照只需要区分两件事：**哪条着色器在算雾**，以及**和谁比**。

**两条路径**

- 无光影：走我们自己的 [cgc_baked_entity.vsh](core/src/main/resources/assets/customgun/shaders/core/cgc_baked_entity.vsh:49)，`vertexDistance = fog_distance(mat4(1), IViewRotMat * ModelViewMat * Position, FogShape)`。历史 bug 是「在模型空间清零 Y」，表现为**远处的枪完全不雾化**，或旋转框时雾量跟着变。
- 开光影：这个着色器不参与，雾由光影包的实体 program 负责，只做目视一致性检查。

**最省事的参照物**：展示框的木框本身是原版 `entityCutout` 渲染的，和枪在同一格、同一距离。直接看「枪 vs 木框」是否同步变白/变灰，比另外摆参照方块准确。掉落物同理。

**三个必测场景**

|场景 |怎么摆 |正确表现 |能抓到的错 |
|---|---|---|---|
|陆地（CYLINDER） |渲染距离 2–3，展示框放在 28–32 / 43–48 格的雾带里（雾带 = 最后 clamp(RD×16/10, 4, 64) 格） |枪与木框同色，雾量只随水平距离变化 |枪比框清晰 = 旧 bug 还在 |
|旋转 / 高度 |同一距离，框内物品转 0/4/8，框放到相机高度 ±10 格 |圆柱雾下雾量完全相同 |雾量随旋转变化 = 用了模型空间坐标 |
|水下（SPHERE） |渲染距离 ≥ 7（否则原版会把 end=96 钳到渲染距离并改回 CYLINDER），无水下呼吸，框放在 10/30/60/90 格 |雾随三维距离变化，高度越高越重；枪与木框一致 |球雾分支没走对 |

每个场景再补一项：**开光影同一位置**，确认枪的雾和同距离地形/框一致，没有「双重雾」或枪不上雾。

**对照方法**

1. 用 `/tp @s x y z yaw pitch` 固定相机，记录坐标；`/time set 5000`，渲染距离、视场、光影包两次完全相同。
2. F1 隐藏 HUD 后 F2 截图；烘焙开（正常构建）和烘焙关（`b1-a-3` 的 `ENABLED=false` 构建）各截同一组。
3. 逐张比，只看雾的起始距离和「枪/框」的相对雾量，不比整体亮度。
4. 记录表：场景、距离、旋转、高度、烘焙开/关、期望、实际。

注意渲染距离会直接改雾参数，测完恢复；无光影时才有 `cgc_baked_entity` 可测，开光影的结论不能反过来证明自定义着色器正确。

# 

```
更新报告
```

# 

报告已更新，`b1-a-4` 的雾对照写进了新的第 5.1 节。

**记录的结果**

| 场景 | 摆放 | 烘焙开 | 烘焙关 |
|---|---|---|---|
| 陆地·圆柱，RD 2 | 4 个框在 z=32（雾带末端），两把框内物品分别转 3 / 7 下 | 4 把同级别 | 4 把同级别 |
| 水下·球，RD 7 | 10 / 30 / 60 / 90 格各一把，60 格上方 35 格再放一把 | 90 不可见、60 可见、上方极其淡 | 60 已不可见 |

**结论**

- 陆地通过：枪和木框同步雾化，旋转、高度都没有改变雾量，「模型空间清 Y」的旧 bug 未复现。不过该位置在雾带末端（RD 2 的 fog end 就是 32，约 100% 雾），对旋转的分辨力有限，报告里建议下次再在雾带中段 28–32 格放一组。
- 水下烘焙开的表现与球雾换算一致（start −8 / end 96：60 格约 72% 雾、上方 35 格约 83%）。
- **水下烘焙关那次的「60 格已不可见」和换算对不上**（应该是 72%、可见）。最可能的原因是第 3 步陆地测试需要 RD 2，之后没有把渲染距离调回 7——RD 2 时水下 fog end 会被钳到 32 格，60 格自然全雾。报告里已标为待复测，复测时先确认 RD = 7。
- 开光影时雾由光影包负责，两次对照一致；包的雾整体更淡、画面更亮。

**同步**：[b1a-static-bake.md](docs/architecture/client/render/refactor/b1a-static-bake.md:171) 的验收状态改为「第 3 项陆地通过、水下待复测」，[TODO.md](docs/architecture/client/render/TODO.md:16) 的待补项改为「水下雾复测」。改动未提交。

如果那次水下确实是 RD 7，那这个差异就不是渲染距离造成的，需要单独查——届时告诉我，我再按烘焙关/开两条路径的水下雾参数对比一下。

# 

```
更正报告，这个只是等待问题
```

# 

已更正。

- [报告 5.1](docs/architecture/client/render/benchmark/26.35.11.1-4/测试数据计算报告.md:74)：水下烘焙关那次的「60 格已不可见」改为**等待时间不足**——截图时场景还没稳定，不是渲染距离没调回 7，也不是两条路径的雾差异；两次对照都算通过。
- 验收表里雾项从「水下待复测」改为**通过**，结论里的「仍缺」只剩内置探针与 26.3。
- [b1a-static-bake.md](docs/architecture/client/render/refactor/b1a-static-bake.md:171) 的验收状态改为「第 3 项（雾）通过」，[TODO.md](docs/architecture/client/render/TODO.md:16) 的待补项只剩内置探针。

陆地那组仍保留一条说明：4 个框在雾带末端（≈100% 雾），对旋转的分辨力有限，要更严格可以在 RD 2 的 28–32 格之间再放一组——这条只是建议，不影响通过结论。

改动未提交。

# fork2

```
见1.20.1最近两个commit，我进行了风格统一调整
需要说明原本ModelObject和BedrockPart用的hash常数是否必须是同一个数字（是否需要引用相同来源）
并且如果26.3有近两个commit的风格调整代码的，则也在26.3做同样的修改；若26.3没有该部分代码则不用改
```

# 

## 1. 两个 hash 常量是否必须相同

**不必须，也不需要引用同一个来源。** 现在两者都指向 `MathUtil.GOLDEN_RATIO_64`，只是「同一个已知常量只定义一次」的顺手写法，不是语义约束。

- [MathUtil.java:29](core/src/main/java/dev/xcolorful/customgun/core/util/MathUtil.java:29) 是常量的唯一定义（Fibonacci hashing 的 64 位黄金比例）。
- [ModelObject.java:33](core/src/main/java/dev/xcolorful/customgun/client/model/ModelObject.java:33) `FINGERPRINT_SEED` 是**模型级**指纹链的初值：`fingerprint = fingerprint*31 + part.renderStatus(...)`，最终进 `StaticBakeCache.Key`。`_AttachmentModelRender` 给瞄具子网格也用它。
- [BedrockPart.java:241](core/src/main/java/dev/xcolorful/customgun/client/model/bedrock/BedrockPart.java:241) `STATE_HASH_SEED` 是**单节点**自身 hash 的初值，只在 `stateHash()` 内部使用；子树 hash 由它开始、被父节点 `*31 +` 折叠。

需要的性质只有两条：同一状态得到同一结果；状态变化时结果高概率不同。初值取任何 64 位常量都满足，两者互不参与对方的计算，所以**取值不同不会影响正确性**。共用一个数字唯一的后果是外层链和内层链初值相同——由于两边都用 `*31 +` 且内层值已经混合过，观察不到差异；想让两级更独立也可以换成不同常量，没有实际收益。

另有两点值得知道：

- **值本身没有兼容性要求**：指纹只在进程内比较（缓存键 + `cgc$lastStateHash`），不落盘、不跨会话，缓存随资源重载清空。改常量不会造成任何失效或存档问题。
- **同源 ≠ 必须同值**：`GOLDEN_RATIO_64` 只统一了「这个数是什么」的说明；两个具名常量仍可各自独立演进，改动时不需要同步。反过来，如果以后想区分两级 hash，直接把其中一个指向别的常量即可。

另外确认一下这次改动的语义：`stateHash()` 改成无参、内部播种后，等价于原来的 `stateHash(GOLDEN_RATIO_64)`（原调用点传的也都是这个值），所以 hash 值与改动前一致，缓存行为不变。

## 2. 26.3 的同步情况

我把两个 commit 的改动逐项对到 26.3（`Render-refactor-26.3`）：

**没有对应代码、未改动**：

- B1-a 全部实现（`ModelObject#renderBaked`、`StaticBake`/`BakedMesh`/`DiscardVertexConsumer`/`StaticBakeCache`、`BedrockPart#renderStatus/renderGeometry/stateHash`、`cgc_baked_entity.vsh`）——26.3 没有 `client/model/bake/`，也没有这些方法。
- core shader 注册（`ICoreShaderRegistrar`/`IShaderRegistrar`、`ForgeCoreShaderRegistration`、`ForgeClientRenderRegistry`、`BakedEntityRenderState`）——26.3 的 `ClientRenderRegistry` 里没有这部分。
- `MathUtil.GOLDEN_RATIO_64`——26.3 没有 hash seed 使用方，不加未使用的常量。

**有对应代码、已做同样修改**（仅注释/Javadoc）：

|文件|改动|
|---|---|
|[26.3 ModelObject.java](D:/Github/Custom-Gun-Continued-26.3neoforge/core/src/main/java/dev/xcolorful/customgun/client/model/ModelObject.java)|删除「对应原模组…」类注释|
|[26.3 ClientRenderProfiler.java](D:/Github/Custom-Gun-Continued-26.3neoforge/core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderProfiler.java)|类注释与「测量与数据登记」行的句末句号|
|[26.3 GpuFrameTimer.java](D:/Github/Custom-Gun-Continued-26.3neoforge/core/src/main/java/dev/xcolorful/customgun/client/util/GpuFrameTimer.java)|类注释句末句号|

26.3 的 `GpuFrameTimer`（renderpearl 查询池）和 `ClientRenderProfiler`（26.2+ 延迟渲染版、无 `BAKE` 探针）实现本身与 1.20.1 不同，所以只同步了确实对应的标点/类注释，其余语义没有动。改动全部是注释，未编译验证；两个 worktree 都未提交。
