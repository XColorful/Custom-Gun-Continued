> #89 提示词

# 

```
读 `./docs/architecture/client/render/PLAN.md`，结合benchmark研究结果，在现有渲染文档体系中新增专门分析完整渲染链路的文档（数量不确定）

Home.md中的总览视角图仍保持现有的纯业务视角，不需要调整为技术链路视角
具体要求：
- 控制流程粒度：本次新增文档不必重复描述每个步骤的具体细节：例如，描述枪械提交渲染时，可将其作为一个整体（不拆分为弹匣、配件、枪体等对分析无关的子项）避免最终绘制的流程图过于复杂
- 基于源码分析：需要阅读源码，梳理并分析完整渲染链路，同时更新相关文档；具体性能开销和量级依赖用户运行调试代码获取的日志
- 合理拆分研究任务：完整渲染链路的研究范围较大，可以先完成整体梳理，再将不同部分分配给多个 Agent 并行研究，降低单个 Agent 的上下文负担，减少因上下文过长导致的分析偏差和幻觉。
- 遵循现有文档规范：文档格式和组织方式参照现有渲染文档。考虑到最终内容可能较多，全写在个文档不一定合适，可以根据实际分析结果新增多个文档
```

# 

I have the source-level picture. Now I'll write the docs, splitting the four chain segments across parallel agents so each has a narrow research scope.

While those run, I'll write the chain overview doc.

Good catch from source: `RenderType` is memoized per texture in 1.20.1 — I need to correct that assumption before the submission doc is written.

分为四个部分的文档正在并行撰写：入口与调度、每帧状态与数据、几何生成与提交、场景差异与静态化。总览 [chain/Home.md](docs/architecture/client/render/chain/Home.md) 与入口文档已经落盘并接入了导航。

已修正一处关键判断：1.20.1 的 `RenderType` 实体工厂是按纹理 memoize 的（`ENTITY_CUTOUT = Util.memoize(...)`），所以真正的结构性损耗不是「每帧新建」，而是「纹理即批键、不同纹理必然分批」；只有 `energySwirl` 这类带浮点参数的工厂会在每帧新建。这条已同步给正在写提交阶段文档的 Agent。

等四个 Agent 返回后我会统一校对交叉引用、数字口径和 P 编号，再汇报。

三份已完成（入口、状态与数据、场景与静态化），几何与提交那份还在写。已修正两处换算笔误（切换槽位帧时间是 0.838 ms，不是 1.210 ms），并在总览里改正了 RenderType 的结论。等最后一份返回后统一校对。

新增了 `docs/architecture/client/render/chain/` 下 5 篇文档，`render/Home.md` 的导航表和 `PLAN.md` 的「渲染链路分析」一节已指向它；业务视角的总览图没有改动。

**文档划分**（按链路分层，而不是按场景拆分，避免重复现有文档）

| 文档 | 覆盖链路 |
|---|---|
| [chain/Home.md](docs/architecture/client/render/chain/Home.md) | 帧级总图、阶段划分、四类成本量纲、基准锚点、P1–P13 损耗点索引、埋点与数据登记表 |
| [chain/entry-and-dispatch.md](docs/architecture/client/render/chain/entry-and-dispatch.md) | 一帧里 CGC 被调用几次、阶段归属、派发判定链、early-out 成本 |
| [chain/state-and-data.md](docs/architecture/client/render/chain/state-and-data.md) | 状态机更新、每帧附件/NBT 读取、节点变换、每帧分配清单 |
| [chain/geometry-and-submission.md](docs/architecture/client/render/chain/geometry-and-submission.md) | 逐顶点生成、缓冲源三条路径、flush 点清单、RenderType 批键、模板窗口、draw 估算 |
| [chain/scenes-and-static.md](docs/architecture/client/render/chain/scenes-and-static.md) | 六种场景的成本乘数、多场景归因、静态化缺口、LOD 边界 |

**源码分析得到的三条定量结论**

1. 成本是「每枪每次提交」的线性量，与场景无关：第一人称单枪 +0.636 ms，multi 里 9 把展示框枪约 0.538 ms/把。
2. 与 TaCZ 1.2.0 的 13.6 倍差距（0.039 ms/把 vs 0.538 ms/把）基本可全部归因于静态展示复用——两端在标准持枪场景几乎同速。
3. 批处理被 `ModelObject.render` 末尾的 `endBatch()` 打断，次数随子模型数增长（枪体 + 每个配件 + 文字 + 手臂），且用的是全局共享缓冲源。

**过程中的两处修正**：1.20.1 的 `RenderType` 实体工厂是按纹理 `Util.memoize` 的，所以「每帧新建」不成立，真正的问题是「纹理即批键，不同纹理必然分批」，仅 `energySwirl` 每帧新建；另外 delegate 渲染器实际忽略传入的 `VertexConsumer`，各自重新取 buffer。

**未完成**：除 benchmark 归因出的数值外，所有单段耗时、flush 次数、draw call 数都标了「待测」——需要按 Home 里的埋点表在 `DebugKey`（F12，当前空实现）后加计时才能回填。文件均未提交。

# 

```
现在开始加测试日志。

之前基准测试的方式是：
- 按下MSI Afterburner快捷键10秒后再按，指定目录下有新文件（每次追加写入）

现在需要的测试方式类似：
- 按下DebugKey开始收集数据（非debug情况下应该只有一个if的开销），再按下停止收集
- 测试期间我会同时跑一份已有的benchmark，以分析volatile boolean的影响
- 数据收集完后立即进行计算，追加写入文件，对收集数据项的写入风格应该跟Afterburner类似
```

# 

Plan mode is off, so I'll implement. Starting with the profiler core.

代码已落地：新增 `ClientRenderProfiler`（volatile 开关 + 12 个探针 + 4 个计数器），F12 开始/停止，结束时统计并追加写入游戏目录的 `customgun-render-profile.txt`；帧边界挂在已有的 `RENDER_FRAME_EVENT`（整帧渲染结束后）。埋点分布在提交、状态更新、数据刷新、模型渲染、flush、delegate、模板清空等位置，未采集时每个埋点只有一次 volatile 读加一次分支。

正在编译 core 与 forge-compat 验证。

Build passes. Let me lint the touched files for warnings/errors.

埋点已实现并通过编译。

**用法**：F12 开始采集，再按一次停止；聊天栏会提示开始/完成与文件绝对路径。结果追加写入 `./customgun-render-profile.txt`（相对 JVM 工作目录，通常就是游戏目录，与 `DebugCommand` 的写法一致）。

**输出格式**（对齐 Afterburner）：

```
09-11-2026, 23:42:40 customgun render profile completed, 8472 frames rendered in 10.512 s
                     Average frame time :    1.241 ms ( 805.8 FPS)
                     Minimum frame time :    0.913 ms
                     Maximum frame time :    4.221 ms
                    1% high frame time  :    1.980 ms
                 CGC render per frame :    0.636 ms ( 51.2% of frame)
                First-person submissions :     1.00 / frame
                  ...
                First-person submit :    0.636 ms (min   0.501 / max   1.204 / p99   1.180, 100.0% of frames)
```

**探针**（12 个 + 4 个计数器，定义见 [chain/Home.md](docs/architecture/client/render/chain/Home.md) 的「测量与数据登记」）：第一/第三人称整次提交、状态机 `update`、附件数据刷新、`renderScope`、`ModelObject.render` 整体、节点快照、顶点遍历、`endBatch` 合计、delegate、枪口缓存、模板清空；计数含提交次数、flush 次数、提交立方体数（×24 即顶点量）。

**开销**：未采集时每个埋点一次 `volatile` 读 + 一次分支（`begin()`/`end()` 成对展开），每帧另有 1 次；唯一的例外是立方体计数，每个立方体多一次非 volatile 判断——如果你觉得它干扰测量，我可以把这一项去掉。

**实现要点**
- [ClientRenderProfiler.java](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderProfiler.java)：数组在首次开始时一次性分配（10 万帧上限），热路径无分配；超过上限丢弃并标注。
- 帧边界用已有的 `RENDER_FRAME_EVENT`（Forge 上是 `TickEvent.RenderTickEvent` 的 END，即整帧渲染结束），一个样本完整覆盖世界、手部、GUI。
- 探针可嵌套（提交包含模型渲染，模型渲染包含 flush），报告各算各的，不能相加。
- 未做预热丢弃：开始后的前几十帧含 JIT/分配毛刺，建议一次采 10 秒以上。

**改动文件**：`ClientRenderProfiler` 新增；`DebugKey`、`ClientEventHandlers`、`GunItemRenderer`、`_GunModelRender`、`ModelObject`、`_AttachmentModelRender`、`TextRender`、`HandRender`、`BedrockPart` 各加探测点。文档同步更新了 [chain/Home.md](docs/architecture/client/render/chain/Home.md) 与 [PLAN.md](docs/architecture/client/render/PLAN.md)。

**未验证**：运行时行为与真实耗时（这里跑不了客户端）；26.3 工作区未改动，`RENDER_FRAME_EVENT` 与 `Minecraft` API 在那边需要移植时再确认。

# 

```
跑了一次benchmark，两个文件内容放在D:\Temp-File\下`cgc1 (1).txt`和`customgun-render-profile.txt`，看下检测逻辑是否有问题，CGC没测到数据
```

# 

数据其实采到了——第 2 段（00:12:22）就是持枪段。我先修一个会误导「命中率」的真实逻辑问题，再说明对账结果。

采集逻辑没有问题——**数据就在第 2 段**，其余三段本来就不该有数据。

## 四段与 benchmark 的对应

| 采集时间 | benchmark | 帧率（采集 / Afterburner） | 提交/帧 | CGC 耗时 |
|---|---|---|---:|---:|
| 00:10:29 | 空快捷栏 1313.7 | 1313.4 / 1313.7 | 0 | 0 |
| 00:12:22 | 持枪 728.9 | 722.7 / 728.9 | **1.00** | **0.530 ms** |
| 00:13:28 | 切换槽位 1272.9 | 1272.0 / 1272.9 | 0 | 0 |
| 00:14:21 | 清空 1332.4 | 1332.8 / 1332.4 | 0 | 0 |

空快捷栏、`/clear` 手上没有枪，切换槽位那一步枪只以 **GUI 槽位贴图**出现——它不走 `_renderSlotTexture` 之外的提交路径，所以提交探针是 0。这也解释了切换段那个奇怪的 `Cubes submitted: 1.00 / frame`：GUI 图标用的 `SlotModel` 恰好是 1 个立方体，计数到了，但没有计时。

## 对账

- 基准增量：`1000/728.9 − 1000/1313.7 = 1.372 − 0.761 = 0.611 ms`
- 实测 CGC：`0.530 ms`，覆盖 **87%**
- 采集自身开销：`1.384 − 1.372 = 0.012 ms/帧`（0.84%）；空手段采集与 Afterburner 完全一致（0.7613 / 0.7612），说明 volatile 开关在没枪时没有可测开销
- 明细：状态 0.029 + 数据 0.003 + 模型 0.473 + 枪口 0.001 + 模板 0.005 ≈ 0.511；模型内部遍历 0.304、flush 0.109（3 次/帧 ≈ 0.036 ms/次）、delegate 0.079、快照 0.009
- 缺口 ≈ 0.07 ms（扣掉采集开销后）：`RenderHandEvent` 派发与入口判定、`BeforeRenderHandEvent` 的手部相机动画、`COMPUTE_CAMERA_ANGLES / COMPUTE_FOV`，以及持枪时才有的准心/HUD——这些目前都没有探针

`max` 那几项（3.1 / 2.6 ms）是单帧毛刺，看 `p99` 更稳。

## 顺手修的一个逻辑问题

`Scope render` 显示只有 78.3% 命中：极短区间的 `nanoTime` 差可能正好为 0，原来用「耗时 > 0」判断是否命中，会把这类帧算成未命中，导致命中率偏低、平均值偏高。现在改为在 `end()` 里单独记调用次数（[ClientRenderProfiler.java](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderProfiler.java)），命中率按调用次数算。core 已重新编译通过。

另外子项之和 0.501 略大于父项 `Model render total` 0.473，是嵌套与聚合口径导致的（配件模型的 flush/traverse 记在全局，父项只算自己那层），文档里已经约定「各算各的、不能相加」，对结论没有影响。

# 

```
有还要补的就都补上，预期能把当前文档里待测的都测完
```

# 

补齐了，现在文档里每一条「待测」都有对应探针。

## 新增探针（8 个，共 20）

|探针|位置|对应待测项|
|---|---|---|
|`Hand event entry`|`FirstPersonRender.handleEvent`|入口事件与判定开销|
|`Third-person visual update`|`_LocalAnimHandler.onRenderFrame`|第三人称每帧 `visualUpdate`|
|`State machine init`|`AnimateGeoItemRenderer.tryInit`|切枪初始化毛刺（平时显示 `-`，只看切枪那一下）|
|`Functional nodes`|`FunctionalBedrockPart.render`|功能性节点登记|
|`GUI slot icon`|`GunItemRenderer._renderSlotTexture`|切换槽位那一步的 GUI 图标成本|
|`Camera angles`|`GunCameraHelper` 的 `COMPUTE_CAMERA_ANGLES_EVENT`|上次缺口|
|`Hand camera`|`BEFORE_RENDER_HAND_EVENT` 分支|上次缺口|
|`FOV`|`COMPUTE_FOV_EVENT` + `COMPUTE_FOV_MODIFIER_EVENT`|上次缺口|

## 新增计数器（4 个，共 8）

- `Nodes captured`：每帧节点快照数，×2 约等于 `NodeTransform` 分配量
- `Geometry batches`：每帧 `getBuffer` 次数，即纹理批数（draw call 上界）
- `Stencil windows`：每帧开启模板窗口的次数
- `LOD hits`：LOD 真正替换的次数

计数点分别落在 `ModelObject.render`、`BeamRender`、`MuzzleFlashRender`（两层各一次）、`TextRender`、`HandRender`、`_AttachmentModelRender`、槽位贴图，以及 `_GunModelRender._renderScope` 与两处 LOD 替换分支。

## 预期下一次采集的变化

- `Scope render` 命中率会变成 100%（上一版 78.3% 是零耗时被误判成未命中，已修）
- 持枪段应能看到 `Hand event entry` + 三个相机探针，把上次未归因的 ~0.07 ms 拆开
- `Geometry batches` ≈ 3~4/帧（枪体 + 配件 + 文本/手臂），与 `endBatch calls` 对比能看出每批里合了几个子模型
- 切槽位那一步会给出 `GUI slot icon` 的耗时，对应 benchmark 的 +0.043 ms
- 展示框 / 多人场景会用 `World-item submit` 与 `LOD hits` 补齐 P10/P11

文档同步更新：[chain/Home.md](docs/architecture/client/render/chain/Home.md) 的探针表，以及四份子文档的「待测项」全部改成探针名。`core` 与 `forge-compat` 编译通过，代码未提交。

# 

```
又更新了一次benchmark，状态机init如果只在切枪时执行一次就得始终记录，直到写文件时才再次开放记录
测试时是切枪等帧数跑热之后才开始计时，所以测不到
等这次改完我再跑全部benchmark
```

# 

状态机初始化已改成「始终记录」。

**行为**
- [AnimateGeoItemRenderer.tryInit](core/src/main/java/dev/xcolorful/customgun/client/renderer/item/AnimateGeoItemRenderer.java) 每次执行都计时并调用 `recordInit`，**不经过 `enabled` 开关判断**（切枪只发生一次、每次两对 `nanoTime`，开销可以忽略）。
- 累计值跨采集窗口保存：切枪在开始计时之前、帧数跑热之后再按 F12，也能在写文件时看到。
- 每次**成功写文件后清零**，下一次切枪重新累计——符合你说的「写文件时才再次开放记录」。
- 报告末尾单独一行，不混在 20 个探针里：

```
State machine init (since last report) :    1.842 ms (last, 1 calls, max    1.842, at 00:41:07)
```

带 `at` 时刻，是因为如果切枪发生在某一段报告写入之前，这一行会落在**前一段**（比如进存档就握着枪，就会出现在空快捷栏那一段），看时间能直接对上。

`core` 编译通过。这次可以跑全套 benchmark 了。

# 

```
数据已经测好了，放在benchmark的26.35.11.1-1下
状态机init似乎还是没测到，但是现在不改CGC代码，没测到的以后再测
现在只写python脚本和报告：
- 由于只有一个端，Afterburner的表可以跟builtin的表在一起
- 如果统计的表太多，允许用两到三张图片
- 在./docs/architecture/client/render/benchmark下加一个文档，用来连接下面两个文件夹的报告
- 在./docs/architecture/client/render/Home.md加一个连接，到./benchmark/下的文档
```

# 

数据在，先写解析脚本摸清结构，再做图。

数据解析成功。多组数据的呈现顺序与预期相反，需要按实际内容标注，而不是按步骤名。现在写完整脚本。

脚本、报告和索引都已生成。

**产出**
- [analyze.py](docs/architecture/client/render/benchmark/26.35.11.1-1/analyze.py)：解析 25 个文本，过滤非 javaw 记录，按内容分组 multi，输出三张图。
- [测试数据计算报告.md](docs/architecture/client/render/benchmark/26.35.11.1-1/测试数据计算报告.md)：Afterburner 与内置探针同表。
- 三张图：标准测试 / multi / 稳定性。
- [benchmark/Home.md](docs/architecture/client/render/benchmark/Home.md)：连接两个批次；[render/Home.md](docs/architecture/client/render/Home.md) 已加导航行。

**核心数字**（6 次重复）

|项|值|
|---|---|
|第一人称持枪增量|+0.6725 ms/帧（−45.8%）|
|其中 CGC 采样|0.5565 ms，覆盖 82.8%|
|单次提交构成|几何遍历 56%、flush 21%、delegate 15%、状态 6%|
|单价|0.70 µs/立方体、39 µs/次 flush|
|9 个展示框|4.73 ms/帧（77.8%），165 Hz 余量 0.999×|
|单把展示框枪|0.518 ms（与第一人称 0.557–0.595 同量级）|
|采集开销|持枪 +0.0061 ms，空手 +0.0027 ms|

**两件数据上的事**

1. **状态机初始化其实测到了**——报告末尾的 `State machine init (since last report)` 那行就是窗口外的累计值：13 个样本，均值 **11.94 ± 3.86 ms**（典型 11–17 ms，另有一次 0.37 ms 的重复初始化）。采集窗口内的同名探针仍为空，那是因为切枪发生在开始计时之前。

2. **发现一个共享模型状态泄漏**：「仅 9 个展示框」比「展示框 + 手持枪」每把多 **61 个立方体**、功能节点多 **0.4 ms/9 把**，而且方向是"没有手持枪时更慢"。逐块看，5/6 场的 FP=0 段都慢，唯一例外那场的立方体数正好等于快的那组。这与 `cleanAnimationTransform()` 只在第一人称渲染结束时把 `additionalMagazineNode.visible` 置 false 吻合——会话里没渲染过手持枪时，`mag_additional` 一直可见，每帧多编译一个弹匣。按 61 立方体 / 0.40 ms 算出 0.73 µs/立方体，和几何遍历的 0.70 对上。既有性能代价，也可能表现为展示框里多画一个弹匣；按你的要求没改代码，已写进报告的「4.1」。

另外 multi 两段的实际顺序与 `标准.txt` 描述相反，报告里按「是否含第一人称枪」标注，没有沿用「持枪 / 清空」的名字。

# 

```
看一下git工作区说明，报告说明加点补丁
```

# 

看了工作区：之前那批内容已经提交（`beb59d28b`、`52b2b3a49`），现在只剩你对 [标准.txt](docs/architecture/client/render/benchmark/26.35.11.1-1/标准.txt) 的两条修改。已按它们给报告打补丁：

**1. multi 顺序** —— 原文说「1 持枪 / 2 清空」，实际是「1 初始无手持枪 / 2 持枪」。报告 0 节改成直接引用更新后的 `标准.txt`，并说明脚本仍按内置探针的提交计数（0 / 1）分组，不依赖录制顺序；删掉了原来「与描述相反」的措辞。

**2. 启动器影响** —— 新增说明：官方启动器后台未最小化会略微拉低帧数，测试时已手动删除大部分 `Minecraft.exe` 帧数块，`cgc1 (3).txt` 残留 4 条（约 26 FPS），解析时只保留 `javaw.exe`；受影响的场次在 4.2 单独标注。

同步改动：
- [analyze.py](docs/architecture/client/render/benchmark/26.35.11.1-1/analyze.py) 顶部注释（语法已校验）
- [测试数据计算报告.md](docs/architecture/client/render/benchmark/26.35.11.1-1/测试数据计算报告.md) 的 0 节与 4.2
- [benchmark/Home.md](docs/architecture/client/render/benchmark/Home.md) 的约定条目

数值与结论未变；图未重新生成（内容不受影响）。
