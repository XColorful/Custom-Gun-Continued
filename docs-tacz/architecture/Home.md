[English](#English)

# 架构总览

> 本文档作为项目架构的导航索引

## 项目结构

基于`com.tacz.guns`顶层包的模块划分

### API
> _./api_

- [API索引](./api/api-index.md)：通过接口分类和职责介绍进行筛选
- [事件API](./api/event-api.md)：模组的自定义事件类型、模组的事件机制及其注册监听方式

### 方块
> _./block_

- TargetBlock：标靶方块
- AbstractGunSmithTableBlock：枪械合成台基类
- GunSmithTableBlockA/B/C：三种尺寸的枪械合成台
- StatueBlock：雕像方块
- entity：方块实体

### 客户端
> _./client_

#### 动画
> _./client/animation_

- statemachine：动画状态机
- third：第三人称动画
- screen：改装界面动画

#### 事件
> _./client/event_

客户端事件处理：
- CameraSetupEvent：相机设置事件
- ClientHitMark：命中标记事件
- ClientPreventGunClick：阻止枪械点击事件
- CommonNetworkCacheEvent：网络缓存事件
- FirstPersonRenderEvent：第一人称渲染事件
- FirstPersonRenderGunEvent：第一人称枪械渲染事件
- InventoryEvent：背包事件
- PlayerHurtByGunEvent：玩家被枪击事件
- PreventsHotbarEvent：阻止快捷栏事件
- RefreshClonePlayerDataEvent：刷新克隆玩家数据事件
- ReloadResourceEvent：重载资源事件
- RenderCrosshairEvent：渲染准星事件
- RenderHeadShotAABB：渲染爆头碰撞盒
- TickAnimationEvent：动画 Tick 事件
- TooltipEvent：提示框事件

#### 游戏操作
> _./client/gameplay_

客户端本地玩家切面实现，处理玩家输入对应的射击逻辑：
- LocalPlayerAim：瞄准
- LocalPlayerBolt：拉栓
- LocalPlayerCrawl：趴下
- LocalPlayerDraw：拔枪
- LocalPlayerFireSelect：开火模式切换
- LocalPlayerInspect：检视
- LocalPlayerMelee：近战
- LocalPlayerReload：换弹
- LocalPlayerShoot：射击
- LocalPlayerSprint：冲刺
- LocalPlayerDataHolder：本地玩家数据持有

#### GUI
> _./client/gui_

- GunRefitScreen：枪械改装界面
- GunSmithTableScreen：枪械合成台界面
- GunPackProgressScreen：枪包加载进度界面
- overlay：HUD 覆盖层
	- GunHudOverlay：枪械 HUD（弹药、开火模式等）
	- HeatBarOverlay：热量条
	- KillAmountOverlay：击杀数
	- InteractKeyTextOverlay：交互键提示
- components：GUI 组件（refit、smith）
- compat：Cloth Config 兼容界面
- toast：升级提示

#### 初始化
> _./client/init_

- ClientSetupEvent：客户端初始化
	- 注册键位
	- GUI 覆盖层
	- 资源重载监听
	- 提示框组件工厂
- ModEntitiesRender：实体渲染器注册
- ParticleFactoryRegistry：粒子工厂注册
- ModContainerScreen：模组配置界面

#### 按键
> _./client/input_

- AimKey：瞄准键
- ShootKey：射击键
- ReloadKey：换弹键
- MeleeKey：近战键
- FireSelectKey：开火模式切换键
- InspectKey：检视键
- CrawlKey：趴下键
- InteractKey：交互键
- RefitKey：改装键
- ZoomKey：开镜键
- ConfigKey：配置键

#### 模型
> _./client/model_

- bedrock：基岩版模型类
- BedrockGunModel：枪械基岩模型
- BedrockAmmoModel：子弹基岩模型
- BedrockAttachmentModel：配件基岩模型
- BedrockAnimatedModel：动画基岩模型
- FunctionalBedrockPart：功能性模型部件
- SlotModel：插槽模型
- GunModelConstant：模型常量
- IFunctionalRenderer：功能性渲染器接口
- functional：功能性渲染器（AttachmentRender、BeamRenderer、MuzzleFlashRender、ShellRender 等）
- listener：模型监听器（camera、constraint、model）
- papi：Placeholder API 支持

#### 粒子
> _./client/particle_

- AmmoParticleSpawner：子弹粒子生成器
- BulletHoleParticle：弹孔粒子

#### 渲染器
> _./client/renderer_

- item：物品渲染器
- entity：实体渲染器
- block：方块渲染器
- crosshair：准星渲染
- other：其他渲染

#### 资源
> _./client/resource_

客户端资源包管理：
- ClientAssetsManager：客户端资源管理器
- ClientAssetLoadDispatcher：资源加载分发器
- ClientIndexManager：客户端索引管理器
- GunDisplayInstance：枪械显示实例
- InternalAssetLoader：内部资源加载器
- index：客户端索引
- manager：资源子管理器
	- DisplayManager
	- GltfManager
	- PackInfoManager
- pojo：资源包 POJO
	- display：显示数据（GunDisplay、AmmoDisplay、AttachmentDisplay、BlockDisplay 等）
	- animation：动画数据（BedrockAnimation、gltf）
	- model：模型数据（BedrockModelPOJO 等）
- serialize：客户端资源序列化器

#### 声音
> _./client/sound_

- GunSoundInstance：枪械声音实例（基于资源路径的声音）
- EntityTrackingGunSoundInstance：实体跟踪枪械声音实例
- SoundPlayManager：客户端声音播放管理器
- GunSoundPreload：声音预加载

#### 提示框
> _./client/tooltip_

- ClientGunTooltip：客户端枪械提示框
- ClientAmmoBoxTooltip：客户端子弹盒提示框
- ClientAttachmentItemTooltip：客户端配件提示框

### 指令
> _./command_

- RootCommand：服务端指令根节点（_/tacz_）
- sub：子命令
	- AttachmentLockCommand：配件锁定指令
	- ConfigCommand：配置指令
	- ConvertCommand：转换指令
	- DebugCommand：调试指令
	- DummyAmmoCommand：虚拟子弹指令
	- HideTooltipPartCommand：隐藏提示框指令
	- ListPackCommand：枪包列表指令
	- OverwriteCommand：覆写指令
	- ReloadCommand：重载指令

### 模组联动
> _./compat_

- ar：加速渲染兼容
- carryon：Carry On 模组兼容
- cloth：Cloth Config 兼容
- controllable：Controllable 模组兼容
- jei：JEI 物品管理器兼容
- kubejs：KubeJS 脚本兼容
- oculus：Oculus 光影兼容
- optifine：OptiFine 兼容
- playeranimator：Player Animator 兼容
- shouldersurfing：Shoulder Surfing 兼容

### 配置
> _./config_

- common：通用配置项
	- AmmoConfig：子弹配置
	- GunConfig：枪械配置
	- OtherConfig：杂项配置
- client：客户端配置项
	- KeyConfig：按键配置
	- RenderConfig：渲染配置
	- ResourceConfig：资源配置
	- SoundConfig：声音配置
	- ZoomConfig：开镜配置
- sync：同步配置
	- SyncConfig：网络同步配置
- util：配置工具
	- HeadShotAABBConfigRead：爆头碰撞盒配置读取
	- InteractKeyConfigRead：交互键配置读取

### 合成
> _./crafting_

- GunSmithTableRecipe：枪械合成配方
- GunSmithTableIngredient：合成材料
- GunSmithTableSerializer：配方序列化器
- result：合成结果

### 调试
> _./debug_

- GunMeleeDebug：枪械近战调试工具

### 实体
> _./entity_

- EntityKineticBullet：枪射物实体（动能弹）
> 迁移映射为 [枪射物](/docs/architecture/core/Home.md#枪射物)
- shooter：射手实体切面
	> 迁移映射为 [Living Shooter (aspect)](/docs/architecture/core/Home.md#实体)
	- LivingEntityAim：瞄准
	- LivingEntityAmmoCheck：子弹检查
	- LivingEntityBolt：拉栓
	- LivingEntityCrawl：趴下
	- LivingEntityDrawGun：拔枪
	- LivingEntityFireSelect：开火模式切换
	- LivingEntityHeat：热量
	- LivingEntityMelee：近战
	- LivingEntityReload：换弹
	- LivingEntityShoot：射击
	- LivingEntitySpeedModifier：速度修改
	- LivingEntitySprint：冲刺
	- ShooterDataHolder：射手数据持有
		> 迁移映射为 [ShooterProperty](/docs/architecture/core/Home.md#实体)
- TargetMinecart：标靶矿车
- sync：实体数据同步

### 事件
> _./event_

服务端事件处理：
- ChangeGunPropertyEvent：修改枪械属性事件
- EntityDamageEvent：实体伤害事件
- HitboxHelperEvent：碰撞箱辅助事件
- KnockbackChange：击退修改事件
- LoadingConfigEvent：加载配置事件
- PlayerRespawnEvent：玩家重生事件
- PreventGunClick：阻止枪械点击事件
- ServerTickEvent：服务端 Tick 事件
- SyncBaseTimestamp：同步基准时间戳
- SyncedEntityDataEvent：同步实体数据事件
- TravelToDimensionEvent：跨维度事件
- CommonLoadPack：双端加载枪包
- ammo：子弹事件（BellRing、DestroyGlassBlock）

### 初始化
> _./init_

- ModBlocks：模组方块
- ModCreativeTabs：创造模式栏
- ModDamageTypes：模组伤害类型
- ModEntities：模组实体
- ModItems：模组物品
- ModParticles：模组粒子
- ModRecipe：模组配方
- ModSounds：模组声音
- ModPainting：模组画作
- ModLootModifiers：战利品修改器
- ModAttributes：模组属性
- ModContainer：模组容器
- CommonRegistry：双端初始化（注册属性、资源包加载）
- CapabilityRegistry：Capability 注册
- CommandRegistry：指令注册
- CompatRegistry：联动初始化

### 背包
> _./inventory_

- GunSmithTableMenu：枪械合成台 GUI 容器
- tooltip：提示框数据

### 物品
> _./item_

- AmmoItem：子弹物品
- AmmoBoxItem：子弹盒物品
- AttachmentItem：配件物品
- ModernKineticGunItem：现代动能枪械物品（核心枪械实现）
> 迁移映射为 [枪械](/docs/architecture/core/Home.md#枪械)
- ModernKineticGunScriptAPI：枪械脚本 API
- GunSmithTableItem：合成台物品
- DefaultTableItem：默认台物品
- TargetMinecartItem：标靶矿车物品
- GunTooltipPart：枪械提示框组件

### 战利品
> _./loot_

- LootTableInjectorModifier：战利品表注入修改器

### Mixin
> _./mixin_

- common：双端 Mixin
- client：客户端 Mixin

### 网络
> _./network_

- NetworkHandler：网络处理器（注册网络消息、发送消息）
- LoginIndexHolder：登录索引持有
- IMessage：网络消息接口
- message：网络消息
	- event：事件消息
	- handshake：握手消息
	- 客户端 → 服务端消息
	- 服务端 → 客户端消息

### 粒子
> _./particles_

- BulletHoleOption：弹孔粒子选项

### 资源
> _./resource_

数据包加载与管理：
- CommonAssetsManager：双端资源管理器
- ICommonResourceProvider：资源提供接口
- GunPackLoader：枪包加载器
- PackConvertor：枪包格式转换器
- PackMeta：枪包元数据
- filter：数据过滤器
- index：数据索引
- manager：数据管理器
- modifier：属性修改器
	- AttachmentCacheProperty：配件缓存属性
- network：资源网络同步
- pojo：数据 POJO
	- data：数据定义（gun、attachment、block、recipe、loot）
- serialize：数据序列化器

### 声音
> _./sound_

- SoundManager：双端声音管理器（根据枪械状态播放对应声音）

### 工具
> _./util_

- AllowAttachmentTagMatcher：配件标签匹配
- AttachmentDataUtils：配件数据工具
- ColorHex：颜色工具
- CycleTaskHelper：循环任务辅助
- DelayedTask：延迟任务
- EntityUtil：实体工具（射线追踪）
- ExplodeUtil：爆炸工具
- HitboxHelper：碰撞箱辅助
- InputExtraCheck：输入额外检查
- LaserColorUtil：激光颜色工具
- RenderDistance：渲染距离
- RenderHelper：渲染辅助
- ResourceScanner：资源扫描器
- TacHitResult：命中结果
- block：方块工具
- datafixer：数据修复工具
- math：数学工具
- GetJarResources：Jar 资源读取
- Md5Utils：MD5 工具
- PathHandler：路径处理
- TacPathVisitor：文件路径遍历

# English

> This document serves as a navigation index for the project architecture

## Project Structure

Module division based on the `com.tacz.guns` top-level package

### API
> _./api_

- [API Index](./api/api-index.md#English): Filter by interface classification and responsibility introduction
- [Event API](./api/event-api.md#English): Mod's custom event types, mod's event mechanism, and its registration and listening methods

### Block
> _./block_

- TargetBlock: Target block
- AbstractGunSmithTableBlock: Gun smith table base class
- GunSmithTableBlockA/B/C: Three sizes of gun smith tables
- StatueBlock: Statue block
- entity: Block entities

### Client
> _./client_

#### Animation
> _./client/animation_

- statemachine: Animation state machine
- third: Third-person animation
- screen: Refit screen animation

#### Event
> _./client/event_

Handle client events:
- CameraSetupEvent: Camera setup event
- ClientHitMark: Hit mark event
- ClientPreventGunClick: Prevent gun click event
- CommonNetworkCacheEvent: Network cache event
- FirstPersonRenderEvent: First person render event
- FirstPersonRenderGunEvent: First person gun render event
- InventoryEvent: Inventory event
- PlayerHurtByGunEvent: Player hurt by gun event
- PreventsHotbarEvent: Prevent hotbar event
- RefreshClonePlayerDataEvent: Refresh clone player data event
- ReloadResourceEvent: Reload resource event
- RenderCrosshairEvent: Render crosshair event
- RenderHeadShotAABB: Render headshot AABB
- TickAnimationEvent: Animation tick event
- TooltipEvent: Tooltip event

#### Gameplay
> _./client/gameplay_

Client local player aspect implementations, handling player input-driven shooting logic:
- LocalPlayerAim: Aim
- LocalPlayerBolt: Bolt
- LocalPlayerCrawl: Crawl
- LocalPlayerDraw: Draw gun
- LocalPlayerFireSelect: Fire mode select
- LocalPlayerInspect: Inspect
- LocalPlayerMelee: Melee
- LocalPlayerReload: Reload
- LocalPlayerShoot: Shoot
- LocalPlayerSprint: Sprint
- LocalPlayerDataHolder: Local player data holder

#### GUI
> _./client/gui_

- GunRefitScreen: Gun refit screen
- GunSmithTableScreen: Gun smith table screen
- GunPackProgressScreen: Gun pack loading progress screen
- overlay: HUD overlays
	- GunHudOverlay: Gun HUD (ammo, fire mode, etc.)
	- HeatBarOverlay: Heat bar
	- KillAmountOverlay: Kill count
	- InteractKeyTextOverlay: Interact key hint
- components: GUI components (refit, smith)
- compat: Cloth Config compatible screen
- toast: Level-up toast

#### Initialization
> _./client/init_

- ClientSetupEvent: Client initialization
	- Register key mappings
	- GUI overlays
	- Resource reload listener
	- Tooltip component factories
- ModEntitiesRender: Entity renderer registration
- ParticleFactoryRegistry: Particle factory registration
- ModContainerScreen: Mod config screen

#### Input
> _./client/input_

- AimKey: Aim key
- ShootKey: Shoot key
- ReloadKey: Reload key
- MeleeKey: Melee key
- FireSelectKey: Fire mode select key
- InspectKey: Inspect key
- CrawlKey: Crawl key
- InteractKey: Interact key
- RefitKey: Refit key
- ZoomKey: Zoom key
- ConfigKey: Config key

#### Model
> _./client/model_

- bedrock: Bedrock model classes
- BedrockGunModel: Gun bedrock model
- BedrockAmmoModel: Ammo bedrock model
- BedrockAttachmentModel: Attachment bedrock model
- BedrockAnimatedModel: Animated bedrock model
- FunctionalBedrockPart: Functional model part
- SlotModel: Slot model
- GunModelConstant: Model constants
- IFunctionalRenderer: Functional renderer interface
- functional: Functional renderers (AttachmentRender, BeamRenderer, MuzzleFlashRender, ShellRender, etc.)
- listener: Model listeners (camera, constraint, model)
- papi: Placeholder API support

#### Particle
> _./client/particle_

- AmmoParticleSpawner: Ammo particle spawner
- BulletHoleParticle: Bullet hole particle

#### Renderer
> _./client/renderer_

- item: Item renderers
- entity: Entity renderers
- block: Block renderers
- crosshair: Crosshair render
- other: Other renderers

#### Resource
> _./client/resource_

Client resource pack management:
- ClientAssetsManager: Client assets manager
- ClientAssetLoadDispatcher: Asset load dispatcher
- ClientIndexManager: Client index manager
- GunDisplayInstance: Gun display instance
- InternalAssetLoader: Internal asset loader
- index: Client indices
- manager: Asset sub-managers
	- DisplayManager
	- GltfManager
	- PackInfoManager
- pojo: Resource pack POJOs
	- display: Display data (GunDisplay, AmmoDisplay, AttachmentDisplay, BlockDisplay, etc.)
	- animation: Animation data (BedrockAnimation, gltf)
	- model: Model data (BedrockModelPOJO, etc.)
- serialize: Client resource serializers

#### Sound
> _./client/sound_

- GunSoundInstance: Gun sound instance (resource-path based sound)
- EntityTrackingGunSoundInstance: Entity tracking gun sound instance
- SoundPlayManager: Client sound play manager
- GunSoundPreload: Sound preloading

#### Tooltip
> _./client/tooltip_

- ClientGunTooltip: Client gun tooltip
- ClientAmmoBoxTooltip: Client ammo box tooltip
- ClientAttachmentItemTooltip: Client attachment tooltip

### Command
> _./command_

- RootCommand: Server root command node (`/tacz`)
- sub: Sub commands
	- AttachmentLockCommand: Attachment lock command
	- ConfigCommand: Config command
	- ConvertCommand: Convert command
	- DebugCommand: Debug command
	- DummyAmmoCommand: Dummy ammo command
	- HideTooltipPartCommand: Hide tooltip part command
	- ListPackCommand: List gun pack command
	- OverwriteCommand: Overwrite command
	- ReloadCommand: Reload command

### Mod Compat
> _./compat_

- ar: Accelerated rendering compat
- carryon: Carry On mod compat
- cloth: Cloth Config compat
- controllable: Controllable mod compat
- jei: JEI compat
- kubejs: KubeJS script compat
- oculus: Oculus shader compat
- optifine: OptiFine compat
- playeranimator: Player Animator compat
- shouldersurfing: Shoulder Surfing compat

### Config
> _./config_

- common: Common config items
	- AmmoConfig: Ammo config
	- GunConfig: Gun config
	- OtherConfig: Miscellaneous config
- client: Client config items
	- KeyConfig: Key config
	- RenderConfig: Render config
	- ResourceConfig: Resource config
	- SoundConfig: Sound config
	- ZoomConfig: Zoom config
- sync: Sync config
	- SyncConfig: Network sync config
- util: Config utilities
	- HeadShotAABBConfigRead: Headshot AABB config reader
	- InteractKeyConfigRead: Interact key config reader

### Crafting
> _./crafting_

- GunSmithTableRecipe: Gun smith table recipe
- GunSmithTableIngredient: Table ingredient
- GunSmithTableSerializer: Recipe serializer
- result: Crafting results

### Debug
> _./debug_

- GunMeleeDebug: Gun melee debug utility

### Entity
> _./entity_

- EntityKineticBullet: Gun projectile entity (kinetic bullet)
> Migration mapped to [Gun Projectile](/docs/architecture/core/Home.md#Projectile)
- shooter: Living shooter aspects
	> Migration mapped to [Living Shooter (aspect)](/docs/architecture/core/Home.md#Entity)
	- LivingEntityAim: Aim
	- LivingEntityAmmoCheck: Ammo check
	- LivingEntityBolt: Bolt
	- LivingEntityCrawl: Crawl
	- LivingEntityDrawGun: Draw gun
	- LivingEntityFireSelect: Fire mode select
	- LivingEntityHeat: Heat
	- LivingEntityMelee: Melee
	- LivingEntityReload: Reload
	- LivingEntityShoot: Shoot
	- LivingEntitySpeedModifier: Speed modifier
	- LivingEntitySprint: Sprint
	- ShooterDataHolder: Shooter data holder
		> Migration mapped to [ShooterProperty](/docs/architecture/core/Home.md#Entity)
- TargetMinecart: Target minecart
- sync: Entity data sync

### Event
> _./event_

Handle server events:
- ChangeGunPropertyEvent: Change gun property event
- EntityDamageEvent: Entity damage event
- HitboxHelperEvent: Hitbox helper event
- KnockbackChange: Knockback change event
- LoadingConfigEvent: Loading config event
- PlayerRespawnEvent: Player respawn event
- PreventGunClick: Prevent gun click event
- ServerTickEvent: Server tick event
- SyncBaseTimestamp: Sync base timestamp
- SyncedEntityDataEvent: Synced entity data event
- TravelToDimensionEvent: Travel to dimension event
- CommonLoadPack: Dual-side load gun pack
- ammo: Ammo events (BellRing, DestroyGlassBlock)

### Initialization
> _./init_

- ModBlocks: Mod blocks
- ModCreativeTabs: Creative mode tabs
- ModDamageTypes: Mod damage types
- ModEntities: Mod entities
- ModItems: Mod items
- ModParticles: Mod particles
- ModRecipe: Mod recipes
- ModSounds: Mod sounds
- ModPainting: Mod paintings
- ModLootModifiers: Loot modifiers
- ModAttributes: Mod attributes
- ModContainer: Mod container
- CommonRegistry: Common setup (register attributes, resource pack loading)
- CapabilityRegistry: Capability registration
- CommandRegistry: Command registration
- CompatRegistry: Compat initialization

### Inventory
> _./inventory_

- GunSmithTableMenu: Gun smith table GUI container
- tooltip: Tooltip data

### Item
> _./item_

- AmmoItem: Ammo item
- AmmoBoxItem: Ammo box item
- AttachmentItem: Attachment item
- ModernKineticGunItem: Modern kinetic gun item (core gun implementation)
> Migration mapped to [Gun](/docs/architecture/core/Home.md#Gun)
- ModernKineticGunScriptAPI: Gun script API
- GunSmithTableItem: Smith table item
- DefaultTableItem: Default table item
- TargetMinecartItem: Target minecart item
- GunTooltipPart: Gun tooltip part

### Loot
> _./loot_

- LootTableInjectorModifier: Loot table injector modifier

### Mixin
> _./mixin_

- common: Dual-side mixins
- client: Client mixins

### Network
> _./network_

- NetworkHandler: Network handler (register messages, send messages)
- LoginIndexHolder: Login index holder
- IMessage: Network message interface
- message: Network messages
	- event: Event messages
	- handshake: Handshake messages
	- Client → Server messages
	- Server → Client messages

### Particle
> _./particles_

- BulletHoleOption: Bullet hole particle option

### Resource
> _./resource_

Datapack loading and management:
- CommonAssetsManager: Common asset manager
- ICommonResourceProvider: Resource provider interface
- GunPackLoader: Gun pack loader
- PackConvertor: Gun pack format converter
- PackMeta: Gun pack metadata
- filter: Data filters
- index: Data indices
- manager: Data managers
- modifier: Property modifiers
	- AttachmentCacheProperty: Attachment cache property
- network: Resource network sync
- pojo: Data POJOs
	- data: Data definitions (gun, attachment, block, recipe, loot)
- serialize: Data serializers

### Sound
> _./sound_

- SoundManager: Dual-side sound manager (plays corresponding sounds based on gun state)

### Utility
> _./util_

- AllowAttachmentTagMatcher: Attachment tag matching
- AttachmentDataUtils: Attachment data utilities
- ColorHex: Color utilities
- CycleTaskHelper: Cycle task helper
- DelayedTask: Delayed task
- EntityUtil: Entity utilities (ray trace)
- ExplodeUtil: Explosion utilities
- HitboxHelper: Hitbox helper
- InputExtraCheck: Input extra check
- LaserColorUtil: Laser color utilities
- RenderDistance: Render distance
- RenderHelper: Render helper
- ResourceScanner: Resource scanner
- TacHitResult: Hit result
- block: Block utilities
- datafixer: Data fix utilities
- math: Math utilities
- GetJarResources: Jar resource reading
- Md5Utils: MD5 utilities
- PathHandler: Path handling
- TacPathVisitor: File path visitor
