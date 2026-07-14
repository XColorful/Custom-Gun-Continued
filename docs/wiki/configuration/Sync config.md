[English](#English)

# 同步配置
> wiki 版本：`0.0.1`.0

## 交互键

路径：`interact_key`
- `InteractKeyWhitelistBlocks`（字符串列表）：按下交互键时允许交互的白名单方块
- `InteractKeyWhitelistEntities`（字符串列表）：按下交互键时允许交互的白名单实体
- `InteractKeyBlacklistBlocks`（字符串列表）：按下交互键时阻止交互的黑名单方块
- `InteractKeyBlacklistEntities`（字符串列表）：按下交互键时阻止交互的黑名单实体
```toml
[interact_key]
	InteractKeyWhitelistBlocks = []
	InteractKeyWhitelistEntities = []
	InteractKeyBlacklistBlocks = []
	InteractKeyBlacklistEntities = []
```

## 基础系数

路径：`base_multiplier`
- `DamageBaseMultiplier`（浮点，≥0.0）：所有基础伤害数值乘以该系数
- `ArmorIgnoreBaseMultiplier`（浮点，≥0.0）：所有穿甲伤害数值乘以该系数
- `HeadShotBaseMultiplier`（浮点，≥0.0）：所有爆头伤害数值乘以该系数
- `WeightSpeedMultiplier`（浮点，≥-1.0）：每公斤重量减少的速度百分比，设为负数则禁用
```toml
[base_multiplier]
	DamageBaseMultiplier = 1.0
	ArmorIgnoreBaseMultiplier = 1.0
	HeadShotBaseMultiplier = 1.0
	WeightSpeedMultiplier = 0.015
```

## 杂项

路径：`misc`
- `HeadShotAABB`（字符串列表）：实体爆头判定碰撞箱
- `AmmoBoxStackSize`（整数，≥1）：子弹盒可容纳的子弹最大堆叠数
- `ClientGunPackDownloadUrls`（字符串列表）：已弃用，使用原版服务端资源包
- `EnableCrawl`（bool）：是否允许玩家使用趴下功能
- `EnableDefaultGunSmithTableFilter`（bool）：是否启用默认枪械改装台的配方限制
- `ServerShootNetworkCheck`（bool）：[调试] 是否在服务端执行射击时的网络校验
- `ServerShootCooldownCheck`（bool）：[调试] 是否在服务端执行射击冷却校验
```toml
[misc]
	HeadShotAABB = []
	AmmoBoxStackSize = 3
	ClientGunPackDownloadUrls = []
	EnableCrawl = true
	EnableDefaultGunSmithTableFilter = true
	ServerShootNetworkCheck = true
	ServerShootCooldownCheck = true
```

# English
> wiki verison: `0.0.1`.0

## Sync Config

### Interact Key

Path: `interact_key`
- `InteractKeyWhitelistBlocks` (string list): Whitelist blocks that can be interacted with when the interact key is pressed
- `InteractKeyWhitelistEntities` (string list): Whitelist entities that can be interacted with when the interact key is pressed
- `InteractKeyBlacklistBlocks` (string list): Blacklist blocks that cannot be interacted with when the interact key is pressed
- `InteractKeyBlacklistEntities` (string list): Blacklist entities that cannot be interacted with when the interact key is pressed
```toml
[interact_key]
	InteractKeyWhitelistBlocks = []
	InteractKeyWhitelistEntities = []
	InteractKeyBlacklistBlocks = []
	InteractKeyBlacklistEntities = []
```

### Base Multiplier

Path: `base_multiplier`
- `DamageBaseMultiplier` (float, ≥0.0): All base damage numbers are multiplied by this factor
- `ArmorIgnoreBaseMultiplier` (float, ≥0.0): All armor ignore damage numbers are multiplied by this factor
- `HeadShotBaseMultiplier` (float, ≥0.0): All head shot damage numbers are multiplied by this factor
- `WeightSpeedMultiplier` (float, ≥-1.0): The movement speed will decrease per kg of weight; set a negative value to disable
```toml
[base_multiplier]
	DamageBaseMultiplier = 1.0
	ArmorIgnoreBaseMultiplier = 1.0
	HeadShotBaseMultiplier = 1.0
	WeightSpeedMultiplier = 0.015
```

### Misc

Path: `misc`
- `HeadShotAABB` (string list): The entity's head hitbox during the headshot
- `AmmoBoxStackSize` (integer, ≥1): The maximum stack size of ammo that the ammo box can hold
- `ClientGunPackDownloadUrls` (string list): Deprecated. Use vanilla server resource pack
- `EnableCrawl` (bool): Whether players are allowed to use the crawl feature
- `EnableDefaultGunSmithTableFilter` (bool): Enable the recipe limit of the default gunsmith table
- `ServerShootNetworkCheck` (bool): [Debug] Do server-side network check while shooting
- `ServerShootCooldownCheck` (bool): [Debug] Do server-side shoot cooldown check
```toml
[misc]
	HeadShotAABB = []
	AmmoBoxStackSize = 3
	ClientGunPackDownloadUrls = []
	EnableCrawl = true
	EnableDefaultGunSmithTableFilter = true
	ServerShootNetworkCheck = true
	ServerShootCooldownCheck = true
```
