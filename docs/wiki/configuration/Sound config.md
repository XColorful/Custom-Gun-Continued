[English](#English)

# 声音配置
> wiki 版本：`0.0.1`.0

路径：`sound`
- `HitSoundConcurrencyLimit`（整数，0~128）：同一实体同一声效ID的命中标记声音最大并发数，0表示不限制
- `DefaultSoundConcurrencyLimit`（整数，0~128）：同一实体同一声效ID的普通枪械声音最大并发数，0表示不限制
- `HighFrequencySoundConcurrencyLimit`（整数，0~128）：同一实体同一声效ID的高频枪械声音（射击和动画关键帧声音）最大并发数，0表示不限制
- `FirstPersonAnimationSoundTracking`（bool）：第一人称动画关键帧声音使用非相对实体跟踪世界声源
```toml
[sound]
	HitSoundConcurrencyLimit = 1
	DefaultSoundConcurrencyLimit = 2
	HighFrequencySoundConcurrencyLimit = 4
	FirstPersonAnimationSoundTracking = false
```

# English
> wiki verison: `0.0.1`.0

## Sound Config

Path: `sound`
- `HitSoundConcurrencyLimit` (integer, 0~128): Max active hit marker sounds for the same entity and sound id; 0 disables this limit
- `DefaultSoundConcurrencyLimit` (integer, 0~128): Max active normal gun sounds for the same entity and sound id; 0 disables this limit
- `HighFrequencySoundConcurrencyLimit` (integer, 0~128): Max active high-frequency gun sounds for the same entity and sound id; 0 disables this limit
- `FirstPersonAnimationSoundTracking` (bool): Use a non-relative entity-tracking world sound source for first-person animation keyframe sounds
```toml
[sound]
	HitSoundConcurrencyLimit = 1
	DefaultSoundConcurrencyLimit = 2
	HighFrequencySoundConcurrencyLimit = 4
	FirstPersonAnimationSoundTracking = false
```
