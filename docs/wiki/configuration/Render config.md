[English](#English)

# 渲染配置
> wiki 版本：`0.0.15`.12

路径：`render`
- `EnableLaserFadeOut`（bool）：是否对激光应用淡出效果
- `GunLodRenderDistance`（整数）：远距离模型显示距离，设为0则始终显示
- `DisableGunTilting`（bool）：是否禁止蹲下时倾斜枪械
- `BulletHoleParticleLife`（整数）：弹孔粒子存在时间（tick）
- `BulletHoleParticleFadeThreshold`（浮点，0.0~1.0）：弹孔粒子渲染淡出阈值
- `ReplaceVanillaCrosshair`（bool）：持枪时是否替换原版准心
- `CrosshairType`（枚举）：持枪时的准星样式
	- `DEFAULT`
	- `BLANK`
	- `EMPTY`
	- `DOT_1`
	- `CIRCLE_1`、`CIRCLE_2`、`CIRCLE_3`
	- `CROSS_1`、`CROSS_2`、`CROSS_3`、`CROSS_4`、`CROSS_5`、`CROSS_6`
	- `LINE_1`、`LINE_2`、`LINE_3`
	- `SQUARE_1`、`SQUARE_2`、`SQUARE_3`、`SQUARE_4`、`SQUARE_5`、`SQUARE_6`
	- `TRIDENT_1`、`TRIDENT_2`
- `EnableGunHUD`（bool）：是否显示枪械HUD
- `KeepDisplayGunHUD`（bool）：是否持续显示枪械HUD
- `ForceDisplayGunHUD`（bool）：是否强制显示枪械HUD
- `EnableFirstPersonBulletTracer`（bool）：是否渲染第一人称曳光弹轨迹
- `EnableShooterOperationHUD`（bool）：是否显示射手操作HUD
- `DisableMovementAttributeFov`（bool）：持枪时禁用移动速度属性对FOV的影响
- `AppendResourceLocationInTooltip`（bool）：是否在提示框枪包信息后附加显示资源位置
```toml
[render]
	EnableLaserFadeOut = true
	GunLodRenderDistance = 0
	DisableGunTilting = true
	BulletHoleParticleLife = 400
	BulletHoleParticleFadeThreshold = 0.98
	ReplaceVanillaCrosshair = true
	CrosshairType = "DEFAULT"
	EnableGunHUD = true
	KeepDisplayGunHUD = false
	ForceDisplayGunHUD = true
	EnableFirstPersonBulletTracer = true
	EnableShooterOperationHUD = true
	DisableMovementAttributeFov = true
	AppendResourceLocationInTooltip = true
```

# English
> wiki version: `0.0.15`.12

## Render Config

Path: `render`
- `EnableLaserFadeOut` (bool): Whether to apply fadeout effect on the laser beam
- `GunLodRenderDistance` (integer): How far to display the LOD model, 0 means always display
- `DisableGunTilting` (bool): Whether to disable gun tilting while crouching
- `BulletHoleParticleLife` (integer): The existence time of bullet hole particles, in ticks
- `BulletHoleParticleFadeThreshold` (float, 0.0~1.0): The threshold for fading out when rendering bullet hole particles
- `ReplaceVanillaCrosshair`(bool): Whether to replace the vanilla crosshair when holding a gun
- `CrosshairType` (enum): The crosshair when holding a gun
	- `DEFAULT`
	- `BLANK`
	- `EMPTY`
	- `DOT_1`
	- `CIRCLE_1`, `CIRCLE_2`, `CIRCLE_3`
	- `CROSS_1`, `CROSS_2`, `CROSS_3`, `CROSS_4`, `CROSS_5`, `CROSS_6`
	- `LINE_1`, `LINE_2`, `LINE_3`
	- `SQUARE_1`, `SQUARE_2`, `SQUARE_3`, `SQUARE_4`, `SQUARE_5`, `SQUARE_6`
	- `TRIDENT_1`, `TRIDENT_2`
- `EnableGunHUD` (bool): Whether to display the gun's HUD
- `KeepDisplayGunHUD` (bool): Whether to keep the gun's HUD displayed
- `ForceDisplayGunHUD` (bool): Whether to force the gun's HUD to be displayed
- `EnableFirstPersonBulletTracer` (bool): Whether to render first person bullet trail
- `EnableShooterOperationHUD` (bool): Whether to display the shooter operation's HUD
- `DisableMovementAttributeFov` (bool): Disable the FOV effect from the movement speed attribute while holding a gun
- `AppendResourceLocationInTooltip` (bool): Whether to append resource location after gunpack information in tooltips
```toml
[render]
	EnableLaserFadeOut = true
	GunLodRenderDistance = 0
	DisableGunTilting = true
	BulletHoleParticleLife = 400
	BulletHoleParticleFadeThreshold = 0.98
	ReplaceVanillaCrosshair = true
	CrosshairType = "DEFAULT"
	EnableGunHUD = true
	KeepDisplayGunHUD = false
	ForceDisplayGunHUD = true
	EnableFirstPersonBulletTracer = true
	EnableShooterOperationHUD = true
	DisableMovementAttributeFov = true
	AppendResourceLocationInTooltip = true
```
