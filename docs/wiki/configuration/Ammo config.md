[English](#English)

# 子弹配置
> wiki 版本：`0.0.1`.0

路径：`ammo`
- `ExplosiveAmmoDestroysBlock`（bool）：具有爆炸属性的子弹是否能破坏方块
- `ExplosiveAmmoFire`（bool）：具有爆炸属性的子弹是否能点燃周围
- `ExplosiveAmmoKnockBack`（bool）：具有爆炸属性的子弹是否能造成击退
- `ExplosiveAmmoVisibleDistance`（整数）：爆炸效果可视距离
- `PassThroughBlocks`（字符串列表）：子弹可穿透的方块
- `DestroyGlass`（bool）：子弹是否能破坏玻璃
- `IgniteBlock`（bool）：子弹是否能点燃方块
- `IgniteEntity`（bool）：子弹是否能点燃实体
- `GlobalBulletSpeedModifier`（浮点，0.01~20.0）：全局子弹速度系数
```toml
[ammo]
	ExplosiveAmmoDestroysBlock = true
	ExplosiveAmmoFire = false
	ExplosiveAmmoKnockBack = true
	ExplosiveAmmoVisibleDistance = 192
	PassThroughBlocks = []
	DestroyGlass = true
	IgniteBlock = true
	IgniteEntity = true
	GlobalBulletSpeedModifier = 2.0
```

# English
> wiki verison: `0.0.1`.0

## Ammo Config

Path: `ammo`
- `ExplosiveAmmoDestroysBlock` (bool): Whether ammo with explosive properties can break blocks
- `ExplosiveAmmoFire` (bool): Whether ammo with explosive properties can set the surroundings on fire
- `ExplosiveAmmoKnockBack` (bool): Whether ammo with explosive properties can add knockback effect
- `ExplosiveAmmoVisibleDistance` (integer): The distance at which the explosion effect can be seen
- `PassThroughBlocks` (string list): Blocks that the ammo can pass through
- `DestroyGlass` (bool): Whether ammo can break glass
- `IgniteBlock` (bool): Whether ammo can ignite blocks
- `IgniteEntity` (bool): Whether ammo can ignite entities
- `GlobalBulletSpeedModifier` (float, 0.01~20.0): Global bullet speed modifier, the initial speed of the bullet will be multiplied by this value
```toml
[ammo]
	ExplosiveAmmoDestroysBlock = true
	ExplosiveAmmoFire = false
	ExplosiveAmmoKnockBack = true
	ExplosiveAmmoVisibleDistance = 192
	PassThroughBlocks = []
	DestroyGlass = true
	IgniteBlock = true
	IgniteEntity = true
	GlobalBulletSpeedModifier = 2.0
```
