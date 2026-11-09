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
