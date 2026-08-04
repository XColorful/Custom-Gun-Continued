[English](#English)

# API索引

## 方块
> _./core/api/block_

- [IBulletVictimBlock](/docs/api/core/block/IBulletVictimBlock.md)：受弹方块

### 受弹方块
> _./core/api/block/victim_

- [IBulletVictimBlockGetter](/docs/api/core/block/victim/IBulletVictimBlockGetter)：获取`IBulletVictimBlock`

## 双端
> _./core/api/common_

- [ISideOnly](/docs/api/core/common/ISideOnly.md)：提供是否运行在正确的端上的判定
- [ISideExecutor](/docs/api/core/common/ISideExecutor.md)：等价于`DistExecutor`，封装端执行逻辑
- McLogicalSide：等价于`LogicalSide`
- McSide：等价于`Dist`

## 配置
> _./core/api/config_

- IModConfigSpec：封装`ForgeConfigSpec`
- IModConfigSpecBuilder：封装`ForgeConfigSpec.Builder`

## 实体
> _./core/api/entity_

- [ILivingShooter](/docs/api/core/entity/ILivingShooter.md)：射手生物
	- modifier：射手修饰
- [IGunProjectile](/docs/api/core/entity/IGunProjectile.md)：枪射物
	- [GunProjectileProperty](/docs/api/core/entity/GunProjectileProperty.md)：枪射物属性
- [IBulletVictimEntity](/docs/api/core/entity/IBulletVictimEntity.md)：受弹实体
- [IEntityHitboxHistory](/docs/api/core/entity/IEntityHitboxHistory.md)：实体碰撞箱史

## 事件
> _./core/api/event_

- [EventPriority](/docs/api/core/event/EventPriority.md)：事件优先级，同 Forge/NeoForge

事件处理器接口：
- [ICustomEventHandler](/docs/api/core/event/ICustomEventHandler.md)：自定义事件处理器接口，供扩展模组用
- [IEventHandler](/docs/api/core/event/IEventHandler.md)：模组事件处理器接口

监听事件：
- [ICustomEventRegister](/docs/api/core/event/ICustomEventRegister.md)：注册监听自定义事件
- [IEventRegister](/docs/api/core/event/IEventRegister.md)：注册监听模组事件

发布事件：
- [ICustomEventPoster](/docs/api/core/event/ICustomEventPoster.md)：自定义事件发布器

### 模组事件
> _./core/api/event_

> [事件API](./event-api.md)

- [EventType](/docs/api/core/event/EventType.md)：模组事件类型
- [IEvent](/docs/api/core/event/IEvent.md)：模组事件接口
> - IServerTickEvent：服务端 tick 事件
> - IEntityJoinLevelEvent：实体加入世界事件
> - ILivingKnockbackEvent：生物击退事件
> - IPlayerCloneEvent：玩家复制事件
> - IPlayerStartTrackingEvent：玩家开始追踪事件
> - IAddServerReloadListenerEvent：服务端资源重载监听事件
> - ITagsUpdatedEvent：标签更新事件
> - IDatapackSyncEvent：数据包同步事件

### 自定义事件
> _./core/api/event_

- [CustomEventType](/docs/api/core/event/CustomEventType.md)：自定义事件类型
- [ICustomEvent](/docs/api/core/event/ICustomEvent.md)：自定义事件接口

## 枪械
> _./core/api/gun_

- [IGunManager](/docs/api/core/gun/IGunManager.md)：枪械管理器（全能易用门面）
	> - [IGunMainManager](/docs/api/core/gun/IGunMainManager.md)：枪械主管理器，提供子管理器及热插拔
	> - [GunManagerGroup](/docs/api/core/gun/GunManagerGroup.md)：枪械管理器组
- [IGunSubManager](/docs/api/core/gun/IGunSubManager.md)：枪械子管理器（主/子管理器同构，即`IGunManager`也是`IGunSubManager`）
	- Action：枪械动作
		- [IGunActionManager](/docs/api/core/gun/action/IGunActionManager.md)：枪械动作管理器
	- Attack：枪械攻击
		- [IGunAttackManager](/docs/api/core/gun/attack/IGunAttackManager.md)：枪械攻击管理器
	- Inventory：枪械背包
		- [IGunInventoryManager](/docs/api/core/gun/inventory/IGunInventoryManager.md)：枪械背包管理器
	- Script：枪械脚本
		- [IGunScriptManager](/docs/api/core/gun/script/IGunScriptManager.md)：枪械脚本管理器
	- State：枪械状态
		- [IGunStateManager](/docs/api/core/gun/state/IGunStateManager.md)：枪械状态管理器
- [IGunRuntime](/docs/api/core/gun/IGunRuntime.md)：枪械运行时接口，聚合枪械子管理器 Runtime 接口

## 初始化
> _./core/api/init_

### 初始注册
> _./core/api/init/registry_

- Registry：平台无关的注册
	- IMenuTypeFactory：封装网络菜单创建逻辑
	- IRegistrar：注册对象集合接口
	- IRegistrarFactory：创建平台无关的注册对象集合
	- IRegistryObject：平台无关的注册对象引用接口

## 物品
> _./core/api/item_

- IItemModifier：物品修饰工具
- [IGun](/docs/api/core/item/IGun.md)：枪械接口
- [IAttachment](/docs/api/core/item/IAttachment.md)：配件接口
	- [IAttachmentModifier](docs/api/core/item/attachment/modifier/IAttachmentModifier.md)：配件修饰工具
- [IAmmo](/docs/api/core/item/IAmmo.md)：子弹接口
	- [IAmmoBox](/docs/api/core/item/IAmmoBox.md)：子弹盒接口
- IBlock：方块物品接口

### 枪械
> _./core/api/item/gun_

- [IGunGetter](/docs/api/core/item/gun/IGunGetter.md)：获取`IGun`
- [\_IGunPropertyAccess](/docs/api/core/item/gun/_IGunPropertyAccess.md)：仅供第三方脚本调用

### 配件
> _./core/api/item/attachment_

- [IAttachmentGetter](/docs/api/core/item/attachment/IAttachmentGetter.md)：获取`IAttachment`

### 子弹
> _./core/api/item/ammo_

- [IAmmoGetter](/docs/api/core/item/ammo/IAmmoGetter.md)：获取`IAmmo`

#### 子弹盒
> _./core/api/item/ammo_

- [IAmmoBoxGetter](/docs/api/core/item/ammobox/IAmmoBoxGetter.md)：获取`IAmmoBoxGetter`

### 方块物品
> _./core/api/item/block_

- [IBlockGetter](/docs/api/core/item/block/IBlockGetter.md)：获取`IBlock`

## 我的世界
> _./core/api/minecraft_

- [IMcRegistry](/docs/api/core/minecraft/IMcRegistry.md)：`ResourceLocation`操作，查询模组加载，获取`MinecraftServer`

### Capability系统
> _./core/api/minecraft/capability_

- [IInventoryCapability](/docs/api/core/minecraft/capability/IInventoryCapability.md)：等价于`IItemHandler`，封装物品栏操作

## 网络
> _./core/api/network_

- [INetworkAdapter](/docs/api/core/network/INetworkAdapter.md)：抽象由平台实现的注册网络消息、向玩家发送消息
- [INetworkHook](/docs/api/core/network/INetworkHook.md)：网络钩子，用于打开容器 GUI

### 网络消息
> _./core/api/network/message_

- IMessage：网络消息接口

## 枪射物
> _./core/api/projectile_

- [IProjectileManager](/docs/api/core/projectile/IProjectileManager.md)：枪射物管理器（全能易用门面）
	> - [IProjectileMainManager](/docs/api/core/projectile/IProjectileMainManager.md)：枪射物主管理器，提供子管理器及热插拔
- [IProjectileSubManager](/docs/api/core/projectile/IProjectileSubManager.md)：枪射物子管理器（主/子管理器同构，即`IProjectileManager`也是`IProjectileSubManager`）
	- [ProjectileManagerGroup](/docs/api/core/projectile/ProjectileManagerGroup.md)：枪射物管理器组，以`managerGroupTag`标识子管理器组合
	- Effect：枪射物效果
		- [IProjectileEffectManager](/docs/api/core/projectile/effect/IProjectileEffectManager.md)：枪射物效果管理器（生成视觉/炫技效果）
	- Impact：枪射物作用
		- [IProjectileImpactManager](/docs/api/core/projectile/impact/IProjectileImpactManager.md)：枪射物作用管理器（处理枪射物与对象相互作用造成的影响）
	- Physics：枪射物物理
		- [IProjectilePhysicsManager](/docs/api/core/projectile/physics/IProjectilePhysicsManager.md)：枪射物物理管理器（计算物理状态）
	- Process：枪射物进程
		- [IProjectileProcessManager](/docs/api/core/projectile/process/IProjectileProcessManager.md)：枪射物进程管理器（调度各子管理器）
- [IProjectileRuntime](/docs/api/core/projectile/IProjectileRuntime.md)：枪射物运行时接口，聚合枪射物子管理器 Runtime 接口

## 配方
> _./core/api/recipe_

- RecipeResultType：配方结果类型

## 资源
> _./core/api/resource_

- [ResourceApi](/docs/api/core/resource/ResourceApi.md)：资源API（全能易用门面），提供数据包资源获取

### 数据包
> _./core/api/resource/data_

- [DataFolderType](/docs/api/core/resource/data/DataFolderType.md)：数据包文件夹类型

## 脚本
> _./core/api/script_

## 声音
> _./core/api/sound_

# English

## Block
> _./core/api/block_

- [IBulletVictimBlock](/docs/api/core/block/IBulletVictimBlock.md): Bullet victim block

### Bullet Victim Block
> _./core/api/block/victim_

- [IBulletVictimBlockGetter](/docs/api/core/block/victim/IBulletVictimBlockGetter): Get `IBulletVictimBlock`

## Common
> _./core/api/common_

- [ISideOnly](/docs/api/core/common/ISideOnly.md): Provides determination of whether running on the correct side
- [ISideExecutor](/docs/api/core/common/ISideExecutor.md): Equivalent to `DistExecutor`, encapsulates side execution logic
- McLogicalSide: Equivalent to `LogicalSide`
- McSide: Equivalent to `Dist`

## Config
> _./core/api/config_

- IModConfigSpec: Encapsulates `ForgeConfigSpec`
- IModConfigSpecBuilder: Encapsulates `ForgeConfigSpec.Builder`

## Entity
> _./core/api/entity_

- [ILivingShooter](/docs/api/core/entity/ILivingShooter.md): Living shooter
	- modifier: Shooter modifier
- [IGunProjectile](/docs/api/core/entity/IGunProjectile.md): Gun projectile
	- [GunProjectileProperty](/docs/api/core/entity/GunProjectileProperty.md): Gun projectile property
- [IBulletVictimEntity](/docs/api/core/entity/IBulletVictimEntity.md): Bullet victim entity
- [IEntityHitboxHistory](/docs/api/core/entity/IEntityHitboxHistory.md): Entity hitbox history

## Event

- [EventPriority](/docs/api/core/event/EventPriority.md): Event priority, same as Forge/NeoForge

Event Handler Interface:
- [ICustomEventHandler](/docs/api/core/event/ICustomEventHandler.md): Custom event handler interface, for extension mods
- [IEventHandler](/docs/api/core/event/IEventHandler.md): Mod event handler interface

Listen Event:
- [ICustomEventRegister](/docs/api/core/event/ICustomEventRegister.md): Register to listen for custom events
- [IEventRegister](/docs/api/core/event/IEventRegister.md): Register to listen for mod events

Post Event:
- [ICustomEventPoster](/docs/api/core/event/ICustomEventPoster.md): Custom event poster

### Mod Event
> _./core/api/event_

> [Event API](./event-api.md)

- [EventType](/docs/api/core/event/EventType.md): Mod event type
- [IEvent](/docs/api/core/event/IEvent.md): Mod event interface
> - IServerTickEvent: Server tick event
> - IEntityJoinLevelEvent: Entity join level event
> - ILivingKnockbackEvent: Living knockback event
> - IPlayerCloneEvent: Player clone event
> - IPlayerStartTrackingEvent: Player start tracking event
> - IAddServerReloadListenerEvent: Server datapack reload listener event
> - ITagsUpdatedEvent: Tags updated event
> - IDatapackSyncEvent: Datapack sync event

### Custom Event
> _./core/api/event_

- [CustomEventType](/docs/api/core/event/CustomEventType.md): Custom event type
- [ICustomEvent](/docs/api/core/event/ICustomEvent.md): Custom event interface

## Gun
> _./core/api/gun_

- [IGunManager](/docs/api/core/gun/IGunManager.md): Gun manager (almighty easy-to-use facade)
	> - [IGunMainManager](/docs/api/core/gun/IGunMainManager.md): Gun main manager, provides sub-managers and hot-swapping
	> - [GunManagerGroup](/docs/api/core/gun/GunManagerGroup.md): Gun manager group
- [IGunSubManager](/docs/api/core/gun/IGunSubManager.md): Gun sub-manager (isomorphic to main/sub manager, i.e., `IGunManager` is also `IGunSubManager`)
	- Action:
		- [IGunActionManager](/docs/api/core/gun/action/IGunActionManager.md): Gun action manager
	- Attack:
		- [IGunAttackManager](/docs/api/core/gun/attack/IGunAttackManager.md): Gun attack manager
	- Inventory:
		- [IGunInventoryManager](/docs/api/core/gun/inventory/IGunInventoryManager.md): Gun inventory manager
    - Script:
	    - [IGunScriptManager](/docs/api/core/gun/script/IGunScriptManager.md): Gun script manager
	- State:
		- [IGunStateManager](/docs/api/core/gun/state/IGunStateManager.md): Gun state manager
- [IGunRuntime](/docs/api/core/gun/IGunRuntime.md): Gun runtime interface, aggregating Gun sub-manager Runtime interfaces

## Init
> _./core/api/init_

### Initial registration
> _./core/api/init/registry_

- Registry: Platform-independent registration
	- IMenuTypeFactory: Encapsulate network menu creation logic
	- IRegistrar: Registration object collection interface
	- IRegistrarFactory: Create platform-independent registration object collection
	- IRegistryObject: Platform-independent registration object reference interface

## Item
> _./core/api/item_

- IItemModifier: Item modifier
- [IGun](/docs/api/core/item/IGun.md): Gun interface
- [IAttachment](/docs/api/core/item/IAttachment.md): Attachment interface
	- [IAttachmentModifier](/docs/api/core/item/attachment/modifier/IAttachmentModifier.md): Attachment modifier
- [IAmmo](/docs/api/core/item/IAmmo.md): Ammo interface
	- [IAmmoBox](/docs/api/core/item/IAmmoBox.md): Ammo box interface
- IBlock: Block item interface

### Gun
> _./core/api/item/gun_

- [IGunGetter](/docs/api/core/item/gun/IGunGetter.md): Get `IGun`
- [\_IGunPropertyAccess](/docs/api/core/item/gun/_IGunPropertyAccess.md): For use by third-party scripts only.

### Attachment
> _./core/api/item/attachment_

- [IAttachmentGetter](/docs/api/core/item/attachment/IAttachmentGetter.md): Get `IAttachment`

### Ammo
> _./core/api/item/ammo_

- [IAmmoGetter](/docs/api/core/item/ammo/IAmmoGetter.md): Get `IAmmo`

#### Ammo box
> _./core/api/item/ammobox_

- [IAmmoBoxGetter](/docs/api/core/item/ammobox/IAmmoBoxGetter.md): Get `IAmmoBoxGetter`

### Block item
> _./core/api/item/block_

- [IBlockGetter](/docs/api/core/item/block/IBlockGetter.md): Get `IBlock`

## Minecraft
> _./core/api/minecraft_

- [IMcRegistry](/docs/api/core/minecraft/IMcRegistry.md): `ResourceLocation` operation, query mod loading, get `MinecraftServer`

### Capability system
> _./core/api/minecraft/capability_

- [IInventoryCapability](/docs/api/core/minecraft/capability/IInventoryCapability.md): Equivalent to `IItemHandler`, encapsulates inventory operations

## Network
> _./core/api/network_

- [INetworkAdapter](/docs/api/core/network/INetworkAdapter.md): Abstract registration of network messages and sending messages to players implemented by platform
- [INetworkHook](/docs/api/core/network/INetworkHook.md): Network hook, used to open container GUI

### Network message
> _./core/api/network/message_

- IMessage: Network message interface

## Gun Projectile
> _./core/api/projectile_

- [IProjectileManager](/docs/api/core/projectile/IProjectileManager.md): Gun projectile manager (almighty easy-to-use facade)
	> - [IProjectileMainManager](/docs/api/core/projectile/IProjectileMainManager.md): Gun projectile main manager, provides sub-managers and hot-swapping
- [IProjectileSubManager](/docs/api/core/projectile/IProjectileSubManager.md): Gun projectile sub-manager (isomorphic to main/sub manager, i.e., `IProjectileManager` is also `IProjectileSubManager`)
	- [ProjectileManagerGroup](/docs/api/core/projectile/ProjectileManagerGroup.md): Gun projectile manager group, identifies sub-manager combinations by `managerGroupTag`
	- Effect:
		- [IProjectileEffectManager](/docs/api/core/projectile/effect/IProjectileEffectManager.md): Gun projectile effect manager (generates visual/flair effects)
	- Impact:
		- [IProjectileImpactManager](/docs/api/core/projectile/impact/IProjectileImpactManager.md): Gun projectile impact manager (handles effects caused by interactions between projectile and targets)
	- Physics:
		- [IProjectilePhysicsManager](/docs/api/core/projectile/physics/IProjectilePhysicsManager.md): Gun projectile physics manager (calculates physical states)
	- Process:
		- [IProjectileProcessManager](/docs/api/core/projectile/process/IProjectileProcessManager.md): Gun projectile process manager (orchestrates other sub-managers)
- [IProjectileRuntime](/docs/api/core/projectile/IProjectileRuntime.md): Gun projectile runtime interface, aggregating Gun projectile sub-manager Runtime interfaces

## Recipe
> _./core/api/recipe_

- RecipeResultType: Recipe result type

## Resource
> _./core/api/resource_

- [ResourceApi](/docs/api/core/resource/ResourceApi.md): Resource API (almighty easy-to-use facade), provides Datapack resource retrieval

### Datapack
> _./core/api/resource/data_

- [DataFolderType](/docs/api/core/resource/data/DataFolderType.md): Datapack folder type

## Script
> _./core/api/script_

## Sound
> _./core/api/sound_