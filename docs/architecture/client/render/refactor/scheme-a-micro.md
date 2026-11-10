# 方案 A：即时管线内的微优化

> 保留现有「每帧遍历场景图、逐顶点写入 `VertexConsumer`」的结构，只降低成本系数。它是 [重构方案总览](./Home.md) 里的阶段 1，不是终点：无论做得多好，成本仍然随「顶点数 × 提交次数」线性增长。

## 定位

|项|内容|
|---|---|
|解决的大头|几何遍历（0.2890 ms，57.0%）里的一部分、`endBatch`（0.1063 ms，21.0%）的重复调用|
|不能解决|顶点总量与提交次数本身；展示框每把枪仍要完整生成一遍|
|成本模型|不变：`Σ(顶点 × 提交)`|
|预期|单次提交 0.507 → **0.30–0.40 ms**（推算）；multi 4.15 → **2.4–2.8 ms**|
|风险|低；不改提交模型，不碰光影|

## 清单

按「证据强度 × 与后续方案是否冲突」排序。**与烘焙/缓存冲突的项（会在 B 阶段被整段替换）标为「过渡」**，只在不推迟 B 的前提下做。

|#|项|位置|预期|性质|
|---|---|---|---|---|
|A1|合并子模型的 `endBatch`：枪体、配件、文字、手臂各一次 → 每次提交一次|`ModelObject.java:243-248`、`TextRender`、`HandRender`|实测标准持枪 3 次、multi 9 次；合并后按纹理批次走|过渡，B2 后消失|
|A2|去掉 1.20.1 上的双重节点快照：`animatedTransforms` + `cleanedTransforms` 是为 26.2 延迟渲染准备的，1.20.1 渲染期节点不会被清空|`ModelObject.java:206-226`、`:261-264`|实测 0.0102 ms/提交（multi 9 把合计 0.064）|确定性收益|
|A3|顶点热路径去分配：每面 `new Vector3f`、每顶点 `new Vector4f`，改为手写矩阵元素乘法或复用栈上变量|`BedrockCubeBox.java:94-107`、`BedrockCubePerFace.java:90-104`|实测每枪分配仅 0.10 MB，逃逸分析已消掉大部分，收益低于原估计|与 B 冲突（B 不再逐顶点生成）|
|A4|delegate 渲染器去分配：`Matrix3f` / `Matrix4f` / `PoseStack` / 闭包每帧新建|`AttachmentRender.java:109-120`、`MuzzleFlashRender`、`TextRender`|delegate 0.0790 ms 的一部分；分配同样不是主因|与 B 冲突|
|A5|`delegateRenderers` 用 `clear()` 复用列表，不再每帧 `new ArrayList<>()`|`ModelObject.java:258`|O(1)，减少 GC|保留|
|A6|每个子模型重复 `getBuffer(renderType)`；同一纹理应复用同一个 `VertexConsumer`|`ModelObject.java:195-197`、各 delegate|减少桶查找与重复批|保留|
|A7|`RenderType.energySwirl` 每帧新建实例（未 memoize）；改为缓存|`MuzzleFlashRender.java:166`|每帧少一次分配与一个批|保留|
|A8|数据刷新每帧读 NBT / 建 `ItemStack`|`_GunModelRender.java:70-106`|0.0030 ms，占比 0.5%|低优先，B 阶段顺手做|

## 为什么封顶在 0.3 ms 左右

遍历的 0.2890 ms 对应 446 立方体 / 10704 顶点，约 **27 ns/顶点**（推算）。每个顶点的工作是「一次 4×4 矩阵乘 + 一次 `VertexConsumer.vertex` 写入」，理论下限在 10–15 ns 量级；但实测每枪分配只有 0.10 MB，说明临时向量已被逃逸分析消掉，去分配收益有限。乐观估计遍历能到 0.15–0.20 ms。

`endBatch` 合并后仍要按纹理批次各 draw 一次：实测单次 draw 约 10 µs，标准持枪 3 次约 0.03 ms，multi 9 次约 0.09 ms；这部分不会消失。

因此 A 的收益是**线性系数**，multi 场景 9 把枪仍会落在 2.4–2.8 ms，离 1.0 ms 的目标仍有 1.4 ms 以上差距。

## 与后续阶段的关系

- A1 / A5 / A6 / A7 是**结构清理**，B 阶段仍需要（B 也要按纹理批次提交），可以先做。
- A2 是 1.20.1 的确定性浪费，先做。
- A3 / A4 会被 B 整段替换，**不建议投入**；实测分配量只有 0.10 MB/枪，即使不换方案，这两项的收益也低于原估计。如果 B 的可行性验证失败，再回头做。

这也是 [总览](./Home.md#推荐路线) 把 A 定义为「阶段 1、只做廉价部分」的原因：它买的是迭代时间，不是目标。

## 验收

- 探针：`Geometry traverse`、`endBatch flush`、`Delegate render`、`Nodes captured`、`endBatch calls`。
- 判据：`endBatch calls` 降到 1–2；`Node snapshot` 在 1.20.1 上为 0；单次提交总耗时下降 ≥30%，且渲染结果与改动前逐帧一致（含瞄具模板、附加弹匣、配件替换）。
