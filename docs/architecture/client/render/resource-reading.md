# 资源读取

> 渲染体系的所有美术资源都从 `ClientResourceApi` 获取。本文只说明「该读哪个对象、它代表什么、被谁用」，不展开资源包的完整管理流程。

## 获取入口

`ClientResourceApi` 是所有客户端资源读取的唯一入口，内部代理到 `_AllAssetsManager`（各资源管理器）和 `_AssetsInstanceManager`（Instance 索引）。它提供两组 getter：

- POJO getter：读原始 JSON 解析结果。
- Instance getter：读二次校验后的运行时对象。

开发者渲染时几乎总是用 Instance getter，POJO getter 主要用于查看原始配置。

## POJO 与 Instance 的区别

资源包 JSON 文件先被 `ResourcePojoManager` 子类读成 POJO，此时只做自身字段的类型 / 非空校验。资源重载完成后，`_AssetsInstanceManager.reload()` 对所有 POJO 做二次校验（跨 POJO 索引检查），构建 Instance 并缓存。

Instance 是渲染真正依赖的对象：它缓存了模型对象、动画控制器、状态机等运行时结构。二次校验失败的资源会被丢弃，不进入 Instance 索引。

## 读取什么对象

### 模型

- `getBedrockModel(location)` → `BedrockModel`（POJO）。基岩版几何文件 `.geo.json` 的解析结果，是 `ModelObject` 场景图的来源。
- Instance 里的模型对象（`GunModelObject` / `AttachmentModelObject` / `AmmoModelObject`）不直接 getter，而是由各自的 Instance 持有。

### 显示配置

- `getGunDisplay(location)` → `GunDisplay`（POJO）。枪械显示配置：模型、纹理、动画、脚本路径、枪口火焰、激光、抛壳、包围显示等。
- `getAttachmentDisplay(location)` → `AttachmentDisplay`（POJO）。配件显示配置：模型、LOD、适配器节点、瞄具、激光。
- `getAmmoDisplay(location)` → `AmmoDisplay`（POJO）。弹药显示配置：子弹实体模型、弹壳模型、粒子、曳光弹颜色。
- `getBlockDisplay(location)` → `BlockDisplay`（POJO）。方块显示配置。

### 动画

- `getBedrockAnimation(location)` → `BedrockAnimation`（POJO）。基岩版动画文件，是 `AnimController` 里动画原型的来源。
- `getGltfAnimation(location)` → `GltfAnimation`（POJO）。glTF 动画。

### 脚本

- `getAssetsScript(location)` → `AssetsScript`（POJO）。状态机 Lua 脚本的编译结果，构建 `LuaAnimStateMachine` 时使用。

### Instance

- `getGunDisplayInstance(gunItem)` → `GunDisplayInstance`。枪械显示实例，持有 `GunModelObject`、LOD 模型、状态机、状态机参数、声音、曳光弹颜色等。第一人称渲染链路从这里拿到模型和状态机。
- `getClientGunIndexInstance(gunLocation)` → `ClientGunIndexInstance`。枪械索引，持有枪械数据（`GunData`）。
- `getClientAttachmentIndexInstance(attachmentLocation)` → `ClientAttachmentIndexInstance`。配件索引，持有配件模型、LOD 模型、配件显示配置与数据。
- `getClientAmmoIndexInstance(ammoLocation)` → `ClientAmmoIndexInstance`。弹药索引，持有弹药模型与弹壳模型、弹药显示配置。
- `getClientBlockIndexInstance(blockLocation)` → `ClientBlockIndexInstance`。方块索引。

## 资源与渲染模块的对应

|美术资源|读取对象|最终使用模块|
|---|---|---|
|枪械模型 + 纹理|`GunDisplayInstance` → `GunModelObject`|`GunItemRenderer` / `_GunModelRender`|
|枪械 LOD|`GunDisplayInstance` → `getGunModelLod`|`GunItemRenderer.renderByItem`|
|枪械动画|`GunDisplay` 的动画路径 → `BedrockAnimation`|`AnimController`（经 `AnimationHelper`）|
|状态机脚本|`GunDisplay` 的脚本路径 → `AssetsScript`|`LuaAnimStateMachine`|
|配件模型 + 纹理|`ClientAttachmentIndexInstance` → `AttachmentModelObject`|`AttachmentRender` / `_GunModelRender.renderScope`|
|瞄具视野 / FOV|`ClientAttachmentIndexInstance` → `AttachmentDisplay`|`GunCameraHelper` / 第一人称定位|
|弹壳模型|`ClientAmmoIndexInstance` → `AmmoModelObject`|`ShellRender`|
|子弹实体模型|`ClientAmmoIndexInstance` → `AmmoModelObject`|`GunProjectileRenderer`|
|枪口火焰 / 激光 / 抛壳参数|`GunDisplayInstance` → `GunDisplay`|`MuzzleFlashRender` / `BeamRender` / `ShellRender`|
|枪械数据（后坐、射速等）|`ClientGunIndexInstance` / `GunIndexInstance`|后坐力、状态机脚本、tooltip|

## 资源重载

资源包重载时 `_AllAssetsManager` 重新注册各资源管理器，`_AssetsInstanceManager.reload()` 清空并重建所有 Instance。若玩家手持枪械，重载后会刷新属性缓存并自动切一次枪，强制状态机重新初始化，让新资源生效。
