[English](#English)

# 架构总览

> 本文档作为项目架构的导航索引

## 项目结构

基于`dev.xcolorful.customgun.core`顶层包的模块划分

### API
> _./core/api_

- [API索引](./api/api-index.md)：通过接口分类和职责介绍进行筛选，并传送至 _./docs/api_ 下内容以查看详细参数
- [事件API](./api/event-api.md)：模组的自定义事件类型、模组的事件机制及其注册监听方式

### 指令
> _./core/command_

- [服务端指令](./command/server-command.md)
- [指令参数名列表](./command/command-arg.md)
- 命令：
	- AttachmentLockCommand：[配件锁定指令](/docs/wiki/command/Attachment%20lock%20command.md)
	- ConfigCommand：[配置指令](/docs/wiki/command/Config%20command.md)
	- ConvertCommand：[转换指令](/docs/wiki/command/Convert%20command.md)
	- DebugCommand：[调试指令](/docs/wiki/command/Debug%20command.md)
	- DummyAmmoCommand：[虚拟子弹指令](/docs/wiki/command/Dummy%20ammo%20command.md)
	- HideTooltipPartCommand：[隐藏提示框指令](/docs/wiki/command/Hide%20tooltip%20part%20command.md)
	- ListPackCommand：[枪包列表指令](/docs/wiki/command/List%20pack%20command.md)
	- ReloadCommand：[重载指令](/docs/wiki/command/Reload%20command.md)

### 模组联动
> _./core/compat_

### 配置
> _./core/config_

- AmmoConfig：子弹配置
- GunConfig：枪械配置
- OtherConfig：杂项配置
- SyncConfig：同步配置

### 实体
> _./core/entity_

- Gun Projectile：枪射物
	- GunProjectile：枪射物（实体）
- Living Shooter (aspect)：射手生物（切面）
	> [射手框架](./entity/shooter/shooter-framework.md)
	- LivingShooterAim：瞄准
	- LivingShooterAmmoCheck：子弹检查
	- LivingShooterBolt：拉栓
	- LivingShooterProne：趴下
	- LivingShooterDrawGun：拔枪
	- LivingShooterSwitchFireMode：切换开火模式
	- LivingShooterHeat：热量
	- LivingShooterMelee：近战
	- LivingShooterReload：换弹
	- LivingShooterShoot：射击
	- ~~LivingShooterSpeedModifier：速度修改~~
	- LivingShooterSprint：冲刺
	- modifier：[射手修饰框架](./entity/shooter/modifier/Home.md)
		- ShooterGunModifierManager：射手枪械修饰管理器
	- player：射手玩家
	- world：游戏内世界相关操作
- Bullet Victim：受弹实体
	- BulletVictimKnockback：击退处理
- LivingShooterSyncHandler：同步射手生物数据



### 事件
> _./core/event_

> [事件API](./api/event-api.md)

- [EventDispatcher](./event/event-dispatcher.md)：事件派发器
- [EventPoster](./event/event-poster.md)：事件分发器
- [EventRegister](./event/event-register.md)：事件注册器
- Custom：自定义事件注册
	- CoreEventHandlers：服务端事件注册

### 枪械
> _./core/gun_

[枪械框架](./gun/gun-framework.md)：
- [GunManager](./gun/gun-manager.md)：枪械管理器（全能易用门面）

🤔深度定制枪械：[注册枪械管理器组](./gun/register-gun-manager-group.md)
- Action：枪械动作
	- [GunActionManager](./gun/action/gun-action-manager.md)：枪械动作管理器
- Attack：枪械攻击
	- [GunAttackManager](./gun/attack/gun-attack-manager.md)：枪械攻击管理器
- Inventory：枪械背包
	- [GunInventoryManager](./gun/inventory/gun-inventory-manager.md)：枪械背包管理器
- Script：枪械脚本
	- [GunScriptManager](./gun/script/gun-script-manager.md)：枪械脚本管理器
	> [枪械脚本框架](./gun/script/gun-script-framework.md)
- State：枪械状态
	- [GunStateManager](./gun/state/gun-state-manager.md)：枪械状态管理器

### GUI
> _./core/gui_

- Tooltip：提示框
	- AmmoBoxTooltip：子弹盒提示框
	- AttachmentTooltip：配件提示框
	- GunTooltip：枪械提示框

### 初始化
> _./core/init_

- Registry：模组注册表
	- ModCreativeTabs：创造模式栏
	- ModEntities：模组实体
	- ModItems：模组物品
	- ModParticles：模组粒子
	- ModRecipe：模组配方
	- ModSounds：模组声音
- CapabilityRegistry：Capability注册
- CommandRegistry：指令注册
- CommonSetup：双端设置
- ModConfig：模组配置初始化
- ModEvent：模组事件

### 物品
> _./core/item_

- Ammo：子弹
	- [AmmoItem](./item/ammo/ammo-item.md)：子弹物品
- AmmoBox：子弹盒
	- [AmmoBoxItem](./item/ammobox/ammo-box-item.md)：子弹盒物品
- Attachment：配件
	- [AttachmentItem](./item/attachment/attachment-item.md)：配件物品
	- modifier：配件修饰工具
- Gun：枪械
	- [GunItem](./item/gun/gun-item.md)：枪械物品

### Mixin
> _./core/mixin_

- Entity：实体
- Network：网络消息

### 网络
> _./core/network_

- Message：网络消息
	- event：事件消息
	- handshake：握手消息
	- 客户端->服务端消息
	- 服务端->客户端消息
- NetworkHandler：网络处理器（注册网络消息、发送消息）
- NetworkHook：网络钩子

### 粒子
> _./core/particle_

- BulletHoleOption：弹孔粒子选项

### 枪射物
> _./core/projectile_

[枪射物框架](./projectile/projectile-framework.md)：
- [ProjectileManager](./projectile/projectile-manager.md)：枪射物管理器（全能易用门面）

🤔深度定制枪射物：[注册枪射物管理器组](./projectile/register-projectile-manager-group.md)
- Effect：枪射物效果
	- [ProjectileEffectManager](./projectile/effect/projectile-effect-manager.md)：枪射物效果管理器
- Impact：枪射物作用
	- [ProjectileImpactManager](./projectile/impact/projectile-impact-manager.md)：枪射物作用管理器
- Physics：枪射物物理
	- [ProjectilePhysicsManager](./projectile/physics/projectile-physics-manager.md)：枪射物物理理管理器
- Process：枪射物进程
	- [ProjectileProcessManager](./projectile/process/projectile-process-manager.md)：枪射物进程管理器

### 配方
> _./core/recipe_

- TableIngredient：合成材料
- TableRecipe：合成配方
- TableRecipeSerializer：合成配方序列化器
- TableResult：合成结果

### 资源
> _./core/resource_

[资源文件说明](/docs/wiki/resource/Resource%20introduction.md#数据包)：
- Data：数据包POJO
- Instance：POJO对象
	- Data：数据包POJO对象
- Network：数据同步
	- SyncDataType：同步的数据包POJO类型

### 声音
> _./core/sound_

- SoundManager：声音管理器

### 文本
> _./core/text_

- Placeholder：占位符

### 工具
> _./core/util_

> 统一封装项目通用能力
> 
> 当存在对应工具时，必须优先使用工具提供的统一接口，而非直接调用平台或 Minecraft API，以保持一致性与兼容性

- ChatUtils：聊天栏消息、标题、动作栏统一发送入口
- ClassUtils：基本数据结构，用于简化算法
	- 已封装组合数据结构：`ArraySet`、`QueueSet`、`ArrayMap`
- ColorUtils：处理颜色字符串、游戏内颜色等
- ComponentUtils：文本组件工具
- FileUtils：文件工具
- IngredientUtils：`Ingredient.of()` 重载工具
- JsonUtils：JSON 序列化封装
- MathUtil：数学工具
- NBTUtils：NBT 统一封装
	- 必须使用此处而非直接调用 Minecraft `CompoundTag` API，以保持兼容性
- NetworkUtils：`FriendlyByteBuf` 相关兼容封装
	- 对于已封装的 `FriendlyByteBuf` 操作，必须使用此处
	- 未封装的方法直接调用 `FriendlyByteBuf` 即可
- RayTraceUtils：射线追踪工具
- ScriptUtils：LuaJ工具
- SendUtils：网络消息统一发送入口
	- 所有消息发送均通过此处，与网络实现（`NetworkHandler`）解耦
- WorldUtils：游戏内世界相关操作

# English

> This document serves as a navigation index for the project architecture

## Project Structure

Module division based on the `dev.xcolorful.customgun.core` top-level package

### API
> _./core/api_

- [API Index](./api/api-index.md#English): Filter by interface classification and responsibility introduction, and link to content under _./docs/api_ to view detailed parameters
- [Event API](./api/event-api.md#English): Mod's custom event types, mod's event mechanism, and its registration and listening methods

### Command
> _./core/command_

- [Server Command](./command/server-command.md#English)
- [Command Argument List](./command/command-arg.md#English)
- Commands:
	- AttachmentLockCommand: [Attachment lock command](/docs/wiki/command/Attachment%20lock%20command.md#English)
	- ConfigCommand: [Config command](/docs/wiki/command/Config%20command.md#English)
	- ConvertCommand: [Convert command](/docs/wiki/command/Convert%20command.md#English)
	- DebugCommand: [Debug command](/docs/wiki/command/Debug%20command.md#English)
	- DummyAmmoCommand: [Dummy ammo command](/docs/wiki/command/Dummy%20ammo%20command.md#English)
	- HideTooltipPartCommand: Hide tooltip part command
	- ListPackCommand: [List pack command](/docs/wiki/command/List%20pack%20command.md#English)
	- ReloadCommand: [Reload command](/docs/wiki/command/Reload%20command.md#English)

### Mod Compat
> _./core/compat_

### Config
> _./core/config_

- AmmoConfig: Ammo config
- GunConfig: Gun config
- OtherConfig: Miscellaneous config
- SyncConfig: Sync config

### Entity
> _./core/entity_

- Gun Projectile:
	- GunProjectile: Gun projectile (entity)
- Living Shooter (aspect):
	> [Shooter framework](./entity/shooter/shooter-framework.md#English)
	- LivingShooterAim: Aim
	- LivingShooterAmmoCheck: Ammo check
	- LivingShooterBolt: Bolt
	- LivingShooterProne: Prone
	- LivingShooterDrawGun: Draw gun
	- LivingShooterSwitchFireMode: Switch fire mode
	- LivingShooterHeat: Heat
	- LivingShooterMelee: Melee
	- LivingShooterReload: Reload
	- LivingShooterShoot: Shoot
	- ~~LivingShooterSpeedModifier: Speed modifier~~
	- LivingShooterSprint: Sprint
	- modifier: [Shooter modifier framework](./entity/shooter/modifier/Home.md#English)
		- ShooterGunModifierManager: Shooter gun modifier manager
	- player: Player shooter
	- world: In-game world related operations
- Bullet Victim:
	- BulletVictimKnockback: Knockback handling
- LivingShooterSyncHandler: Sync Living Shooter data

### Event
> _./core/event_

> [Event API](./api/event-api.md#English)

- [EventDispatcher](./event/event-dispatcher.md#English): Event dispatcher
- [EventPoster](./event/event-poster.md#English): Event poster
- [EventRegister](./event/event-register.md#English): Event register
- Custom: Custom event registration
	- CoreEventHandlers: Server event registration

### Gun
> _./core/gun_

[Gun Framework](./gun/gun-framework.md#English):
- [GunManager](./gun/gun-manager.md#English): Gun manager (almighty easy-to-use facade)

🤔Deeply customized gun: [Register GunManagerGroup](./gun/register-gun-manager-group.md)
- Action:
	- [GunActionManager](./gun/action/gun-action-manager.md#English): Gun action manager
- Attack:
	- [GunAttackManager](./gun/attack/gun-attack-manager.md#English): Gun attack manager
- Inventory:
	- [GunInventoryManager](./gun/inventory/gun-inventory-manager.md#English): Gun inventory manager
- Script:
	- [GunScriptManager](./gun/script/gun-script-manager.md#English): Gun script manager
	> [Gun script framework](./gun/script/gun-script-framework.md#English)
- State:
	- [GunStateManager](./gun/state/gun-state-manager.md#English): Gun state manager

### GUI
> _./core/gui_

- Tooltip:
	- AmmoBoxTooltip: Ammo box tooltip
	- AttachmentTooltip: Attachment tooltip
	- GunTooltip: Gun tooltip

### Initialization
> _./core/init_

- Registry: Mod registry
	- ModCreativeTabs: Creative mode tabs
	- ModEntities: Mod entities
	- ModItems: Mod items
	- ModParticles: Mod particles
	- ModRecipe: Mod recipes
	- ModSounds: Mod sounds
- CapabilityRegistry: Capability registration
- CommandRegistry: Command registration
- CommonSetup: Common set up
- ModConfig: Mod config initialization
- ModEvent: Mod event

### Item
> _./core/item_

- Ammo:
	- [AmmoItem](./item/ammo/ammo-item.md#English): Ammo item
- AmmoBox:
	- [AmmoBoxItem](./item/ammobox/ammo-box-item.md#English): Ammo box item
- Attachment:
	- [AttachmentItem](./item/attachment/attachment-item.md#English): Attachment item
	- modifier: Attachment modifier
- Gun:
	- [GunItem](./item/gun/gun-item.md#English): Gun item

### Mixin
> _./core/mixin_

- Entity: Entity
- Network: Network messages

### Network
> _./core/network_

- Message: Network messages
	- event: Event message
	- handshake: Handshake message
	- Client -> Server message
	- Server -> Client message
- NetworkHandler: Network handler (register network messages, send messages)
- NetworkHook: Network hook

### Particle
> _./core/particle_

- BulletHoleOption: Bullet hole particle option

### Projectile
> _./core/projectile_

[Gun Projectile Framework](./projectile/projectile-framework.md#English):
- [ProjectileManager](./projectile/projectile-manager.md#English): Gun Projectile manager (almighty easy-to-use facade)

🤔Deeply customized gun projectile: [Register ProjectileManagerGroup](./projectile/register-projectile-manager-group.md#English)
- Effect:
	- [ProjectileEffectManager](./projectile/effect/projectile-effect-manager.md#English): Gun Projectile effect manager
- Impact:
	- [ProjectileImpactManager](./projectile/impact/projectile-impact-manager.md#English): Gun Projectile impact manager
- Physics:
	- [ProjectilePhysicsManager](./projectile/physics/projectile-physics-manager.md#English): Gun Projectile physics manager
- Process:
	- [ProjectileProcessManager](./projectile/process/projectile-process-manager.md#English): Gun Projectile process manager

### Recipe
> _./core/recipe_

- TableIngredient: Table ingredient
- TableRecipe: Table recipe
- TableRecipeSerializer: Table recipe serializer
- TableResult: Table result

### Resource
> _./core/resource_

[Resource-introduction](/docs/wiki/resource/Resource%20introduction.md#Datapack):
- Data: Datapack POJO
- Instance: POJO object
	- Data: Datapack POJO object
- Network: Data sync
	- SyncDataType: The type of synced datapack POJO

### Sound
> _./core/sound_

- SoundManager: Sound manager

### Text
> _./core/text_

- Placeholder

### Utility
> _./core/util_

> Shared wrappers for common project functionality.
>
> When an equivalent utility exists, it must be used instead of calling platform or Minecraft APIs directly, to preserve consistency and compatibility.

- ChatUtils: Unified entry point for sending chat messages, titles, action bars
- ClassUtils: Basic data structures, used to simplify algorithms
	- Encapsulates combined data structures: `ArraySet`, `QueueSet`, `ArrayMap`
- ColorUtils: Utilities for color strings and in-game colors
- ComponentUtils: Text component utilities
- FileUtils: File utilities
- IngredientUtils: Utilities for `Ingredient.of()` overloads
- JsonUtils: JSON serialization wrapper
- MathUtil: Math utilities
- NBTUtils: Unified NBT wrapper
	- Must be used instead of directly calling Minecraft `CompoundTag` APIs to preserve compatibility
- NetworkUtils: Compatibility wrappers for `FriendlyByteBuf`
	- Wrapped `FriendlyByteBuf` operations must use this utility
	- Operations without wrappers should call `FriendlyByteBuf` directly
- RayTraceUtils: Ray trace utilities
- ScriptUtils: LuaJ utilities
- SendUtils: Unified entry point for sending network messages
	- All network messages must be sent through this utility, decoupling callers from the underlying `NetworkHandler`
- WorldUtils: In-game world related operations
