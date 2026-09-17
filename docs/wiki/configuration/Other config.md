[English](#English)

# 杂项配置
> wiki 版本：`0.0.15`.1

路径：`other`
- `ServerHitboxOffset`（浮点）：服务端碰撞箱偏移（如果碰撞箱靠前则填入负数）
- `ServerHitboxLatencyFix`（bool）：服务端碰撞箱延迟修复
- `ServerHitboxLatencyMaxSaveMs`（浮点，≥250.0）：服务端碰撞箱延迟修复保存的最大延迟（毫秒）
```toml
[other]
	ServerHitboxOffset = 3.0
	ServerHitboxLatencyFix = true
	ServerHitboxLatencyMaxSaveMs = 1000.0
```

# English
> wiki version: `0.0.15`.1

## Other Config

Path: `other`
- `ServerHitboxOffset` (float): Server hitbox offset (If the hitbox is ahead, fill in a negative number)
- `ServerHitboxLatencyFix` (bool): Server hitbox latency fix
- `ServerHitboxLatencyMaxSaveMs` (float, ≥250.0): The maximum latency (in milliseconds) for the server hitbox latency fix saved
```toml
[other]
	ServerHitboxOffset = 3.0
	ServerHitboxLatencyFix = true
	ServerHitboxLatencyMaxSaveMs = 1000.0
```
