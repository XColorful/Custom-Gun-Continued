# 功能性渲染器

> 枪械模型上绑定的一组动态渲染逻辑。它们不改变模型的几何数据，而是在渲染主循环的特定阶段替换某个节点的默认绘制，或追加额外的绘制。本文说明这套机制，以及各个具体渲染器解决什么问题。

## 机制：功能性渲染器与节点

`IModelComponentRenderer` 是一个渲染回调接口。模型对象通过 `setFunctionalRenderer(nodeName, function)` 把一个节点绑定到一个渲染逻辑：渲染该节点时，若绑定了功能性渲染器，则由渲染器决定「替换默认几何」还是「返回 null 走默认几何」。绑定在 `AnimatedModelObject` 上，具体到枪械由 `GunModelObject` 在 `_GunLoader.constructFunctionalRenderer()` 里完成。

两类绑定方式：

- 纯可见性控制：回调里只设置节点的 `visible`，然后返回 null，节点用默认几何渲染。用于弹药、弹匣、瞄具、护木等「按状态显隐」的节点。
- 替换渲染：回调返回一个 `IModelComponentRenderer`，接管该节点的渲染。用于枪口火焰、抛壳、配件、手臂、文字等。

## 机制：delegate 渲染

部分功能性渲染器需要独立的 `PoseStack` 或与枪身共享顶点缓冲，不能直接在节点渲染回调里绘制。它们通过 `ModelObject.delegateRender(renderer)` 把自己注册到 `delegateRenderers` 列表，等模型主体渲染结束、当前 RenderType 批次结束之后再逐个执行。

使用 delegate 的渲染器：枪口火焰、抛壳、配件、手臂、文字。它们的特点是在回调里缓存当前 `PoseStack` 的 normal / pose 矩阵，delegate 阶段用缓存的矩阵重建独立栈再绘制。

## 枪口火焰

`MuzzleFlashRender` 绑定到 `muzzle_flash_origin` 节点。开火时 `onShoot()` 记录时间戳和随机旋转角，之后 50ms 内用两层 `SlotModel`（平面模型）渲染：先半透明背景层，再加性混合发光层。开火时间戳由 `GUN_FIRE_EVENT` 写入（见 [枪械附加变换模块](./gun-render-addons.md)）。渲染前检查枪口配件是否抑制火焰（消音器），并只在 `isSelf` 为 true（第一人称或本地玩家自己的枪）时渲染。

## 抛壳

`ShellRender` 绑定到 `shell_origin`（及 `shell_1`、`shell_2` 等）节点。维护一个弹壳队列，每个弹壳记录创建时间戳、随机速度偏移和触发时的模型矩阵。渲染时按匀变速直线运动计算位移、按角速度计算旋转，用 `ClientAmmoIndexInstance` 里的弹壳模型绘制。

弹壳由状态机脚本通过 `popShellFrom(index)` 触发（见 [动画状态机脚本 API](./animation-script-api.md)），触发点对应脚本里「抛壳」的关键帧。队列有上限和存活时间清理，避免无限增长。

## 激光

`BeamRender` 是静态工具类，不是节点渲染器。模型对象在渲染阶段找到所有 `laser_beam` 节点路径，在这些路径末端绘制矩形截面的光束（四个四边形），使用加性混合、无剔除的专用 RenderType。长度、宽度、颜色来自 `_LaserDisplay` 配置，第一 / 第三人称分别取不同的长度宽度。它同时服务于枪械和配件模型。

## 配件渲染

`AttachmentRender` 绑定到各配件槽位的 `_pos` 节点。渲染时读取当前槽位的配件物品，从 `ClientAttachmentIndexInstance` 拿配件模型与纹理，做 LOD 替换后绘制。瞄具配件不走这个通用路径，而是在 `_GunModelRender.renderScope()` 里提前渲染并配合模板测试（见下）。缺失配件时渲染黑紫材质提醒。

## 手臂渲染

`HandRender`（`Left` / `Right`）绑定到 `lefthand_pos` / `righthand_pos` 节点，仅第一人称生效。渲染时委托到模型渲染结束后，在定位组节点决定的位置和朝向上绘制 Minecraft 玩家手臂模型的对应部分。改装界面打开时手臂渲染被关闭（`setRenderHand(false)`）。

## 文字渲染

`TextRender` 绑定到 `GunDisplay.modelNodeTextDisplay` 指定的模型节点。渲染时先经占位符系统解析文本，再在节点位置用 `Font.drawInBatch` 绘制 3D 文本，支持颜色、阴影、缩放、光照等级配置。仅第一人称生效。

## 瞄具模板测试

瞄具（scope / sight）的镜内效果靠 OpenGL 模板缓冲实现。`_AttachmentModelRender` 和 `_GunModelRender.renderScope()` 配合：

1. 先渲染目镜写入模板值，再用模板测试让镜身只在「非目镜区域」绘制，从而在镜片范围外遮盖枪身。
2. 组合镜（scope + sight）与长筒镜（scope）、机械瞄具（sight）走不同的模板分支。
3. 圆形遮罩通过绘制三角形扇写入模板，实现透过镜片看世界的圆形视野。

这套逻辑在配件模型渲染内部完成，是配件模型区别于普通模型的核心。

## 节点可见性

以下节点通过「只改 visible、返回 null」的 lambda 控制显隐，它们与枪械当前状态绑定：

- 弹药：膛内子弹（闭膛待击）、弹匣内子弹、弹链，按是否有弹药显隐。
- 弹匣：标准弹匣与扩容弹匣 L1 / L2 / L3，按当前弹匣类别显隐。
- 瞄具：导轨 `mount`、提把 `carry`、机械瞄具 `sight`、折叠准星 `sight_folded`，按是否安装瞄具显隐。
- 护木：默认护木与战术护木，按是否安装镭射 / 握把显隐。
- 额外弹匣：换弹动画期间临时显示的第二个弹匣。

## 转接口渲染

`_GunLoader.constructAttachmentAdapterNodeRender()` 绑定 `attachment_adapter` 节点：渲染时根据当前已安装配件的 `adapterNodeName` 决定转接口的哪些子节点可见，实现「不同配件挂载点」的切换。

## LOD

枪械与配件都可配置低模。渲染时若在低模范围内（`ClientRenderDistance.shouldRenderLod()`）且存在 LOD 模型，就用低模替换高模。第一人称绕过 LOD。

## 保持物品

`KeepingItemRenderer` 不是功能性渲染器，但服务于拔枪 / 收枪动画：`ItemInHandRendererMixin` 实现它，`cgc$keep(itemStack, timeMs)` 让主手在指定时间内继续持有被切走的物品，使收枪动画能播完而不因物品切换而中断。渲染入口用它读取「当前实际应渲染的物品」（见 [渲染入口与场景](./rendering-entry-points.md)）。
