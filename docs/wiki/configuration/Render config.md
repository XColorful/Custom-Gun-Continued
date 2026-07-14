[English](#English)

# 渲染配置
> wiki 版本：`0.0.1`.0

路径：`render`
- `EnableLaserFadeOut`（bool）：是否对激光应用淡出效果
- `GunLodRenderDistance`（整数）：远距离模型显示距离，设为0则始终显示
- `BulletHoleParticleLife`（整数）：弹孔粒子存在时间（tick）
- `BulletHoleParticleFadeThreshold`（浮点，0.0~1.0）：弹孔粒子渲染淡出阈值
- `CrosshairType`（枚举）：持枪时的准星样式
	- `EMPTY`
	- `DOT_1`
	- `CIRCLE_1`、`CIRCLE_2`、`CIRCLE_3`
	- `CROSS_1`、`CROSS_2`、`CROSS_3`、`CROSS_4`、`CROSS_5`、`CROSS_6`
	- `LINE_1`、`LINE_2`、`LINE_3`
	- `SQUARE_1`、`SQUARE_2`、`SQUARE_3`、`SQUARE_4`、`SQUARE_5`、`SQUARE_6`
	- `TRIDENT_1`、`TRIDENT_2`
- `HitMarketStartPosition`（浮点，-1024.0~1024.0）：命中标记的起始位置
- `HeadShotDebugHitbox`（bool）：是否显示爆头判定碰撞箱
- `GunHUDEnable`（bool）：是否显示枪械HUD
- `KillAmountEnable`（bool）：是否显示击杀数
- `KillAmountDurationSecond`（浮点）：击杀数显示持续时间（秒）
- `TargetRenderDistance`（整数）：目标最远渲染距离
- `FirstPersonBulletTracerEnable`（bool）：是否渲染第一人称曳光弹轨迹
- `DisableInteractHudText`（bool）：禁用屏幕中央的交互HUD文本
- `AutoSelectGunSmithTableFilter`（bool）：手持枪械/配件/子弹打开改装台时是否自动选择过滤
- `DamageCounterResetTime`（整数，≥10）：伤害计数器重置时间（毫秒）
- `DisableMovementAttributeFov`（bool）：持枪时禁用移动速度属性对FOV的影响
- `EnableGunLocationInTooltip`（bool）：启用高级提示框时在提示框中显示枪械位置
- `EnableBlockEntityTranslucent`（bool）：渲染方块实体时启用半透明
```toml
[render]
	EnableLaserFadeOut = true
	GunLodRenderDistance = 0
	BulletHoleParticleLife = 400
	BulletHoleParticleFadeThreshold = 0.98
	CrosshairType = "DOT_1"
	HitMarketStartPosition = 4.0
	HeadShotDebugHitbox = false
	GunHUDEnable = true
	KillAmountEnable = true
	KillAmountDurationSecond = 3.0
	TargetRenderDistance = 128
	FirstPersonBulletTracerEnable = true
	DisableInteractHudText = false
	AutoSelectGunSmithTableFilter = true
	DamageCounterResetTime = 2000
	DisableMovementAttributeFov = true
	EnableGunLocationInTooltip = true
	EnableBlockEntityTranslucent = false
```

# English
> wiki verison: `0.0.1`.0

## Render Config

Path: `render`
- `EnableLaserFadeOut` (bool): Whether to apply fadeout effect on the laser beam
- `GunLodRenderDistance` (integer): How far to display the LOD model, 0 means always display
- `BulletHoleParticleLife` (integer): The existence time of bullet hole particles, in ticks
- `BulletHoleParticleFadeThreshold` (float, 0.0~1.0): The threshold for fading out when rendering bullet hole particles
- `CrosshairType` (enum): The crosshair when holding a gun
	- `EMPTY`
	- `DOT_1`
	- `CIRCLE_1`, `CIRCLE_2`, `CIRCLE_3`
	- `CROSS_1`, `CROSS_2`, `CROSS_3`, `CROSS_4`, `CROSS_5`, `CROSS_6`
	- `LINE_1`, `LINE_2`, `LINE_3`
	- `SQUARE_1`, `SQUARE_2`, `SQUARE_3`, `SQUARE_4`, `SQUARE_5`, `SQUARE_6`
	- `TRIDENT_1`, `TRIDENT_2`
- `HitMarketStartPosition` (float, -1024.0~1024.0): The starting position of the hit marker
- `HeadShotDebugHitbox` (bool): Whether to display the head shot's hitbox
- `GunHUDEnable` (bool): Whether to display the gun's HUD
- `KillAmountEnable` (bool): Whether to display the kill amount
- `KillAmountDurationSecond` (float): The duration of the kill amount, in seconds
- `TargetRenderDistance` (integer): The farthest render distance of the target, including minecarts type
- `FirstPersonBulletTracerEnable` (bool): Whether to render first person bullet trail
- `DisableInteractHudText` (bool): Disable the interact HUD text in the center of the screen
- `AutoSelectGunSmithTableFilter` (bool): Whether to automatically select the gun smith table's held item filter
- `DamageCounterResetTime` (integer, ≥10): Max time the damage counter will reset
- `DisableMovementAttributeFov` (bool): Disable the FOV effect from the movement speed attribute while holding a gun
- `EnableGunLocationInTooltip` (bool): Enable the display of the gun location in the tooltip when Advanced Tooltip is enabled
- `EnableBlockEntityTranslucent` (bool): Enable translucent while rendering block entities
```toml
[render]
	EnableLaserFadeOut = true
	GunLodRenderDistance = 0
	BulletHoleParticleLife = 400
	BulletHoleParticleFadeThreshold = 0.98
	CrosshairType = "DOT_1"
	HitMarketStartPosition = 4.0
	HeadShotDebugHitbox = false
	GunHUDEnable = true
	KillAmountEnable = true
	KillAmountDurationSecond = 3.0
	TargetRenderDistance = 128
	FirstPersonBulletTracerEnable = true
	DisableInteractHudText = false
	AutoSelectGunSmithTableFilter = true
	DamageCounterResetTime = 2000
	DisableMovementAttributeFov = true
	EnableGunLocationInTooltip = true
	EnableBlockEntityTranslucent = false
```
