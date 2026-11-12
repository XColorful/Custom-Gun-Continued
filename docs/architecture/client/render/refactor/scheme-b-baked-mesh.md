# 方案 B：几何烘焙与静态复用

> 这是唯一能直接打掉「每帧重新生成全部顶点」（0.2890 ms，57.0%）的方案。做法是：把场景图生成的顶点保存成持久网格，之后每帧只做「绑定 + 用矩阵绘制」，不再逐顶点做变换。
>
> 分两层：**B1 模型空间烘焙 + 每实例矩阵**（覆盖所有场景），**B2 静态实例世界空间合并**（把 multi 的「每枪一次上传」压成「每纹理一次 draw」）。两层可以独立验收。

## 定位

|项|内容|
|---|---|
|解决的大头|几何遍历 0.2890 ms（标准）/ 0.353 ms（每把展示框）、以及上传 0.063–0.077 ms/枪|
|不能解决|draw 数本身（B1 每实例仍需按纹理各一次，约 10 µs/次）；动态内容（文字、手臂、枪口火焰、抛壳）|
|成本模型|新：`Σ(draw × 次数) + Σ(动态顶点)`|
|预期|B1：0.461–0.507 → **约 0.03 ms/枪**（draw 0.026 + 少量状态）；B1 后 multi ≈1.15–1.2 ms，B2 后 ≈0.95–1.0 ms（推算）|
|风险|光影接管下的自绘、缓存失效、显存占用|

**与优化前的等价关系**：优化前的即时管线每帧都要把全部顶点重新变换并提交，成本 ≈「每帧全量重烘」。两者都付「逐顶点生成 + 上传」，差别只在 `BufferSource` 分批、状态快照等外围开销。所以**「每帧全烘」不是优化，它就是基线**——B1 的收益全部来自「缓存命中的部分连顶点生成都不做」，一个顶点都不生成才是收益来源。

### 实测依据（第二批）

- 静态展示每帧 `State changes = 0`，而第一人称是 0.98–2.00：展示框的节点状态确实不变，B 的缓存命中条件成立。
- 每枪上传 388 KB、上传耗时 0.063 ms；每枪 draw 约 2 次、合计 0.026 ms；每枪分配仅 0.101 MB。
- 第一人称每帧只有 12 条动画通道在写节点；root 的 bob / 后坐由 `GunRendererAddon` 直接写，适合提升为绘制矩阵。

## 烘焙什么

一次烘焙产出一份 `VertexBuffer`（`Usage.STATIC`），内容等价于当前 `shouldRender` 遍历写进 `BufferBuilder` 的全部顶点：

- 顶点格式与 `RenderType` 一致（`DefaultVertexFormat.NEW_ENTITY`，`Mode.QUADS`）；
- 每个「纹理 + 渲染状态」一份网格：枪体一份，每个配件模型各一份；
- 烘焙时把当前节点变换、`visible`、配件替换结果全部烘进顶点，`light` / `overlay` 也在顶点里（`BedrockCubeBox.java:107-109`）。

1.20.1 的链路（原版 API 已具备，见 [可行性验证](#可行性验证)）：

```java
BufferBuilder b = Tesselator.getInstance().getBuilder();
b.begin(renderType.mode(), renderType.format());
for (BedrockPart part : shouldRender) {
    part.render(identityPose, ctx, b, light, overlay, 1, 1, 1, 1);
}
VertexBuffer vb = new VertexBuffer(VertexBuffer.Usage.STATIC);
vb.upload(b.end());
```

绘制：

```java
PoseStack mv = RenderSystem.getModelViewStack();
mv.pushPose();
mv.mulPoseMatrix(instanceMatrix);
RenderSystem.applyModelViewMatrix();

renderType.setupRenderState();
vb.bind();
vb.drawWithShader(RenderSystem.getModelViewMatrix(),
                  RenderSystem.getProjectionMatrix(),
                  RenderSystem.getShader());
renderType.clearRenderState();

mv.popPose();
RenderSystem.applyModelViewMatrix();
```

`VertexBuffer.drawWithShader` 显式接收模型视图矩阵；原版 `BufferUploader` 的即时绘制也是同一套调用。因此**不需要把实例矩阵应用到顶点上**，这正是 B1 相对「缓存字节再回放」的关键优势。

### 低风险回退：`putBulkData`

Forge 的 `BufferBuilder.putBulkData(ByteBuffer)` 可以整块回放预烘焙顶点，仍走现有 `BufferSource`。代价是每帧仍复制 + 上传（约 30–40 µs），但省掉逐顶点变换与分配，且完全不碰 `RenderType` / shader。若 B1 在光影下被卡住，可先退到这条路径。

## 缓存键

缓存必须以「模型实例 + 当前渲染状态」为键，**不能包含世界矩阵**（矩阵是每实例的绘制参数）。最小键直接沿用运行时真正会变的节点字段（`NodeTransform` 覆盖的字段集，`NodeTransform.java:9-41`）：

|层|内容|
|---|---|
|模型身份（加载期固定）|`ModelObject` 引用、纹理、`RenderType` 实例、LOD 变体、`display` 缩放|
|提交期状态|全部节点的 `offset(3) + quaternion(4) + scale(3) + visible(1)` 指纹；配件 `attachmentLocation` 集合；`currentMagazineCategory`；`renderMount`；`packedLight`；`overlay`|

108 节点 × 11 个 float 的累加哈希约 1–3 µs/submit（推算），相对 0.31 ms 的遍历可以忽略。

**共享实例约束**：同一把枪的所有展示框共享一个 `GunModelObject`，而 `_GunModelRender` 每次提交都会改写 `currentGunItem` / `currentAttachmentItem`（`_GunModelRender.java:62-106`）。因此烘焙结果不能挂在「当前配件」上，必须键化保存；同款 9 把展示框因此天然共享同一份 B1 网格。

## 失效条件

|触发|处置|
|---|---|
|节点变换指纹变化|重烘该网格（第一人称每帧都会命中，见下）|
|`visible` 变化（含 `additionalMagazineNode`、功能节点）|重烘|
|配件集合 / 弹匣类别 / 瞄具 `showMount` 变化|重烘|
|`light` / `overlay` 变化|重烘（光照已烘进顶点）|
|纹理或 LOD 变体变化|换网格|
|资源重载 / `resetCache`|丢弃全部|
|实例的 `display` 缩放非均匀|缩放参与键，否则法线偏移|

漏判的后果是画错而不是崩溃：配件或弹匣残留、展示框亮度不更新、换弹时附加弹匣不出现。因此**必须提供强制重建开关**（沿用 `DebugKey`）用于人工验收。

### 功能节点不能整段跳过

`FunctionalBedrockPart.render` 会先执行 functional 回调，回调可能直接绘制（手臂、枪口火焰、抛壳、配件）并修改 `visible`（`FunctionalBedrockPart.java:62-90`）。缓存只能跳过「普通 cube 的顶点发射」，所以 `BedrockPart.render` 需要拆成两段：

1. **状态段**：递归执行 functional 回调、算出替换/可见性结果（每帧仍执行）；
2. **几何段**：普通 cube 走烘焙网格，只有「本次被替换或状态变了」的子树才即时生成。

功能节点本身的耗时只有约 0.003 ms/枪（实测 0.026 ms / 9 把），拆段不会抵消烘焙收益。

## 第一人称：分区烘焙

第一人称的节点每帧都被动画改写，整枪重烘没有收益。可行的切分：

- 渲染前由 `GunRendererAddon` / `GunItemRenderer` 写在 **root 节点**上的 bob / 后坐偏移，提升为**绘制矩阵**（root-local 空间），不参与失效；
- 动画真正改写的骨骼（枪机、拉栓、扳机、弹匣、手臂）对应子树标脏，每帧只重写这些子树；
- 静态子树保留烘焙网格，与脏子树共用同一个 root 矩阵绘制。

成本变成「脏子树顶点 + 每个纹理 1–2 次 draw」。若脏子树的顶点量占全枪 30%，第一人称的遍历可从 0.303 ms 降到约 0.09 ms，加上 draw 与状态，总提交约 **0.10–0.15 ms**（推算，是否达到 ≤0.10 ms 取决于实际动画覆盖的节点比例，**待测**）。

实测（[`b1-a-7`](../benchmark/26.35.11.1-4/b1-a-7/测试数据计算报告.md)、[`b1-a-8`](../benchmark/26.35.11.1-4/b1-a-8/测试数据计算报告.md)，换弹 = 动态场景上界）：脏子树按**节点**计的 p99 覆盖为 ak47 53%、m4a1 裸配 22%、m4a1 满配 8.8%；按**顶点**计为 44.0% / 20.7% / 9.3%，两者相差不到 3.3 个百分点——动画骨骼的 cube 密度接近模型平均，节点口径可以作为代理。换弹峰值下满配仍有 **91%** 的遍历可跳过，遍历残留约 0.08 ms。

### 美术制作约束

以下由新成本模型（`Σ(脏子树顶点) × C_vertex + draw`）直接推出，可以直接作为模型与动画的制作准则：

1. **动画骨骼放在层级低处、子树要小。** 重烘焙按「脏节点为根的整棵子树」走；静态装饰（贴片、导轨、瞄具座、枪托外壳）不要挂在动画骨骼下面。
2. **动画骨骼上少放 cube。** 成本按顶点计。不可见面剔除这类制作期优化会直接乘在**每一次**重烘焙上，比 LOD 更有价值。
3. **整枪的呼吸 / 晃动放在根节点。** 根变换由绘制矩阵承担，免费；同样的动画放在根的子骨骼上会让整个模型每帧变脏。
   - 层级写成 `root → bone → gun` 时，动画驱动 `bone` 等于驱动整把枪，`gun` 整棵重烘；整枪级运动必须写在 `root`（`parent == null`），因为只有根变换会被提升为绘制矩阵。
   - 如果模型不得不保留一条「无 cube 的单子节点链」，B1-b 可以考虑把这种变换链一并折进矩阵；当前按根节点处理，其余祖先动画都会计入脏子树。
4. **高频动画（开火、换弹）只驱动叶子骨骼。** 不要让整个枪身骨骼参与轻微晃动，那等于把静态部分都拖进脏子树。
5. **可见性切换只用于小子树。** 节点的 `visible` 改变会让该节点变脏并重烘焙整棵子树；隐藏期间本身不产生额外成本。
6. **替换渲染只用于小节点。** functional 回调直接发射几何的节点会让整个模型退回即时管线（B1-b 要改成单节点退回）。
7. **材质与纹理尽量合并。** B2 的下限是「全场景不同 `RenderType` 数」，新纹理 / 新渲染状态各加一次 draw。
8. **LOD 在新成本模型下没有收益**（顶点不再主变量），除非用于内存或画质。

## B2：静态实例的世界空间合并

展示框里的枪满足「节点姿态不变 + 世界矩阵不变」两个条件（`renderByItem` 不调用 `stateMachine.update`，也不 `clean`）。因此可以：

1. 在网格烘焙阶段直接把世界矩阵乘进顶点，得到世界空间网格；
2. 按「纹理 + 光照」把所有静态实例合并进同一个 `VertexBuffer`；
3. 每帧只做一次 `bind + draw`，**没有上传、没有矩阵、没有遍历**。

9 把同款枪因此从 9 次提交 / 9 次 flush 变成「每个纹理 1 次 draw」。这是 multi 场景逼近 1.0 ms 目标的关键一步。

失效与生命周期：

- 键：展示框实体（或掉落物实体）→ 所属合并缓冲；
- 内容变化（换枪、换配件、拆框）、光照变化、区块卸载 / 重载 → 标记重建；
- 内存：一把枪约 0.38 MB，9 把合并后约 3.5 MB；需要一个上限与 LRU，`VertexBuffer.close()` 必须在渲染线程调用。

地面物品不满足「世界矩阵不变」（原版物品实体会旋转 / 浮动），只能走 B1 的每实例矩阵。

## 26.2+ 的对应实现

26.3 的延迟渲染**没有**「提交现成网格」的入口：`OrderedSubmitNodeCollector` 的所有 submit 方法都不接收 `MeshData` / `GpuBuffer`，`submitCustomGeometry` 每帧仍执行一次 lambda。可行路径是：

- 用 `RegisterFeatureRenderersEvent` 注册自定义 `FeatureRenderer`，在 execute 阶段拿 `RenderPass`；
- 用 `GpuDevice.createBuffer(..., ByteBuffer)` 持久化网格，`PreparedRenderType.drawFromBuffer(...)` + `RenderPass.drawIndexed` 绘制；
- 模板改由 `DepthStencilState` 携带（26.3 已无逐枪 `glClear`）。

约束：标准 entity 管线把模型矩阵烘进了顶点坐标，**换矩阵只能靠 shader 消费 `DynamicTransforms` 的管线，或自建 per-instance 管线**。因此 26.3 上 B2（世界空间、静态）比 B1（每实例矩阵）更稳；第一人称的分区烘焙在 26.3 上需要额外的管线工作。**未验证**。

## 可行性验证

|项|结论|依据|
|---|---|---|
|1.20.1 烘焙顶点到持久 VBO|可行|`BufferBuilder.end() → RenderedBuffer`、`VertexBuffer.upload`、构造器 public|
|1.20.1 每实例矩阵绘制|可行|`VertexBuffer.drawWithShader(modelView, projection, shader)` 显式接收矩阵|
|绕开 `BufferSource`|可行，但排序语义变化|半透明枪不再进入全局源的排序|
|模板窗口|不受影响|模板是裸 GL 状态，与绘制方式无关；瞄具遮罩仍需每帧即时画|
|Oculus / Iris|**未验证，且风险最高**|光影下 `endBatch` 只是入队、由 Oculus 在其管线里稍后执行；自绘会立刻画进当前 pass，可能绕过光影的材质 / 阴影，甚至画到错误的 pass|
|26.3 预烘焙|有条件可行|需自定义 FeatureRenderer；无现成 MeshData 提交入口|

## 风险

|风险|影响|缓解|
|---|---|---|
|光影接管 `RenderType` 与缓冲源|自绘可能绕过光影材质 / 阴影 pass，或画进错误的 pass|**双路径**：检测到光影时退回 `putBulkData`（仍走 `BufferSource`），无光影时走持久 VBO；实机 A/B 对比|
|半透明排序|透明枪的混合顺序改变|透明枪不走合并缓冲，保持单独提交|
|非均匀缩放|法线偏差|缩放纳入缓存键，或在烘焙时归一化法线|
|光照烘焙|换光源需重烘|`light` 纳入指纹；只在值变化时重烘|
|显存|每配置 0.38 MB|LRU + 上限；同款枪共享一份|
|失效遗漏|画面错误|强制重建开关 + 验收清单|

## 分步落地

1. **B1-a**：只覆盖展示框 / 地面 / 第三人称（节点静态），第一人称保持现状。验证缓存正确性与光影表现。
2. **B1-b**：第一人称分区烘焙（静态子树 + 脏子树）。
3. **B2**：展示框世界空间合并。
4. 每一步单独 Commit、单独跑标准 4 步 + multi，并在开 / 关光影下各验收一次。

## 验收

- 探针：`Geometry traverse`（应只剩动态部分）、`Model render total`、新增「烘焙次数 / 烘焙耗时」「上传字节」「draw 数」。
- 计数：`Cubes submitted` 在静态场景的首帧后应降到 0；`endBatch calls` 应降到「纹理批次数」。
- 画面：配件替换、附加弹匣、瞄具模板、透明枪、展示框亮度、掉落物旋转逐项对照。

## 待测项

|项目|手段|现状|
|---|---|---|
|单次 draw（不含上传）耗时|拆分 `endBatch`|待测|
|Oculus 下自绘是否进入光影|实机 A/B|未验证|
|第一人称动画覆盖的节点比例|按动画通道统计|待测|
|烘焙耗时与显存占用|新增探针|待测|
|26.3 自定义 FeatureRenderer 的管线映射|原型|未验证|
