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

[fork1](#fork1)
[fork2](#fork2)

# fork1

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

# fork2

```
先不处理附加弹匣的状态泄漏问题
在26.3分支复制一样的探针，可以根据26.2+延迟渲染修改一些探针（在相同泄露隐患的情况下进行测试）
- 测试的条目可能跟1.20.1不一样
- DebugKey等相同可复用的部分就尽量保持完全一致，使得1.20.1最终合并到26.3时无冲突
- 预计测试同样是跑6+6次，相同方式测试
```

# 

现在把分析脚本做成两个版本通用，并补齐文档说明。

26.3 分支的埋点已复制完成，编译通过（`core` + `neoforge-compat`）。

**保持不变的部分**（合并 1.20.1 → 26.3 时不会冲突）
- `DebugKey.java` 与 1.20.1 逐字一致：F12、`InputAction.PRESS` 判定、`ClientRenderProfiler.start()/stopAndWrite()`。
- `CustomInputKeyTag.DEBUG`、`CustomInputKey.DEBUG`、`ClientKeyMappingRegistry` 的注册行，与 1.20.1 的 diff 相同。
- `ClientEventHandlers` 里 `RENDER_FRAME_EVENT` + `EventPriority.LOW` 的注册。
- `ClientRenderProfiler` 的开关、统计、输出格式、窗口外累计的状态机初始化（`State machine init (since last report)`）完全一致。

**随延迟渲染调整的部分**

|1.20.1|26.3|说明|
|---|---|---|
|`Model render total`|`Model submit`|只测提交阶段（节点快照采集 + `submitCustomGeometry`）|
|`endBatch flush`|`Model flush`|flush 回调整体，顶点生成发生在这里|
|`endBatch calls`|`Geometry submits` / `Flush callbacks`|提交次数与回调执行次数分开计|
|`CGC render per frame`|提交 + `Model flush`|否则只统计到提交阶段会大幅低估|
|`Stencil clear`|保留|26.2 起为空实现，应测到 0，用来确认该成本消失|
|`displayClientMessage`|`sendSystemMessage`|26.3 移除了前者|

探针索引、计数索引、命名顺序与 1.20.1 一一对应，`Functional nodes` 仍在 flush 回调内、`Node snapshot` / `Geometry traverse` / `Delegate render` 的相对关系不变。附加弹匣状态泄漏按要求未修，测试时会一并带进 26.3 的展示框场景。

**测试与出报告**
- 采集方式与 1.20.1 完全相同：F12 开始 / 停止，标准 4 步 + multi 2 步各 6 次。
- `analyze.py` 已改成同时识别两版字段名（`Model render total`/`Model submit`、`endBatch flush`/`Model flush`、`endBatch calls`/`Geometry submits`），26.3 的数据放到新目录后直接复用即可。

两个工作区的改动都未提交：1.20.1 在 `Render-analysis`，26.3 在 `Render-refactor-26.3`。

# 

```
测试数据好了，在1.20.1重新跑分析，更新报告
```

# 

1.20.1 工作区里现在有两端数据：`cgc1`(1.20.1) 与 `cgc2`(26.3)。我把分析脚本改成双臂版本。

报告已按双端重写（`cgc1` = 1.20.1，`cgc2` = 26.3），脚本、三张图和文字都更新了。

**主要结果**

|项|1.20.1|26.3|
|---|---:|---:|
|空手基线|0.8003 ms|0.9909 ms|
|持枪增量|+0.6725 ms|**+0.5601 ms（−17%）**|
|CGC 采样|0.5565 ms（覆盖 82.8%）|0.3017 ms（覆盖 53.9%）|
|CPU 侧几何+提交|0.4288 ms（遍历 0.311 + flush 0.118）|**0.2303 ms（54%）**|
|9 展示框帧时间|6.07 ms（165 FPS）|**3.72 ms（270 FPS）**|
|单把展示框枪|0.526 ms|**0.199 ms（−62%）**|
|165 Hz 余量（9 框）|0.999×（贴住）|1.63×|

**两个注意点**

1. **跨版本不能直接比 CGC 数值**：26.3 的 CGC 只含「提交 + flush 回调」，render pass 提交与手部/文字 overlay 不在探针内，所以覆盖率只有 53.9%（1.20.1 为 82.8%）。跨版本结论用的是 Afterburner 的帧时间增量与帧时间本身。
2. **附加弹匣状态泄漏在两端都复现**：`cgc1` 1/6 场次无泄漏、5/6 有；`cgc2` 2/6 无、4/6 有。有泄漏时立方体 4005 → 4554、功能节点 0.02 → 0.23 ms（26.3）/ 0.03 → 0.44 ms（1.20.1），逐块完全同构。已写入报告 5.1，未改代码。

**改动文件**（均未提交）
- `26.35.11.1-1/analyze.py`：改为按端循环，兼容两版字段名，multi 按内容分组。
- `26.35.11.1-1/测试数据计算报告.md`：双端表格、跨版本对照、探针口径说明。
- 三张 `测试数据总览-*.png` 重新生成。
- `benchmark/Home.md` 的批次描述改为「CGC 1.20.1 与 26.3 双端」；`chain/Home.md` 的数据登记补了 26.3 数值。

# 
---

```
读 `./docs/architecture/client/render/PLAN.md`，结合现有benchmark报告、渲染链路分析，需要在渲染链路分析里补充相关文档来确定重构方案

具体要求：
- 渲染链路分析的Home.md仍然保持对应当前代码状态的分析
- 需要考虑CPU和GPU是否有充分利用的问题
- 优化上限是空手时的FPS（约1300），优化下限是当前渲染性能
- 如果有多种优化方案，分别需要考虑是否能解决CGC当前渲染开销大头，又或者是否是一套新的渲染开销计算方式
- 基于源码分析：需要阅读源码，梳理并进行可行性分析，同时更新相关文档；具体性能开销和量级依赖用户运行调试代码获取的日志
- 合理拆分研究任务：完整重构方案的研究范围较大，可以考虑先完成整体方向分析，再将不同细节分配给多个 Agent 并行研究，降低单个 Agent 的上下文负担，减少因上下文过长导致的分析偏差和幻觉
- 遵循现有文档规范：文档格式和组织方式参照现有渲染文档。考虑到最终内容可能较多，新增方案全写在个文档不一定合适，允许新增多个文档，但不代表一定要写多个文档
```

# 

我先看文档结构和 PLAN.md，再决定拆解方案。

我已读完 PLAN、两批 benchmark 报告和 chain/ 下全部四篇分析文档，并核对了 1.20.1 与 26.3 的关键源码。现在先把细节研究并行分派出去。

六项细节研究并行启动。

研究任务已全部并行启动。趁等待，我先写链路分析侧的事实文档（CPU/GPU 与上限推导）。

研究结果陆续返回。计数器的实测值修正了每帧 flush 的次数：第一人称实际是枪体 1 次加双手 2 次，共 3 次，而不是原先推算的 5–7 次。我先按实测数据更正链路文档。

最后一项研究已返回，并发现一个重要的目标口径问题：在 multi 场景中，帧时间里有约 1.2–1.33 ms 是场景本身（展示框实体、方块、HUD），即使 CGC 成本归零，也到不了 0.8 ms。我现在按这个结果修正目标值。

已完成。渲染链路分析现在分成「事实」和「方案」两部分，`chain/Home.md` 仍然只描述当前代码状态。

## 新增文档

**链路分析（事实侧）**
- [cpu-gpu-headroom.md](docs/architecture/client/render/chain/cpu-gpu-headroom.md)：判定核心证据、GPU 侧量级推算、draw 成本反推、优化上限推导、测量缺口与采集方式。

**重构方案（新建 `refactor/`）**
- [Home.md](docs/architecture/client/render/refactor/Home.md)：目标与成功判据、成本事实、方案分层、评估矩阵、推荐路线、新成本模型、风险与验收。
- [scheme-a-micro.md](docs/architecture/client/render/refactor/scheme-a-micro.md)：保留即时管线的微优化，及其收益封顶。
- [scheme-b-baked-mesh.md](docs/architecture/client/render/refactor/scheme-b-baked-mesh.md)：几何烘焙（B1 模型空间 + 每实例矩阵、B2 静态实例世界空间合并），含 1.20.1 / 26.3 的 API 可行性、缓存键、失效条件、光影双路径。
- [scheme-c-draw-batching.md](docs/architecture/client/render/refactor/scheme-c-draw-batching.md)：跨实例 draw 合并与实例化。
- [scheme-d-culling-lod-parallel.md](docs/architecture/client/render/refactor/scheme-d-culling-lod-parallel.md)：剔除 / LOD / 多线程，含不采用多线程的理由。

同步更新了 [PLAN.md](docs/architecture/client/render/PLAN.md)、[渲染 Home 导航](docs/architecture/client/render/Home.md)、[chain Home](docs/architecture/client/render/chain/Home.md)、[geometry-and-submission.md](docs/architecture/client/render/chain/geometry-and-submission.md)（补实测 flush 次数与隐式 flush）与 [TODO.md](docs/architecture/client/render/TODO.md)。

## 关键结论

1. **大头是每帧重造全部顶点**（0.3108 ms，55.8%），其次是 `endBatch`（0.1180 ms，21.2%）。实测第一人称只有 3 次 flush（枪体 + 双手），单次约 40 µs，与 384 KB 上传的带宽量级吻合——所以 flush 的主体很可能是缓冲上传，而非 draw。
2. **微优化只能拿系数收益**，multi 仍会在 2.6 ms 以上；要接近上限必须换成本模型：从「顶点数 × 提交次数」变成「draw 数 + 动态顶点 + 重建次数」。
3. **1.20.1 烘焙可行**：`BufferBuilder → RenderedBuffer → VertexBuffer.upload`，`drawWithShader(modelView, ...)` 直接吃实例矩阵，原版 API 齐全；最大未知是 Oculus 光影下自绘落在哪个 pass，方案里给了「有光影退回 `putBulkData`」的双路径。
4. **multi 的目标不是 0.95 ms**：该场景自身有约 1.2–1.33 ms 基线（9 个展示框 + 方块 + HUD），CGC 归零也到不了空手帧时间，所以目标定为 ≤1.55 ms；标准场景与第一人称仍按你给的 ≈1300 FPS 上限推导。
5. 剔除几乎无收益（原版已做视锥 + 64×包围盒距离剔除），LOD 只减顶点不减提交，多线程被 B 覆盖、不建议做。

未修改任何源码，也没有提交；所有量级都标了「推算 / 待测」，需要用 `DebugKey` 实机日志回填。

# 

```
先补benchmark，我跑测试
- 默认我还是按benchmark/26.35.11-2/标准.txt的方式来测试，如果覆盖不到则需要额外说明
- 目前大方向暂定往接近上限的做法，例如如果按文档里B实现完会使前面的优化失效的，就直接开始B的；但是先不进行优化实现，只写测试代码
- 尽可能补全文档里待测的值，允许mixin
- 如有必要可以翻Oculus/Iris源码，目前benchmark是全程不开光影的；Iris源码仓库的较新，D:\Temp-File\iris-neoforge-1.11.7+mc26.3.jar是测试用的版本
- 说明一下是否要测26.3，如果需要则一并补上26.3探针
```

# 

先确认测试口径与现有日志格式，再决定探针要补哪些点。

开始写探针。先扩 `ClientRenderProfiler`：拆上传/draw、GPU 忙时、分配字节、动画通道数。

1.20.1 编译通过。现在给 26.3 补同口径的 GPU 与分配探针。

探针代码已加好，1.20.1 已编译通过。现在补测试批次说明与解析脚本。

再补一个能直接验证「静态场景是否真的每帧重建」的计数：提交时的节点状态指纹变化次数。

发现你已经建了 `benchmark/26.35.11.1-2/`，我把它当作目标目录，合并过去并删掉自己重复建的目录。

探针和批次说明都准备好了，双端编译通过（`exit 0`），没有改任何渲染逻辑，也没有提交。

## 加了什么

**1.20.1（本分支）**
- [BufferUploaderMixin.java](core/src/main/java/dev/xcolorful/customgun/client/mixin/renderer/BufferUploaderMixin.java)：把一次 `endBatch` 拆成 `endBatch upload`（`VertexBuffer.upload`）与 `endBatch draw`（`drawWithShader`），同时统计 draw 次数与上传字节（顶点+索引）。
- [GpuFrameTimer.java](core/src/main/java/dev/xcolorful/customgun/client/util/GpuFrameTimer.java)：整帧 `GPU frame busy`，用 `GL_TIME_ELAPSED`，延迟一帧读取；F3 打开时原版会占用同类查询，探针自动跳过。
- [ClientRenderProfiler.java](core/src/main/java/dev/xcolorful/customgun/client/util/ClientRenderProfiler.java)：新增 `Allocated bytes`（渲染线程 `ThreadMXBean`）、`Animation channels`、`State changes` 三个计数器。
- [ModelObject.java](core/src/main/java/dev/xcolorful/customgun/client/model/ModelObject.java)：采集窗口内计算 11 个节点字段的指纹，统计「本次提交的几何是否真的变了」——这直接回答 B 方案的核心假设：静态展示首帧后应为 0，第一人称每帧 >0。

**26.3（工作区）**
- 同口径的 `GPU frame busy`（`GpuQueryPool` + 命令编码器时间戳）、`Allocated bytes`、`Animation channels`、`State changes`。
- 26.3 暂时没有 upload/draw 拆分与 draw 计数：它的提交/绘制分离，CGC 拿不到最终 draw，要等自定义 `FeatureRenderer` 的烘焙实现后再补；目前可用已有的 `Flush callbacks` 作为 draw 数的近似。

## 测试口径

用你已建的 [26.35.11.1-2/标准.txt](docs/architecture/client/render/benchmark/26.35.11.1-2/标准.txt)（我把探针说明和新增步骤追加进去了，没有覆盖你原来的内容）。与分析脚本 [analyze_extra.py](docs/architecture/client/render/benchmark/26.35.11.1-2/analyze_extra.py)。

标准 4 步与 multi 前两段照旧。**多出来的一段**：multi 第 3 段清空 9 个展示框里的枪（逐把 `/data remove entity @e[type=minecraft:item_frame,limit=1] Item`），保持框、石头、HUD、朝向不变。这一段直接测出场景基线，替代之前 `6.0688 − 4.7348 = 1.334 ms` 的推算——它决定 multi 到底应该以 1.55 ms 还是别的值为目标。

**覆盖不到、需要额外说明的**：
- 光影路径不测（你确认全程不开光影）。B 的自绘在 Oculus 下是否进对 pass，这批数据回答不了。
- 26.3 的 upload/draw 拆分与 draw 数暂缺，如上。
- `State changes` 与 `Animation channels` 在采集窗口内会带来很小的额外开销（每枪约 108 节点 × 11 次哈希），报告里已经把它算进帧时间，跨批次比较时注意。

## 26.3 要不要测

**建议测，但用缩减矩阵**：标准 4 步 + multi 3 段、每端至少 3 次。理由是 26.3 是延迟渲染的目标平台，需要确认两件事——GPU 是否同样远未饱和，以及提交/flush 的成本结构在 26.3 上与 1.20.1 的 0.23 ms 是否稳定。26.3 的 multi 跑热慢，3 次足够看趋势，不必追 6 次。

`Custom-Gun-Continued` 与 `26.3neoforge` 两个工作区都有未提交改动，按约定停在这里等你验收。

# 

```
multi的baseline我已经在benchmark/26.35.11.1-2/multi-baseline/测好了，这部分的结果等全部测完一起在报告里，不另建报告
默认之后有这个结果，把清除展示框的步骤、能被multi-baseline测过的都清理掉
清理完我在开测
```

# 

基线已测：1.20.1 为 1174.3±23.9 FPS（0.852 ms），26.3 为 901.7±42.8 FPS（1.109 ms）。我先把测试步骤里多余的一段删掉，再按实测值修正文档。

已清理完成，可以开测。

## multi-baseline 读数

按 6 次重复计算：

|端|FPS|帧时间|
|---|---:|---:|
|1.20.1|1174.3 ± 23.9|**0.852 ms**|
|26.3|901.7 ± 42.8|**1.109 ms**|

这直接推翻了之前由 `6.0688 − 4.7348` 推出的 1.334 ms 基线：场景本身只比空快捷栏（0.800 ms）高约 0.05 ms，画枪的约 5.2 ms 几乎全是 CGC。差出来的约 0.48 ms 是探针没覆盖到的 CGC 工作，例如共享缓冲源里被连带 flush 的几何。

## 清理内容

- [标准.txt](docs/architecture/client/render/benchmark/26.35.11.1-2/标准.txt)：删掉“清空 9 个展示框”的第 3 段及其子项；探针说明里改为“基线已在 `multi-baseline/` 测好，本批次不再重复，结果并入本批次报告”。multi 回到原来的 2 段。
- [benchmark/Home.md](docs/architecture/client/render/benchmark/Home.md)：批次行改为 multi 2 段 + baseline 子目录；补了基线的数值与“不另建报告”的说明。
- [chain/Home.md](docs/architecture/client/render/chain/Home.md)、[cpu-gpu-headroom.md](docs/architecture/client/render/chain/cpu-gpu-headroom.md)：上限推导改用实测基线，multi 与标准场景共用同一个空手上限。
- [refactor/Home.md](docs/architecture/client/render/refactor/Home.md)：multi 目标从 ≤1.55 ms 恢复为 **≤1.0 ms**（0.852 + 约 0.15）。
- [scheme-a](docs/architecture/client/render/refactor/scheme-a-micro.md) / [scheme-b](docs/architecture/client/render/refactor/scheme-b-baked-mesh.md) / [scheme-d](docs/architecture/client/render/refactor/scheme-d-culling-lod-parallel.md)：按新基线重算，D 的敏感性表改为 `0.852 + 5.217 × f`，全剔除上限从 1.33 改成 0.85，结论仍是“最多压到约 3.5 ms，触不到 1.0”。
- [TODO.md](docs/architecture/client/render/TODO.md)：场景基线标记为已测。

一个口径提醒：baseline 用的是 `customgun-26.11.35.1`（改探针之前的构建），但那段场景没有枪、也不开 F12，新增探针不产生开销，因此与本批次数据可比。

# 

[fork3](#fork3)
[fork4](#fork4)

# fork3

```
1.20.1 BufferUploaderMixin没通过编译，26.3已经通过编译
```

# 

`ShaderInstance` 在 1.20.1 里属于 `net.minecraft.client.renderer`，不是 `com.mojang.blaze3d.shader`（那是 26.x 的包名）。已修正，正在用 UTF-8 重新全量编译验证。

修好了，1.20.1 和 26.3 现在都是 `0 error` + `BUILD SUCCESSFUL`。

## 两处错误

1. **`ShaderInstance` 包名不同**：1.20.1 是 `net.minecraft.client.renderer.ShaderInstance`，`com.mojang.blaze3d.shader.ShaderInstance` 是 26.x 的路径。导入和 `@Redirect` 的目标描述符都改了。
2. **`DrawState.indexBufferSize()` 是私有方法**：改用公开的 `state.bufferSize()`（等价于顶点 + 索引字节）。

## 关于之前的「编译通过」

是我的验证方式有问题：命令写成了 `gradlew ... | tail -40`，shell 返回的是 `tail` 的退出码，所以 Gradle 失败也被显示成 exit 0。这次改成写完整日志再检查 `error:` 数量与 `BUILD SUCCESSFUL`，双端都确认过了。

另外重跑时发现一个与本次改动无关的现象：在这个 CP1252 环境的 shell 里做全量编译（`--rerun-tasks`），仓库里已有的中文注释会报 `unmappable character for encoding windows-1252`。加 `JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8` 后正常。你在 IDEA 里构建应该碰不到；如果以后要在纯 shell 里做全量构建，可以留意这一点。

# fork4

```
数据测好了，开始分析写报告
注意到1.20.1空手开测后FPS比基准掉了200FPS，测量引入的开销较大
```

# 

数据齐了。先把标准口径统计跑出来，再单独解析新探针。

核心数据已经解析出来，其中包括几个会改变结论的点：upload 占 flush 的主体、每次 draw 约 10 µs、静态场景 State changes 为 0、分配量远低于预估。现在写报告。

报告已写好。现在把实测值回填到链路与方案文档。

报告写完了：[26.35.11.1-2/测试数据计算报告.md](docs/architecture/client/render/benchmark/26.35.11.1-2/测试数据计算报告.md)，对应的链路与方案文档已按实测回填。

## 几个会改变判断的结果

1. **上传是 flush 的主体，draw 是稳定的下限。** multi 里 CGC 上传 0.535 ms、draw 0.181 ms；单次 draw 在标准与 multi 都是 **9–10 µs**。持久 VBO 能去掉上传，但 18 次 draw 仍在，所以 B1 之后 multi 约 1.15–1.2 ms，要靠 B2 合并到 3–4 次 draw 才到 0.95–1.0 ms。

2. **分配不是问题。** 每枪实际只分配约 **0.10 MB**（顶点数据本身是 0.388 MB），说明 JIT 逃逸分析把临时 `Vector3f`/`Vector4f` 基本消掉了。方案 A 里「去分配」的两项（A3/A4）价值比原先估计低得多，已在文档里降级。

3. **静态展示的几何确实每帧相同。** `State changes` 在 9 个展示框场景是 **0.00**；第一人称是 0.98–2.00、12 条活跃动画通道。B 的缓存前提成立，第一人称适合「root 矩阵 + 脏子树」。

4. **GPU「忙时」这个指标不成立。** 整帧 `GL_TIME_ELAPSED` 恒为帧时间的 99.4%–99.8%：它测的是第一条到最后一条 GPU 命令的跨度，CPU 间断提交时自然等于帧时间，不能区分 CPU-bound / GPU-bound。已在报告、链路文档和方案里改成「命令跨度」，并写明需要 per-pass 计时或驱动计数器。

5. **模板清空在 multi 下 0.039 ms/帧**（9 把各清一次）。现在只占 0.7%，但烘焙之后它会变成剩余成本里显眼的一项，应改为每帧最多清一次。

## 关于探针开销

AF 跨批次对照显示：26.3 全步骤、1.20.1 除持枪外都多出 **0.06–0.09 ms/帧**（空手约 7%，multi 约 1.3%）。26.3 没有 upload/draw mixin 也一样慢，说明开销来自两端共用的整帧 GPU 查询（每帧创建查询 + 可用性轮询）和分配采样，而不是 `nanoTime` 埋点——AF 与内置帧时间之差仍然只有 0.003–0.006 ms。

你在现场看到的 1300 → 1100（约 200 FPS）比这个更大。两者口径不同，报告里保留了这个差异，建议下次在同一会话里做一次「F12 开 / 关」A/B 才能定论。若继续用这套探针，下批采集前应把 GPU 查询与分配采样改成每 N 帧一次。

## 结论对路线的影响

- multi 目标仍为 **≤1.0 ms**，第一人称目标放宽到 **≤1.05 ms**（CGC ≤0.25）：delegate（文字 / 手臂 / 火焰 / 抛壳）与状态更新会留下约 0.25 ms，遍历和上传全消也到不了 0.15。
- 按你之前的方向，A 里只值得做「合并同纹理 flush、去掉 1.20.1 双重节点快照、模板每帧一清」，其余直接进 B1-a（静态展示烘焙）。

两个工作区的改动都还没提交，等你验收。

# 

```
现在距离确定重构路线+开始重构，是否只差CPU/GPU占用率？还有别的要测的吗
```

# 

不完全是。**“占用率”不是关键指标，绝对 GPU 时间才是**；而且在 multi 场景中，已有的 TaCZ 数据已经能把 GPU 的上界卡得比较死：

- TaCZ 1.2.0 用同一套 9 把手枪模型，整帧 1.185 ms，它的空手基线 0.79 ms → 9 把枪的 CPU+GPU 合计上限约 0.39 ms。也就是说，GPU 画这 9 个模型不可能超过 0.4 ms。
- 目标是 1.0 ms 帧时间，GPU 侧大约占 30–40%，**不是阻塞项**。CGC 的模型与 TaCZ 是同资源，这个上界可以直接借用。

第一人称不确定：近景 overdraw 和 fill 可能是 GPU 的大头，TaCZ 的 1.54 ms 只能给出“能跑进 1.5 ms”，给不出“能跑进 1.05 ms”。这一项值得用 Afterburner 抓一段持枪时的 GPU 占用（几秒钟就够），或者在 CGC draw 区间加 per-pass 计时。它不是路线问题，是目标数值问题。

真正还缺的、会影响设计的是另外三件事：

| 缺什么 | 为什么影响路线 | 能不能现在测 |
|---|---|---|
| **全套配件的枪**（瞄具 + 激光 + 握把 + 扩容弹匣） | 当前 multi 测试枪几乎是裸枪：delegate 只有 0.001 ms、约 2 draw/枪，配件模型与模板窗口都没触发。B2 的 draw 合并收益、第一人称的 delegate 残留下限都被低估了 | 能，加一个测试场景即可，不用改代码 |
| **第一人称动画覆盖的顶点比例** | 我们只知道有 12 条通道，不知道这些通道驱动的子树占多少 cube。B1-b「只重写脏子树」能省多少遍历，取决于这个比例 | 能，加一个 per-node 脏计数（和现有 State changes 同一处，按节点累加），或者等 B1-a 原型时顺手量 |
| **Iris 开启时光影下的自绘是否可行** | 决定 B1 是单路径还是“无光影走 VBO、有光影退 putBulkData”的双路径；benchmark 全程无光影，回答不了 | 不能，要等 B1 原型做出来才能 A/B |

可选、优先级低：第三人称/多人、透明枪的排序、26.3 的 draw/upload 拆分（后者本来就要等 B 的 FeatureRenderer 实现）。

**结论**：路线本身不用等 GPU 数据——B1 → B2 仍是唯一能到 1.0 ms 的路径，A 只做少量结构清理。开重构前建议只补两件便宜的事：一次“全套配件”的 multi 场景（外加 Afterburner GPU 占用采集），和一个按节点的脏计数；光影路径留到 B1 原型再验证。这两件我可以先把探针和测试步骤补上。

# 

不完全是。**“占用率”不是关键指标，绝对 GPU 时间才是**；而且在 multi 场景中，已有的 TaCZ 数据已经能把 GPU 的上界卡得比较死：

- TaCZ 1.2.0 用同一套 9 把手枪模型，整帧 1.185 ms，它的空手基线 0.79 ms → 9 把枪的 CPU+GPU 合计上限约 0.39 ms。也就是说，GPU 画这 9 个模型不可能超过 0.4 ms。
- 目标是 1.0 ms 帧时间，GPU 侧大约占 30–40%，**不是阻塞项**。CGC 的模型与 TaCZ 是同资源，这个上界可以直接借用。

第一人称不确定：近景 overdraw 和 fill 可能是 GPU 的大头，TaCZ 的 1.54 ms 只能给出“能跑进 1.5 ms”，给不出“能跑进 1.05 ms”。这一项值得用 Afterburner 抓一段持枪时的 GPU 占用（几秒钟就够），或者在 CGC draw 区间加 per-pass 计时。它不是路线问题，是目标数值问题。

真正还缺的、会影响设计的是另外三件事：

| 缺什么 | 为什么影响路线 | 能不能现在测 |
|---|---|---|
| **全套配件的枪**（瞄具 + 激光 + 握把 + 扩容弹匣） | 当前 multi 测试枪几乎是裸枪：delegate 只有 0.001 ms、约 2 draw/枪，配件模型与模板窗口都没触发。B2 的 draw 合并收益、第一人称的 delegate 残留下限都被低估了 | 能，加一个测试场景即可，不用改代码 |
| **第一人称动画覆盖的顶点比例** | 我们只知道有 12 条通道，不知道这些通道驱动的子树占多少 cube。B1-b「只重写脏子树」能省多少遍历，取决于这个比例 | 能，加一个 per-node 脏计数（和现有 State changes 同一处，按节点累加），或者等 B1-a 原型时顺手量 |
| **Iris 开启时光影下的自绘是否可行** | 决定 B1 是单路径还是“无光影走 VBO、有光影退 putBulkData”的双路径；benchmark 全程无光影，回答不了 | 不能，要等 B1 原型做出来才能 A/B |

可选、优先级低：第三人称/多人、透明枪的排序、26.3 的 draw/upload 拆分（后者本来就要等 B 的 FeatureRenderer 实现）。

**结论**：路线本身不用等 GPU 数据——B1 → B2 仍是唯一能到 1.0 ms 的路径，A 只做少量结构清理。开重构前建议只补两件便宜的事：一次“全套配件”的 multi 场景（外加 Afterburner GPU 占用采集），和一个按节点的脏计数；光影路径留到 B1 原型再验证。这两件我可以先把探针和测试步骤补上。

# 
---

```
读 `./docs/architecture/client/render/PLAN.md`和`#89 Render-analysis.md`第583-629行
- 26.35.11.1-3已经跑完，分析并写报告
- 如果还有要测的数据就再说明要测什么；如果没有就开始确定重构路线，并且要在文档里预计优化效果
```

# 

Now I'll write the analysis script for the new batch.

Now the route document. I'll update the goals, add the new facts, and add an expected-effect table per stage.

分析完成，报告与路线都写进文档了。

## 26.35.11.1-3 的结论

报告：[测试数据计算报告.md](docs/architecture/client/render/benchmark/26.35.11.1-3/测试数据计算报告.md)，脚本 [analyze.py](docs/architecture/client/render/benchmark/26.35.11.1-3/analyze.py)（自带分组与三张图）。

**标准（第一人称）**

| 步骤 | 1.20.1 帧时间 | 1.20.1 CGC | 26.3 帧时间 | 26.3 CGC |
|---|---:|---:|---:|---:|
| 空快捷栏 | 0.849 | 0 | 1.111 | 0 |
| 裸配 | 1.766 | 0.810 | 1.933 | 0.554 |
| 满配 | 3.018 (n=2) | **1.951** | 3.224 | 0.884 |

**multi（9 个满配展示框）**：1.20.1 14.72 ms / CGC 12.90（每枪 1.434）；26.3 10.57 ms / CGC 5.08。

五条会改变设计的事实：

1. **满配不引入新成本形态，只是放大顶点。** 750 → 1735 立方体（+131%），CGC 0.81 → 1.95，增量 1.14 ms 与 `+985 cubes × 1.1 µs` 吻合。烘焙按网格做，附件自动被覆盖。
2. **瞄具模板是最大单块**：第一人称 0.719 ms（36.8% CGC），multi 0.488 ms/枪（34%）。它是 `_renderScope` 里并列于枪体遍历的一次独立渲染，不是嵌套。
3. **每枪 draw 从 2 次涨到 9 次**（multi 共 81 次 CGC draw，8 次 endBatch/枪）。按实测 8.8 µs/draw，仅 draw 就是 **0.72 ms/帧**——这是烘焙后的地板，也是 B2 必须存在的理由。
4. **26.3 只快 1.39×，不是 2.5×。** 它的 CGC 只解释了 54% 的枪械增量，**4.38 ms 落在 render pass 里**没有任何探针。延迟渲染没让几何变便宜，只是换了归属；跨版本只能比帧时间。
5. 1.20.1 的探针外还有 0.97 ms（占枪械增量 7%）；探针固定开销仍约 0.048 ms/帧（空手 0.849 vs 无探针 0.800）。

## 还需要测什么

**不需要新的游戏内采集批次**——之前列的三项缺口：满配场景已补；按节点脏计数和 Iris 对照本来就该在 B1 原型上做。但有三个埋点应当随 B1 代码一起加，因为它们决定验收数字：

- **全场景不同 `RenderType` 去重计数** → B2 的 draw 下限（当前只知道 9 次/枪，去重后是 10–14 还是 30 未知）
- **包住「本帧所有 CGC draw」的 `GL_TIME_ELAPSED`** → 那 0.97 ms / 4.38 ms 到底在不在 GPU 侧
- **按节点脏计数** → B1-b 脏子树比例

## 路线（已写进 [refactor/Home.md](docs/architecture/client/render/refactor/Home.md)）

B1-a 静态烘焙 → B1-b 第一人称 → **B2 跨实例合并（必需，不是加分项）**；A 的剩余项并入 B1-a（实测合计不到 5%，不单独立阶段）。

预期效果（从实测项直接推算，验收时回填）：

- **multi 9 满配**：CGC 12.90 → B1-a 后 0.85–0.95 → B2 后 0.25–0.35；帧时间 14.72 → 1.9–2.7 → **1.2–1.6**
- **第一人称裸配**：CGC 0.81 → 0.25–0.35；帧 1.77 → **1.15–1.25**
- **第一人称满配**：CGC 1.95 → 0.45–0.60；帧 3.02 → **1.45–1.65**

原来的「multi ≤1.0 ms」建立在裸枪 2 次 draw 之上，已改为 **≤1.6 ms**（冲刺 ≤1.2），并注明区间来自那 0.97 ms 未归因项。

同步更新了 [chain/Home.md](docs/architecture/client/render/chain/Home.md)（锚点、损耗点新增 P14 瞄具模板、单枪量级）、[TODO.md](docs/architecture/client/render/TODO.md)、[PLAN.md](docs/architecture/client/render/PLAN.md)、[benchmark/Home.md](docs/architecture/client/render/benchmark/Home.md)。所有改动**未提交**，等你验收。

# 

```
在报告里补一下git工作区里对26.3-multi第一次的说明，并且之前每一次测试，单个文件是一次启动Minecraft+进入存档，换文件就代表重新启动Minecraft+进存档
```

# 

找到了：[标准.txt](docs/architecture/client/render/benchmark/26.35.11.1-3/标准.txt) 第 62 行已写明“26.3-multi 第一次要等最大空手 FPS…有先第一人称拿枪预热”。我把这一点和“一个文件=一次启动并进档”的采集约定补进报告。

已补进报告，三处：

**1. 采集约定（所有批次共用）** — [benchmark/Home.md](docs/architecture/client/render/benchmark/Home.md) 的「约定」新增一条：单个数据文件 = 一次启动 Minecraft + 进入存档，换文件即重启游戏并重新进档。因此「重复」是跨进程重复，文件之间含 JIT / GPU 时钟 / 区块加载的噪声，跨文件绝对值不做精确比较。

**2. 26.3-multi 第一次的说明** — [测试数据计算报告.md](docs/architecture/client/render/benchmark/26.35.11.1-3/测试数据计算报告.md) 第 7 节，依据你在 [标准.txt](docs/architecture/client/render/benchmark/26.35.11.1-3/标准.txt) 里记的那一条：26.3 帧率掉下去后恢复慢，所以第一次采集要等空手 FPS 升到最大值、并先第一人称拿枪预热，后两次不再多等。结果三次 multi 为 103.3 / 90.7 / 90.9 FPS（满配手持 88.2 / 76.4 / 76.5），第 1 次领先约 13% —— 这是**采集状态差异，不是场景差异**。第 8 节的立方体数异常也改成只陈述事实（12 771 vs 12 852，来源未确认），不再归因成「预热差异」。

**3. 顺手核对的数** — 既然第 1 次偏快，我验了跨端倍数是否被它带偏：两端漂移方向相同，剔除各自第 1 次后为 15.1 vs 11.0 ms，倍数仍是 1.37×（原 1.39×），所以「26.3 只快 1.3–1.4×」这个结论不依赖某一次采集。

文档改动仍未提交。下一步是否开始阶段 1（B1-a 静态烘焙）？

# 

```
读 `./docs/architecture/client/render/PLAN.md`，了解CGC目前确定的重构方案
CGC基于TaCZ1.1.8，性能相当
现在需要对比TaCZ1.2.0对1.1.8的更新，大致了解TaCZ做了哪些优化（至少包含静态优化），是否跟CGC方案重合，CGC方案是否比TaCZ上限更高或交叉等
列出TaCZ1.2.0对1.1.8所有的优化项
```

# 

I'll start by reading the CGC refactor plan, then investigate the TaCZ 1.1.8 → 1.2.0 changes.

Environment cwd shifted to TaCZ118 due to my `cd`; I'll use absolute paths with `git -C` from here on.

# TaCZ 1.2.0 相对 1.1.8 的优化盘点（含与 CGC 方案对照）

**证据口径**：TaCZ 侧代码读的是 tag `1.2.0-forge-mc1.20.1` 与 `1.1.8-hotfix` 的实际 diff；SBM 侧是编译产物（2.2.2 无 `v2` 包、2.6.2 有），其内部机制由类结构/字节码推断，已在文中标注「推断」。

---

## 一、渲染：唯一的性能改动是「静态展示静态化」

### A1. 依赖库换代是载体，不是 TaCZ 自己的代码

|项|1.1.8|1.2.0|
|---|---|---|
|SimpleBedrockModel|2.2.2（只有 v1 包，0 个 v2 类）|2.6.2（新增整套 `v2` 运行时）|

TaCZ 侧只写了约 700 行适配（`ExternalGunAppearance` / `DisplayGunGeometry` / `DisplayGunMeshGroup` / `ExternalGunItemVboRenderer`），真正的烘焙、捕获、GPU 缓存、合批都在库里。CGC 不依赖 SBM，所以这部分**不能直接借用，只能照设计自研**。

### A2. 模型层：烘焙 + 实例化状态

- `ExternalGunModel`：由同一份枪包几何**另建一份不可变烘焙模型**（`BakedBedrockModel`），与动画用的 `BedrockGunModel` 彻底分离。
- `DisplayGunModelInstance`：烘焙期把骨骼名解析成一次性索引（枪口、抛壳点 `shell/shell_N`、各配件挂点、激光束、弹膛/弹匣可视骨骼、默认/战术护木、弹匣等级），运行期**零字符串查找、零按名遍历**。
- 可见性走 `BoneState.visible`，动画数据完全不参与静态路径。

### A3. 几何捕获：整枪一次捕获、按 RenderType 合并

- `DisplayGunGeometry.capture()` 把**枪体 + 全部配件 + 变体/转接口 + LOD 选择**写进同一个 `GeometryCollector`，每类材质一个 buffer，`RenderType.entityCutout(texture)` + 一个 `emissive(texture)`。
- 因此单枪 draw 数 ≈ **不同纹理/材质数**，而不是子模型数；这是它和 CGC 当前「每 flush 一组」最本质的差别。
- 几何键 `GeometryKey = resourceRevision + displayId + 配件集合(type/id/variant/adapter) + 枪膛有弹 + 弹匣有弹 + useLod`。

### A4. GPU 侧：B1（持久 VBO）与 B2（世界空间合批）都做了

|机制|类|要点|
|---|---|---|
|**B1 物品静态网格**|`StaticMeshRenderer` / `StaticMeshCache`|键 = (appearance, `ItemDisplayContext`)；持久 VBO；128 条 / 256 MB 上限；空闲 300 s 回收；命中后按材质 bind→draw|
|**B2 世界网格合批**|`WorldMeshGroup` / `SectionMeshBatches`|按 **16³ section + RenderType** 合批，带 `LightSlice`；`patchLight` 只补光不重建；在 `RenderLevelStageEvent` 里**每帧统一绘制一次**，按材质顺序 + 距离排序；策略 `INSTANCE`/`SECTION`；300 s / 128 条 / 512 MB|
|上传/捕获预算|`MeshUploadQueue` / `MeshPreparationPolicy`|按帧配额（捕获次数、捕获纳秒、上传字节、驻留字节），**避免首帧/换枪帧的尖峰**；不是后台线程，是分帧摊平|
|光照与几何解耦|`MeshIntegerAttributes`（推断）|光/overlay 走整数属性在绘制期设置，改光不重新烘焙几何|

### A5. 失效与回退（这是最值得抄的部分）

- 渲染缓存以 `ClientIndexManager.resourceRevision()` 作世代号，资源重载整体失效。
- 展示槽位 `DisplayGunSlot` 把 **`revision`（变换）与 `appearanceRevision`（外观/几何）拆开**：旋转只更新实例矩阵，不重捕几何。
- 槽位变化靠 `consumeChange()` → `group.markDirty()` 推送，不做每帧比对。
- 接管范围很窄：`THIRD_PERSON_RIGHT_HAND` / `GROUND` / `FIXED`（且不在 GUI/预览/屏幕内）；配件不受支持、模型未就绪、驱动不支持实例属性 → **回退旧路径**，不是半成品状态。

### A6. LOD（D 档）

- 物品：`RenderDistance.inRenderHighPolyModelDistance`；展示方块：新增 `GUN_LOD_RENDER_DISTANCE`（0 = 恒用远景模型）。
- 配套补了一批 LOD 模型资源；LOD 也进入几何键，切换远近不串缓存。

### A7. 资源预热

- `ClientAssetLoadDispatcher`：单线程后台加载器 + 世代失效；`preloadExternalModels()` 在后台完成烘焙与网格准备，渲染时直接读结果、不阻塞。

> 注意：`26.35.11.1` 的 9 个展示框是**原版物品展示框**（FIXED 上下文），所以 0.039 ms/枪 实测验证的是 **B1 这一条**；B2（section 合批）服务于 1.2.0 新增的挂板/展示柜，目前没有性能数据支撑。

---

## 二、渲染的其余改动（**不是**优化项，别误记）

|改动|性质|
|---|---|
|`UnshadedVertexConsumer`（发光部件在 entity shader 下保持全亮）|视觉修正|
|所有渲染入口改为显式传 `bufferSource`；新增 `tacz.oculus.mixins.json` + `UnflushableWrapperAccessor`|Iris/Oculus 模板 flush 正确性|
|`ScopeLensRenderer` / `ScopeThermalRenderer` / 测距 / 弹量数显|新特性，不是省成本|
|枪口火/烟改 SBM snowstorm 粒子；第三人称枪口粒子|新表现|
|瞄具 comb 分支合并、镜片畸变/色散/暗角|重构 + 新特性|

---

## 三、非渲染优化（changelog 里那几条，逐一核实过）

1. **枪包同步**：`GunPackSync` GZIP 压缩 + 256 KB 分片，客户端上限 64 MB —— 解决「枪包多进不了世界」。
2. **JEI 初始化**：`AttachmentQueryData` 一次遍历同时产出「配件→枪 / 枪→配件 / 枪→弹药」三向查询，替代多次全量扫描，并打点耗时日志。
3. **背包弹量统计**：`ClientAmmoCount` 缓存（枪 / epoch / 时间窗三重失效），顺带修掉 minigun 全自动异常。
4. **客户端资源后台预热**（A7）。
5. **计时统一 `GunTime`**：`System.nanoTime` 换 epoch 时钟，暂停不推进 —— 一致性修正，不是性能。
6. **缺失 display 警告去重**（`WARNED_MISSING_GUN_DISPLAYS`），避免刷屏。
7. 动画约束取绝对值、Player Animator 2.x 适配、脚本异常兜底 —— 兼容/健壮性。

---

## 四、与 CGC 方案的重合、交叉与上限

| 档位 | TaCZ 1.2.0 | CGC PLAN | 关系 |
|---|---|---|---|
| **B1 静态烘焙 + 每实例矩阵** | 已实现（整枪捕获 + 持久 VBO，含分帧上传预算） | 计划第一步（估 0.85–0.95 ms / 9 把） | **重合**，TaCZ 是同一思路的完整参考实现 |
| **B2 跨实例合并** | 已实现（仅展示方块，section + RenderType 合批，每帧一次绘制） | 计划第三步（估 0.25–0.35 ms / 9 把） | **重合**，但 TaCZ 没把它用到物品框上 |
| **第一人称（动画 + 脏子树 + 瞄具模板重放）** | **零改动** | 计划第二步（满配 1.951 → 0.45–0.60 ms） | CGC **独有**，是真正的上限增量 |
| **C 动态实例化** | 无（静态路径假设无动画） | 备选 | CGC 范围更大 |
| **D LOD** | 已上线（物品 + 展示方块） | 备选、未做 | TaCZ 已验证 D 只是乘数收益 |
| **26.2+ 延迟渲染** | 无该平台，SBM v2 也无适配 | 方案硬约束、以 26.3 验证 | CGC **独有**，TaCZ 无答案 |

### 静态场景：上限不比 TaCZ 高，且 CGC 的 B1 估值偏保守

- TaCZ 实测 9 个展示框 **+0.355 ms 总量（0.039 ms/枪）**；CGC 当前 4.843 ms（0.538 ms/枪），差 **13.6×**。
- CGC 的 B2 目标 0.25–0.35 ms 总量（0.028–0.039 ms/枪）与 TaCZ 实测**同一地板**——说明 B2 的目标值定得准。
- 但 CGC 的 B1 估值（0.09 ms/枪 = 9 draw）是按**当前即时管线的 flush 粒度**推的；TaCZ 的实测（39 µs/枪）意味着烘焙后单枪只剩**约 4 次 draw 量级**，因为它把整枪 + 配件按材质合并了。
    - 推论：**B1-a 里加一步「单枪按材质合并」，B1 就能提前接近 B2 的 draw 地板**，B2 的剩余收益主要变成「跨实例共享同一纹理/网格」而非「9→1」。

### 交叉点：两者都把「光」从几何里拆出去

- TaCZ：物品路径每帧传 `packedLight` 绘制；世界路径用 `LightSlice` + `patchLight`，改光不重捕。
- CGC 的新成本模型（`C_draw` / `C_state` / `C_upload` / `C_vertex` / `C_bake`）**没有把 light 列为失效维度**。展示框场景实测 `State changes = 0` 掩盖了这一点；一旦放到会变光的世界里（昼夜、火把、手持光源），「光照进顶点」的烘焙会反复重建。建议在 B1 设计时就定成「几何不含光，光在绘制期施加」。

### TaCZ 方案里**不要照抄**的三点

1. **无光影探测**：VBO 路径不做 Iris/Oculus 判定，直接自绘；CGC 计划要求的「有光影退回 `putBulkData`」双路径更稳妥（SBM 里没找到 shader 检测，此项为字节码级判断）。
2. **静态路径不画功能节点**：`DisplayGunGeometry` 只走 `renderBoneTree`，没有瞄具玻璃/模板窗口/激光束/枪口火焰；CGC 的验收标准要求瞄具模板从烘焙网格**重放**，比 TaCZ 完整，也就比它贵。
3. **没有第一人称**：TaCZ 把静态路径做成「另一份不可变模型」，代价是彻底放弃动画；CGC 的 B1-b 要处理 root 矩阵 + 脏子树，不能沿用「静态模型副本」这条路。

---

## 五、结论

1. **PLAN 里「无需参考其实现」对性能结论成立**：TaCZ 1.2.0 的全部收益在静态展示，第一人称与 1.1.8 等价，没有可抄的性能数字。
2. **但对「实现参考」不成立**：TaCZ 已经跑通了 CGC 的 B1+B2 同款设计，是唯一有公开落地与实测数据（0.039 ms/枪）的证据。它的几何键组成、appearance/transform 双 revision、按帧上传预算、空闲回收上限、材质顺序绘制、回退矩阵，都直接对应 CGC「测量与验收」「风险登记」里的空缺项。
3. **CGC 上限确实更高，但增量不在静态场景**：第一人称（1.951 → 0.45–0.60 ms）与 26.2+ 延迟渲染是 TaCZ 完全没有覆盖的两块；静态场景两者收敛到同一 draw/材质地板。
4. **最值得回写进 PLAN 的一条**：B1-a 应包含「单枪按材质合并成少量 draw」，否则 B1 的 9 draw 地板是自我设限，会把 B2 的必要性估高。

**未验证项**：B2（section 合批）没有任何实测；SBM 的 `MeshIntegerAttributes` 光照解耦是字节码推断；TaCZ 静态路径在光影下的实际表现未测。

# 

```
把研究到差异作为后续可以进行验证的点，但不作为已经确定的实现方案
- 像TACZ公开版的Issue 791提到SBM借鉴了其他实现并且用相同文件夹的问题，TaCZ开发团队的实现可能并非原创且隐藏隐患，不能作为实现参考
- 且TaCZ实际开发仓库并不公开（多个提交的Issue号跟开源仓库对不上），缺少文档和缺陷分析
- 不更改CGCbenchmark已测得的数值，例如最初TaCZ+加速渲染却没有帧数提升，可能存在各种问题
- 本次更新文档应该能使后续重构时能同时考虑到差异点，并通过benchmark和光影场景实测来验证
```

# 

已按「只登记差异、不作实现依据」的口径落文档。

**新增** [reference-tacz-1.2.0.md](docs/architecture/client/render/refactor/reference-tacz-1.2.0.md)：

- **证据边界一节**先立门槛：公开 Issue #791 中维护者承认的是 SBM **粒子部分**参考 ParticleStorm 并误用同一文件夹（我核对过原文，不能扩大到网格/VBO 部分，文档里明确写了这一点）；开发仓库不公开、Issue 号对不上、无文档与缺陷分析；公开版与加速渲染同装无收益；SBM 2.6.2 只有编译产物。
- **差异表 11 项**（机制级）：模型运行时、静态捕获、持久 VBO、世界合批、**光在顶点 vs 绘制期**、失效键、上传配额、LOD、第一人称无改动、26.2 无适配、非渲染项。仅标注「与 B1/B2/D 重合」或「不重合」，不写「应当采用」。
- **V1–V9 待验证点**：每条一个待答问题 + 验证手段 + 归属阶段。例如 V1（单枪按材质合并后 draw 能否从 9 降到 2–4）、V2（光移出烘焙的收益）、V6（光影双路径是否必需）、V7（静态路径放弃瞄具玻璃/模板的验收清单）。
- **数据边界**：`26.35.11.1/-2/-3` 全部数值原样引用；0.039 ms/枪 只覆盖物品展示框、不覆盖新展示方块的 section 合批；跨端不可相减。

**联动改动（各一行）**：[PLAN.md:53](docs/architecture/client/render/PLAN.md:53) 结论 1 保留「无需参考其实现」，补上不公开/无缺陷分析/加速渲染无收益的理由与指针；[TODO.md](docs/architecture/client/render/TODO.md) 加「外部参照验证 V1–V9，随阶段验证、不改路线与已测数值」；[refactor/Home.md](docs/architecture/client/render/refactor/Home.md) 文档导航加一行。

未改动：B1→B2 路线、任何 benchmark 数值、方案文档的估算值。

# 
---

读`./docs/architecture/client/render/PLAN.md`和`26.35.11.1-4/测试数据计算报告`，现在需要对比TaCZ渲染，排查剩下0.067ms的常数项在哪
- 可以对比TaCZ1.2.0对1.1.8的更新
- 预计按照26.35.11.1-8进行测试，不开光影

# 

## 两条路径的逐帧差

我在对比 TaCZ 1.2.0（`D:/Github/TACZ`，tag `1.2.0-forge-mc1.20.1`）和 CGC 的静态展示路径后，发现了两边的逐帧差：

**TaCZ 1.2.0**：`GunItemRendererWrapper.renderByItem` → `ExternalGunItemVboRenderer.tryRender` → SBM v2 的 `StaticMeshRenderer.tryDraw`。每个展示实例每帧只做：若干次 record 分配，外加 `hasBulletInBarrel`、`getCurrentAmmoCount` 两次 NBT 读取；2～3 次 map 查询；然后对每个材质依次执行 shader 绑定、整数属性设置、draw。**它不遍历模型树，也不重新解析配件。**

**CGC**：`GunItemRenderer.renderByItem` → `_GunModelRender.render`，每帧对 7 类配件重新执行 `getAttachment` 和 `getBuiltinAttachment`，并做资源索引查询（代码里还留着 TODO「渲染时每帧都这么拼命地读NBT？」）→ `ModelObject.renderBaked`，对整棵节点树运行 `renderStatus`，并逐节点计算 18 个字段的指纹 → 按 `Key` 查 LRU → 每网格 1 次 draw。

TaCZ 1.1.8→1.2.0 的 diff 也印证了这一点：1.2.0 新增的 `DisplayGunGeometry`、`ExternalGunAppearance`、`StaticMeshCache` 全都把「解析外观、可见性、材质」的时机从每帧移到了捕获期，运行时只剩下外观键比较。

## 已有数据可以确定的量级

从 `b1-a-5` 的 9 把满配 F12 原始数据（n=4，很稳）：

|项|每帧|折算|
|---|---:|---:|
|`World-item submit`|1.186–1.229 ms|—|
|`Data refresh`|**0.043–0.046 ms**|4.9 µs/提交|
|`endBatch draw`|0.616–0.644 ms|7.6–8.0 µs/draw|
|`Draw calls`|81|—|
|`Nodes captured`|1512|168 节点/提交（ak47 是 108，`26.35.11.1-1` 的 972/9）|

裸枪 9 把 34 µs/枪里，draw 占约 18 µs，`Data refresh` 按满配折算量级在 3–5 µs，剩下的 8–12 µs 就是**唯一还没有探针的那段状态段**（`renderBaked` 里的逐节点 `renderStatus` + 指纹）。7.4 µs/枪的缺口正好落在这两项上，而这两项 TaCZ 都不做。

## 已做的改动（未提交）

- `ClientRenderProfiler`：新增 `STATIC_STATUS = 27`（`PROBE_COUNT` 27→28，名称表加 `Static status`）
- `ModelObject.renderBaked`：把状态段包进该探针（只有 F12 开启时多两次 `nanoTime`，AF 帧时间不受影响）
- 文档：探针表登记在 [chain/Home.md](docs/architecture/client/render/chain/Home.md:161)，待办写进 [TODO.md](docs/architecture/client/render/TODO.md:69)
- `./gradlew :core:compileJava` 通过

## -8 的判读方式

同一份 F12 输出里 `Static status / 9` 和 `Data refresh / 9` 两个数就能决定归因：如果状态段在 4–6 µs/枪，那 0.067 ms 基本就是它，方向是「同一帧同一个模型只算一次指纹」（9 个展示框共享同一个 `ModelObject`，现在算了 9 遍）；如果两项都不大，就把 `World-item submit − Data refresh − Static status − draw` 的余量当作 Key/LRU/矩阵/getBuffer 的杂项。

另外提醒一个口径问题：`26.35.11.1-4` 的 0.866 ms 空展示框基线**只在 CGC 端测过**，而 CGC 端多装了 `cgccpackloader` / `cgccshader`，所以 0.067 ms 里可能有非 CGC 成分——这也是我在下一步框里问的那个基线要不要两端都测的原因。

# 

```
测试跑好了，baseline也有
另外需要比较下这个差距是否刚好是b1-c-a这个待做优化
```

# 

## 帧时间（n=4，TaCZ 有枪 n=3：`tacz1-multi (2)` 只有第 2 步）

|场景|CGC|TaCZ|差|
|---|---:|---:|---:|
|9 个空展示框|0.8591 ± 0.026|0.8470 ± 0.032|+0.012 ms（0.6σ，噪声内）|
|9 把 ak47 展示框|1.1893 ± 0.040|1.1357 ± 0.013|**+0.054 ms**|
|每枪（同批基线相减）|36.7 µs|32.1 µs|**+4.6 µs**|

基线两端相等，所以这次不用再折算跨端基线。**0.067 ms 是 `26.35.11.1-4` 的偏差，原因是那批 0.866 ms 只测了 CGC 端。** 真实差是 0.041–0.054 ms（用原始值相减是 0.054，用同批基线相减是 0.041），每枪 4.6–6.0 µs，约 2.5σ。

## CGC 每枪的 31.8 µs 已全部归因（新增 `Static status` 探针，n=4）

|项|每帧|每提交|
|---|---:|---:|
|`World-item submit`|0.286 ms|31.8 µs|
|**`Static status`（状态段）**|**0.125 ms**|**13.9 µs（43%）**|
|`Data refresh`|0.0265|2.9 µs|
|2 次 draw（`endBatch draw` 中静态部分）|≈0.140|≈15.6 µs|

13.9 + 2.9 + 15.6 = 32.3 ≈ 31.8 ✓，说明这个算术结果支持「每枪 2 次 draw」；并且和 `26.35.11.1-4` 的实测结果一致。

## 结论：这 4.6 µs 不是 B1-c-A

- 本场景 `Light patches = 0`、`Bake rebuilds = 0`、`Bake upload bytes = 0`、`Distinct RenderTypes = 1`，说明**没有任何光照工作在执行**，A 没有东西可省。A 省下的只是「一次光照变化 ≈0.6 ms」，而在稳定状态下，它反而会让每次 draw 多绑定一个 attribute。
- `lighting-decoupling.md` 里 A 的 4.8 µs 是「ak47 一次光照变化时填充 4 B/顶点光照数组」的 CPU 成本，属于**事件成本**而非每帧成本；它和 4.6 µs/枪 的数值接近纯属巧合。
- 更根本的原因在于：**没有任何单个项的值等于 4.6 µs**。CGC 侧能测到的最小项是 `Data refresh` 的 2.9 µs，而状态段一项就是差的 3 倍。因此这个差其实是「两种相当量级的每实例开销之差」；它并不是 CGC 里某个固定常数，TaCZ 自己也在每帧执行 `ExternalGunAppearance.resolve` 和逐 part 的整数属性设置。
- 唯一能在结构上对上的差别是**每枪 draw 数**：TaCZ 的捕获期按材质合并（`GeometryCollector.snapshot()` 丢弃空 pass），裸 ak47 只有 1 次 draw；CGC 按模型对象逐个 draw，结果是 2 次。但 CGC 的非 draw 部分已经有 16.8 µs/枪，要么是 TaCZ 那次 draw 比 CGC 贵一倍，要么是两端正好持平；**不测 TaCZ 内部无法再细分**，继续追这 4.6 µs 的性价比很低。

## 值得做的是状态段

`Static status` **13.9 µs/提交 = 0.125 ms/帧，约占整帧 10%**，比整个与 TaCZ 的差大 3 倍。9 个展示框共享同一个 `ModelObject`，同一帧内状态完全相同，指纹却被算了 9 遍；其中 `Functional nodes` 只占 2.8 µs/枪，剩下 11 µs 是纯遍历 + 18 字段哈希。按「模型 + 帧」去重、同时对含 functional 节点的子树仍每次执行回调，理论上能省下约 0.10 ms/帧，CGC 会明显快于 TaCZ。

另外，第 2 步的帧时间结果是 CGC 1.398 ms、TaCZ 2.048 ms，CGC 快 0.65 ms；这部分是 B1-b 第一人称烘焙的收益，和静态展示无关。结论已写进 [TODO.md](docs/architecture/client/render/TODO.md:69)，`-8` 的批次报告和 `analyze.py` 还没有生成。

# 

```
引入这个状态段去重对于展示框都是不同枪械的场景是否有优化？如果没有优化还会引入额外常数开销就不加入（见B2否决原因）
补报告
```

# 

## 状态段去重：对「展示框放不同枪」没有优化，因此不加入

- 去重的键只能是共享的 `ModelObject`。9 个展示框**放不同枪**时对应 9 个不同模型，**命中率为 0**，收益为 0。
- 引入的额外开销是：每次提交多一次帧序号比较，这部分可忽略；真正的负担在语义上：functional 节点的委托是**逐提交登记**的，所以去重时不能跳过含回调的子树，只能跳过纯几何子树。这样一来，分支、缓存失效的判定规则以及 addon 重新挂回调的时机都变得复杂，而这些正是 B2 被否决的那类风险。
- 这和 B2 的否决原因属于同一类情形：**收益只在「同一把枪重复展示」时成立**。按同一套标准，不加入。
- 附带说明：不同枪的场景里状态段仍是 13.89 µs/枪 = 0.125 ms/帧（约占整帧 8%）。要优化它只能更换成本模型，比如用动画写入时维护的 revision 替代逐节点哈希，或者只在姿态类节点上计算指纹；那是另一项设计，不在这一轮。

## -8 报告已生成

新增 [analyze.py](docs/architecture/client/render/benchmark/26.35.11.1-8/analyze.py)（运行 `python analyze.py` 会重新计算全部数值并生成图）和 [测试数据计算报告.md](docs/architecture/client/render/benchmark/26.35.11.1-8/测试数据计算报告.md)，图是 [测试数据总览-1-帧时间.png](docs/architecture/client/render/benchmark/26.35.11.1-8/测试数据总览-1-帧时间.png)。

写报告时发现两处需要修正之前口语结论的地方：

1. **差比上一轮说的更不显著**。用同批基线相减后是 4.74 ± 3.29 µs/枪（**1.4σ**），只有有枪时原始差 0.0545 ± 0.0215 ms 才达到 2.5σ。因为每端基线自身也有噪声，四项标准误合起来就把差值推到噪声范围内。报告里两个口径都列了。
2. **`Static status` 占每提交 43.7%**，纯遍历 + 18 字段哈希是 11.1 µs/提交（≈103 ns/节点，108 个节点）。整体构成是「闭合」的：`World-item submit` 31.78 = 状态段 13.89 + 数据刷新 2.94 + 余量 14.94 µs（2 次 draw + 缓存键/矩阵/getBuffer），没有剩下无法解释的部分。

文档同步：benchmark 的批次表、各批次分工、[TODO](docs/architecture/client/render/TODO.md:69)（含否决理由）、[reference-tacz-1.2.0.md](docs/architecture/client/render/refactor/reference-tacz-1.2.0.md) 的 V1 都更新了。

工作区里 `26.35.11.1-6/测试数据计算报告.md` 和 `docs/deprecated/prompt/#96.md` 有改动，那些不是本次改的，我没有动。
