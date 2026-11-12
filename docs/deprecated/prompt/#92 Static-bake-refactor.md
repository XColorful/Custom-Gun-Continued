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

# 
---

```
读`./docs/architecture/client/render/PLAN.md`，看下B1-a是否已完成
- 如果还有没完成或有需要在B1-b前要测的，列出
- 说明是否可以开始B1-b，预计是进行什么优化，优化目标是否已测定
```

# 

PLAN.md 指向 TODO.md 作为入口，我先读它和重构方案。

读完 PLAN.md、TODO.md、`refactor/Home.md`、`b1a-static-bake.md` 与 `26.35.11.1-4` 验收报告，并核对了代码与两个 worktree 的实际状态。

## B1-a 状态：1.20.1 已完成并验收，但有 3 项明确未收尾

已确认的事实（代码 `StaticBake.ENABLED = true`，1.20.1 分支已 commit）：

- 功能验收通过（2026-11-11，[报告](docs/architecture/client/render/benchmark/26.35.11.1-4/测试数据计算报告.md)）：画面 / 光照 / 雾 / 失效 / 掉落物 / 资源重载 / 光影 7 项全过
- 无光影 multi：CGC 4.597 → 0.307 ms（−93.3%），整帧 5.449 → 1.174 ms
- 开光影 A/B：23.949 → 7.193 ms（3.3×），推翻了「光影下收益不可见」的旧推断
- 顺带 A 项：已移除 1.20.1 双重节点快照

未完成 / 搁置项（都已在文档登记，不是遗忘）：

| 项 | 状态 | 归属 |
|---|---|---|
| 内置探针数据（验收清单第 8 项） | **未做**，本批全靠 Afterburner 外部采集 | 随 B1-b 一起加 |
| 模板每帧一清 | **未做**，0.059 ms/帧 | 明确留给 B2 重排 pass |
| 26.3 实现 | **未开始**，26.3 worktree 无 `model/bake/` 目录 | 阶段 4 |
| 剩余 0.067 ms/枪（对比 TaCZ）、multi 0.97 ms 未归因 | 未归因，缺内置 draw / 模板探针 | 随 B1-b |
| 同模型不同光照二级缓存 | 未做，靠 LRU 32 MiB 兜底 | 风险项 |
| OptiFine 光影、无 cgccshader 的 Oculus | 未验证 / 退回即时管线 | 已知风险，非阻塞 |

## B1-b 前建议补测的（TODO 里明确列出的 3 项）

1. **按节点的脏计数** — TODO 原话「B1-b 前才有意义」。这是决定 B1-b 遍历残留的唯一未测量输入，也决定下面目标区间的宽度。
2. **包住「本帧所有 CGC draw」的 `GL_TIME_ELAPSED`** — 归因 multi 的 0.97 ms；与整帧查询冲突，需先重做计时器分配。
3. **整帧 GPU 查询与分配采样改每 N 帧** — 探针固定开销约 0.048 ms/帧。

严格说这三项都不是 B1-b 的**阻塞项**：PLAN 口径是「新增探针随重构代码一起加，不单独跑采集批次」。第 1 项缺了不会挡住实现，只是 B1-b 的脏子树收益只能事后验证。

## 可以开始 B1-b，目标区间已给但关键输入未测

**做什么**（TODO 阶段 2 + `refactor/Home.md`）：root 变换提升为绘制矩阵（不再逐节点乘 `PoseStack`）、脏子树重写（只重生成动画覆盖的子树）、瞄具模板从烘焙网格重放、附件模型并入烘焙。

**基线已实测**（1.20.1，`26.35.11.1-3`）：

- CGC：裸配 0.810 ms / 满配 1.951 ms
- 几何遍历：0.526 / 0.798 ms
- 瞄具模板：满配 0.719 ms（满配 CGC 的 36.8%）
- flush：0.151 / 0.423 ms；满配 19 次 draw
- 活跃动画通道 14 条

**目标（推算，验收时回填）**：

| 场景 | 当前 | B1-b 目标 CGC | 目标帧时间 |
|---|---:|---:|---:|
| 第一人称裸配 | 0.810 | ≈0.25–0.35 ms | 1.77 → 1.15–1.25 ms |
| 第一人称满配 | 1.951 | ≈0.45–0.60 ms | 3.02 → 1.45–1.65 ms |

需要说清的是：**目标区间是推算值**，其中「几何遍历残留」一栏给的是 0.05–0.15 / 0.1–0.2 ms 的区间，宽度完全来自未测的脏节点占比。也就是说基线已测定、公式已确定，但唯一影响区间落点的实测项没有。如果要让 B1-b 的验收有硬判据，脏计数探针应该在动手前补上，成本很小（在 `State changes` 同一处按节点累加）。

另一个前置判断：B1-b 的第一人称路径会绕过 `StaticBake.eligible` 的 `!firstPerson()` 排除，需要先确认第一人称下 `State changes` 每帧 1–2 次、12 条通道对应的具体节点集合（文档只给了通道数，没有节点分布），否则「脏子树」的粒度没法设计。

# 

```
先补上B1-b前要测的探针
- 需要注意探针是否降低FPS，是否干扰测试项，可以调整已有测试探针
- 预计按26.35.11.1-4/b1-a-5/开测，如果测试需要额外说明则需要指出
```

# 

我先读一遍现有探针实现，确认计时器分配和 `State changes` 的采集点，再动手。

设计已定，开始改代码。先重写 GPU 计时器（分段 + 查询池 + 每 N 帧采样）。

代码改完并编译通过。现在更新文档与测试说明。

`标准.txt` 是用户刚放进 `b1-a-5` 的基准口径（AF 两步、multi 场景），我把测试说明改成与它对齐、只补内部探针的部分。

三项探针已补齐，三个模块编译通过（未进游戏实跑，数值仍需 b1-a-5 采集）。

## 改了什么

**1. CGC 绘制区间的 GPU 时间**（`GpuFrameTimer` 重写 + `ClientRenderProfiler`）

`GL_TIME_ELAPSED` 不能嵌套，所以整帧查询不再是一个一次性查询，而是**按段切分**：CGC 提交开始时结束整帧段、开启 CGC 段，提交结束时反过来。只有这一个活动查询，结果按 tag 分流到 `GPU frame busy`（仍是整帧口径，包含 CGC 段，可与旧批次直接比）和新增的 `CGC GPU busy`。

计时点在第一人称与 `renderByItem` 的 `gunModelObject.render(...)` 两侧（含瞄具模板、配件、delegate 的 draw）。

**2. 按帧去重的节点脏计数**（`BedrockPart` / `FunctionalBedrockPart`）

`Distinct nodes`（本帧第一次访问的节点）+ `Dirty nodes`（状态指纹与上一帧不同），状态取 `stateHash()`，与烘焙缓存失效口径一致。按帧去重是必要的：9 个相同展示框共享同一批节点，按提交累加会把比例稀释成 1/9。报告另给一行 `Dirty node share`。

只统计即时管线（`BedrockPart.render`）；静态烘焙路径为 0，与 `State changes = 0` 一致。

**3. GPU 查询与分配采样降频**

`SAMPLE_EVERY = 4`，未采样帧不开任何查询、不读分配字节。这是"探针是否降 FPS"的主要对策。

## 关于探针开销

- **不采集时**：每个节点多一次 `enabled` 的 volatile 读 + 一次分支（现有 `countCube` 已经是这个模式），无 GL、无哈希。
- **采集时**：查询对象改为固定池复用（去掉每帧 `glGenQueries`/`glDeleteQueries`），且只采样 1/4 帧。
- 为了抵消新增的逐节点哈希，把 `ModelObject.render` 里原有的状态指纹循环改成**一帧只算一次**（原来 9 个展示框会把同一批节点哈希 9 遍）。`State changes` 的语义从"每次提交"变成"每帧"，在现有场景下数值不变（multi 恒 0、第一人称每帧一次提交）。

有一个判读变化要注意：GPU 两行的"占帧比例"从约 99% 变成约 **25%**（采样率），不是掉帧。

## 测试需要额外说明的一点

`b1-a-5/标准.txt` 只定义了 multi 的 AF 两步（无手持 / 开光影），**没有第一人称满配**，而 B1-b 的脏子树比例恰恰要用它。所以我在 [测试说明.md](docs/architecture/client/render/benchmark/26.35.11.1-4/b1-a-5/测试说明.md) 里把两类数据分开写：帧时间仍按 `标准.txt`，F12 探针需要额外采 5 个场景，其中新增的是**第一人称 m4a1 满配**与**multi + 手持满配**。

其余口径：F3 必须关闭（原版占用同类查询，GPU 探针停用）；开光影时 `endBatch` 是入队语义，`CGC GPU busy` 接近 0、只有整帧可信；采集期间不要移动视角/切枪。

文档同步改了 [TODO.md](docs/architecture/client/render/TODO.md)、[chain/Home.md](docs/architecture/client/render/chain/Home.md)、[refactor/Home.md](docs/architecture/client/render/refactor/Home.md)、[b1a-static-bake.md](docs/architecture/client/render/refactor/b1a-static-bake.md)。

改动未提交；另外 `docs/deprecated/prompt/#92` 与 `b1-a-5/标准.txt` 是你自己的改动，我没有动。

# 

```
我改了下标准，看下是否足够测到数据
- 标准同目录的.md要去掉，这里不放文档，等测完再写测试报告
```

# 

`标准.txt` 里有一个会毁掉 multi 帧数据的问题，我先修。`StaticBake` 目前是「F12 开着就每帧强制重建」，b1-a-3 的说明也验证过这一点（`开F12后帧数跌至167左右，优化全没，故不开F12`）。

**结论：够用**，标准里的三步已经覆盖 B1-b 需要的全部三类数据；但有三处要改，另有一个干扰项我已经在代码里修掉了。

## 步骤与数据的对应

| 标准步骤 | 拿到的探针数据 |
|---|---|
| 测试方式 1（空快捷栏） | 帧时间基线、探针自身开销 |
| 测试方式 2（第一人称裸配） | `Distinct nodes` / `Dirty nodes` / `Dirty node share` |
| 测试方式 3（第一人称满配） | 同上 + `Scope render`（B1-b 主目标场景） |
| multi 1（9 个满配展示框、空手） | `CGC GPU busy`、`Distinct RenderTypes`、`Bake rebuilds` 稳态 |
| multi 2 / 3 | multi 与第一人称混合时的分段归因 |

开光影被删掉了，对新探针没有影响（光影下 `CGC GPU busy` 本来就不可信）；只是不再有光影帧时间基线，B1-b 的目标表是无光影数字，所以不阻塞。

## 已修的干扰：F12 会强制每帧重烘

`StaticBake` 原来的条件是「`ClientRenderProfiler.enabled` → 跳过缓存、每帧重建」，这是 b1-a-3 标准里「开F12后帧数跌至167左右，优化全没，故不开F12」的根因。本次改成独立的 `StaticBake.FORCE_REBUILD`（默认 false），F12 采集恢复稳态，`Bake rebuilds` 才有可能是 0（验收清单第 8 项本来就要求这个）。人工验收缓存失效时手动打开该开关即可。

## 仍建议调整的三点

1. **帧时间与 F12 分两次跑**。现在 F12 开着不再毁掉 multi，但 b1-a-4 的帧时间（1.174 ms）是**完全不开 F12** 测的；同一次运行里采探针，帧时间会带上探针开销，两组数不能直接比。探针数据本身不需要 AF。
2. **文件名**：标准里写「DebugKey保存的Benchmark.txt」，实际是游戏目录下的 `customgun-render-profile.txt`；`Benchmark.txt` 是 Afterburner 的。别收错文件。
3. **「单个Benckmark.txt保存以上两次记录」**：现在有 3 步，这句要改成三次。

## 一个可比性提醒

multi 现在放的是 9 把**满配**枪，而 b1-a-4 的 1.174 ms / 0.307 ms 来自 9 把 ak47（每枪 2 次 draw）。这一批的 multi 数字只能和 b1-a-3 的重构前满配场景（14.723 ms）对照，不要拿 b1-a-4 当基线；同一批里最好保留一组裸配展示框，否则 B1-a 的收益没法在新场景下复述。

`b1-a-5/测试说明.md` 已删除，目录里只剩 `标准.txt`；采集口径的说明移到 [TODO.md](docs/architecture/client/render/TODO.md) 的链接和 [chain/Home.md](docs/architecture/client/render/chain/Home.md) 的探针登记里。代码改动仍未提交。

# 

```
是否这个测试是不需要Afterburner？
- 应该怎么测？拿什么枪？
```

# 

**探针数据不需要 Afterburner**；只有"跨构建比较帧时间"才需要 AF，而且不能和 F12 同时开。

## 为什么

- F12 的 `customgun-render-profile.txt` 自己就带 `Average frame time`、`CGC render per frame` 和全部探针值，这就是 B1-b 判断"脏子树比例 / CGC GPU 归因"的全部输入。
- 但 F12 采集期本身有开销（逐节点哈希一次/帧、1/4 帧的 GPU 查询），帧时间会被抬高。b1-a-4 的 1.174 / 1.795 ms 是**完全不开 F12**测的，所以拿 F12 期间的帧时间去比会系统性偏高。
- AF 的用途只是"干净帧时间"。如果你不打算和旧批次比帧时间，可以整批不碰 AF。

推荐：**一次运行只做一件事**。要帧时间的那次不开 F12；要探针的那次不开 AF。

## 怎么测

**A. multi 组（9 个满配展示框，验证 CGC GPU 归因 + 稳态烘焙）**

1. 进档等展示框内容稳定，关 F3，按住不动视角
2. 空手 → F12 → 10 s → F12 停止（这组是主数据）
3. 再手持裸配、手持满配各采一次（用于"9 把枪 + 第一人称"混合归因）

**B. 第一人称组（验证脏节点比例，B1-b 的输入）**

1. 空快捷栏 → F12 → 10 s
2. `/function customgun:testgun` → F12 → 10 s
3. `/clear` → `/function customgun:testgun_full` → F12 → 10 s

每一段 F12 停下来都会往 `customgun-render-profile.txt` 追加一个区块；一个区块对应一次采集，按场景归档成单独的 txt。

重复次数：探针数据同一构建内 n=3 足够（统计量是帧均）；帧时间仍按旧批次 n=4、每次独立启动。

## 拿什么枪

datapack 里就是这两种，和 b1-a-3 的 26.35.11.1-3 满配基线同一套：

| 函数 | 内容 |
|---|---|
| `/function customgun:testgun` | `tacz:m4a1` 裸配（无配件） |
| `/function customgun:testgun_full` | `tacz:m4a1` + 枪口 `muzzle_brake_cthulhu`、激光 `laser_compact`、握把 `grip_vertical_military`、瞄具 `scope_mk5hd`、枪托 `stock_tactical_ar` |

注意：b1-a-3/4 的 multi 用的是 9 把 `tacz:ak47`（每枪 2 次 draw），这一批是 9 把 m4a1 满配。两者不是同一场景，帧时间只能和 26.35.11.1-3 的满配 multi（重构前 14.723 ms）比，不能和 b1-a-4 的 1.174 ms 比。

## 每段要能回答的问题

| 段 | 看什么 | 期望 |
|---|---|---|
| multi 空手 | `CGC GPU busy` vs 帧时间 | 0.97 ms 未归因项是否落在 GPU；`Bake rebuilds` ≈ 0；`Distinct RenderTypes` 给出 B2 的下限 |
| 第一人称裸配 | `Distinct nodes`、`Dirty node share` | 与 26.35.11.1-3 的 0.810 ms 同口径；脏比例是 B1-b 的核心输入 |
| 第一人称满配 | 同上 + `Scope render` | 瞄具模板 0.719 ms 是否仍在；满配脏比例 |

另外确认一下：`standard` 里"DebugKey保存的Benchmark.txt"应改成 `customgun-render-profile.txt`，否则容易收成 AF 的那份文件。

# 

```
Afterburner测帧时间，但是之前已经测过了静态优化的帧时间比较？本次是否不需要afterburner？
- 是否有必要用m4/满配测？是否改为ak一样行？
```

# 

## 1. Afterburner 不是完全不需要，但只需要跑“第一人称”那两段

静态优化的帧时间确实已经测完、也验收过了（b1-a-4：multi 1.174 ms、光影 3.3×）。本批 multi 的作用**只有一个**——给 `CGC GPU busy` / `Distinct RenderTypes` / `Bake rebuilds` 取探针数据，这不需要 AF。

但第一人称不一样：B1-b 的目标（裸配 0.810 → 0.25–0.35 ms CGC、满配 1.951 → 0.45–0.60 ms）是从 `26.35.11.1-3` 的**AF 帧时间**推出来的。要用同一把尺子验收 B1-b，就需要一次当前构建的干净基线。

所以：

| 场景 | AF | F12 | 原因 |
|---|---|---|---|
| multi（空手 / 手持） | 不需要 | 需要 | 只做探针归因；帧时间已有 |
| 第一人称裸配 m4a1 | **需要** | 单独一次 | B1-b 的入口基线，且不能带探针开销 |
| 第一人称满配 m4a1 | **需要** | 单独一次 | 同上，瞄具模板也在这 |

如果不想用 AF，也成立，但要接受一个后果：B1-b 验收只能看 F12 报告里的 `CGC render per frame` 与 `Average frame time` 的**前后相对变化**，不能拿 1.766 / 3.018 ms 去比——因为 F12 采集期帧时间被探针抬高。

## 2. m4 裸配 + 满配有必要，ak 不能替代

- **裸配**：`testgun` 给的是 `tacz:m4a1` 无配件，正好对上 `26.35.11.1-3` 的第一人称 0.810 ms / 1.766 ms 点位。换成 ak47 会得到另一组数字，B1-b 的目标区间就得重算。
- **满配**必须测，因为 B1-b 的第二项工作就是“瞄具模板从烘焙网格重放”：`scope_mk5hd` 的模板渲染是满配第一人称 0.719 ms、占 CGC 36.8% 的最大单项；ak47 不带瞄具，这一项直接测不到。配件同时决定 delegate / flush 的数量（满配 19 次 draw）。
- **multi 也要满配**：B2 的必要性来自满配的每枪 9 次 draw（81 次/帧）。用 ak47（每枪 2 次 draw）会把 multi 的 draw 地板测低到约 1/4，B2 的下限就失去意义。

可以把 ak47 当作**补充项**（它和 b1-a-4 的 1.795 ms 同场景，用来确认本轮探针改动没有引入回归），但它替代不了 m4a1 裸配/满配这两段。

一个可比性提醒：新标准里 multi 是 9 把 m4a1 满配，b1-a-4 的 multi 是 9 把 ak47，两者不能互比；满配 multi 只能对照 `26.35.11.1-3` 重构前的 14.723 ms。

# 

```
那把afterburner跟内置分开测是否就够了？
```

# 

够，这是最干净的口径。分开跑还有一个额外好处：两边的数字各自有明确的含义，不会互相干扰。

## 两次运行

**A. AF 运行（全程不开 F12）**——只取干净帧时间

1. 空快捷栏
2. `/function customgun:testgun`（m4a1 裸配）
3. `/clear` → `/function customgun:testgun_full`（m4a1 满配）

multi 那组如果也想要一个当前构建的帧时间（9 把满配，B2 的起点），再加一段空手即可；不需要的话可以只跑上面 3 步。

**B. F12 运行（不开 AF）**——只取探针数据

- 第一人称：空手 / 裸配 / 满配，各采 10 s
- multi：空手 / 手持裸配 / 手持满配，各采 10 s

## 这样够不够

| B1-b 需要的输入 | 来源 |
|---|---|
| 第一人称 CGC 基线（0.810 / 1.951） | A（帧时间）+ B（`CGC render per frame`） |
| 脏节点比例、瞄具模板占比 | B |
| multi 的 0.97 ms 是否在 GPU | B（`CGC GPU busy`） |
| B2 的 draw 下限 | B（`Distinct RenderTypes`） |
| 烘焙稳态 | B（`Bake rebuilds`） |

全覆盖。

## 三个约束

1. **A 运行里绝对别按 F12**：探针采集期的帧时间不可用（含逐节点哈希与 GPU 查询开销）。按了就把那一段作废重跑。
2. **两次运行用同一构建、同一地图、同一朝向**，否则 A 与 B 之间、以及与 `26.35.11.1-3` 的对比都会掺入场景差。
3. **B 运行里不要拿 `Average frame time` 当帧时间**——它只是同口径的内部参考。

补充一个可选点：ak47 的裸配作为回归项（对照 b1-a-4 的 1.795 ms）只需要在 A 里加一段，成本很小，能证明这轮探针改动没有引入回归。

# 

```
数据测好了，写报告
```

# 

数据齐了（4 次独立启动 × AF + F12 交替运行）。核心结论我先记一下：脏节点比例（空闲）只有 0.06%–0.27%，multi 的 0.97 ms“未归因”其实就是场景基线，而静态瞄具模板仍有 0.35 ms。现在写报告。

报告已写入 [测试数据计算报告.md](docs/architecture/client/render/benchmark/26.35.11.1-4/b1-a-5/测试数据计算报告.md)，数据为 4 次独立启动（AF 与 F12 交替、时间不重叠），`python analyze.py` 可复现。

## 主要结论

**1. B1-a 在满配 multi 上成立**
- 每枪 CGC 1.199 / 9 = **0.133 ms**（重构前 1.434，−90.7%）；帧时间 14.723 → **2.146 ms**（−85.4%）
- 稳态 `Bake rebuilds = 0`，上传只剩模板窗口的 30.8 KB

**2. 第一人称没有回归**
| 项 | 26.35.11.1-3 | 本批 |
|---|---:|---:|
| 裸配 CGC | 0.810 | 0.802 |
| 满配 CGC | 1.951 | 1.916（瞄具模板 0.713、遍历 0.798、flush 0.406） |

**3. B1-b 的脏子树方向成立，但只是下界**
- 空闲时脏节点 **0.06%–0.27%**（满配 1/1577），遍历 0.798 ms 几乎可以全消
- 这是静止视角的数；开火/换弹会大面积改写节点，**未测**

**4. 0.97 ms「未归因项」有答案了**
- multi 帧时间 − CGC = **0.947 ms**，与空展示框基线 0.866 ms 同量级；CGC 的 GPU 时间只有 0.260 ms
- 它是场景基线，不是 CGC 的 GPU 成本

**5. B2 的地板要上调**
- `Distinct RenderTypes = 6`（81 次 draw 可压到 6），单次 draw 7.8 µs
- 但方案里估 0.05 ms 的静态瞄具模板实测 **0.349 ms**，且不参与合批
- 修正：B2 后 CGC 约 **0.55–0.65**、帧时间约 **1.56 ms**（≤1.6 目标仍可达，余量很小）

**6. 探针开销可接受**
- F12 采集期比同场景 AF 帧时间高 **0.03–0.11 ms**，与旧探针同量级

文档侧已回填：[TODO](docs/architecture/client/render/TODO.md)（三项数据标记为已测得，新增两个缺口）、[refactor/Home.md](docs/architecture/client/render/refactor/Home.md)（multi 表换成实测值、B2 预估修正）、[chain/Home.md](docs/architecture/client/render/chain/Home.md)（数据登记）、[b1a-static-bake.md](docs/architecture/client/render/refactor/b1a-static-bake.md)（验收第 8 项已补齐）。

## 需要你定的一件事

报告里留了两个口径缺口：**multi 的「9 个空展示框」基线**（用于坐实 0.95 ms 是场景基线，目前是按 0.866 ms 的旧基线量级推断）和**开火/换弹时的脏节点比例**（B1-b 的遍历残留不能只按空闲值估）。

是先补这两组数据再进入 B1-b，还是直接开始 B1-b、把它们并入 B1-b 的验收批次？

# 

```
b2-a-7测装弹的标准和换弹代码已经好了，你看下按照这个标准，现有探针是否能捕获到脏节点需要的信息？
先不要写b1-a-6的报告
```

# 

看了 `b2-a-7/标准.txt`、`DebugKey` 的自动换弹实现和 `LivingShooterReload` 的补弹钩子。结论：**能捕获一部分，但最关键的“脏子树覆盖”量测不到；报告口径也不够。**

## 现有探针能拿到的

自动换弹序列（tick 0 / 3 / 6 秒各一次，10 秒自动停止）在 F12 窗口内的每个块都会给出：

| 指标 | 对换弹的意义 |
|---|---|
| `Dirty nodes` / `Distinct nodes` / `Dirty node share` | 每帧有多少节点**自身的局部变换**发生了变化 |
| `Animation channels` | 动画是否真的在跑（静止 14 条但值为常数） |
| `Geometry traverse` / `flush` / `delegate` | 换弹帧的遍历与提交成本 |
| `State changes` | 每帧整体指纹变化（第一人称单场景可信） |

脏计数是按帧与其上一帧比较的，动画逐帧插值，所以换弹期间每帧都会有非零值——**这一项本身是有效的**。

## 三个缺口

**1. 脏节点 ≠ 脏子树（最关键）**

`Dirty nodes` 只统计“自己变了”的节点。B1-b 的重烘焙成本是按子树算的：一根骨骼动了，它下面所有子骨骼的烘焙顶点都要重写。如果动画只驱动 5 根骨骼、但它们覆盖了模型 80% 的节点，现在的探针会报“5 个脏节点”，而实际要重烘焙的是 80%——**方向完全相反**。

要拿到这个数，需要在计数时累加每个脏节点的子树大小（祖先已计入则跳过，避免重复计数）。

**2. 报告只有窗口均值**

计数器目前只输出 `平均 / 帧`，`counterSums` 里其实已经按帧存了原始值。换弹动画持续约 2 秒、三次共约 6 秒，均值会被空闲段稀释，也看不出峰值——而 B1-b 要的是“换弹中最坏一帧要重烘焙多少”。需要给 `Dirty nodes`（和新的子树计数）补 min/max/p99。

**3. 窗口里没有空闲对照段**

自动换弹在 tick 0 就触发，10 秒窗口里没有一段静止帧做内对照，无法从均值里减掉空闲基线。两个办法：把触发时间改成 2 / 5 / 8 秒，或者直接借用 `b1-a-5` 的静止基线（已有：0.06%–0.27%）。

## 顺带一个设计约束（不是探针问题）

换弹时 `MAG_ADDITIONAL` 的 functional 回调会**直接发射几何**，命中 `StaticBake` 的退回条件，整个模型那一帧退回即时管线。第一人称本来就是即时路径，所以现在没有影响；但 B1-b 把第一人称烘焙之后，这几帧会整模型退回，脏子树的收益在换弹最需要的时刻全部消失。B1-b 需要把这种节点做成“单节点退回”，而不是整模型退回。

## 建议的最小改动

1. `BedrockPart` 增加 `subtreeSize`（加载后缓存）与 `Dirty subtree nodes` 计数（脏节点去重、祖先已计入则跳过）
2. 报告给 `Dirty nodes` 与 `Dirty subtree nodes` 输出 min/max/p99（非计数类探针已有这套格式，复用即可）
3. 自动换弹时间点改为 2 / 5 / 8 秒，窗口内保留空闲段
4. 报告增加一行 `Dirty subtree share`

改动量不大（约 40 行）。**注意：探针一变，`b2-a-7` 就得在新构建上重采**，已有数据不能混用。

要我按这四条改吗？还是先只加“子树覆盖”这一项、其余口径用现有数据凑？

# 

```
- MAG_ADDITIONAL单节点返回的问题需要加到文档里待处理
- 0/3/6秒换弹后，9-10秒是空闲的，如果这个空闲不能用再改成1/4/7（8秒开始换弹会换到10秒多）
- 加上测试要的探针
```

# 

三条都收到：MAG_ADDITIONAL 记入待办、换弹时序保持 0/3/6（9–10 s 的空闲段就是对照）、补上子树覆盖与峰值统计。先改代码。

三条都处理完了，没有写 b1-a-6 的报告。

## 探针改动

**1. 新增 `Dirty subtree nodes`**

`BedrockPart.profileNodeState` 在判定节点变脏后，累加以它为根的子树节点数（`subtreeSize()` 加载后缓存，`addChild` 时失效重算）；祖先本帧已经计入时跳过，避免嵌套重复。

根节点（`parent == null`）**不计入**——B1-b 把根变换提升为绘制矩阵，改根不需要重烘焙。所以静止视角下那个每帧变化的 bob 节点不会让脏子树比例虚高到 100%。

**2. 均值之外给了峰值**

报告新增四行：

```
   Dirty nodes (per frame) :  1.23 / frame (min 0.98 / max 45.00 / p99 44.00, 100.0% of frames)
Dirty subtree nodes (per frame) : ...
                     Dirty node share : ...
                  Dirty subtree share : ...
```

min/max/p99 只取非零帧（与探针口径一致），换弹这种短时动画直接看 `max`。

**3. 文档**

- [TODO](docs/architecture/client/render/TODO.md) 阶段 2 下新增待办：`MAG_ADDITIONAL` 改为单节点退回（该节点即时、其余烘焙）
- [b1a-static-bake.md](docs/architecture/client/render/refactor/b1a-static-bake.md) 的「退回即时管线」补了一句 B1-b 的影响
- [chain/Home.md](docs/architecture/client/render/chain/Home.md) 的计数器登记、[refactor/Home.md](docs/architecture/client/render/refactor/Home.md) 的测量表同步更新

换弹时序保持 0/3/6 秒，9–10 秒的空闲段用作静态对照；如果测出来那段不够（比如最后一次换弹拖到 9 秒后），再按你说的改成 1/4/7。

## 需要注意

探针变了，**b2-a-7 必须在新构建上重采**，之前的 `b1-a-6` 数据也不能和新的脏子树口径混用。采集时留意两点：

- `Dirty subtree share` 在空闲段应接近 0（根不计），换弹段会跳起来；如果空闲段也明显非零，说明根之外还有常量动画在改节点，需要单独看。
- `State changes` 在 multi 混合场景会低估（共享实例 + 按帧去重），换弹测试是第一人称单场景，不受影响。

编译已过，改动未提交。

# 

```
b2-a-7改成b1-a-7了
b1-a-6重新测了4次，b1-a-7也测好了
开始分析并写报告
```

# 

发现一个 bug：`Dirty subtree nodes` 在 b1-a-7 里恒为 0——根节点（每帧都因 bob 变脏）也被标了「已计入」，导致所有后代都提前返回。我先修，再看数据。

修复完成了。现在统计 b1-a-6 / b1-a-7 的数据。

两份报告都写完了，另外发现并修掉了一个探针 bug。

## b1-a-6：场景基线实测

[测试数据计算报告](docs/architecture/client/render/benchmark/26.35.11.1-4/b1-a-6/测试数据计算报告.md)（用 5–8 的 n=4，1–4 作为探针构建对照）

- 9 个空展示框：AF **0.864 ± 0.032 ms**，F12 0.899 ms，`Draw calls` 9，上传 30.8 KB
- 加探针前后完全一致（0.863 vs 0.864）→ 新探针在未采集时没有可测开销
- **结论**：b1-a-5 的 0.947 ms「未归因项」− 0.864 = 0.083 ms，它是场景基线，不是 CGC 的 GPU 成本。multi 帧时间 = 基线 + CGC

## b1-a-7：换弹

[测试数据计算报告](docs/architecture/client/render/benchmark/26.35.11.1-4/b1-a-7/测试数据计算报告.md)

| 项 | ak47 | m4a1 裸配 | m4a1 满配 |
|---|---:|---:|---:|
| CGC 增量 | +0.082 ms | +0.071 ms | +0.082 ms |
| 遍历增量 | +0.035 | +0.030 | +0.025 |
| 多出 cube | +23 | +25 | +25 |
| 动画通道 | 12 → 27 | 14 → 32 | 14 → 32 |
| `Dirty nodes` 峰值 | 14 | 12 | 9 |

- **换弹不是收益大头**：CGC 只涨 4%–16%，帧时间增量约 0.1–0.15 ms（F12 帧时间 1.595/1.844/3.086，比静止高 0.13–0.19，其中含探针开销）
- 脏节点静止恒为 1（根 bob），换弹峰值 9–14；均值只有 2.5–3.3，因为动画并非每帧都产生新姿态——**判断脏集必须看 max**

## 发现的 bug（已修）

三段换弹的 `Dirty subtree nodes` 全是 0。原因是根节点每帧都因 bob 变脏，却被标记成「本帧已计入」，所有后代在祖先检查处直接返回，永远不计子树。

修复：只有**真正累加过子树**的节点才打「已计入」标记；根节点仍不计入自身（B1-b 的绘制矩阵承担根变换），但不再阻断后代。已编译通过。

**所以 b1-a-7 的脏子树数据作废**，需要在修复后的构建上补跑三段 F12 换弹（每段 10 秒，n≥3）；AF 段不受影响，不用重跑。

## 文档

- [TODO](docs/architecture/client/render/TODO.md)：空展示框基线、静止/换弹脏节点标记为已测得；新增「换弹脏子树重采」「开火/检视脏节点」两条待办
- [refactor/Home.md](docs/architecture/client/render/refactor/Home.md)：基线改为实测 0.864，补换弹结论
- [chain/Home.md](docs/architecture/client/render/chain/Home.md)：数据登记补两条

补采时注意：换弹段的 `Dirty subtree share` 在 9–10 秒空闲段应回落到 0，如果整段都不为 0，说明根节点之外还有常量动画在改节点，需要单独排查。

# 

```
已经补采好，更新报告
```

# 

报告已按修复后的 5–8（n=4）重写：[b1-a-7/测试数据计算报告.md](docs/architecture/client/render/benchmark/26.35.11.1-4/b1-a-7/测试数据计算报告.md)。修复在实机数据上验证有效——脏子树不再是 0。

## 关键数据

**换弹成本（相对同枪静止）**

| 场景 | CGC 增量 | 帧时间增量 | 遍历增量 |
|---|---:|---:|---:|
| ak47 | +0.098 ms（+19%） | ≈+0.24 ms | +0.043 |
| m4a1 裸配 | +0.060 ms（+7.5%） | ≈+0.13 ms | +0.029 |
| m4a1 满配 | +0.078 ms（+4%） | ≈+0.09–0.12 ms | +0.026 |

**脏节点 vs 脏子树**

| 场景 | 脏节点峰值 | 脏子树峰值 | p99 覆盖 |
|---|---:|---:|---:|
| ak47 | 14 | **198** | **53%** |
| m4a1 裸配 | 12 | **154** | 22% |
| m4a1 满配 | 9 | **156** | 8.8% |

三个直接结论：

1. **`Dirty nodes` 低估了一个数量级**。动画只驱动十几根骨骼，但它们各自的子树有 150–200 个节点要重烘焙。B1-b 的验收必须看 `Dirty subtree share` 的 p99，不能看脏节点数。
2. **ak47 是换弹最坏场景**：遍历节点少、动画子树大小差不多，p99 时有一半遍历无法跳过（残留约 0.18 ms）。满配反而只有 8.8%（残留约 0.07 ms）。
3. **换弹本身不是大头**：CGC 只涨 4%–19%，帧时间 0.1–0.15 ms；B1-b 不必为换弹放宽目标，但要处理 `MAG_ADDITIONAL` 的整模型退回，否则换弹帧会把脏子树的收益整帧丢掉。

裸配与满配的脏子树数值几乎相同（47.3 vs 47.9）——换弹驱动的弹匣/枪机骨骼是同一组，满配多出的配件是独立 `ModelObject`，根变换不计入本模型的脏子树，符合预期。

`analyze.py` 已改为只读 5–8；TODO 的「换弹脏子树」已标记完成，仍缺开火 / 检视的动态脏数据。改动未提交。

# 

```
- 开火一般都只有栓和枪整体移动；检视就是个慢放的装弹（把弹匣拆下来看+装回去+看枪背面），动画显然比装弹少，不作为基准
- 现在测得的脏子树是什么情况？差一个数量级是否是重构完的收益是否没了？
- 按照重构能带来的优化来避免重烘焙或者合适的优化方式的话，对美术做模型/动画时是否可以按特定准则来避免性能浪费？（例如把不可见的面剔除的插件算一种制作时优化）
报告里可以更新相应内容
```

# 

理解了。开火/检视不作为基准这点合理——它们的脏集不会超过换弹。我把「收益是否被吃掉」和「美术准则」两节写进报告。

三点都已写入文档。逐条回答：

## 1. 开火 / 检视不单测

同意，已记入报告的测量范围：开火只驱动栓和整枪（整枪走根矩阵，不进脏子树），检视是换弹的慢放且驱动通道更少，**脏集都以换弹为上界**，所以换弹就是动态场景的基准。TODO 里那条「开火/检视待测」已改成这个结论。

## 2. 脏子树的现状：收益没消失，但口径要分清

现在的数据是**按节点**算的：

| 场景 | 遍历节点 | 脏子树峰值 | p99 覆盖 | p99 可跳过 |
|---|---:|---:|---:|---:|
| ak47 换弹 | 352 | 198 | 53% | 47% |
| m4a1 裸配 | 613 | 154 | 22% | 78% |
| m4a1 满配 | 1578 | 156 | **8.8%** | **91%** |

「脏节点比脏子树差一个数量级」说明的是**旧指标（脏节点）严重低估成本**——动画只驱动十几根骨骼，但它们各自的子树有 150–200 个节点要重写。这恰恰证明必须用脏子树口径，而不是收益消失。

收益判断：

- B1-b 的主目标是满配第一人称，换弹峰值下仍有 **91% 的遍历可跳过**，残留约 0.07 ms（总 CGC 1.994 的 3.5%），目标 0.45–0.60 ms 不用放宽。
- 换弹只占少数帧：20%–33% 的帧完全干净，整段均值覆盖只有 3%–18%。用 p99 评估即可，不必按峰值设目标。
- ak47 相对最坏（p99 只剩 47% 可跳过，残留约 0.18 ms），但它总遍历只有 0.338 ms，也不是 B1-b 的目标场景。

**唯一没定的变量是 cube 密度**：脏子树按节点计，重烘焙按顶点计。弹匣、枪机这类实体部件的 cube 密度通常高于平均（三把枪整体约 1.1–1.27 cube/节点），所以实际残留可能比节点占比高一些。要下定论需要一个 cube 加权的脏计数，已登记为待办探针。

另外注意：ak47 与 m4a1 的脏子树**绝对量差不多**（154–198 个节点），占比差异主要来自模型总规模（352 vs 1578 个节点），不是 ak47 的层级一定更差——但两者都在制作期可控。

## 3. 美术准则：可以，而且比 LOD 更有价值

完整清单写进了[方案 B 的「美术制作约束」](docs/architecture/client/render/refactor/scheme-b-baked-mesh.md#美术制作约束)，报告里给了摘要和链接。核心是成本模型 `Σ(脏子树顶点) × C_vertex + draw` 的直接推论：

1. **动画骨骼放层级低处、子树要小**——静态装饰（贴片、导轨、瞄具座、枪托外壳）不要挂在动画骨骼下面，重烘焙按整棵子树走。
2. **动画骨骼上少放 cube**——不可见面剔除这类制作期优化会直接乘在**每一次**重烘焙上，收益比 LOD 高，而且与本次重构完全兼容。
3. **整枪呼吸/晃动放根节点**——根变换由绘制矩阵承担，免费；同样的动画放在根的子骨骼上，整个模型每帧变脏。这是最容易被忽视的一条。
4. **高频动画只驱动叶子骨骼**，可见性切换只用于小子树，替换渲染只用于小节点，材质/纹理尽量合并（B2 的下限是 `RenderType` 数）。

这些准则可以写成美术侧的检查项：统计每根动画骨骼的子树 cube 数，设一个上限（例如不超过全枪的 15%），就等价于给重烘焙成本设了上限。
