# B1-a 静态烘焙：实施记录

> 对应 [TODO](../TODO.md) 阶段 1。方案取舍见 [B 几何烘焙](./scheme-b-baked-mesh.md)，成本事实见 [渲染链路](../chain/Home.md)。
> 本文只记录**本次落地的实现事实**：改了哪些路径、缓存键与失效条件、哪些情况会退回即时管线，以及验收清单。
> 状态：已实现，默认开启（`StaticBake.ENABLED = true`），**待人工验收**（开 / 关光影各一次）。

## 覆盖范围

|场景|枪体|非瞄具配件|瞄具（镜身 / 目镜环 / 瞄具模型本体）|备注|
|---|---|---|---|---|
|物品展示框（FIXED）|烘焙|烘焙|烘焙|主目标|
|地面物品（GROUND）|烘焙|烘焙|烘焙|实例矩阵来自实体姿态，不进缓存|
|第三人称|烘焙|烘焙|烘焙|同上|
|GUI / Tooltip|—|—|—|仍走槽位贴图，不经过模型渲染|
|第一人称|即时（B1-b）|即时|即时|不在本阶段范围|

瞄具那组 pass 由 `_AttachmentModelRender#renderModelPart` 单独烘焙（按节点路径做子网格），
枪体与配件模型由 `ModelObject#render` 烘焙；两者共用同一套缓存与绘制逻辑。

## 着色器：实例矩阵必须参与着色

烘焙网格是**模型空间**顶点，实例矩阵通过 `drawWithShader` 的 `ModelViewMat` 传入。原版实体着色器不能直接吃这个矩阵
（1.20.1 / 1.20.2 / 1.20.4 / 1.21.1 的 `rendertype_entity_cutout.vsh` 相同）：

```glsl
vertexDistance = fog_distance(ModelViewMat, IViewRotMat * Position, FogShape);
vertexColor    = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color);
```

- 光照用**原始 `Normal`**：原版在 CPU 端用 `PoseStack.Pose#normal()` 把法线算到视图空间再写顶点，烘焙网格存的是模型空间法线；
- 雾用 `IViewRotMat * Position`：原版 `Position` 已是视图空间，烘焙网格是模型空间。

因此「持久 VBO + 每实例矩阵」必须配一个**会消费 `ModelViewMat` 的着色器**。落地成两条路径：

|条件|使用的着色器|说明|
|---|---|---|
|无光影包|`customgun:cgc_baked_entity`|CGC 自己的 core shader，见 `core/src/main/resources/assets/customgun/shaders/core/`：位置 `ProjMat * ModelViewMat * Position`，法线 `inverse-transpose(mat3(ModelViewMat))`（对任意仿射姿态都与原版 `PoseStack.Pose#normal()` 等价，含非等比缩放），雾先算 `IViewRotMat * ModelViewMat * Position` 再交给 `fog_distance`；片元逐行照抄 `entity_cutout`|
|开光影包|光影包自己的实体 program|Oculus/Iris 在 `GameRenderer.getRendertype*Shader()` 注入，把该 `RenderType` 的 shader 换成 `ENTITIES_CUTOUT_DIFFUSE`（按 shadow / hand / block-entity / entities 阶段分支）；`ExtendedShader.apply()` 用 `ModelViewMat` 算 `NormalMat = inverse-transpose(ModelViewMat)`，位置、法线、雾全部由实例矩阵驱动。调用方只需 `setupRenderState()` 后用 `RenderSystem.getShader()`|

**平台接线**（`core` 不允许直接 import Forge 事件，沿用 `ClientRenderRegistry` 的注册点约定）：

- core：`ClientRenderRegistry#onRegisterShaders(ICoreShaderRegistrar)` 声明要注册哪些 shader；
  `ClientRenderRegistry.BakedEntityRenderState` 保存最新实例；
- forge-compat：`ForgeClientRenderRegistry` 监听 `RegisterShadersEvent`，把事件转成 `IShaderRegistrar`
  （每次资源重载都会重新注册 `cgc_baked_entity`）；
- `StaticBake#resolveEntityShader()` 每次绘制时查询（资源重载会换 `ShaderInstance` 实例）。

**光影侧不需要改 cgccshader**：1.20.1 的 Oculus 按 `GameRenderer` getter 换 shader，用现有的
`IrisCompat.isUsingRenderPack()` 就够；1.20.1 的 RenderType 没有 26.x 的 `RenderPipeline` 映射表，不需要 `assignPipeline`。
前置条件仍是装了 `cgccshader`（`IrisCompat` 的 mixin 由它提供）；没装时 `isUsingRenderPack()` 恒为 false，
烘焙路径仍能正确退回即时管线，只是光影下拿不到优化（见「退回即时管线」第 5 条）。

### 光影包会扩展顶点格式

开光影包时 Iris 的 `MixinBufferBuilder` 把 `NEW_ENTITY` 换成 `IrisVertexFormats.ENTITY`（多出 entityId / midTexCoord / tangent）。
烘焙网格的 VBO 布局因此随光影状态变化：

- 缓存键带 `shaderPack` 布尔；
- `BakedMesh` 记录烘焙时**实际**写出的格式是否被扩展，绘制前和当前光影状态比对；
  不一致（刚切光影包）本帧退回即时管线，下一帧重新烘焙。

## 渲染流程

每帧对每个静态实例：

1. **状态段**（`BedrockPart#renderStatus`）：递归执行 functional 回调、确定 `visible`、登记委托，
   同时累加节点状态指纹。子树内没有 functional 节点时只做指纹累加，不维护 `PoseStack`。
2. **缓存查找**：`StaticBakeCache`，键见下。
3. **几何段**（未命中时）：`BedrockPart#renderGeometry` 用单位 `PoseStack` 把节点姿态烘进顶点，
   上传到 `VertexBuffer(Usage.STATIC)`。几何段**不执行** functional 回调——回调已在状态段执行过，
   因此委托拿到的仍是真实姿态矩阵，也不会重复登记。
   `BufferBuilder` 按烘焙嵌套深度**复用**：1.20.1 的构造会申请 `容量 × 6` 的原生内存且没有释放路径，每次重建新建会在 F12 强制重建下几秒耗尽页面文件。
4. **绘制**：`RenderType.setupRenderState()` → 选着色器（见上）→ `VertexBuffer.bind()` →
   `drawWithShader(RenderSystem.getModelViewMatrix() * 姿态矩阵, 投影矩阵, shader)` →
   `VertexBuffer.unbind()` → `clearRenderState()`。**不上传、不 flush、不经过 `BufferSource`。**
5. **委托**：与即时管线相同，在网格之后执行（配件 / 文字 / 火焰 / 抛壳按各自条件早退）。

## 缓存键与失效条件

`StaticBakeCache.Key`：

|字段|含义|失效表现|
|---|---|---|
|`owner`|`ModelObject` 实例（身份比较）|换模型 / 换 LOD 变体即换键|
|`subKey`|同一模型内的子网格（例如瞄具的节点路径）|不同部件互不覆盖|
|`renderType`|纹理 + 渲染状态（1.20.1 按纹理 memoize）|换纹理即换键|
|`light` / `overlay`|光照已烘进顶点|展示框亮度变化时重烘|
|`fingerprint`|状态段采集的节点状态指纹|见下|
|`shaderPack`|是否开着光影包（顶点格式随之扩展）|切换光影包时整体换键|
|`red/green/blue/alpha`|顶点颜色|颜色变化时重烘|

指纹覆盖**子树内每个可见节点**的全部变换字段：`x/y/z`、`xRot/yRot/zRot`、
`offsetX/Y/Z`、`additionalQuaternion`、`xScale/yScale/zScale`、`visible`。
节点姿态由动画（offset / 四元数 / 缩放）或 addon 直接改写时都会命中失效。

其它失效入口：

- **资源重载**：`_AssetsInstanceManager#clear` 调用 `StaticBakeCache.clearAll()`，
  在渲染线程释放全部 `VertexBuffer`（旧模型对象会被整体替换，不释放会泄漏显存）。
- **F12 采集期间强制重建**：`ClientRenderProfiler.enabled` 时跳过查找、每帧重烘，
  用于人工验收「失效是否正确」并测量烘焙成本。
- **LRU 上限**：128 个网格 / 32 MiB，超出时淘汰最久未用的网格并在渲染线程释放；
  淘汰至少保留一个，避免单个超大网格刚插入就被关闭（那会让模型直接不画）。
  条目上限按「一把枪 = 枪体 + 若干配件 + 瞄具子网格、配置不同会各自成键」估算，内存上限由字节数兜底。

> 风险：光照已烘进顶点，**每个不同的 `light` 值都要一份完整网格**。展示框分散在不同亮度位置时，
> 同款枪会分裂成多份，容易撞上 32 项上限而反复重烘（比原遍历还慢）。后续需要按
> 「同模型不同光照」做二级缓存（同一份顶点数据、按光照重新上传）或提高上限。

## 退回即时管线的情况

以下情况不做烘焙，走原路径（`fail loud` 而不是画出错误结果）：

1. **第一人称**：`StaticBake.eligible` 直接排除（B1-b 处理）。
2. **functional 回调直接发射几何**（而不是登记委托）：状态段用 `DiscardVertexConsumer`
   计数顶点，发现非零即退回即时管线，并丢弃状态段登记的委托以免重复渲染。
   目前内置节点里只有换弹时的附加弹匣（`MAG_ADDITIONAL`，静态场景恒为隐藏）属于这一类，
   该项是为第三方渲染 addon 准备的安全网。
3. **没有可用着色器**：`cgc_baked_entity` 注册失败（资源缺失 / 编译失败，注册处会记录错误日志）
   且没有光影包时，本帧退回即时管线；网格被缓存淘汰后再被绘制也会退回。
4. **顶点格式与光影状态不一致**：刚切换光影包时（VBO 是另一种格式），本帧退回即时管线，下一帧重烘。
   没装 `cgccshader` 的 Oculus 用户会一直命中这一条：画面正确，但没有烘焙收益。

> 第 2、3 条的退回发生在状态段之后：functional 回调会在同一帧被执行两次（状态段一次、即时遍历一次）。
> 内置回调都是幂等的（只写 `visible` / 登记委托），第三方 addon 若在回调里做非幂等的副作用需要注意。

## 顺带完成的 A 项

- **去掉 1.20.1 的双重节点快照**：`ModelObject#render` 里两组 `NodeTransform.capture` +
  两次 `apply` 在即时模式下是纯开销（multi 实测 0.135 ms/帧），已移除；26.2 需要的
  保存 / 恢复以 `[26.2, )` 注释保留。
- **同纹理 flush**：静态路径不再调用 `endBatch`，每个网格就是一次 draw，纹理相同也无需分桶。

## 未完成 / 明确搁置

|项|原因|
|---|---|
|模板每帧一清|静态展示里的瞄具窗口只开启模板测试、不写模板值（模板由第一人称路径写），多把枪共用一张模板缓冲时，前一把枪留下的值会参与后一把枪的测试。改成每帧一清会改变这一语义，收益只有 0.059 ms/帧，留给 B2 重排 pass 时一起处理|
|CGC 绘制区间的 `GL_TIME_ELAPSED`|会与 `GpuFrameTimer` 的整帧查询嵌套（同一时刻只允许一个 `TIME_ELAPSED` 查询），需要重做计时器分配，本阶段未做|
|按节点的脏计数|B1-b 前才有意义，未做|
|GPU 查询与分配采样改每 N 帧|未做，属探针自身开销优化|
|同模型不同光照的二级缓存|见上「风险」，先用 LRU 上限兜住|

## 26.3 / 跨版本

26.3 的实体着色器结构不同，需要单独验证，不能照抄 1.20.1 的实现：

- 26.3 的 `entity.vsh` 位置已消费 `ModelViewMat`，实体管线带 `NO_CARDINAL_LIGHTING`（顶点法线不参与光照），
  但 `fog_spherical_distance(Position)` 用的是**原始顶点坐标**——标准实体管线把模型矩阵烘进了顶点，
  烘焙网格是模型空间，雾需要标量字段或自定义着色器确认。
- 26.3 的烘焙绘制要走自定义 `RenderPipeline` + `GpuDevice.createBuffer` + `PreparedRenderType.drawFromBuffer`
  （或 `RenderPass.drawIndexed`），并且：
  - 在 `ClientRenderRegistry.onRegisterRenderPipelines` 里注册管线；
  - 在 `IrisCompat.registerRenderPipelines` 注入点由 `cgccshader`（`CGC-Compat-Shader-iris-26.3neoforge`）
    调 `IrisApi.assignPipeline(pipeline, IrisProgram.ENTITIES)`，否则 Iris 查不到 program，几何不画或极淡；
  - 若走自定义管线，需同时处理 OIT（26.3 开「改进透明」时 `oitPipelineSet` 为空会直接抛异常）。
- 具体以 [PLAN](../PLAN.md) 的「阶段 4」为准；本阶段只在 1.20.1 落地。

## 新增探针

|名称|类型|含义|
|---|---|---|
|`Static bake`|计时|一次烘焙的耗时|
|`Bake rebuilds`|计数|每帧烘焙次数；静态场景稳态应为 0|
|`Distinct RenderTypes`|计数|本帧出现过的 `RenderType` 实例数（只统计 CGC 经过的位置），B2 的 draw 下限|

`Node snapshot` 在 1.20.1 上不再有命中（报告里显示 `-`），属预期。

## 验收清单（人工）

> 2026-11-11/12 验收（1.20.1）：第 1、2、4、5、6、7 项通过；第 3 项（雾）通过；第 8 项（内置探针）未做。数据见 [阶段验收报告](../benchmark/26.35.11.1-4/测试数据计算报告.md)。

单把枪、9 个展示框各跑一次，**开 / 关光影各一次**：

1. **画面**：枪体、瞄具镜身与目镜环、配件（枪口 / 激光 / 握把 / 弹匣 / 枪托）位置正确。
2. **光照**：把展示框放到亮度不同的位置，枪体亮度跟随；旋转展示框 / 掉落物，光照随朝向正确变化
   （这是烘焙着色器最容易出错的一项）。
3. **雾**：远处的展示框 / 掉落物有雾，近处没有；与关掉烘焙时对比一致。
4. **失效**：改装界面换配件、换弹匣类别（扩容 1/2/3）、装 / 卸瞄具后展示框内容立即更新。
5. **掉落物**：地面枪随实体旋转，姿态正确。
6. **资源重载**：F3+T 后不再复现旧模型，显存不增长（LRU + `clearAll`）。
7. **光影**：Oculus 开光影包后画面与关闭时一致（含阴影、发光、半透明）；切换光影包时不应出现
   一帧的旧格式画面（格式不一致会退回即时管线）。
8. **探针**：F12 后 `Bake rebuilds` 稳态为 0；`endBatch calls` / `Upload bytes` 在静态场景显著下降；
   `Cubes submitted` 只出现在首帧（F12 强制重建时每帧都有）。

## 已知风险

- **没装 `cgccshader` 的 Oculus 用户**：`isUsingRenderPack()` 拿不到真值，烘焙在光影下退回即时管线（画面正确、无收益）。
- **OptiFine 光影**：不在本次目标内。`IrisCompat.isUsingRenderPack` 只反映 Oculus/Iris，OptiFine 光影下会走无光影分支的自定义着色器，画面与性能均未验证。
- **缓存漏判**：指纹覆盖节点全字段，但**不覆盖**由 functional 回调写入、又不体现在 `visible` 上的语义
  （目前内置回调没有这种状态）。第三方 addon 若这样做，需要自己触发重建或退回即时管线。
- **半透明**：静态场景的枪体固定使用 `entityCutout`，不涉及排序；透明枪只在第一人称路径出现。
