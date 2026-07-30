[English](#English)

# 射手框架

## 射手切面

一般流程：
- 检查射手状态（`ILivingShooter`）
- 调用枪械检查（`IGun`/`IGunRuntime`）
- 调用枪械操作（`IGun`/`IGunRuntime`） -> 射手状态

要点：
- `LivingShooterAspect`只负责射手状态，抽离枪械逻辑
- `LocalShooterAspect`跟`LivingShooterAspect`对齐
- 服务端`ShooterProperty`对应客户端`LocalShooterProperty.clientStateLock`状态锁

# English

## Shooter aspect
