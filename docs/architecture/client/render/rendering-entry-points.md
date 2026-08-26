# 渲染入口与场景

> 一杆枪的模型对象和状态机都是现成的，问题是 Minecraft 的渲染循环会在哪些时机、以什么方式把控制权交给 CGC。本文说明每个渲染场景的进入点，以及它们如何落到同一个 BEWLR 渲染器上。

## 物品与渲染器的绑定

枪械、配件、弹药三类物品的渲染都走同一个机制：核心层定义了物品类（`GunItem` / `AttachmentItem` / `AmmoItem`），平台层通过 Mixin 让它们实现 `IAnimateGeoItem` 接口。该接口的 `cgc$getCustomRenderer()` 返回一个懒加载的 BEWLR 渲染器（`GunItemRenderer` / `AttachmentItemRenderer` / `AmmoItemRenderer`）。

因此，无论渲染场景是什么，只要拿到了 `ItemStack`，就能通过 `IAnimateGeoItem.cgc$getCustomRenderer(stack)` 拿到它的渲染器。这个接口是各场景统一调度的枢纽。

## 第一人称

第一人称持枪不经过原版的手持物品渲染路径，而是由事件接管：

1. `ItemInHandRendererMixin` 在 `renderHandsWithItems` 头部投递 `BeforeRenderHandEvent`（供摄像机动画消费）。
2. 平台层监听原版 `RenderHandEvent`，包装成 CGC 的 `RENDER_HAND_EVENT`。
3. `FirstPersonRender` 处理 `RENDER_HAND_EVENT`，从事件拿到的物品栈取出渲染器，调用 `renderFirstPerson()`，并取消原版渲染（`setCanceled(true)`）。

副手如果拿着枪，直接取消副手渲染，避免与原版副手物品渲染冲突。

第一人称链路与摄像机 / FOV 的处理详见 [主渲染链路](./render-pipeline.md)。

## 第三人称

第三人称分两部分：

- 主手 / 副手正常持有的枪械：走原版 `ItemRenderer.renderStatic` → BEWLR 的 `renderByItem(THIRD_PERSON_RIGHT_HAND)`，由 `GunItemRenderer.renderByItem()` 应用第三人称定位组变换后渲染。`ItemInHandLayerMixin` 在主手持枪时取消左臂渲染，防止重复。
- 副手枪械与热键栏包围显示：`ItemInHandLayerMixin` 在 `ItemInHandLayer.render` 尾部调用 `HumanoidOffhandRender.renderGun()`，读取 display 中的 offhand / hotbar 包围显示配置，逐个渲染到实体身上。

## GUI、掉落物、展示框

这三个场景由原版 `ItemRenderer.renderStatic` 直接驱动，最终进入 BEWLR 的 `renderByItem()`：

- GUI 场景（含物品栏图标、tooltip 里的 3D 模型）：`GunItemRenderer.renderByItem()` 看到 `ItemDisplayContext.GUI` 时改为渲染扁平的槽位图标纹理（`slotTextureLocation`），不渲染 3D 模型。
- 掉落物（`GROUND`）与展示框（`FIXED`）：应用对应定位组的位移 / 旋转 / 缩放后渲染 3D 模型。
- 第一人称左右手、第三人称副手在这些场景里被显式跳过，交给上面的专用路径处理。

## 逐帧驱动

动画状态机不是只在渲染时更新，还有两处持续驱动：

- `_LocalAnimHandler` 在客户端 tick 时根据玩家移动状态（待机 / 走路 / 冲刺 / 潜行）向状态机投递 `idle` / `walk` / `run` 信号。
- `_LocalAnimHandler` 在渲染帧且非第一人称时，对当前物品做 `needReInit` 检查并在需要时重新初始化状态机，再调用 `visualUpdate()`（只更新状态和声音，不写模型）。

第一人称的逐帧更新发生在 `renderFirstPerson()` 内部，由渲染器自身在渲染前调用状态机 `update()`。

## 场景汇总

|场景|入口|落到哪个渲染器|备注|
|---|---|---|---|
|第一人称主手|`RENDER_HAND_EVENT` → `FirstPersonRender`|`GunItemRenderer.renderFirstPerson()`|取消原版渲染|
|第一人称副手|`FirstPersonRender` 直接取消|无|副手持枪不渲染|
|第三人称主手|原版 `renderStatic` → `renderByItem`|`GunItemRenderer.renderByItem()`|应用第三人称定位组|
|第三人称副手 / 热键栏|`HumanoidOffhandRender`|`renderStatic(FIXED)`|包围显示|
|GUI / tooltip|原版 `renderStatic` → `renderByItem`|`GunItemRenderer.renderByItem()`|槽位图标|
|掉落物 / 展示框|原版 `renderStatic` → `renderByItem`|`GunItemRenderer.renderByItem()`|定位组变换|
|逐 tick 信号|`_LocalAnimHandler`|状态机 `trigger()`|待机 / 走路 / 冲刺|
|逐帧动画|`_LocalAnimHandler` / 渲染器|状态机 `update()` / `visualUpdate()`|非第一人称与第一人称|
