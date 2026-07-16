[English](#English)

# API索引

## 双端
> _./api_

- DefaultAssets：默认资源常量
- GunProperties：枪械属性（缓存子弹属性）
- GunProperty：枪械属性
- TimelessAPI：资源API
> 迁移映射为 [ResourceApi](/docs/architecture/core/api/api-index.md#资源)
- CacheModifiableByScript：脚本可修改缓存
- ValueModifiableAtRuntime：运行时可修改值

## 实体
> _./api/entity_

- IGunOperator：射手生物操作接口
> 迁移映射为 [ILivingShooter](/docs/architecture/core/api/api-index.md#实体)
- ITargetEntity：受弹实体接口
> 迁移映射为 [IBulletVictimEntity](/docs/architecture/core/api/api-index.md#实体)
- KnockBackModifier：击退修改器枚举
- ReloadState：换弹状态枚举
- ShootResult：射击结果枚举

## 事件
> _./api/event_

> [事件API](./event-api.md)

- common：双端事件
	- GunDrawEvent：拔枪事件
	- GunFireEvent：开火前事件
	- GunFireSelectEvent：开火模式切换事件
	- GunMeleeEvent：近战事件
	- GunShootEvent：射击事件
	- GunReloadEvent：换弹事件
	- GunFinishReloadEvent：换弹完成事件
	- EntityHurtByGunEvent：实体被枪击伤害事件
	- EntityKillByGunEvent：实体被枪击杀事件
	- AttachmentPropertyEvent：配件属性事件
	- GunDamageSourcePart：枪械伤害源组件
	- KubeJSGunEventPoster：KubeJS 事件发布器
- server：服务端事件
	- AmmoHitBlockEvent：子弹击中方块事件

## 物品
> _./api/item_

- IAmmo：子弹物品接口
- IAmmoBox：子弹盒物品接口
- IAttachment：配件物品接口
- IGun：枪械物品接口
- IAnimationItem：动画物品接口
- IBlock：方块物品接口
- GunTabType：枪械物品分类枚举

### 枪械
> _./api/item/gun_

- AbstractGunItem：枪械物品抽象基类
> 迁移映射为 [IGunRuntime](/docs/architecture/core/Home.md#物品)
- FireMode：开火模式枚举
- GunItemManager：枪械物品管理器

### 配件
> _./api/item/attachment_

- AttachmentType：配件类型枚举

### NBT 访问器
> _./api/item/nbt_

- GunDataAccessor：枪械数据 NBT 访问器
- AmmoDataAccessor：子弹数据 NBT 访问器
- AmmoBoxDataAccessor：子弹盒数据 NBT 访问器
- AttachmentDataAccessor：配件数据 NBT 访问器
- BlockDataAccessor：方块数据 NBT 访问器

## 配件修改器
> _./api/modifier_

- IAttachmentModifier：配件修改器接口
- JsonProperty：JSON 属性注解
- CacheValue：属性缓存
- ParameterizedCache：参数化缓存
- ParameterizedCachePair：参数化缓存对

## 资源
> _./api/resource_

- ResourceManager：资源管理器，提供数据包资源获取
- JsonResourceLoader：JSON 资源加载器

## Lua 虚拟机库
> _./api/vmlib_

- LuaLibrary：Lua 库基类
- LuaGunLogicConstant：枪械逻辑 Lua 常量

## 工具
> _./api/util_

- LuaEntityAccessor：Lua 实体访问器
- LuaNbtAccessor：Lua NBT 访问器

## 客户端 API
> _./api/client_

### 动画
> _./api/client/animation_

- ObjectAnimation：对象动画
- statemachine：动画状态机
- gltf：glTF 动画支持
- interpolator：插值器

### 事件
> _./api/client/event_

- ClientEvent：客户端事件基类

### 游戏操作
> _./api/client/gameplay_

- IClientPlayerGunOperator：客户端玩家枪械操作接口
> 迁移映射为 [ILocalShooter](/docs/architecture/client/api/api-index.md#实体)

### 其他
> _./api/client/other_

- GunModelTypeManager：枪械模型类型管理器
- IThirdPersonAnimation：第三人称动画接口
- ThirdPersonManager：第三人称管理器
- KeepingItemRenderer：持物渲染器

# English

## Common
> _./api_

- DefaultAssets: Default asset constants
- GunProperties: Gun properties (cached bullet properties)
- GunProperty: Gun property
- TimelessAPI: Resource API
> Migration mapped to [ResourceApi](/docs/architecture/core/api/api-index.md#Resource)
- CacheModifiableByScript: Script modifiable cache
- ValueModifiableAtRuntime: Runtime modifiable value

## Entity
> _./api/entity_

- IGunOperator: Shooter entity operation interface
> Migration mapped to [ILivingShooter](/docs/architecture/core/api/api-index.md#Entity)
- ITargetEntity: Target entity interface
> Migration mapped to [IBulletVictimEntity](/docs/architecture/core/api/api-index.md#Entity)
- KnockBackModifier: Knockback modifier enum
- ReloadState: Reload state enum
- ShootResult: Shoot result enum

## Event
> _./api/event_

> [Event API](./event-api.md#English)

- common: Dual-side events
	- GunDrawEvent: Gun draw event
	- GunFireEvent: Gun fire (pre) event
	- GunFireSelectEvent: Fire mode select event
	- GunMeleeEvent: Gun melee event
	- GunShootEvent: Gun shoot event
	- GunReloadEvent: Gun reload event
	- GunFinishReloadEvent: Gun finish reload event
	- EntityHurtByGunEvent: Entity hurt by gun event
	- EntityKillByGunEvent: Entity killed by gun event
	- AttachmentPropertyEvent: Attachment property event
	- GunDamageSourcePart: Gun damage source part
	- KubeJSGunEventPoster: KubeJS gun event poster
- server: Server-side events
	- AmmoHitBlockEvent: Ammo hit block event

## Item
> _./api/item_

- IAmmo: Ammo item interface
- IAmmoBox: Ammo box item interface
- IAttachment: Attachment item interface
- IGun: Gun item interface
- IAnimationItem: Animation item interface
- IBlock: Block item interface
- GunTabType: Gun item category enum

### Gun
> _./api/item/gun_

- AbstractGunItem: Gun item abstract base class
> Migration mapped to [IGunRuntime](/docs/architecture/core/Home.md#Item)
- FireMode: Fire mode enum
- GunItemManager: Gun item manager

### Attachment
> _./api/item/attachment_

- AttachmentType: Attachment type enum

### NBT Accessor
> _./api/item/nbt_

- GunDataAccessor: Gun data NBT accessor
- AmmoDataAccessor: Ammo data NBT accessor
- AmmoBoxDataAccessor: Ammo box data NBT accessor
- AttachmentDataAccessor: Attachment data NBT accessor
- BlockDataAccessor: Block data NBT accessor

## Attachment Modifier
> _./api/modifier_

- IAttachmentModifier: Attachment modifier interface
- JsonProperty: JSON property annotation
- CacheValue: Property cache
- ParameterizedCache: Parameterized cache
- ParameterizedCachePair: Parameterized cache pair

## Resource
> _./api/resource_

- ResourceManager: Resource manager, provides datapack resource retrieval
- JsonResourceLoader: JSON resource loader

## Lua Virtual Machine Library
> _./api/vmlib_

- LuaLibrary: Lua library base class
- LuaGunLogicConstant: Gun logic Lua constants

## Utility
> _./api/util_

- LuaEntityAccessor: Lua entity accessor
- LuaNbtAccessor: Lua NBT accessor

## Client API
> _./api/client_

### Animation
> _./api/client/animation_

- ObjectAnimation: Object animation
- statemachine: Animation state machine
- gltf: glTF animation support
- interpolator: Interpolator

### Event
> _./api/client/event_

- ClientEvent: Client event base class

### Gameplay
> _./api/client/gameplay_

- IClientPlayerGunOperator: Client player gun operation interface
> Migration mapped to [ILocalShooter](/docs/architecture/client/api/api-index.md#Entity)

### Other
> _./api/client/other_

- GunModelTypeManager: Gun model type manager
- IThirdPersonAnimation: Third-person animation interface
- ThirdPersonManager: Third-person manager
- KeepingItemRenderer: Keeping item renderer
