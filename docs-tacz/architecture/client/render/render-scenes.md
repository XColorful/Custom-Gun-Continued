# 渲染场景

> 第一人称、第三人称、手臂、GUI、改装界面、tooltip、实体与方块之间的区别

TaCZ 的多种渲染场景共用基岩版模型与动画系统，但入口、是否驱动状态机、施加哪些变换各有不同。本章对比各场景，说明它们从哪里进入、共享什么、差异在哪。

## 场景对比

|场景|入口|状态机|姿态变换|定位组|
|---|---|---|---|---|
|第一人称枪械|simplebedrockmodel `FirstPersonRenderHandler` → `renderFirstPerson`|`update()` 写模型|后坐/跳跃/瞄准/改装/约束|`idle_view` / `iron_view` / `scope_view`|
|第一人称手臂|`LeftHandRender` / `RightHandRender`|间接继承枪械变换|无独立变换|`lefthand_pos` / `righthand_pos`|
|第三人称枪械|`ItemInHandLayer` → `renderByItem`|`visualUpdate()` 只同步|无姿态变换|`thirdperson_hand`|
|第三人称手臂/持枪姿态|`HumanoidModelMixin` → `InnerThirdPersonManager`|无|手臂旋转|无（作用于玩家模型）|
|副手/背上枪械|`ItemInHandLayerMixin` → `HumanoidOffhandRender`|无|固定变换|`offhand_show` / `hotbar_show`|
|GUI 物品槽|`GuiGraphics.renderItem` → `renderByItem`|无|无|`SlotModel` 平面|
|掉落物/展示框|`renderByItem`|无|定位/缩放|`ground` / `fixed`|
|改装界面 3D 枪|复用第一人称路径|`update()`|改装视角定位|`refit_view_<type>`|
|tooltip 图片|`GuiGraphics.renderItem` → BEWLR|无|无|`SlotModel` 平面|
|子弹实体/曳光弹|`EntityBulletRenderer`|无|轨迹旋转|弹药 entity 模型|
|方块实体|`GunSmithTableRenderer` / `StatueRenderer` / `TargetRenderer`|无|朝向/浮动|`BedrockModel`|

## 第一人称

第一人称是最完整的链路：状态机 `update()` 把动画写进模型，随后施加整套姿态变换（瞄准/后坐/跳跃/改装/约束），渲染时枪口火焰、抛壳、手臂等附加模块以 `isSelf` 标志只对本地玩家生效。它是唯一会走状态机「写模型」路径的场景。

## 第三人称

第三人称枪械经原版 `ItemInHandLayer` 进入 `renderByItem`。它不施加第一人称姿态变换，也不把状态机动画写进模型，而是用 `visualUpdate()` 让状态机保持计时和音效同步。持枪的手臂姿态由 `InnerThirdPersonManager` 独立驱动：它根据枪械配置的第三人称动画名，调用 `IThirdPersonAnimation.animateGunHold` / `animateGunAim` 直接摆玩家模型的手臂/身体/头部，或委托 Player Animator 模组。

## 第一人称手臂

第一人称手臂不是独立渲染的，而是枪械模型上的两个功能性渲染器：在 `lefthand_pos` / `righthand_pos` 节点处调用玩家渲染器把手臂画到枪上，继承枪械的全部变换。改装界面打开时手臂渲染被关闭。

## 副手与背上枪械

`HumanoidOffhandRender` 处理两件事：副手持枪，以及把枪械显示在背上（`hotbar_show` 定义各快捷栏槽位对应的显示层）。这是纯静态变换渲染——按 `LayerGunShow` 的位移/旋转/缩放直接 `renderStatic`，完全不经过状态机，也不做相机或定位组变换。

## GUI、tooltip 与掉落物

这些场景都走 `renderByItem` 的 BEWLR 路径，只画 `SlotModel` 平面图标（GUI）或做定位/缩放后画 3D 模型（掉落物 `ground`、展示框 `fixed`）。它们不驱动状态机，也没有附加渲染。tooltip 里的物品图片、改装界面与合成台界面的槽位图标，都经 `GuiGraphics.renderItem` 复用这条路径。

## 改装界面

改装界面本身不画 3D 枪——它不暂停游戏，第一人称枪械继续渲染，只是姿态被 `RefitTransform` 状态改写：镜头在 `refit_view_<type>` 定位组之间插值，聚焦到当前配件槽，并关闭手臂。因此改装界面的 3D 枪本质是第一人称路径的一个变体。

## 子弹实体与方块实体

`EntityBulletRenderer` 渲染子弹实体与曳光弹：子弹模型旋转到轨迹方向，曳光弹用加性混合发光渲染，第一人称下起点取第一人称渲染缓存出的枪口偏移。方块实体渲染器（合成台、雕像、靶子）直接操作 `BedrockModel`，其中雕像会渲染一柄展示枪械（经 BEWLR），靶子的上半节点由实体旋转驱动。
