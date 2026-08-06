# 渲染体系移植 TODO

> 以 [TaCZ 渲染体系总览](/docs-tacz/architecture/client/render/Home.md) 的 Mermaid 子图为单位，列出所有待移植类/包目录

已移植的类同样列在表格中展示其 CGC 对应位置。已弃用（`_Deprecated_`）的类不列入待移植清单。

## 移植顺序

- [x] [1. 几何 POJO 层](#1) — 所有渲染器的基础数据结构
- [x] [2. 动画 POJO 层](#2) — 动画数据的解析入口
- [x] [3. Display POJO 层](#3) — 显示配置的数据结构
- [x] [4. 资源加载与索引层](#4) — 资源管道和 Instance 构建
- [ ] [5. 几何运行时层](#5) — 将 POJO 数据转换为可渲染的场景图
- [ ] [6. 模型对象层](#6) — 场景图之上构建分类型的模型对象（枪械/配件/弹药），注册动画监听器和功能性渲染器
- [ ] [7. 模型监听器层](#7) — 动画数据写入场景图的桥梁
- [ ] [8. 动画 API 层](#8) — 动画实例、控制器、状态机、glTF 支持
- [ ] [9. 动画实现层](#9) — 枪械专用的状态上下文和第三人称管理器
- [ ] [10. PAPI 层](#10) — 模型文字覆盖的占位符系统
- [ ] [11. 功能性渲染器](#11) — 枪口火焰、抛壳、激光束、配件渲染、手臂渲染
- [ ] [12. 物品渲染器](#12) — BEWLR 渲染器（AnimateGeoItemRenderer 及其枪械子类）
- [ ] [13. 实体/方块渲染器](#13) — EntityBulletRenderer、StatueRenderer、TargetRenderer
- [ ] [14. 客户端事件层](#14) — 第一人称渲染事件、相机事件、动画 Tick
- [x] [15. Mixin 层](#15) — 对 MC 渲染管线的注入点
- [ ] [16. 其他 API](#16) — 模型类型管理、第三人称动画接口

---

## 1
## 1. 几何 POJO 层 — client.resource.pojo.model

对应文档：[TaCZ 客户端资源 POJO](/docs-tacz/architecture/client/render/client-resource-pojos.md) 模型 POJO 章节。

此层为基岩版模型的 JSON 数据结构定义，已全部移植。

|TaCZ `com.tacz.guns.client.resource.pojo.model`|CGC `client.resource.assets.model`|
|---|---|
|`BedrockModelPOJO`|`BedrockModel`|
|`BedrockVersion`|_Deprecated_|
|`BonesItem`|`bedrock.geometry._Bone`|
|`CubesItem`|`bedrock.geometry.bone._Cube`|
|`Description`|`bedrock.geometry._Description`|
|`GeometryModelLegacy`|_Deprecated_（由 format_version 统一）|
|`GeometryModelNew`|`bedrock._GeometryModel`|
|`FaceItem`|（合并至 `_FaceUv`）|
|`FaceUVsItem`|`bedrock.geometry.bone.cube._FaceUv`|

[↑ 回到移植顺序](#移植顺序)

---

## 2
## 2. 动画 POJO 层 — client.resource.pojo.animation

对应文档：[TaCZ 客户端资源 POJO](/docs-tacz/architecture/client/render/client-resource-pojos.md) 动画 POJO 章节。

此层为基岩版动画的 JSON 数据结构定义，已全部移植。

|TaCZ `com.tacz.guns.client.resource.pojo.animation`|CGC `client.resource.assets.animation`|
|---|---|
|`bedrock.BedrockAnimationFile`|`BedrockAnimation`|
|`bedrock.BedrockAnimation`|`bedrock._Animation`|
|`bedrock.AnimationBone`|`bedrock.animation._Bone`|
|`bedrock.AnimationKeyframes` / `Keyframe`|`bedrock.animation.bone._KeyFrame`|
|`bedrock.SoundEffectKeyframes`|`bedrock.animation._SoundEffects`|
|`gltf.*`（Accessor / Animation / Buffer / Node 等）|（待确定——目前为 `GltfAnimation` 占位 POJO）|

|TaCZ `com.tacz.guns.client.resource.serialize`|CGC 对应|
|---|---|
|`AnimationKeyframesSerializer`|`ClientJsonUtils.readKeyFrames`|
|`SoundEffectKeyframesSerializer`|`_SoundEffects.fromJsonReader`|
|`Vector3fSerializer`|_Deprecated_|
|`ItemStackSerializer`||

[↑ 回到移植顺序](#移植顺序)

---

## 3
## 3. Display POJO 层 — client.resource.pojo.display

对应文档：[TaCZ 客户端资源 POJO](/docs-tacz/architecture/client/render/client-resource-pojos.md) Display 数据章节。

此层为所有 Display 数据的 POJO 定义，已全部移植。

|TaCZ `com.tacz.guns.client.resource.pojo.display`|CGC `client.resource.assets.display`|
|---|---|
|`IDisplay`|_Deprecated_|
|`gun.GunDisplay`|`GunDisplay`|
|`gun.GunLod`|`_LodDisplay`|
|`gun.GunTransform`|（合并至 `_ModelTransform`）|
|`gun.MuzzleFlash`|`gun._MuzzleFlashDisplay`|
|`gun.ShellEjection`|`gun._ShellEjectionParam`|
|`gun.GunAmmo`|`gun._AmmoDisplayOverride`|
|`gun.TextShow` / `Align`|`_ModelNodeTextDisplay`|
|`gun.LayerGunShow`|`gun._SurroundDisplay`|
|`gun.ControllableData`|`gun._ControllableData`|
|`gun.AmmoCountStyle`|`core.api.item.gun.AmmoCountType`|
|`gun.DamageStyle`|`client.api.item.gun.DamageDisplayType`|
|`gun.DefaultAnimationType`|_Deprecated_|
|`ammo.AmmoDisplay`|`AmmoDisplay`|
|`ammo.AmmoEntityDisplay`|`ammo._AmmoEntityDisplay`|
|`ammo.ShellDisplay`|`ammo._ShellDisplay`|
|`ammo.AmmoParticle`|`ammo._AmmoParticle`|
|`ammo.AmmoTransform`|（合并至 `_ModelTransform`）|
|`attachment.AttachmentDisplay`|`AttachmentDisplay`|
|`attachment.AttachmentLod`|`_LodDisplay`|
|`block.BlockDisplay`|`BlockDisplay`|
|`LaserConfig`|`_LaserDisplay`|
|`TransformScale`|`_ModelTransformScale`|
|`CommonTransformObject`|_Deprecated_|
|`PackInfo`|`info.GunpackInfo`|

[↑ 回到移植顺序](#移植顺序)

---

## 4
## 4. 资源加载与索引层 — client.resource

对应文档：[TaCZ 渲染体系 Home](/docs-tacz/architecture/client/render/Home.md) 资源加载层 + 数据索引层。

此层为 Resource Manager 调度和 Instance 索引构建。

|TaCZ `com.tacz.guns.client.resource`|CGC `client.resource`|状态|
|---|---|---|
|`ClientAssetsManager`|`_AllAssetsManager`|已完成|
|`ClientAssetLoadDispatcher`|_Deprecated_|已完成|
|`ClientIndexManager`|`_AssetsInstanceManager`|已完成|
|`GunDisplayInstance`|`instance.assets.GunDisplayInstance`|已完成|
|`InternalAssetLoader`|_Deprecated_|已完成|
|`index.ClientGunIndex`|`instance.data.ClientGunIndexInstance`|已完成|
|`index.ClientAttachmentIndex`|`instance.data.ClientAttachmentIndexInstance`|已完成|
|`index.ClientAmmoIndex`|`instance.data.ClientAmmoIndexInstance`|已完成|
|`index.ClientBlockIndex`|`instance.data.ClientBlockIndexInstance`|已完成|
|`index.ClientAttachmentSkinIndex`|（待确定）||

|TaCZ `com.tacz.guns.client.resource.manager`|CGC `client.resource.assets`|状态|
|---|---|---|
|`DisplayManager`|`DisplayManager`（含 Gun / Attachment / Ammo / Block 子类）|已完成|
|`PackInfoManager`|`GunpackInfoManager`|已完成|
|`GltfManager`|`AnimationManager.GltfAnimationManager`|已完成|

[↑ 回到移植顺序](#移植顺序)

---

## 5
## 5. 几何运行时层 — client.model.bedrock

对应文档：[TaCZ 基岩版模型与几何系统](/docs-tacz/architecture/client/render/bedrock-model-geometry.md) 场景图章节。

此层为基岩版模型的**运行时**场景图类（BedrockPart / BedrockCube 等），负责构建坐标转换后的节点树并执行渲染。

|TaCZ `com.tacz.guns.client.model.bedrock`|CGC 目标|状态|
|---|---|---|
|`BedrockModel`|（待移植——基岩版场景图构建与渲染根类）||
|`BedrockPart`|（待移植——场景图节点）||
|`BedrockCube` (interface)|（待移植——立方体面几何接口）||
|`BedrockCubeBox`|（待移植——统一 UV 立方体）||
|`BedrockCubePerFace`|（待移植——逐面 UV 立方体）||
|`BedrockPolygon`|（待移植——单个面四边形）||
|`BedrockVertex`|（待移植——顶点数据）||
|`ModelRendererWrapper`|（待移植——BedrockPart 动画属性包装器）||

> **注意**：此层是 POJO 到运行时模型的桥梁。CGC 的 `client.model.ModelObject` 及其子类在概念上对应 TaCZ 的 `BedrockAnimatedModel` 体系（见第 6 节），但 `BedrockModel` 的场景图构建逻辑（`loadNewModel` / `loadLegacyModel` / 坐标转换）尚未移植到 CGC。

[↑ 回到移植顺序](#移植顺序)

---

## 6
## 6. 模型对象层 — client.model

对应文档：[TaCZ 基岩版模型与几何系统](/docs-tacz/architecture/client/render/bedrock-model-geometry.md) 模型类型层次章节。

此层为模型对象层次（动画模型、枪械模型、配件模型、弹药模型），负责动画监听器供应和功能性渲染器注册。

|TaCZ `com.tacz.guns.client.model`|CGC `client.model`|状态|
|---|---|---|
|`BedrockAnimatedModel`|`AnimatedModelObject`|已完成（占位）|
|`BedrockGunModel`|`GunModelObject`|已完成（占位）|
|`BedrockAttachmentModel`|`AttachmentModelObject`|已完成（占位）|
|`BedrockAmmoModel`|`AmmoModelObject`|已完成（占位）|
|`FunctionalBedrockPart`|（待确定）||
|`SlotModel`|（待确定）||
|`GunModelConstant`|（待确定——节点名称常量）||
|`IFunctionalRenderer`|`api.model.IModelComponentRenderer`|已完成|

> **注意**：CGC 的 `*ModelObject` 类目前为占位骨架（仅构造 + `fromPojo` 工厂），尚未移植实际的场景图构建逻辑（来自 `BedrockModel`）、动画监听器供应（来自 `BedrockAnimatedModel`）、瞄具渲染（来自 `BedrockAttachmentModel`）、功能性渲染器注册（来自 `BedrockGunModel`）。

[↑ 回到移植顺序](#移植顺序)

---

## 7
## 7. 模型监听器层 — client.model.listener

对应文档：[TaCZ 动画系统](/docs-tacz/architecture/client/render/animation-system.md) AnimationListener 章节。

此层为动画监听器的具体实现，负责将动画关键帧数据写入模型部件。

|TaCZ `com.tacz.guns.client.model.listener`|CGC 目标|状态|
|---|---|---|
|`camera.CameraAnimationObject`|（待确定）||
|`camera.CameraRotateListener`|（待确定）||
|`constraint.ConstraintObject`|（待确定）||
|`constraint.ConstraintRotateListener`|（待确定）||
|`constraint.ConstraintTranslateListener`|（待确定）||
|`model.ModelRotateListener`|（待确定）||
|`model.ModelScaleListener`|（待确定）||
|`model.ModelTranslateListener`|（待确定）||
|`model.ModelAdditionalMagazineListener`|（待确定）||

[↑ 回到移植顺序](#移植顺序)

---

## 8
## 8. PAPI 层 — client.model.papi

对应文档：[TaCZ 功能性渲染器](/docs-tacz/architecture/client/render/functional-renderers.md) 文字覆盖章节。

|TaCZ `com.tacz.guns.client.model.papi`|CGC 目标|状态|
|---|---|---|
|`PapiManager`|（待确定）||
|`AmmoCountPapi`|（待确定）||
|`PlayerNamePapi`|（待确定）||

[↑ 回到移植顺序](#移植顺序)

---

## 9
## 9. 功能性渲染器 — client.model.functional

对应文档：[TaCZ 功能性渲染器](/docs-tacz/architecture/client/render/functional-renderers.md)。

|TaCZ `com.tacz.guns.client.model.functional`|CGC 目标|状态|
|---|---|---|
|`MuzzleFlashRender`|`renderer.model.MuzzleFlashRender`|已完成（占位）|
|`ShellRender`|`renderer.model.ShellRender`|已完成（占位）|
|`AttachmentRender`|（待确定）||
|`BeamRenderer`|（待确定）||
|`LeftHandRender`|（待确定）||
|`RightHandRender`|（待确定）||
|`TextShowRender`|（待确定）||

[↑ 回到移植顺序](#移植顺序)

---

## 10
## 10. 动画 API 层 — api.client.animation

对应文档：[TaCZ 动画系统](/docs-tacz/architecture/client/render/animation-system.md)。

CGC 中 `ObjectAnimation` 和 `ObjectAnimationSoundChannel` 为占位骨架。

|TaCZ `com.tacz.guns.api.client.animation`|CGC `client.api.animation`|状态|
|---|---|---|
|`ObjectAnimation`|`ObjectAnimation`|已完成（占位）|
|`ObjectAnimationChannel`|（待确定）||
|`ObjectAnimationRunner`|（待确定）||
|`ObjectAnimationSoundChannel`|`ObjectAnimationSoundChannel`|已完成（占位）|
|`AnimationController`|（待确定）||
|`AnimationPlan`|（待确定）||
|`Animations`|（待确定）||
|`AnimationListener`|（待确定）||
|`AnimationListenerSupplier`|（待确定）||
|`AnimationChannelContent`|（待确定）||
|`AnimationSoundChannelContent`|（待确定）||
|`DiscreteTrackArray`|（待确定）||

|TaCZ `com.tacz.guns.api.client.animation.interpolator`|CGC 目标|状态|
|---|---|---|
|`Interpolator`|（待确定）||
|`InterpolatorUtil`|（待确定）||
|`Linear`|（待确定）||
|`Step`|（待确定）||
|`Spline`|（待确定）||
|`SLerp`|（待确定）||
|`CustomInterpolator`|（待确定）||

|TaCZ `com.tacz.guns.api.client.animation.gltf`|CGC 目标|状态|
|---|---|---|
|`AnimationStructure`|（待确定）||
|`AnimationModel`|（待确定）||
|`AccessorModel` / `BufferModel` / `NodeModel` 等|（待确定）||
|`accessor.*`（AccessorData / AccessorFloatData 等）|（待确定）||
|`ElementType` / `GltfConstants` / `Buffers` / `NumberArrays`|（待确定）||

|TaCZ `com.tacz.guns.api.client.animation.statemachine`|CGC 目标|状态|
|---|---|---|
|`AnimationStateMachine`|（待确定）||
|`AnimationState`|（待确定）||
|`AnimationStateContext`|（待确定）||
|`AnimationConstant`|（待确定）||
|`LuaAnimationStateMachine`|（待确定）||
|`LuaAnimationState`|（待确定）||
|`LuaStateMachineFactory`|（待确定）||
|`TrackArrayMismatchException`|（待确定）||

[↑ 回到移植顺序](#移植顺序)

---

## 11
## 11. 动画实现层 — client.animation

对应文档：[TaCZ 动画系统](/docs-tacz/architecture/client/render/animation-system.md) 触发条件与状态上下文章节。

|TaCZ `com.tacz.guns.client.animation`|CGC `client.animation`|状态|
|---|---|---|
|`statemachine.GunAnimationConstant`|`statemachine.GunAnimationState`|已完成|
|`statemachine.GunAnimationStateContext`|（待确定）||
|`statemachine.ItemAnimationStateContext`|（待确定）||
|`statemachine.ThrowableAnimationStateContext`|_Deprecated_||
|`screen.RefitTransform`|（待确定）||
|`third.InnerThirdPersonManager`|`third.InnerThirdPersonManager`|已完成（占位）||

[↑ 回到移植顺序](#移植顺序)

---

## 12
## 12. 物品渲染器 — client.renderer.item

对应文档：[TaCZ 渲染管线](/docs-tacz/architecture/client/render/render-pipeline.md) 物品渲染器章节。

|TaCZ `com.tacz.guns.client.renderer.item`|CGC `client.renderer.item`|状态|
|---|---|---|
|`AnimateGeoItemRenderer`|`AnimateGeoItemRenderer`|已完成（占位）|
|`GunItemRendererWrapper`|（待确定）||
|`AmmoItemRenderer`|（待确定）||
|`AttachmentItemRenderer`|（待确定）||
|`GunSmithTableItemRenderer`|_Deprecated_||

[↑ 回到移植顺序](#移植顺序)

---

## 13
## 13. 实体/方块渲染器 — client.renderer.entity / block / other

对应文档：[TaCZ 渲染管线](/docs-tacz/architecture/client/render/render-pipeline.md) 实体/方块渲染器章节。

|TaCZ `com.tacz.guns.client.renderer`|CGC `client.renderer`|状态|
|---|---|---|
|`entity.EntityBulletRenderer`|（待确定）||
|`entity.TargetMinecartRenderer`|_Deprecated_||
|`block.GunSmithTableRenderer`|_Deprecated_||
|`block.StatueRenderer`|（待确定）||
|`block.TargetRenderer`|（待确定）||
|`other.GunHurtBobTweak`|`victim.GunHurtBobTweak`|已完成|
|`other.HumanoidOffhandRender`|`shooter.HumanoidOffhandRender`|已完成（占位）|
|`crosshair.CrosshairType`|`api.textures.crosshair.CrosshairType`|已完成|

[↑ 回到移植顺序](#移植顺序)

---

## 14
## 14. 客户端事件层 — client.event

对应文档：[TaCZ 渲染管线](/docs-tacz/architecture/client/render/render-pipeline.md) Mixin 钩子章节（渲染相关事件）。

|TaCZ `com.tacz.guns.client.event`|CGC 目标|状态|
|---|---|---|
|`FirstPersonRenderEvent`|（待确定）||
|`FirstPersonRenderGunEvent`|（待确定）||
|`CameraSetupEvent`|（待确定）||
|`TickAnimationEvent`|（待确定）||
|`RenderCrosshairEvent`|`gui.crosshair.CrosshairRender`|已完成|
|`ClientHitMark`|`entity.ClientHitMarkHandler`|已完成|
|`RenderHeadShotAABB`|`renderer.entity.HeadAABBRender`（已弃用）|已完成|
|`PlayerHurtByGunEvent`|`renderer.victim.onProjectileHit`|已完成|
|`ReloadResourceEvent`|_Deprecated_||
|`PlayerEnterWorld`|_Deprecated_||
|`PreventsHotbarEvent`|_Deprecated_||
|`tooltip.TooltipEvent`|`gui.tooltip.PojoLocationTooltip`|已完成|
|`InventoryEvent`|`mixin.entity.LocalPlayerMixin` + `entity.shooter.player._LocalMessageHandler`|已完成|
|`CommonNetworkCacheEvent`|`init.ClientModEvent`|已完成|
|`ClientPreventGunClick`|`input.player.InteractKey`|已完成|
|`RefreshClonePlayerDataEvent`|`entity.shooter.player._LocalPlayerHandler`|已完成|

|TaCZ `com.tacz.guns.api.client.event`|CGC `client.api.event`|状态|
|---|---|---|
|`BeforeRenderHandEvent`|`render.BeforeRenderHandEvent`|已完成|
|`RenderItemInHandBobEvent.BobHurt`|`render.ItemInHandBobEvent.Hurt`|已完成|
|`RenderItemInHandBobEvent.BobView`|`render.ItemInHandBobEvent.View`|已完成|
|`RenderLevelBobEvent.BobHurt`|`render.LevelBobEvent.Hurt`|已完成|
|`RenderLevelBobEvent.BobView`|`render.LevelBobEvent.View`|已完成|
|`SwapItemWithOffHand`|`player.SwapItemWithOffHandEvent`|已完成|

[↑ 回到移植顺序](#移植顺序)

---

## 15
## 15. Mixin 层 — client.mixin

对应文档：[TaCZ 渲染管线](/docs-tacz/architecture/client/render/render-pipeline.md) Mixin 钩子章节。

|TaCZ `com.tacz.guns.mixin.client`|CGC `client.mixin`|状态|
|---|---|---|
|`GameRendererMixin`|`renderer.GameRendererMixin`|已完成|
|`ItemInHandRendererMixin`|`renderer.ItemInHandRendererMixin`|已完成|
|`ItemInHandLayerMixin`|`renderer.ItemInHandLayerMixin`|已完成|
|`HumanoidModelMixin`|`model.HumanoidModelMixin`|已完成|
|`PlayerModelMixin`|`model.PlayerModelMixin`|已完成|
|`MouseHandlerMixin`|`MouseHandlerMixin`|已完成|
|`LocalPlayerMixin`|`entity.LocalPlayerMixin`|已完成|
|`AbstractButtonMixin`|`gui.AbstractButtonMixin`|已完成|
|`SoundManagerPreparationsMixin`|`sound.SoundManagerMixin`|已完成|
|`LanguageMixin`|_Deprecated_||
|`ar.ARCompatMixinPlugin`|（待确定）||
|`ar.BedrockPartMixin`|（待确定）||
|`StairBlockAccessor`|（待确定）||

[↑ 回到移植顺序](#移植顺序)

---

## 16
## 16. 其他 API — api.client.other

对应文档：跨文档引用（`KeepingItemRenderer` 等）。

|TaCZ `com.tacz.guns.api.client.other`|CGC 目标|状态|
|---|---|---|
|`KeepingItemRenderer`|`client.api.renderer.KeepingItemRenderer`|已完成|
|`GunModelTypeManager`|（待确定）||
|`IThirdPersonAnimation`|（待确定）||
|`ThirdPersonManager`|`client.api.item.gun.ThirdPersonAnimationType`|已完成|

[↑ 回到移植顺序](#移植顺序)
