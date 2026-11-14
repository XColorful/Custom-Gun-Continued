> #95 提示词

# 

```
读`./docs/architecture/client/render/PLAN.md`，现在完成B2
```

# 

这个分支上有一个为本次任务写的 prompt 文件，我先读它，同时查找烘焙相关代码。

我已读完 PLAN/TODO、B1-a/B1-c 实施记录和 scheme-B，以及现有 `StaticBake`/`StaticBakeCache`/`BakedMesh` 与展示框渲染链路。B2 落到代码上有两处会改变画面语义的取舍，需要你拍板后我再动手。

已定：无光影下合批；展示框不再开启模板测试。我先确认光影基线数据和帧末 flush 的挂接点。

设计已定。现在开始实现：先给 `BakedMesh` 加一个「只发射不上传」的入口，再写 `StaticBatch`。

写作完成后，我用 JOML 离线验证一下相机剥离与锚点变换的数学是否自洽（这是 B2 最容易出错的一环）。

数学验证通过（位置误差 ~3e-5 格、法线误差 ~1e-7）。现在补文档。

B2 已完成实现（1.20.1，无光影路径），全模块 `compileJava` 通过，`core`/`forge-compat` 两个 jar 均能构建。

**核心改动**

- [StaticBatch.java](core/src/main/java/dev/xcolorful/customgun/client/model/bake/StaticBatch.java)（新增）：把 `FIXED` 展示实例按 `RenderType` 合并成一次 draw。
    - 世界矩阵从姿态里剥掉相机（`W = T(camPos) * R_cam⁻¹ * pose`），顶点按批次锚点存放，镜头移动不触发重建；
    - 每个实例记录自己的顶点区间和光照，光照变化只 patch 该区间并 `glBufferSubData`，不重建整批；
    - 实例增删 / 内容变化才重排上传；镜头移出视锥保留、实体卸载或换物品才丢弃。
- [ModelObject.java:390](core/src/main/java/dev/xcolorful/customgun/client/model/ModelObject.java:390)：`renderBaked` 在 `FIXED` 下先走合批，失败再退回 `drawOrBake`。
- [BakedMesh.java](core/src/main/java/dev/xcolorful/customgun/client/model/bake/BakedMesh.java)：新增只发射不上传的 `emitGeometry`（复用同一个 `BufferBuilder` 池）。
- [LevelRendererMixin.java](core/src/main/java/dev/xcolorful/customgun/client/mixin/renderer/LevelRendererMixin.java)（新增）+ [EntityRenderDispatcherMixin.java](core/src/main/java/dev/xcolorful/customgun/client/mixin/renderer/EntityRenderDispatcherMixin.java)：提供帧边界与实例身份。
- [_GunModelRender.java:179](core/src/main/java/dev/xcolorful/customgun/client/model/_GunModelRender.java:179)：展示框/掉落物/第三人称不再开启模板测试（只保留第一人称窗口）。
- 探针新增 `Batch build` 计时与 `Batch rebuilds` 计数。

**已做的验证**：相机剥离、锚点、法线变换用 JOML 离线核对（位置误差 ~3e-5 格、法线 ~1e-7）；refmap 已正确生成 `renderLevel` 与 `checkPoseStack` 的 SRM 映射。**未做的验证**：进游戏看画面与 F12 数据（`Batch rebuilds` 移动段应为 0、`Draw calls` 应从 81 降到个位数、光影下应完全走 B1）。

**需要你决定的新平台 API**（是否要包进兼容层）：`RenderSystem.getInverseViewRotationMatrix()`、`Camera#getPosition()`、`Level#getEntity(int)`、`Entity#shouldRenderAtSqrDistance(double)`、`GlStateManager._glBindBuffer`/`GL15.glBufferSubData`/`GL20.glGetVertexAttribi`，以及 **Forge 专有的 `BufferBuilder#putBulkData(ByteBuffer)`**——后者在 26.3/NeoForge 或其他 loader 上需要另找拼接方式。

文档已同步：新增 [b2-cross-instance-batch.md](docs/architecture/client/render/refactor/b2-cross-instance-batch.md)，并更新了 PLAN、TODO、refactor/Home、b1a 与光照解耦文档。

# 

```
26.35.11.1-7/b2-1和b2-2测试跑好了，写26.35.11.1-7报告
```

# 

这批数据里有个关键异常（`Batch build` 全程 0 命中、draw 数仍是 B1 的量级）。我先把两个批次的数据完整解析出来确认。

数据已确认：两个批次里 **B2 的合批路径零命中**（`Batch build` 全程 `-`、`Batch rebuilds = 0`、9 个 ak47 展示框仍是 10 次 draw）。我先补定位探针，再写报告。

数据核对完成。现在写报告。

报告已写好：[测试数据计算报告.md](docs/architecture/client/render/benchmark/26.35.11.1-7/测试数据计算报告.md)。

**先说结论：这两批数据里 B2 合批从未真正执行**，所以它们目前只能当 B1 基线用，不能拿来评价 B2。

证据：
- `Batch build` 探针在 19 个 F12 片段里全部是 `-`，`Batch rebuilds = 0`；
- 9 个 ak47 展示框（`Distinct RenderTypes = 1`）仍然是 **10 次 draw**（9 次逐实例 + 1 次基线），合批生效后应约为 1 次；
- 9 个满配框 + 第一人称是 70 次 draw，也是逐实例量级。

同时排除了几个简单原因：不是 `StaticBake.ENABLED` 关了（烘焙路径在工作，`Cubes submitted = 0`、`Geometry traverse` 无命中），不是光影包状态（展示提交是 9 次而非 19 次），也不是光照解耦路径。剩下最可疑的是实例身份没登记（`EntityRenderDispatcherMixin` 的压栈没执行）或 `submit()` 的某个守卫，需要一次带日志的复跑才能定论。

**报告里的可用数据**（均值 ± 标准差，n=4）：

|场景|AF 帧时间|CGC（F12）|
|---|---:|---:|
|9 ak47 框·空手|1.070 ± 0.013 ms|0.185|
|9 ak47 框 + 第一人称 ak47|1.329 ± 0.027 ms|0.355|
|9 满配框·空手|1.725 ± 0.027 ms|未采 F12|
|9 满配框 + 第一人称满配|2.266 ± 0.052 ms|1.231|
|9 满配框 + 光影|8.776 ± 0.020 ms|4.411|

对照 `b1-a-5` 同场景：空手 2.146 → 1.725，第一人称满配档 4.133 → 2.266（主要来自 B1-b），`Stencil windows` 10 → 1。这些不是合批带来的。

**已补的定位手段**（已编译通过，未提交）：
- 计数器 `Batch candidates` / `Batch submits`：两者差值是"进入判定但被守卫拒绝"的实例数；
- 一次性日志 `[B2] FIXED submit: batching=… entityStackEmpty=… shaderPack=…`。

**下一批建议**：用当前源码重建，只跑 b2-1 的「空手 + 9 个 ak47 展示框」并做一次 F12（约 10 s），保留 `logs/latest.log`。看日志那一行就能确定是 `batching()` 为假还是 `submit()` 被拒。另外 b2-2 的空手档缺 F12 片段，下一批顺手补上。

# 

```
测好了，继续排查
```

# 

`Batch candidates = 9`、`Batch submits = 0` —— 判定通过了，是 `submit()` 里返回 false。继续定位是哪一个守卫。

`candidates = 9`、`submits = 0`：`batching()` 为真，失败在 `submit()` 内部。加一条一次性判定日志，把每个守卫的实际值写出来。

`candidates = 9`、`submits = 0` 说明 `batching()` 已通过、失败在 `submit()` 内部。我先把 `submit` 的每个拒绝点都变成可观测的，同时把姿态比较从哈希改成带容差的矩阵比较（哈希在相机动时会抖动，可能触发"重建 5 次后拉黑"）。

日志文件我这边拿不到（游戏在另一台机器），所以把判定结果也做成计数器，下一批 F12 直接可见。

关键修正：我之前把基线算错了。`b1-a-6`（同场景 9 个**空**展示框）的基线是 **9 次 draw**——空展示框本身每帧各一次。所以 b2-1 的 `Draw calls = 10` 是 **9 次框 + 1 次合批**，b2-2 的 70 是 **9 次框 + 6 个 RenderType + 第一人称**。B2 其实一直在生效。

基线确认：同场景 9 个空展示框就是 **9 次 draw**。重新解读后，B2 一直是生效的。现在改写报告并修正文档里的错误结论。

排查结果：**B2 其实一直在生效**。上一轮是我把 draw 基线读错了。

**错在哪**：`b1-a-6`（同场景 9 个**空**展示框）的基线是 **9 次 draw**——每个展示框自己的框体各画一次。所以：

- b2-1 空手档 `Draw calls = 10` = 9（框体）+ **1 次合批**（9 把 ak47 只有 1 个 RenderType）——不是 9 次逐实例 + 1；
- b2-1 +第一人称档 21.8 ≈ 10 + 12（第一人称 ak47）；
- b2-2 +第一人称档 70 ≈ 9 + **≤6**（9 把满配按 6 个 RenderType 合并）+ 第一人称满配。对照 `b1-a-5` 同场景：9 + 72（每把枪 8 次）。

`Batch candidates = 9/frame`（光影档为 0）也印证了 `batching()` 在跑；`Batch build` / `Batch rebuilds` 为 `-` / 0 是**稳态应有的值**（批在预热阶段就建好，采集窗口内没有重建），不是「未生效」。

**实测收益**：满配 multi 空手帧时间 2.146 → **1.725 ± 0.027 ms**（−19.6%）；按 72 → 6 次 draw、单次 7.8 µs 推算节省 ≈0.51 ms，与实测同量级。光影档整段回退 B1（`Batch candidates = 0`）。

**这轮顺手改的代码**（已编译通过）：
- 姿态比较从「量化哈希」改成**带容差的矩阵比较**（相机矩阵乘回去的浮点噪声不会再被误判成姿态变化，镜头移动时不会反复重烘）；
- 「每帧都在变」的实例从永久拉黑改成 **1200 帧后重试**；
- 新增计数 `Batch draws` / `Batch: guard reject` / `Batch: emit reject` / `Batch: content rebake` / `Batch: unbatched` / `Batch: no shader`，`Batch submits` 现在两条分支都计数；保留每条路径的一次性日志。

文档已改正：[报告](docs/architecture/client/render/benchmark/26.35.11.1-7/测试数据计算报告.md)、[B2 实施记录](docs/architecture/client/render/refactor/b2-cross-instance-batch.md)、[TODO](docs/architecture/client/render/TODO.md)。

**还没做的验收**：合批画面与 `StaticBatch.ENABLED = false` 的 A/B 目视对比；逐格移动时 `Batch rebuilds = 0` 且 `Light patches > 0`（参考 `b1-b-3` 协议）；改装/拆框后展示框内容立即更新。代码未提交。

# 

```
b2-3测了夜晚的，更新报告
以及B2的合批是否只对多个同一枪械生效？如果Ground类型且实际没旋转的模型是否会生效？对不同枪械是否生效？玩家手持枪械是否生效？是否会把玩家手持枪械给取消渲染？
```

# 

b2-3 是夜晚移动光照（第一人称、**无物品展示框**）。我把它作为独立一节补进报告，同时回答合批范围的问题。

b2-3 已并入 [报告](docs/architecture/client/render/benchmark/26.35.11.1-7/测试数据计算报告.md)（第 7 节）。

**b2-3 结果**（夜晚、逐格移动、n=3）

|步骤|AF 帧时间|1% low|0.1% low|
|---|---:|---:|---:|
|空手（夜晚）|0.814 ± 0.013 ms|519.3|423.2|
|第一人称 ak47·移动|1.196 ± 0.026 ms|342.5|180.8|
|光影 ak47·火把原地 / 移动|6.484 / 6.312 ms|150.1 / 150.3|74.3 / 144.5|
|第一人称满配·移动|1.459 ± 0.025 ms|348.3|198.0|
|光影满配·火把原地 / 移动|7.031 / 6.860 ms|138.6 / 135.9|89.0 / **58.6**|

移动段全部 `Bake rebuilds = 0`，一次光照变化 **0.147–0.628 ms**（B1-c 之前是 0.6 / 2.7 / 1.4 / 9.2 ms），满配+光影的 0.1% low 58.6 与 `b1-c-b` 的 58.5 持平，原地与移动几乎同速——光照尖峰没有回归。

**一个需要指出的口径**：b2-3 的 标准.txt 写明「**无物品展示框**」，所以这一组只覆盖第一人称的光照改写（B1-c-B），`Batch candidates = 0`，并没有测到 B2 自己的「按实例区间 patch」。B2 的移动光照还需要一个**夜晚 + 9 个展示框**的场景。

---

**合批范围（代码实际行为）**

1. **不是「同一把枪才合并」。** 分组键是 `RenderType`（纹理 + 渲染状态），实例键是 `(实体, RenderType, 模型对象, 子网格)`。同款枪天然共享一个 RenderType；不同枪只要纹理/状态相同也会进同一批。展示部分的 draw 数 = **同场景不同 RenderType 数**，与枪数无关。单个实例也会走合批（1 块、1 次 draw），只是没有收益。
2. **GROUND 不合批**，即使这一刻看起来没转。`batching()` 只接受 `ItemDisplayContext.FIXED`；掉落物位姿由实体动画（旋转/浮动）驱动，实现里不做「本帧是否在变」的额外判断。要覆盖它得改成「位姿在容差内不变才加入、变化时移出」，而掉落物常见多份副本，收益有限。
3. **不同枪械生效**，但按纹理分组：不同纹理各成一组、各一次 draw；同纹理的附件也会合进对应组。瞄具镜身/模板这类走 `drawOrBakePatchable` 的子网格保持逐实例，不参与合批。
4. **玩家手持不生效**：第一人称走 B1-b 分块烘焙，第三人称是 `THIRD_PERSON_*`、走 B1 每实例矩阵。
5. **不会取消手持枪的渲染**：非 FIXED 上下文 `batching()` 直接 false，`renderBaked` 原样落回原有路径；FIXED 下 `submit()` 失败也落回逐实例。被合批的展示实例只是**延后**到实体循环结束后统一绘制，`flush()` 挂在 `LevelRenderer.renderLevel` 之后，正常帧必然执行，不会丢画。

这些问答也已写进 [B2 实施记录](docs/architecture/client/render/refactor/b2-cross-instance-batch.md) 的「合批范围」一节。改动未提交。

# 

```
那么这个B2优化：
- 对于实际场景多把不同枪同时渲染，静态展示框展示不同枪械下是没有提升的？
- B2是否引入的更多兼容性风险？
- 默认每把枪都用不同的纹理，如果只有在多个展示框展示同一把枪才有收益，则B2视为无实际收益的优化，需要更新文档
- 对于9把展示框同枪的收益，需要计算收益固定值，并像`lighting-decoupling.md`用到的FPS图画一个收益图在26.35.11.1-7下
```

# 

报告已更新（[26.35.11.1-7](docs/architecture/client/render/benchmark/26.35.11.1-7/测试数据计算报告.md) 第 8、9 节），同时修正了之前一个偏乐观的口径：**瞄具镜身每枪 2 次 draw 没有参与合批**，所以同款满配是 **72 → 24**（6 次合批 + 18 次瞄具镜身），不是 72 → 6。

**逐条回答**

1. **多把不同枪同时展示，没有提升？** 对**枪体**成立：枪体纹理按枪唯一，不同枪各成一组，draw 数不变。但合批键是 `RenderType`，所以仍有两处能省：
  - 不同枪**共用同一配件/瞄具纹理**时，配件合并（9 把不同满配、共用一套配件：72 → 32，省 40 次 ≈0.31 ms）；
  - 裸枪混合展示：**0**。

2. **兼容性风险？** 比 B1 高，报告里列了表：两个 mixin（版本相关、注入失败直接崩）、绘制延后到实体循环之后、相机矩阵剥离依赖 `getInverseViewRotationMatrix` 语义、裸 GL + Forge 专有 `putBulkData`、展示实例状态识别靠实体+`Item` 保活、模板测试语义改变。现有缓解：光影包下整段回退 B1、单实例失败回退 B1、上限与资源重载清理、一次性日志+计数。

3. **是否算“无实际收益”？** 不宜一概而论，但你的判断方向是对的：**本批的 −19.6% 只在“9 把同款满配”下成立**，不能外推。真实的收益表是：
   | 场景（9 框） | 节省 |
   |---|---:|
   | 同款满配 | 48 次 ≈0.37 ms |
   | 同款裸枪 | 8 次 ≈0.06 ms |
   | 不同裸枪 | **0** |
   | 不同满配、共用配件 | 40 次 ≈0.31 ms |
   | 不同满配、配件各异 | **0** |

   因此文档已改为「收益有边界、依赖同款重复或共享配件，不可外推」，并在 B2 文档、TODO、refactor/Home 各写了一句（不重复展开，细节只留在报告第 8 节）。如果产品的实际展示场景以「混合收藏墙」为主，B2 的单帧收益确实接近 0，而兼容性成本是实打实的——这属于取舍问题，我在下一步框里问你是否先默认关掉。

4. **固定收益与 FPS 图**：固定收益 **0.42 ms**（draw 口径 48×7.8 µs≈0.37 ms）。图已生成：[B2收益-固定0.42ms对帧率的影响.png](docs/architecture/client/render/benchmark/26.35.11.1-7/B2收益-固定0.42ms对帧率的影响.png)，脚本 [plot_b2_gain.py](docs/architecture/client/render/benchmark/26.35.11.1-7/plot_b2_gain.py)，参考点：60→61.6(+2.6%)、144→153.3(+6.5%)、240→267.0(+11.2%)、466→579.7(+24.4%)、1000→1727(+72.7%)。

另外 b2-3（夜晚移动）也并进了同一报告第 7 节：移动段 `Bake rebuilds=0`、单次光照改写 0.147–0.628 ms（B1-c 前 0.6–9.2 ms）。它**没有展示框**，只验证了第一人称；B2 自己的按实例光照仍待一个「夜晚 + 展示框」场景。
