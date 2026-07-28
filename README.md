# 自定义枪械永续 | Custom Gun Continued

[中文](#自定义枪械永续) | [English](#custom-gun-continued)

# 自定义枪械永续

😎[wiki](https://github.com/XColorful/Custom-Gun-Continued/wiki) | 📄[docs](https://github.com/XColorful/Custom-Gun-Continued/tree/HEAD/docs)

本模组是基于[TaCZ](https://github.com/MCModderAnchor/TACZ) 1.1.8 [GPLv3](https://www.gnu.org/licenses/gpl-3.0.txt) 授权的衍生作品；由于 [CC BY-NC-ND 4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/)，本模组**不包含原美术资产**。
- **跨版本是首要目标**，为了能方便地**同步支持新版本**而使模组永久延续
- 本模组针对[TaCZ](https://github.com/MCModderAnchor/TACZ)中存在的一些臃肿的设计进行了**深度重构**翻新

## 为什么这样做

> “移植不是目标，**同步**才是目标”

模组的生命在于有人玩，有整合包用
- 建平台抽象层**无法避免移植**，但将版本差异变成“**一次性的移植**”（后续更新无需反复坐牢）
- 对“移植难”祛魅，消灭“移植”这一深入人心的概念，转而看到“同步”后的模组界新世纪
- 待“版本自由”解放开发者后，玩家不再苦于版本锁定，整合包不再面临“生态断代”……

## 项目范围

本模组从最初打算就**只做到除渲染、模组联动、方块以外的内容为止**
- 渲染对技术有硬门槛，且原版渲染变化太大导致无法实现跨版本同步更新
- 该重构经验也探明了更多的平台抽象层的写法，提供了一套现成的跨版本方案（GPLv3开源）供其他模组参考，对己对他都有利
- 原仓库各模块已高度耦合，难以在保留原有结构的前提下重构底层架构；因此选择新建仓库从零开始，让各系统能够按依赖关系逐步构建，也意味着以新的名称重新出发

目前总结发现并企图解决以下问题：
- API学习和记忆：同一开发者对同样的API在各处有不同实现和重写
- 字段名不易学习：类/变量命名含义表述偏离实际功能、使用标志性名称而不表达含义
- 文档覆盖不足：Wiki及示例注释未覆盖全部字段及行为，理解完整语义需要查阅代码
- “上帝类”：一目千行、难以消化的上帝类（`ModernKineticGunItem`+`ModernKineticGunScriptAPI`、`EntityKineticBullet`）、上帝接口（`IGunOperator`、《`IGun`系列全家桶》）
- 硬编码字符串：路径、类型名等以“inline字符串”的形式散落各处，而没有提取枚举或集中管理
- 隐蔽的视角局限：各开发者往往只熟悉局部调用链，间接导致一些影响不大但潜在的重入（时序）问题和双端隔离/冲突问题

> 由以上原因，使得新人想要入坑或了解该项目架构具有相当的门槛；每次阅读和修改历史代码，都需要额外理解不同命名风格和实现方式，增加了阅读、维护和二次开发成本

## 现在做了什么

目前已完成的内容均同步支持1.20.1-26.2，即“写一份1.20.1的代码，能轻松合并到所有高版本分支而几乎无需修改”
- 完全重写的`resource`体系：**全字段重命名**（兼容老字段名读取），使用流式序列化/反序列化代替Gson装饰器（性能提升），使用原版资源包安装路径而不再绑定 _./tacz_ 目录
- `@SubscribeEvent`、`SimpleChannel`、`Capability/AttachmentType`、`IClientItemExtensions`与Forge/NeoForge**完全解耦**（1.20.1-26.2使用统一的平台抽象层）
- `NBT`、`Component`、`ValueInput`/`ValueOutput`与核心代码**完全解耦**（1.20.1-26.2使用统一的`NBTUtils`封装）
- 语义化重构：重新设计的包路径、类名、字段名、函数名、对“inline字符串”进行枚举提取
- 引入扩展性设计：`IGunSubManager`、`IProjectileSubManager`、`GunTooltipMask`

### 命名语义化重构

|旧名称|新名称|
|---|---|
|`TimelessAPI`|`ResourceApi`+`ClientResourceApi`|
|`CommonAssetsManager`、`CommonNetworkCache`|`_AllDataManager`+`_DataInstanceManager`、`SyncDataCache`|
|`ClientAssetsManager`+`ClientIndexManager`|`_AllAssetsManager`+`_AssetsInstanceManager`|
|`IGunOperator`|`ILivingShooter`（extends _IGunOperator_, _IShooterState_, _ISynGunState_, _IShooterModifierCacheHolder_）|
|`KnockBackModifier`、`ITargetEntity`|`IBulletVictimEntity` (extends _IBulletVictimEntityImpact_, _IBulletVictimKnockback_)|

### 主Manager-子Manager体系重构

|`ModernKineticGunItem` (extends _AbstractGunItem_)|`GunItem` (implements _IGunRuntime_)|
|---|---|
|`AbstractGunItem`.tickReload()|`IGunActionRuntime` -> `IGunActionManager`.tickReload()|
|`AbstractGunItem`.shoot()|`IGunAttackRuntime` -> `IGunAttackManager`.shoot()|
|`AbstractGunItem`.dropAllAmmo()|`IGunInventoryRuntime` -> `IGunInventoryManager`.dropAllAmmo()|
|`AbstractGunItem`.tickHeat()|`IGunStateRuntime` -> `IGunStateManager`.tickHeat()|

|`EntityKineticBullet`方法|`GunProjectile`（ProjectileManager体系）|
|---|---|
|↓→this.onBulletTick()|`IProjectileProcessManager`.processTick()|
|↓|↓|
|↓|↓→`IProjectileImpactManager`.preImpactTick()|
|↓|↓→`IProjectilePhysicsManager`.physicTick()|
|↓|↓→`IProjectileImpactManager`.impactTick()|
|↓→this.tick()剩余部分|↓→`IProjectilePhysicsManager`.physicMove()|
|↓|`IProjectileProcessManager`.processTick()剩余部分|

### 胖接口拆分

|旧接口|新接口|
|---|---|
|`IGunOperator`|`ILivingShooter`（extends _IGunOperator_, _IShooterState_, _ISynGunState_, _IShooterModifierCacheHolder_）|
||_IGunOperator_（extends _ICommonGunOperator_）|
||_IShooterState_ (extends _IShooterLatency_)|
||_IClientGunOperator_（extends _ICommonGunOperator_）|
|`IGun`|`IGun`（extends _IGunRuntime_, _IAnimationItem_, _IGunDataAccess_, _IGunGetter_）|
||_IGunRuntime_（extends _IGunActionRuntime_, _IGunAttackRuntime_, _IGunInventoryRuntime_, _IGunStateRuntime_）|
||_IGunDataAccess_（extends _IGunStateAccess_, _IGunAmmoDataAccess_, _IGunAttachmentDataAccess_, _IGunExpAccess_, _\_IGunPropertyAccess_）|
|`AttachmentItemDataAccessor`（extends _IAttachment_）|`AttachmentDataAccessor`（extends _AttachmentNBTAccessor_, _IAttachmentDataAccess_）|
||_AttachmentNBTAccessor_（extends _IAttachmentNBTAccess_）|
||_IAttachmentDataAccess_（extends _IAttachmentStateAccess_, _IAttachmentNBTAccess_, _\_IAttachmentPropertyAccess_）|

# Custom Gun Continued

😎[wiki](https://github.com/XColorful/Custom-Gun-Continued/wiki) | 📄[docs](https://github.com/XColorful/Custom-Gun-Continued/tree/HEAD/docs)

This mod is a derivative work of [TaCZ](https://github.com/MCModderAnchor/TACZ) 1.1.8 licensed under [GPLv3](https://www.gnu.org/licenses/gpl-3.0.txt). Due to the original art assets being licensed under [CC BY-NC-ND 4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/), this mod **does not include any original art assets**.
- **Cross-version synchronization is the highest priority**, allowing the mod to be **continuously maintained across future Minecraft versions**.
- This mod performs a **deep architectural refactor** of several bloated designs found in [TaCZ](https://github.com/MCModderAnchor/TACZ).

## Why?

> "Porting is not the goal. **Synchronization** is."

A mod only survives if people play it and modpacks adopt it.
- Building a platform abstraction layer **cannot eliminate porting**, but it turns version differences into a **one-time migration**, making future updates straightforward instead of repeatedly adapting every version.
- Demystify the difficulty of "porting", and replace the traditional mindset of "porting" with continuous cross-version synchronization.
- Once developers are freed from version restrictions, players will no longer be locked to specific versions, and modpack ecosystems will no longer be fragmented by version upgrades.

## Project Scope

From the very beginning, this project was **only intended to implement everything except rendering, mod compatibility, and blocks**.
- Rendering has a high technical barrier, and Minecraft's rendering pipeline changes too significantly between versions to support continuous synchronization.
- The refactoring process also explored more approaches for platform abstraction, providing a ready-to-use cross-version solution (GPLv3 open source) that may benefit other mods as well.
- The original project has become too tightly coupled to refactor its infrastructure incrementally, so a new repository was created to rebuild the architecture layer by layer according to dependency order—marking a fresh start under a new name.

The project aims to address the following issues:
- API learning and memorization: identical functionality is often implemented differently in different places, even by the same developer.
- Poor field naming: class and variable names often deviate from their actual responsibilities, relying on symbolic names rather than meaningful semantics.
- Insufficient documentation: the Wiki and example comments do not cover every field and behavior, requiring source code reading to fully understand the implementation.
- "God Classes": enormous and difficult-to-understand classes (`ModernKineticGunItem` + `ModernKineticGunScriptAPI`, `EntityKineticBullet`) and giant interfaces (`IGunOperator`, the entire `IGun` family).
- Hardcoded strings: paths, type names and similar identifiers are scattered as inline strings instead of centralized constants or enums.
- Hidden perspective limitations: contributors are often only familiar with local call chains, indirectly leading to potential reentrancy (timing) issues and client/server synchronization conflicts.

> These issues significantly increase the learning barrier for newcomers trying to understand the project architecture. Reading and modifying historical code also requires understanding multiple naming styles and implementation patterns, increasing the cost of reading, maintenance, and secondary development.

## What Has Been Done

Everything completed so far supports synchronization across **Minecraft 1.20.1 - NeoForge 26.2**, meaning that **code written for 1.20.1 can be merged into all newer branches with little or no modification**.
- Completely rewritten `resource` system: **all fields have been renamed** (while remaining compatible with legacy field names), streaming serialization/deserialization replaces Gson adapters (performance improvement), and vanilla resource pack locations are now used instead of the fixed _./tacz_ directory.
- `@SubscribeEvent`, `SimpleChannel`, `Capability/AttachmentType`, and `IClientItemExtensions` are **fully decoupled** from Forge/NeoForge (using a unified platform abstraction layer across 1.20.1-26.2).
- `NBT`, `Component`, `ValueInput`/`ValueOutput` are **fully decoupled** from the core logic (using a unified `NBTUtils` abstraction across 1.20.1-26.2).
- Semantic refactoring: redesigned package paths, class names, field names and method names, while extracting inline strings into enums where appropriate.
- Introduced extensibility-oriented designs: `IGunSubManager`, `IProjectileSubManager`, `GunTooltipMask`.

### Semantic Naming Refactor

|Old Name|New Name|
|---|---|
|`TimelessAPI`|`ResourceApi` + `ClientResourceApi`|
|`CommonAssetsManager`, `CommonNetworkCache`|`_AllDataManager` + `_DataInstanceManager`, `SyncDataCache`|
|`ClientAssetsManager` + `ClientIndexManager`|`_AllAssetsManager` + `_AssetsInstanceManager`|
|`IGunOperator`|`ILivingShooter` (extends _IGunOperator_, _IShooterState_, _ISynGunState_, _IShooterModifierCacheHolder_)|
|`KnockBackModifier`, `ITargetEntity`|`IBulletVictimEntity` (extends _IBulletVictimEntityImpact_, _IBulletVictimKnockback_)|

### Main Manager / Sub Manager Refactor

|`ModernKineticGunItem` (extends _AbstractGunItem_)|`GunItem` (implements _IGunRuntime_)|
|---|---|
|`AbstractGunItem`.tickReload()|`IGunActionRuntime` -> `IGunActionManager`.tickReload()|
|`AbstractGunItem`.shoot()|`IGunAttackRuntime` -> `IGunAttackManager`.shoot()|
|`AbstractGunItem`.dropAllAmmo()|`IGunInventoryRuntime` -> `IGunInventoryManager`.dropAllAmmo()|
|`AbstractGunItem`.tickHeat()|`IGunStateRuntime` -> `IGunStateManager`.tickHeat()|

|`EntityKineticBullet` Methods|`GunProjectile` (ProjectileManager System)|
|---|---|
|↓→this.onBulletTick()|`IProjectileProcessManager`.processTick()|
|↓|↓|
|↓|↓→`IProjectileImpactManager`.preImpactTick()|
|↓|↓→`IProjectilePhysicsManager`.physicTick()|
|↓|↓→`IProjectileImpactManager`.impactTick()|
|↓→remaining `this.tick()` logic|↓→`IProjectilePhysicsManager`.physicMove()|
|↓|remaining `IProjectileProcessManager`.processTick() logic|

### Fat Interface Splitting

|Old Interface|New Interface|
|---|---|
|`IGunOperator`|`ILivingShooter` (extends _IGunOperator_, _IShooterState_, _ISynGunState_, _IShooterModifierCacheHolder_)|
||_IGunOperator_ (extends _ICommonGunOperator_)|
||_IShooterState_ (extends _IShooterLatency_)|
||_IClientGunOperator_ (extends _ICommonGunOperator_)|
|`IGun`|`IGun` (extends _IGunRuntime_, _IAnimationItem_, _IGunDataAccess_, _IGunGetter_)|
||_IGunRuntime_ (extends _IGunActionRuntime_, _IGunAttackRuntime_, _IGunInventoryRuntime_, _IGunStateRuntime_)|
||_IGunDataAccess_ (extends _IGunStateAccess_, _IGunAmmoDataAccess_, _IGunAttachmentDataAccess_, _IGunExpAccess_, __IGunPropertyAccess_)|
|`AttachmentItemDataAccessor` (extends _IAttachment_)|`AttachmentDataAccessor` (extends _AttachmentNBTAccessor_, _IAttachmentDataAccess_)|
||_AttachmentNBTAccessor_ (extends _IAttachmentNBTAccess_)|
||_IAttachmentDataAccess_ (extends _IAttachmentStateAccess_, _IAttachmentNBTAccess_, __IAttachmentPropertyAccess_)|
