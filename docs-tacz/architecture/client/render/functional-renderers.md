# 功能性渲染器

> 枪械模型上动态注册的特殊渲染逻辑

功能性渲染器是实现 `IFunctionalRenderer` 接口的类，通过 `BedrockGunModel.setFunctionalRenderer(nodeName, function)` 绑定到特定模型节点。运行时，`FunctionalBedrockPart.render()` 调用绑定的函数，根据返回值决定是否替换节点的默认几何渲染。

## IFunctionalRenderer 接口

```java
void render(PoseStack poseStack, VertexConsumer vertexBuffer,
            ItemDisplayContext transformType, int light, int overlay);
```

参数均为 Minecraft 标准渲染管线参数。功能性渲染器在 `BedrockGunModel.render()` 的特定阶段被调用，可以返回 null 表示"使用节点默认几何渲染"，或完全不通过 `FunctionalBedrockPart` 注册而直接以 delegate 形式延迟到渲染循环末尾执行。

## 枪口火焰 — MuzzleFlashRender

### 触发与生命周期

- 静态方法 `onShoot()` 记录开火时间戳和随机旋转角度（0-360°），并缓存当前 PoseStack 的 normal/pose 矩阵
- 渲染窗口为 50ms，超时后不渲染
- 使用 `isSelf` 静态字段区分第一人称（true）和第三人称（false），防止在第三人物体上渲染自己的枪口火焰

### 消音器检测

渲染前检查枪口配件是否为消音器：遍历枪械已安装配件的 modifier 数据，查找 `SilenceModifier.ID` 键，若存在且启用消音音效则不渲染枪口火焰。

### 两层渲染

1. **半透明背景层**：缩放至 `0.5 × muzzleFlash.getScale()`（前 25ms 内从 0 缓入），绕 Z 轴旋转随机角度，使用 `entityTranslucent` 渲染类型
2. **发光层**：缩放到背景层的一半，使用 `energySwirl`（加性混合发光）渲染类型

两层均通过 `SlotModel`（全亮度照明的平面模型）渲染，以任意角度旋转确保火焰纹理不总是对齐。

### Delegate 模式

枪口火焰使用 `bedrockModel.delegateRender()` 延迟到模型渲染完成后执行，因为它需要操作独立的 PoseStack 矩阵（而不是与模型共享顶点缓冲）。

## 抛壳动画 — ShellRender

### 抛壳队列

维护 `ConcurrentLinkedDeque<Data>`（上限 128 个弹壳），每个 `Data` 记录：
- `timeStamp`：创建时间戳
- `randomOffset`：随机速度偏移量（在 display 参数范围内随机）
- `normal` / `pose`：缓存的 PoseStack 矩阵（从触发抛壳时的模型姿态捕获）

`addShell(randomVelocity)` 在射击时被调用，生成随机速度偏移后加入队列。

### 物理模拟

每个弹壳的渲染位置满足标准匀变速直线运动公式：

```
位移 = initialVelocity × time + 0.5 × acceleration × time²
```

其中 `initialVelocity`、`acceleration`（含重力）、`angularVelocity`（旋转角速度）均来自 `GunDisplay` 的 `shellEjection` 配置段。渲染时：
1. 根据存活时间清理过期弹壳
2. 为每个幸存弹壳创建独立的 PoseStack，从缓存的 pose/normal 矩阵初始化
3. 应用位移（注意 X/Y 轴取反以匹配坐标系差异）和绕三轴的角速度旋转
4. 使用 `entityCutout` 渲染 `BedrockAmmoModel`（弹壳模型）

### 过期清理

`checkShellQueue(lifeTime)` 递归检查队列头部的弹壳是否超时，超时则弹出并重新检查，确保队列中的弹壳数量不会无限增长。

## 激光束 — BeamRenderer

### 渲染方式

`BeamRenderer` 不是 `IFunctionalRenderer`，而是一个静态工具类。`BedrockGunModel` 构造时找到所有 `laser_beam` / `laser_beam_N` 节点路径，渲染时在这些路径的末端发射激光束。

### 束几何

使用四个四边形构成一个矩形截面的光束：
- 上下两个水平面（宽 × 长）
- 左右两个垂直面（宽 × 长）

顶点带有颜色（激光颜色 RGB 分量）和 UV 坐标。每个四边形由 4 个顶点构成，使用 `POSITION_COLOR_TEX_LIGHTMAP` 顶点格式。

### 渲染状态

- **透明度**：加性混合（`SRC_ALPHA` / `ONE`），产生发光效果
- **深度**：颜色和深度写入均开启
- **剔除**：无面剔除（NO_CULL）
- **纹理**：`beam.png`（从 `InternalAssetLoader` 加载）
- **淡出**：启用时末端 alpha 为 0（渐隐）

### 长度与宽度

|视角|长度来源|宽度来源|
|---|---|---|
|第一人称|`LaserConfig.length`|`LaserConfig.width`|
|第三人称|`LaserConfig.thirdPersonLength`|`LaserConfig.thirdPersonWidth`|

### 加速渲染兼容

`ARCompat`（加速渲染）兼容路径：使用 `NEW_ENTITY` 顶点格式的替代 renderType，确保激光束与枪体在同一格式的同一层，这样模板缓冲才能正确剔除枪身后的激光束。

## 配件渲染 — AttachmentRender

每个 `AttachmentType` 实例化一个 `AttachmentRender`，绑定到对应的 `_pos` 节点。

### 渲染流程

1. 从 `BedrockGunModel.getCurrentAttachmentItem().get(type)` 获取配件 ItemStack
2. 缓存当前 PoseStack 的 normal/pose 矩阵
3. 使用 `delegateRender()` 延迟到渲染循环末尾执行（与枪体共享顶点缓冲）
4. 在 delegate 中：从 `ClientAttachmentIndex` 获取 `BedrockAttachmentModel` 和纹理
5. 检查 LOD 模型（距离判断 + 第一人称绕过）
6. 渲染配件模型

### 缺失处理

若 `ClientAttachmentIndex` 中不存在该配件，渲染 `SlotModel` + 缺失纹理作为调试提示。

## 手臂渲染 — LeftHandRender / RightHandRender

仅在第一人称生效。分别绑定到 `lefthand_pos` 和 `righthand_pos` 节点。

通过 `delegateRender()` 延迟渲染，使用与枪体相同的 RenderType。渲染内容为 Minecraft 玩家手臂模型的对应部分，位置和朝向由定位组节点决定。

改装界面打开时手臂渲染被禁用（`setRenderHand(false)`）。

## 文字覆盖 — TextShowRender

绑定到 `GunDisplay.modelNodeTextDisplay` 中指定的模型节点名称。

渲染时通过 `PapiManager` 评估 Placeholder API 文本占位符（如弹药计数、枪械名称等），按 `TextShow.align`（LEFT / CENTER / RIGHT）计算偏移量后调用 `Font.drawInBatch()` 渲染 3D 文本。

支持的颜色、阴影、缩放和光照等级均在 `GunDisplay` 的 `text_show` 配置段中定义。

## 弹药可见性

枪管子弹（`bullet_in_barrel`）：闭膛待击枪械，有子弹时 `visible=true`。检查逻辑来自枪械数据中的 `BoltType`。

弹匣子弹（`bullet_in_mag`）：有弹药时 `visible=true`。

弹链（`bullet_chain`）：机枪等使用弹链供弹的枪械，有弹药时 `visible=true`。

这三种不是独立的渲染器类，而是通过 `setFunctionalRenderer` 注册的 lambda，仅修改 `bedrockPart.visible` 后返回 null，使用节点自身的默认几何渲染。

## 瞄具可见性

以下节点通过 lambda 函数在渲染时根据配件安装状态切换可见性：

- `mount`：安装了瞄具配件时可见
- `carry`：未安装瞄具时可见
- `sight`：未安装瞄具时可见（机械瞄具）
- `sight_folded`：安装了瞄具时可见（折叠准星）

## 弹匣等级可见性

根据当前扩容弹匣等级（从配件数据读取）切换：

- `mag_standard`：等级为 0 时可见
- `mag_extended_1/2/3`：等级 ≥ 对应等级时可见

## 护木可见性

- `handguard_default`：未安装镭射/握把时可见
- `handguard_tactical`：安装镭射或握把时可见

## Delegate 渲染机制

部分功能性渲染器（枪口火焰、抛壳、配件）使用 delegate 模式而非直接在渲染循环中渲染。原因是它们需要操作独立的 PoseStack 或需要与枪体模型共享顶点缓冲。

`BedrockModel.delegateRender(IFunctionalRenderer)` 将渲染器加入 `delegateRenderers` 列表。在 `BedrockModel.render()` 的末尾：
1. 结束当前 RenderType 的 VertexConsumer batch
2. 遍历 `delegateRenderers`，逐一调用每个渲染器
3. 清空 `delegateRenderers`

这意味着 delegate 渲染器在模型的主体渲染之后、下一帧之前执行。
