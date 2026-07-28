[English](#English)

# 架构总览

> 本文档作为项目架构的导航索引

## 项目结构

基于`xiao.customgun.client`顶层包的模块划分

### API
> _./client/api_

- [客户端API索引](./api/api-index.md)：通过接口分类和职责介绍进行筛选，并传送至 _./docs/api_ 下内容以查看详细参数

### 动画
> _./client/animation_

### 指令
> _./client/command_

- [客户端指令](./command/client-command.md)
- [客户端指令参数名列表](./command/command-arg.md)
- 命令：
	- DebugCommand：[客户端调试指令](/docs/wiki/command/Debug%20command.md#客户端指令)
	- ReloadCommand：[客户端重载指令](/docs/wiki/command/Reload%20command.md#客户端指令)

### 模组联动
> _./client/compat_

### 配置
> _./client/config_

- KeyConfig：按键配置
- RenderConfig：渲染配置
- ResourceConfig：资源配置
- SoundConfig：声音配置
- ZoomConfig：开镜配置

### 实体
> _./client/entity_

- Local Shooter (aspect)：本地射手生物（切面）
	- LocalShooterAim：瞄准
	- LocalShooterBolt：拉栓
	- LocalShooterProne：趴下
	- LocalShooterDraw：拔枪
	- LocalShooterSwitchFireMode：切换开火模式
	- LocalShooterInspect：检视
	- LocalShooterMelee：近战
	- LocalShooterReload：换弹
	- LocalShooterShoot：射击
	- LocalShooterSprint：冲刺

### 事件
> _./client/event_

- Custom：自定义事件注册
	- ClientEventHandlers：客户端事件注册

### GUI
> _./client/gui_

- GunRefitScreen：枪械改装界面
- Tooltip：提示框
	- AmmoBox：子弹盒
		- ClientAmmoBoxTooltip：客户端子弹盒提示框
	- Attachment：配件
		- ClientAttachmentTooltip：客户端配件提示框
	- Gun：枪械
		- ClientGunTooltip：客户端枪械提示框
		> - GunAmmoInfoPart：子弹信息组件
		> - GunBaseInfoPart：基础信息组件
		> - GunDescriptionPart：描述组件
		> - GunExtraDamageInfoPart：额外伤害信息组件
		> - GunPackInfoPart：枪包信息组件
		> - GunUpgradesTipPart：升级提示组件

### 初始化
> _./client/init_

- ClientCommandRegistry：客户端指令注册
- ClientModConfig：客户端配置初始化
- ClientModEvent：客户端事件
- ClientModParticles：客户端粒子注册
- ClientTooltipRegistry：客户端提示框注册

### 输入
> _./client/input_

[输入框架](./input/input-framework.md)
- [InputKeyManager](./input/input-key-manager.md)：输入键管理器

🤔深度定制输入键：[注册输入键](./input/register-input-key.md)
- config：配置按键
- player：玩家按键
- shooter：射手按键

### 物品
> _./client/item_

- Ammo：子弹
	- [AmmoItem](./item/ammo/ammo-item.md)：子弹物品（客户端）
- AmmoBox：子弹盒
	- [AmmoBoxItem](./item/ammobox/ammo-box-item.md)：子弹盒物品（客户端）
- Attachment：配件
	- [AttachmentItem](./item/attachment/attachment-item.md)：配件物品（客户端）
- Gun：枪械
	- [GunItem](./item/gun/gun-item.md)：枪械物品（客户端）

### Mixin
> _./client/mixin_

- Entity：实体
- GUI：界面
- Model：模型
- Renderer：渲染器
- Sound：声音
- MouseHandler：鼠标处理器

### 模型
> _./client/model_

- AmmoModelObject：子弹模型对象
- AnimatedModelObject：动画模型对象
- AttachmentModelObject：配件模型对象
- GunModelObject：枪械模型对象

### 网络
> _./client/network_

- Message：网络消息
	- event：处理事件消息
	- 处理服务端->客户端消息

### 粒子
> _./client/particle_

- AmmoParticleSpawner：子弹粒子生成器
- BulletHoleParticle：弹孔粒子

### 渲染器
> _./client/renderer_

- Entity：实体
- GUI：屏幕渲染
- Item：物品
- Model：模型
- Shooter：射手生物
- Victim：受弹实体

### 资源
> _./client/resource_

[资源文件说明](/docs/wiki/resource/Resource%20introduction.md#资源包)：
- Assets：资源包POJO
- Instance：POJO对象
	- Assets：资源包POJO对象
	- Data：数据包POJO对象
- Network：数据同步
	- SyncDataCache：客户端缓存同步数据

### 声音
> _./client/sound_

- ResourceSound：资源声音
- ResourceSoundInstance：资源声音实例
> - EntityTrackingSoundInstance：实体跟踪声音实例
- SoundPlayManager：声音播放管理器

### 工具
> _./client/util_

- ClientGuiUtils：客户端 GUI 操作封装
- ClientJsonUtils：客户端 JSON 序列化封装
- ClientRenderUtils：客户端渲染操作封装
- ClientWorldUtils：客户端游戏内世界相关操作

# English

> This document serves as a navigation index for the project architecture

## Project Structure

Module division based on the `xiao.customgun.client` top-level package

### API
> _./client/api_

- [Client API Index](./api/api-index.md#English): Filter by interface classification and responsibility introduction, and link to content under _./docs/api_ to view detailed parameters

### Animation
> _./client/animation_

### Command
> _./client/command_

- [Client command](./command/client-command.md#English)
- [Client Command Argument List](./command/command-arg.md#English)
- Commands:
	- DebugCommand: [Client Debug command](/docs/wiki/command/Debug%20command.md#Client%20command)
	- ReloadCommand: [Client Reload command](/docs/wiki/command/Reload%20command.md#Client%20command)

### Mod Compat
> _./client/compat_

### Config
> _./client/config_

- KeyConfig: Key config
- RenderConfig: Render config
- ResourceConfig: Resource config
- SoundConfig: Sound config
- ZoomConfig: Zoom config

### Entity
> _./client/entity_

- Local Shooter (aspect):
	- LocalShooterAim: Aim
	- LocalShooterBolt: Bolt
	- LocalShooterProne: Prone
	- LocalShooterDraw: Draw
	- LocalShooterSwitchFireMode: Switch fire mode
	- LocalShooterInspect: Inspect
	- LocalShooterMelee: Melee
	- LocalShooterReload: Reload
	- LocalShooterShoot: Shoot
	- LocalShooterSprint: Sprint

### Event
> _./client/event_

- Custom: Custom event registration
	- ClientEventHandlers: Client event registration

### GUI
> _./client/gui_

- GunRefitScreen: Gun refit screen
- Tooltip:
	- AmmoBox:
		- ClientAmmoBoxTooltip: Client ammo box tooltip
	- Attachment:
		- ClientAttachmentTooltip: Client attachment tooltip
	- Gun:
		- ClientGunTooltip: Client gun tooltip
		> - GunAmmoInfoPart: Ammo info part
		> - GunBaseInfoPart: Base info part
		> - GunDescriptionPart: Description part
		> - GunExtraDamageInfoPart: Extra damage info part
		> - GunPackInfoPart: Gun pack info part
		> - GunUpgradesTipPart: Upgrades tip part

### Initialization
> _./client/init_

- ClientCommandRegistry: Client command registration
- ClientModConfig: Client config initialization
- ClientModEvent: Client events
- ClientModParticles: Client particle registration
- ClientTooltipRegistry: Client tooltip registration

### Input
> _./client/input_

[Input framework](./input/input-framework.md)
- [InputKeyManager](./input/input-key-manager.md): Input key manager

🤔Deeply customized input key: [Register input key](./input/register-input-key.md)
- config: Config keys
- player: Player keys
- shooter: Shooter keys

### Item
> _./client/item_

- Ammo:
	- [AmmoItem](./item/ammo/ammo-item.md#English): Ammo item (client)
- AmmoBox:
	- [AmmoBoxItem](./item/ammobox/ammo-box-item.md#English): Ammo box item (client)
- Attachment:
	- [AttachmentItem](./item/attachment/attachment-item.md#English): Attachment item (client)
- Gun:
	- [GunItem](./item/gun/gun-item.md#English): Gun item (client)

### Mixin
> _./client/mixin_

- Entity: Entity
- GUI: GUI
- Model: Model
- Renderer: Renderer
- Sound: Sound
- MouseHandler: Mouse handler

### Model
> _./client/model_

- AmmoModelObject: Ammo model object
- AnimatedModelObject: Animated model object
- AttachmentModelObject: Attachment model object
- GunModelObject: Gun model object

### Network
> _./client/network_

- Message: Network messages
	- event: Handle event messages
	- Handle Server → Client messages

### Particle
> _./client/particle_

- AmmoParticleSpawner: Ammo particle spawner
- BulletHoleParticle: Bullet hole particle

### Renderer
> _./client/renderer_

- Entity
- GUI
- Item
- Model
- Shooter
- Victim

### Resource
> _./client/resource_

[Resource introduction](/docs/wiki/resource/Resource%20introduction.md#Resourcepack):
- Assets: Resource pack POJO
- Instance: POJO objects
	- Assets: Resource pack POJO object
	- Data: Datapack POJO object
- Network: Data sync
	- SyncDataCache: Client cached sync data

### Sound
> _./client/sound_

- ResourceSound: Resource sound
- ResourceSoundInstance: Resource sound instance
> - EntityTrackingSoundInstance: Entity tracking sound instance
- SoundPlayManager: Sound play manager

### Utility
> _./client/util_

- ClientGuiUtils: Client GUI operations wrapper
- ClientJsonUtils: Client JSON serialization wrapper
- ClientRenderUtils: Client render operations wrapper
- ClientWorldUtils: Client in-game world related operations
