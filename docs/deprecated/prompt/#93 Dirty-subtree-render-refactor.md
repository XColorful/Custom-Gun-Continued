> #93 提示词

# 

```
读`./docs/architecture/client/render/PLAN.md`，完成B1-b脏子树重烘
- 可以根据需要将任务拆解给多Agent执行
```

# 

读取计划文档，了解 B1-b 的具体内容。

清楚 B1-b 的目标了：第一人称 root 变换提升为绘制矩阵 + 脏子树重烘。先定位现有实现。

设计已经清楚。开始实现：先落地脏跟踪与分块几何的基础设施。

现在实现 `ModelObject` 的第一人称分块烘焙。

Now the render dispatch and the chunked path.

实现完成并通过编译。现在补充文档。

实现已通过编译。让一个审查代理独立检查这段新逻辑的正确性。

B1-b 脏子树重烘已落地（1.20.1），编译通过，未提交。

## 实现要点

**分块拓扑（[ModelObject.java](core/src/main/java/dev/xcolorful/customgun/client/model/ModelObject.java)）**
- 顶层节点是块根，自身变换只进绘制矩阵——动画写根（bob / 后坐 / 跳跃）零重烘。
- 状态段逐节点比较 `stateHash()`，某节点首次变脏时在它所在的块上切开，之后只重烘它自己的子树；静态部分留在剩余大块里，稳态 0 重建。
- 块网格不含块根自身的变换与可见性，所以块根的状态变化全程免费；可见性 / 被替换渲染由绘制时的祖先链剪枝处理，被剪枝的块标脏、恢复可见时重建一次。

**单节点退回**
- `FunctionalBedrockPart` 在状态段按节点统计回调写入的顶点，命中的节点（内置只有 `MAG_ADDITIONAL`）整棵子树改为 `BufferSource` 即时绘制，换弹帧不再整模型退回即时管线。

**瞄具模板**
- [`_AttachmentModelRender.renderModelPart`](core/src/main/java/dev/xcolorful/customgun/client/model/_AttachmentModelRender.java:232) 把路径前缀提升为绘制矩阵，枪身骨骼动画不再让瞄具网格逐帧失效。

**退回与生命周期**
- 无着色器时用 `StaticBake.available()` 在状态段之前退回（回调不会执行两次）；分块网格挂在模型上、由 `StaticBakeCache.clearAll()` 统一释放，跨光照（光影阴影 pass）每块保留最多 4 份。

## 文档
新增 [b1b-first-person-bake.md](docs/architecture/client/render/refactor/b1b-first-person-bake.md)（分块规则、失效条件、探针语义、已知限制、人工验收清单）；[TODO.md](docs/architecture/client/render/TODO.md) 阶段 2 已勾选实现项，留下「验收采集」未做；refactor/Home.md 导航已补。

## 还没做
- 实机验收与采集（画面 / 换弹 / 瞄具 / 开光影、`Bake rebuilds`、`Draw calls`、帧时间），这需要你在游戏里跑。
- 一个独立审查代理仍在后台检查新逻辑的正确性，结果回来后我会处理它发现的问题。

审查代理确认主路径没有确信缺陷，只提了几个条件性风险。其中两个值得顺手修掉：即时节点重复登记委托、分块释放绕过线程保护。

审查结果已处理完：主路径没有找到确信的漏画 / 重画 / 旧姿态缺陷，只补了两处条件性风险。

## 审查后追加的修改

- **即时节点重复登记委托**：`cgc$drawImmediate` 重放回调时会再登记一次委托（状态段已经登记过）；现在重放后按标记裁掉重复项。内置的 `MAG_ADDITIONAL` 只写顶点不受影响，这是为第三方 addon 兜底。
- **分块释放的线程保护**：`StaticBakeCache.clearAll()` 释放模型分块网格时，非渲染线程改走 `recordRenderCall`，与同文件静态网格的释放路径一致。

审查确认的两点保留为已知限制，已写进文档：

- 光影下半透明枪：分块直接绘制与延迟回放的委托相对顺序与即时管线不同（与 B1-a 同类风险，首次作用于枪体本身）。
- `MODEL_CHUNKS` 强引用与静态网格缓存同生命周期，随 `clearAll()` 释放。

## 当前状态

- 改动全部未提交（`ModelObject` / `BedrockPart` / `FunctionalBedrockPart` / `_AttachmentModelRender` / `StaticBake` / `StaticBakeCache` + 新增 `BakeDirtyTracker`）。
- 文档：新增 [b1b-first-person-bake.md](docs/architecture/client/render/refactor/b1b-first-person-bake.md)，[TODO.md](docs/architecture/client/render/TODO.md) 已勾选阶段 2 的实现项，refactor/Home.md 与 b1a 文档已补链接。
- 另注意到工作区里多了一个已暂存的 `docs/architecture/client/render/benchmark/26.35.11.1-5/标准.txt`（不是我创建的），看起来是下一批验收的测试标准，我没有动它。

下一步是实机验收：静止 / 换弹、裸配 / 满配、开 / 关光影，重点看 `Bake rebuilds` 稳态是否为 0、换弹时是否只剩脏子树的烘焙、以及半透明枪与瞄具模板的画面是否与 `ENABLED = false` 一致。

# 

```
26.35.11.1-5数据测好了，现在写报告
```

# 

先看这一批的数据与现有报告格式。

数据齐了。写报告。

报告已写好：[26.35.11.1-5/测试数据计算报告.md](docs/architecture/client/render/benchmark/26.35.11.1-5/测试数据计算报告.md)，同目录新增 `analyze.py`（`python analyze.py` 可复现全部数值）。

## 结论

**B1-b 性能验收通过，两个场景目标都达到：**

| 场景 | 帧时间（前 → 后） | CGC（前 → 后） | 1% low |
|---|---:|---:|---:|
| ak47 | 1.380 → **0.991 ms**（−28%） | 0.584 → **0.202**（−65%） | 365 → 470 |
| m4a1 裸配 | 1.707 → **1.004 ms**（−41%） | 0.911 → **0.214**（−76%） | 350 → 473 |
| m4a1 满配 | 2.995 → **1.330 ms**（−56%） | 2.199 → **0.540**（−75%） | 189 → **415** |

目标：裸配 ≤1.2 ms / 满配 ≤1.6 ms，CGC ≤0.30 / ≤0.65。

**换弹段（F12）**：CGC 0.319 / 0.291 / 0.570（较 B1-b 前 −48% / −66% / −71%）；`Geometry traverse` 完全无命中、`Upload bytes` 389 KB → 24–44 KB；`Bake rebuilds` 整段为 0——3 次换弹只在第一次切开骨架时烘了几帧，之后纯矩阵。

**新瓶颈**：满配剩余 CGC 0.570 = draw（约 35 次 CGC draw，≈0.27 ms）+ 瞄具模板 0.189 + delegate 0.185。块切得越多 draw 越多，已作为「待评估」写进 TODO（空闲块回并 / 按 RenderType 合并）。

## 同步更新的文档

- [benchmark/Home.md](docs/architecture/client/render/benchmark/Home.md)：新增本批索引与分工，并记录「B1-b 后 `Distinct/Dirty nodes` 只统计即时块」的口径变化
- [TODO.md](docs/architecture/client/render/TODO.md)：阶段 2 标记完成、补待评估项与仍缺的开光影验收
- [refactor/Home.md](docs/architecture/client/render/refactor/Home.md)：第一人称分阶段表已回填实测列
- [b1b-first-person-bake.md](docs/architecture/client/render/refactor/b1b-first-person-bake.md)：状态改为性能验收通过

注：每个 Afterburner 文件只有 4 条记录（标准的 7 步里换弹那 3 步走 F12），报告第 7 节已说明；所有改动仍未提交。
