# 渲染体系移植 TODO

> 以 [TaCZ 渲染体系总览](/docs-tacz/architecture/client/render/Home.md) 的 Mermaid 子图为单位，列出所有待移植类/包目录

已移植的类同样列在表格中展示其 CGC 对应位置。已弃用（`_Deprecated_`）的类不列入待移植清单。

## 移植顺序

- [x] [1. 几何 POJO 层](#1) — 所有渲染器的基础数据结构
- [x] [2. 动画 POJO 层](#2) — 动画数据的解析入口
- [x] [3. Display POJO 层](#3) — 显示配置的数据结构
- [x] [4. 资源加载与索引层](#4) — 资源管道和 Instance 构建
- [x] [5. 几何运行时层](#5) — 将 POJO 数据转换为可渲染的场景图
- [x] [6. 模型对象层](#6) — 场景图之上构建分类型的模型对象（枪械/配件/弹药），注册动画监听器和功能性渲染器
- [x] [7. 模型监听器层](#7) — 动画数据写入场景图的桥梁
- [x] [8. PAPI 层](#8) — 模型文字覆盖的占位符系统
- [x] [9. 功能性渲染器](#9) — 枪口火焰、抛壳、激光束、配件渲染、手臂渲染
- [x] [10. 动画 API 层](#10) — 动画实例、控制器、状态机、glTF 支持
- [x] [11. 动画实现层](#11) — 枪械专用的状态上下文和第三人称管理器
- [x] [12. 物品渲染器](#12) — BEWLR 渲染器（AnimateGeoItemRenderer 及其枪械子类）
- [x] [13. 实体/方块渲染器](#13) — EntityBulletRenderer、StatueRenderer、TargetRenderer
- [x] [14. 客户端事件层](#14) — 第一人称渲染事件、相机事件、动画 Tick
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
|`FaceUVsItem`|`bedrock.geometry.bone.cube._Uv`|

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
|`index.ClientAttachmentSkinIndex`|_Deprecated_||

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
|`BedrockModel`|`client.model.ModelObject`||
|`BedrockPart`|`client.model.bedrock.BedrockPart`||
|`BedrockCube` (interface)|`api.model.bedrock.IBedrockCube`||
|`BedrockCubeBox`|`client.model.bedrock.BedrockCubeBox`||
|`BedrockCubePerFace`|`client.model.bedrock.BedrockCubePerFace`||
|`BedrockPolygon`|`client.model.bedrock.BedrockPolygon`||
|`BedrockVertex`|`client.model.bedrock.BedrockVertex`||
|`ModelRendererWrapper`|`api.model.bedrock.IBedrockRenderer`||

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
|`FunctionalBedrockPart`|bedrock.`FunctionalBedrockPart`||
|`SlotModel`|bedrock.`SlotModel`||
|`GunModelConstant`|`api.resource.assets.model.bedrock.geometry.NodeName`||
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
|`camera.CameraAnimationObject`|`animation.listener.camera.CameraAnimationObject`||
|`camera.CameraRotateListener`|`animation.listener.camera.CameraRotateListener`||
|`constraint.ConstraintObject`|`animation.listener.constraint.ConstraintObject`||
|`constraint.ConstraintRotateListener`|`animation.listener.constraint.ConstraintRotateListener`||
|`constraint.ConstraintTranslateListener`|`animation.listener.constraint.ConstraintTranslateListener`||
|`model.ModelRotateListener`|`animation.listener.model.ModelRotateListener`||
|`model.ModelScaleListener`|`animation.listener.model.ModelScaleListener`||
|`model.ModelTranslateListener`|`animation.listener.model.ModelTranslateListener`||
|`model.ModelAdditionalMagazineListener`|`animation.listener.model.ModelAdditionalMagazineListener`||

[↑ 回到移植顺序](#移植顺序)

---

## 8
## 8. PAPI 层 — client.model.papi

对应文档：[TaCZ 功能性渲染器](/docs-tacz/architecture/client/render/functional-renderers.md) 文字覆盖章节。

|TaCZ `com.tacz.guns.client.model.papi`|CGC 目标|状态|
|---|---|---|
|`PapiManager`|`core.text.placeholder.PlaceholderManager`||
|`AmmoCountPapi`|_Deprecated_||
|`PlayerNamePapi`|_Deprecated_||

[↑ 回到移植顺序](#移植顺序)

---

## 9
## 9. 功能性渲染器 — client.model.functional

对应文档：[TaCZ 功能性渲染器](/docs-tacz/architecture/client/render/functional-renderers.md)。

|TaCZ `com.tacz.guns.client.model.functional`|CGC 目标|状态|
|---|---|---|
|`MuzzleFlashRender`|`renderer.model.MuzzleFlashRender`|已完成（占位）|
|`ShellRender`|`renderer.model.ShellRender`|已完成（占位）|
|`AttachmentRender`|`renderer.model.AttachmentRender`||
|`BeamRenderer`|`renderer.model.BeamRender`||
|`LeftHandRender`|`renderer.model.HandRender`.`Left`||
|`RightHandRender`|`renderer.model.HandRender`.`Right`||
|`TextShowRender`|`renderer.model.TextRender`||

[↑ 回到移植顺序](#移植顺序)

---

## 10
## 10. 动画 API 层 — api.client.animation

对应文档：[TaCZ 动画系统](/docs-tacz/architecture/client/render/animation-system.md)。

CGC 中 `ObjectAnimation` 和 `ObjectAnimationSoundChannel` 为占位骨架。

|TaCZ `com.tacz.guns.api.client.animation`|CGC `client.animation`|状态|
|---|---|---|
|`ObjectAnimation`|`ObjectAnimation`|已完成（占位）|
|`ObjectAnimationChannel`|`ObjectAnimationChannel`||
|`ObjectAnimationRunner`|`ObjectAnimationRunner`||
|`ObjectAnimationSoundChannel`|`ObjectAnimationSoundChannel`|已完成（占位）|
|`AnimationController`|`controller.AnimController`||
|`AnimationPlan`|`controller.AnimPlan`||
|`Animations`|`AnimationHelper`||
|`AnimationListener`|`api.animation.listener.IAnimationListener`||
|`AnimationListenerSupplier`|`api.animation.listener.IAnimationListenerSupplier`||
|`AnimationChannelContent`|`channel.AnimChannelContent`||
|`AnimationSoundChannelContent`|`channel.SoundChannelContent`||
|`DiscreteTrackArray`|`statemachine.DiscreteTrackArray`||

|TaCZ `com.tacz.guns.api.client.animation.interpolator`|CGC `client.animation.interpolator`|状态|
|---|---|---|
|`Interpolator`|`api.animation.interpolator.IInterpolator`||
|`InterpolatorUtil`|`api.animation.interpolator.interpolator.InterpolatorType`||
|`Linear`|`Linear`||
|`Step`|`Step`||
|`Spline`|`Spline`||
|`SLerp`|`SLerp`||
|`CustomInterpolator`|`CompositeInterpolator`||

|TaCZ `com.tacz.guns.api.client.animation.gltf`|CGC 目标|状态|
|---|---|---|
|`AnimationStructure`|（待确定）||
|`AnimationModel`|（待确定）||
|`AccessorModel` / `BufferModel` / `NodeModel` 等|（待确定）||
|`accessor.*`（AccessorData / AccessorFloatData 等）|（待确定）||
|`ElementType` / `GltfConstants` / `Buffers` / `NumberArrays`|（待确定）||

|TaCZ `com.tacz.guns.api.client.animation.statemachine`|CGC `client.animation.statemachine`|状态|
|---|---|---|
|`AnimationStateMachine`|`AnimStateMachine`||
|`AnimationState`|`api.animation.statemachine.IAnimationStateContext`||
|`AnimationStateContext`|`api.animation.statemachine.AnimStateContext`||
|`AnimationConstant`|_Deprecated_||
|`LuaAnimationStateMachine`|`LuaAnimStateMachine`||
|`LuaAnimationState`|`LuaAnimStateContext`||
|`LuaStateMachineFactory`|`LuaAnimStateContext.Builder`||
|`TrackArrayMismatchException`|`api.animation.statemachine.TrackArrayMismatchException`||

[↑ 回到移植顺序](#移植顺序)

---

## 11
## 11. 动画实现层 — client.animation

对应文档：[TaCZ 动画系统](/docs-tacz/architecture/client/render/animation-system.md) 触发条件与状态上下文章节。

|TaCZ `com.tacz.guns.client.animation`|CGC `client.animation`|状态|
|---|---|---|
|`statemachine.GunAnimationConstant`|`statemachine.GunAnimationState`|已完成|
|`statemachine.GunAnimationStateContext`|`statemachine.GunAnimStateContext`||
|`statemachine.ItemAnimationStateContext`|`statemachine.ItemAnimStateContext`||
|`statemachine.ThrowableAnimationStateContext`|_Deprecated_||
|`screen.RefitTransform`|`screen.RefitScreenTransformState`||
|`third.InnerThirdPersonManager`|`shooter.ShooterAnimationManager`|已完成（占位）||

[↑ 回到移植顺序](#移植顺序)

---

## 12
## 12. 物品渲染器 — client.renderer.item

对应文档：[TaCZ 渲染管线](/docs-tacz/architecture/client/render/render-pipeline.md) 物品渲染器章节。

|TaCZ `com.tacz.guns.client.renderer.item`|CGC `client.renderer.item`|状态|
|---|---|---|
|`AnimateGeoItemRenderer`|`AnimateGeoItemRenderer`|已完成（占位）|
|`GunItemRendererWrapper`|`GunItemRenderer`||
|`AmmoItemRenderer`|`AmmoItemRenderer`||
|`AttachmentItemRenderer`|`AttachmentItemRenderer`||
|`GunSmithTableItemRenderer`|_Deprecated_||

[↑ 回到移植顺序](#移植顺序)

---

## 13
## 13. 实体/方块渲染器 — client.renderer.entity / block / other

对应文档：[TaCZ 渲染管线](/docs-tacz/architecture/client/render/render-pipeline.md) 实体/方块渲染器章节。

|TaCZ `com.tacz.guns.client.renderer`|CGC `client.renderer`|状态|
|---|---|---|
|`entity.EntityBulletRenderer`|`entity.GunProjectileRenderer`||
|`entity.TargetMinecartRenderer`|_Deprecated_||
|`block.GunSmithTableRenderer`|_Deprecated_||
|`block.StatueRenderer`|_Deprecated_||
|`block.TargetRenderer`|_Deprecated_||
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
|`FirstPersonRenderEvent`|`renderer.shooter.FirstPersonRender`||
|`FirstPersonRenderGunEvent`|`renderer.item.gun.GunRendererAddon`||
|`CameraSetupEvent`|`renderer.item.gun.GunCameraHelper`||
|`TickAnimationEvent`|`entity.shooter.player._LocalAnimHandler`||
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
|`IThirdPersonAnimation`|`client.api.animation.shooter.IShooterAnimator`||
|`ThirdPersonManager`|`client.api.item.gun.ThirdPersonAnimationType`|已完成|

[↑ 回到移植顺序](#移植顺序)
