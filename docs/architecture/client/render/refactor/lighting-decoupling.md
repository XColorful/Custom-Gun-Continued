# 光照解耦（uv2 独立于几何）

> 对应 [TODO](../TODO.md) 的「B1-c 光照解耦」。上游约定见 [PLAN](../PLAN.md)，成本事实见 [渲染链路](../chain/Home.md) 与 [`b1-b-3`](../benchmark/26.35.11.1-5/b1-b-3/测试数据计算报告.md)。
> 本文回答三件事：为什么现在要做、两条实现路线各自付什么代价、光影下能不能成立。
> 实现不参考 SBM；方案只从 CGC 自己的顶点格式、着色器与 B2 批次布局推出。

## 问题：不是平均帧时间，是帧节奏

`b1-b-3`（夜晚火把、逐格移动）实测：

|场景|重建次数/秒|单次重建|命中帧占比|1% low|
|---|---:|---:|---:|---:|
|ak47|2.6|0.603 ms|0.30%|337.9|
|ak47 + 光影|2.8|2.700 ms|1.80%|136.7|
|满配|2.8|1.372 ms|0.40%|331.6|
|满配 + 光影|2.9|**9.246 ms**|2.00%|**59.1**（静止 115.7）|

- 平均只占 0.4%–2.6%，**所以平均帧时间看不出问题**；但最坏的满配+光影档每秒约 2.9 次、单次 9 ms，在 145 FPS 下等于每约 350 ms 卡一下，观感是持续卡顿。
- 这个次数是**下界**：它是「每跨一格恰好变一级」的简单场景。室内只要经过灯、窗、楼梯、屋檐，亮度采样点（玩家眼睛位置的 `packedLight`）跨界的频率会更高，平均占比随之上升。
- 室内「视觉很亮」不等于光照值不变：`packedLight` 是方块光与天空光的离散采样，靠近窗户或灯就会变，哪怕画面看起来亮度差不多。
- **B2 会放大它**：跨实例合并成一次 draw 后，一个实例的光照变化会触发整个批次重建（9 把满配约 11 MB），比现在按实例重建更贵。光照解耦因此必须在 B2 之前定下布局，至少不能让 B2 把重建单位放大。

成功判据不是「平均少几微秒」，而是：**光照变化的那一帧回到普通帧的量级，1% low / 0.1% low 不再由它决定**。

## 现状：光照烘进交错顶点

- `light` 是 `StaticBakeCache.Key` / `Cgc$MeshKey` 的一部分；每个 `(RenderType, light, overlay, …)` 一份完整网格，每块最多 4 份。
- 变化一次 = 重新执行顶点发射（全部属性）→ 新建 `VertexBuffer`（VBO + IBO + VAO）→ 全量上传 → 绘制。`Bake rebuilds` 数的就是这个。
- 顶点格式：无光影 `NEW_ENTITY` 36 B/顶点（uv2 在 offset 24）；Oculus/Iris 扩展成 `IrisVertexFormats.ENTITY` 52 B/顶点（uv2 在 offset 32）。两种格式里 uv2 都是 **2×i16、语义上是整型 attribute**。
- 实测字节规模：uv2 全量 **43 KB（ak47）/ 140 KB（满配）**；`b1-b-3` 实测一次光照变化重传的交错字节为 385.8 / 554.9 KB（ak47、有无光影）与 904.1 / 1733.8 KB（满配、有无光影）。

### 9 ms 花在哪：已实测（`b1-b-3` 第 5–8 次）

`Static bake` 已拆成 `Bake emit` / `Bake buffer create` / `Bake upload`，数据见 [`b1-b-3`](../benchmark/26.35.11.1-5/b1-b-3/测试数据计算报告.md) 第 4 节：

|场景|`Static bake`|emit|buffer create|upload|emit 占比|
|---|---:|---:|---:|---:|---:|
|ak47|0.611 ms|0.474|0.013|0.117|77.6%|
|ak47+光影|2.665|2.502|0.014|0.146|93.9%|
|满配|1.361|1.111|0.023|0.223|81.6%|
|满配+光影|9.395|8.844|0.049|0.494|94.1%|

- **尖峰几乎全部是顶点发射**，上传只占 5%–19%，新建缓冲只占 0.5%–2.2%。
- 光影下 emit 变贵的原因是 **`BufferBuilder` 失去快速路径**：`switchFormat` 只对 `DefaultVertexFormat.NEW_ENTITY` 精确匹配时启用 `fastFormat`，Oculus 在 `begin` 前把格式换成 `IrisVertexFormats.ENTITY`（52 B，多出 entityId / midTexCoord / tangent），每个顶点改走通用逐元素路径并多写三个属性。**不是上传停顿。**
- 这条实测否定了本文早先「9 ms 是驱动停顿」的推测，也让两条路线的取舍变了：**A 和 B 都消除 emit，都能消掉尖峰**；差别只剩上传（满配 1.73 MB / 0.494 ms vs 4 B/顶点约 133 KB / ≈0.04 ms）。

## 方案 A：独立 uv2 流

**布局**

- 几何 VBO 保持现有交错格式（36 / 52 B）；其中的 uv2 槽位变成死数据（仍占 4 B/顶点，不读）。
- 另建一条 4 B/顶点（2×i16）的光照 VBO，单独上传。
- 烘焙时记录「恒亮节点」（`illuminated`，恒 15/15）的顶点区间；其余顶点共用本次渲染的 `light`。

**光照变化时**

1. 用新的 `light` 填满光照 VBO（`illuminated` 区间再写回 15/15）；
2. 只上传这 43–140 KB，几何 VBO 与 `VertexBuffer` 对象都不动。

**绘制时**

- `VertexBuffer.bind()` 绑定 VAO 后，把 attribute 4（UV2）指向光照 VBO：
  `glBindBuffer(ARRAY_BUFFER, lightVbo)` + `_vertexAttribIPointer(4, 2, GL_SHORT, 0, 0)`；其余 attribute 仍指向几何 VBO。
- 该 VAO 是每个 `BakedMesh` 私有的，覆盖只作用于自己；重传同一格式不会重置 attribute，换格式（切光影包）后重做一次覆盖即可。

**成本（CPU 微基准，JDK 21，本机）**

|操作|ak47（11,008 顶点）|满配（35,840 顶点）|
|---|---:|---:|
|A：填充 4 B/顶点光照数组|4.8 µs|17.4 µs|
|A：分配 + 填充|—|10.2 µs（复用缓冲时按上行）|
|参考：B：36 B 步长原地 patch|—|20.9 µs|
|参考：B：52 B 步长原地 patch|—|42.6 µs|
|参考：B：保留 52 B CPU 副本（arraycopy）|—|63.1 µs|

CPU 侧两方案都是几十微秒，**真正的差别在 GL 上传**：A 传 4 B/顶点，B 传整个交错缓冲。
（微基准跑在开发机 JDK 21 上，只用来比较两条路线的 CPU 量级，不作为游戏内绝对值。）

**收益**

- 消掉「顶点发射 + 新建 VertexBuffer」；一次光照变化的成本 ≈ 18 µs + 一次 43–140 KB 上传。
- 缓存键去掉 `light`：每块只剩一份几何网格，不再有 4 份光照变体；显存下降（4×36/52 B/顶点 → 36/52 + 4×4 B/顶点）。
- 若用 2–3 个小缓冲轮转 + orphan（`glBufferData(size, null)` 后 `glBufferSubData`），可以避免小上传触发同步停顿。

**内存**

- 几何 VBO 不变，额外 4 B/顶点 × 同时缓存的光照值数（主 pass + 阴影 pass 通常 2 份，最多 4 份）。满配约 140 KB/份。

## 方案 B：交错缓冲内原地重写 uv2

**布局**

- 不再按光照换键，保留一份交错几何网格；
- 烘焙后保留一份 CPU 侧交错字节副本（来自 `RenderedBuffer.vertexBuffer()`，满配实测约 0.90 / 1.73 MB，无 / 有光影）；
- 光照变化时按步长改写每个顶点的 uv2 字段，再把整段交错缓冲重传（同一个 VBO，不新建）。

**变体**

- B1：CPU 副本 + `glBufferData` 全量重传（实现最简单，成本 = 上面的 patch + 全量上传）。
- B2：持久映射（`GL_MAP_UNSYNCHRONIZED_BIT`）后只写 uv2 字段，由驱动回写脏页；省 CPU 副本，但依赖驱动，且仍然会触碰整段映射范围。

**成本**

- CPU：满配 patch 20.9 µs（36 B）/ 42.6 µs（52 B），保留副本一次性 63 µs。
- GPU：每次光照变化重传约 0.90 MB（无光影）/ 1.73 MB（光影，`b1-b-3` 实测单次光照变化字节）。**上传本身在光影下只要 0.494 ms**（满配），相对 9.4 ms 的 emit 可以忽略；B 的真实收益就是把 emit 整段去掉。
- 阴影 pass 若与主 pass 使用不同光照：A 只需换一份 4 B/顶点的小数组；B 需要两份 CPU 副本，或每 pass 重传全量。若选择「每 pass 重传」，满配+光影每次重传约 0.5 ms，仍远低于现状的 9 ms，但不能与 A 的 ≈0.04 ms 相比。

**优势**

- 不改顶点格式、不改 attribute 绑定、不需要知道 UV2 的 attribute 槽位；顶点字节与着色器看到的完全一致，**是跨版本最保守的形态**。

## 对比

|维度|A 独立 uv2 流|B 交错原地重写|
|---|---|---|
|光照变化 CPU（满配）|≈17 µs|≈21–43 µs（+ 一次性 63 µs 副本）|
|光照变化 GPU|4 B/顶点 = 43–140 KB|36/52 B/顶点 ≈ 0.90 / 1.73 MB|
|实测上传耗时（满配+光影）|≈0.04 ms（按 133/1734 折算）|**0.494 ms**|
|顶点发射 / 新建 VertexBuffer|消除|消除|
|光照缓存键|去掉 `light`|去掉 `light`|
|显存|几何 1 份 + 光照片段 2–4 份（小）|几何 1 份|
|CPU 内存|≈0|+0.90–1.73 MB 每满配网格|
|绘制|多一条顶点流（GPU 多取 4 B/顶点）|不变|
|1.20.1 无光影|✅ 自有 shader 的 UV2 在 slot 4|✅|
|1.20.1 Oculus/Iris|✅ 已核实（见下）|✅|
|26.3 延迟渲染|⚠️ 需自定义 pipeline 的第二 vertex binding，光影包是否接受待验证|⚠️ 需自持久化缓冲，但字节布局不变|
|实现风险|中（attribute 覆盖、缓冲轮转）|低（只改上传路径）|
|能消除光影下的 9 ms 尖峰|**能**（只剩 4 B/顶点上传）|**能**（只剩约 0.5 ms 全量上传）|

## 光影兼容性（1.20.1 已核实）

- **UV2 的 attribute 槽位是固定序号，不是着色器随便分配的**：
  - `ShaderInstance` 用构造时的 `VertexFormat` 元素顺序 `glBindAttribLocation(program, i, name)`；`VertexFormat.setupBufferState()` 也按同一序号调用 `_vertexAttribPointer`。
  - `NEW_ENTITY` 的顺序是 Position / Color / UV0 / UV1 / UV2 / Normal / Padding → **UV2 = slot 4**；`cgc_baked_entity.json` 的 attributes 与之一致。
  - Oculus 的 `IrisVertexFormats.ENTITY` 顺序是 Position / Color / UV0 / UV1 / UV2 / Normal / Padding / iris_Entity / mc_midTexCoord / at_tangent / Padding2 → **UV2 仍是 slot 4**。
  - Oculus 的 `MixinShaderInstance` 对 `ExtendedShader` 把属性名加 `iris_` 前缀，并按同一个序号绑定；shader pack 的 `vaUV2` 经 `VanillaCoreTransformer` 改名为 `iris_UV2`，因此 slot 4 就是光影 program 读的光照。
- 因此方案 A 在 1.20.1 **不需要改着色器、不需要改 shader pack、也不需要 cgccshader**：只要在 VAO 上把 slot 4 重新指向光照 VBO。
- **阴影 pass**：光影会给同一个网格换一套光照（有时还换格式）。A 的几何不变，按光照值缓存 1–4 份 4 B/顶点的小数组即可；B 需要两份全量副本，或每 pass 重传。
- **切换光影包**：顶点格式从 36 B 变 52 B，几何必须重烘（现有逻辑）；光照数组与格式无关，可保留。

### 26.3 的约束

- 26.2 起 `RenderPipeline.Builder` 有 `withVertexBinding(bindingIndex, format)`，多条顶点流在 API 上是一等公民；但 CGC 的 26.3 路径是「自持久化缓冲 + `PreparedRenderType.drawFromBuffer`」，需要确认：
  1. `PreparedRenderType` 能否为不同 binding 绑定不同的 `GpuBuffer`；
  2. Iris 26.3 的 pipeline→program 映射是否允许光照来自第二个 binding（shader pack 的 program 由 Iris 生成，未必认识自定义 binding）。
- 若答案是否，26.3 只能走 B；因此**两条路线都要保留，按平台选择**。实测已确认 B 足以消掉尖峰，**1.20.1 先落 B**，A 作为「上传/显存/缓存键」的后续优化；26.3 再按第二 binding 的验证结果决定是否值得做 A。

## 收益（哪些是实测、哪些待测）

**已实测**（`b1-b-3` 第 5–8 次 + 本文微基准）：

- 单次光照变化（命中帧合计）：0.603 / 2.700 / 1.372 / 9.246 ms（n=8）；其中 emit 占 77.6% / 93.9% / 81.6% / 94.1%。
- `Bake upload`：0.117 / 0.146 / 0.223 / 0.494 ms；`Bake buffer create`：0.013 / 0.014 / 0.023 / 0.049 ms。
- 单次光照变化字节：385.8 KB / 554.9 KB / 904.1 KB / 1733.8 KB。
- 光照数组 CPU 成本：A 17 µs、B 21–43 µs（满配），相对重建成本可忽略。
- **只有满配+光影的 1% low 明显下降**（115.7 → 59.1）；ak47+光影 136.7 vs 静止 138.2，肉眼几乎不可见——因为 2.7 ms 能塞进 6.35 ms 的帧，9.2 ms 塞不进 6.9 ms 的帧。

**推算**（原型验收前不写进结论表）：

- B：每次光照变化 ≈ 0.49 ms 上传 + 43 µs patch ≈ **0.55 ms**（满配+光影），无光影 ≈ 0.25 ms；尖峰帧从 9.25 ms 降到普通帧 + 0.5 ms 量级。
- A：每次 ≈ 0.04 ms 上传 + 17 µs 填充 ≈ **0.06 ms**（满配+光影），比 B 再省约 0.5 ms/次（约 0.13% 的平均帧时间）。

**拆分探针**已实现并随现有 `b1-b-3` F12 流程输出（`Bake emit` / `Bake buffer create` / `Bake upload` / `Bake upload bytes`），第 4 节即这组数据；原型阶段只需再补 `Light update` 与 `Bake rebuilds = 0` 的验证。

## 与 B2 的关系

- **正交，但 B2 必须按解耦后的布局建批次**：B2 建批次时记录每个实例的顶点区间，光照按「实例 × 4 B/顶点」存放；否则合并后一次光照变化会重建整批。
- B2 之后，A 的更新粒度是「实例的光照段」（满配约 140 KB/枪），B 是「实例的完整顶点段」（满配约 0.9–1.7 MB/枪）；A 仍更小，但 B 至少不会退回「整批 11 MB」。
- B1-c 先覆盖两条既有路径：`StaticBake.drawOrBake`（整模静态）与 `ModelObject` 的分块路径；B2 直接复用同一套 `BakedMesh` 光照方案。

## 实施顺序与验收

1. ~~拆分探针~~（已完成，见 [`b1-b-3`](../benchmark/26.35.11.1-5/b1-b-3/测试数据计算报告.md) 第 4 节）。
2. **B1-c-B（先做）**：`BakedMesh` 保留 CPU 侧交错字节副本；光照变化时 patch uv2 并重传原 VBO；缓存键去掉 `light`；`DebugKey` 切换 B / OFF。理由：不改顶点格式与 attribute，跨版本最保守，且实测已足以消掉尖峰。
3. **B1-c-A（可选，后续）**：在 B 的同一层接口下增加独立 uv2 流；绘制时覆盖 attribute 4。收益是每次再省约 0.5 ms、省 CPU 副本、避免阴影 pass 双份字节；26.3 的第二 binding 验证后再决定。
4. **验收场景**：
   - 复跑 `b1-b-3`（夜晚火把、逐格移动）：`Bake rebuilds` 在移动段应为 **0**，尖峰落在普通帧；
   - 新增室内场景（灯 + 窗，亮度渐变）验证「视觉亮但光照值变」；
   - 开 / 关光影；ak47 + 满配；每档 4 次独立启动；
   - 判据：光照变化帧 ≤ 普通帧 + 0.2 ms（无光影）/ + 0.5 ms（光影）；1% low 恢复到静止值附近；画面与 `OFF` 一致。

## 开放问题

- 阴影 pass 是否真的每帧换光照；若是，B 需要每网格 2 份 CPU 副本还是每 pass 重传（满配+光影约 0.5 ms/次）。
- 26.3 的 `PreparedRenderType.drawFromBuffer` 是否支持第二 binding；Iris 26.3 的 program 是否接受。
- 半透明 / 模板 pass 的绘制顺序不因解耦改变（与 B1 相同的既有风险）。
