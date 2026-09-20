[English](#English)

# 枪械配置
> wiki 版本：`0.0.17`.3

路径：`gun`
- `DefaultGunFireSoundDistance`（整数）：默认枪械开火声音传播距离（格）
- `DefaultGunSilenceSoundDistance`（整数）：消音器默认开火声音传播距离（格）
- `DefaultGunOtherSoundDistance`（整数）：其他枪械声音（换弹等）传播距离（格）
- `BypassGunFireConsumption`（bool）：创造模式下，枪械开火是否不消耗弹药
- `AutoReloadWhenRespawn`（bool）：玩家复活时自动重新装填所有枪械
```toml
[gun]
	DefaultGunFireSoundDistance = 320
	DefaultGunSilenceSoundDistance = 256
	DefaultGunOtherSoundDistance = 16
	BypassGunFireConsumption = false
	AutoReloadWhenRespawn = false
```

# English
> wiki version: `0.0.17`.3

## Gun Config

Path: `gun`
- `DefaultGunFireSoundDistance` (integer): The default fire sound range (blocks)
- `DefaultGunSilenceSoundDistance` (integer): The silencer default fire sound range (blocks)
- `DefaultGunOtherSoundDistance` (integer): The range (blocks) of other gun sounds, reloading sound etc.
- `BypassGunFireConsumption` (bool): Whether firing guns bypasses ammo consumption in Creative Mode
- `AutoReloadWhenRespawn` (bool): Auto reload all the guns in player inventory
```toml
[gun]
	DefaultGunFireSoundDistance = 320
	DefaultGunSilenceSoundDistance = 256
	DefaultGunOtherSoundDistance = 16
	BypassGunFireConsumption = false
	AutoReloadWhenRespawn = false
```
