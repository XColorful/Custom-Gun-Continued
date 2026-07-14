[English](#English)

## 枪射物管理器

- 外部只需要获取全局静态API`IProjectileManager`即可，`IProjectileManager`具体类再往下层层委派功能
- 外部调用视为拥有所有权限

[![IProjectileManager](/docs/api/core/projectile/IProjectileManager.md)](/docs/api/core/projectile/IProjectileManager.md)

### 枪射物主管理器

- 主管理器调度各子管理器
- 提供其下所有子管理器的获取接口

[![IProjectileMainManager](/docs/api/core/projectile/IProjectileMainManager.md)](/docs/api/core/projectile/IProjectileMainManager.md)

## Gun Projectile Manager

- Externally, only the static API `IProjectileManager` needs to be accessed. The concrete `IProjectileManager` class then delegates functionality layer by layer.
- External calls are treated as having full permissions.

[![IProjectileManager](/docs/api/core/projectile/IProjectileManager.md)](/docs/api/core/projectile/IProjectileManager.md)

### Gun Projectile Main Manager

- The Main Manager orchestrates all Sub-Managers.
- Provides getter interfaces for all its Sub-Managers.

[![IProjectileMainManager](/docs/api/core/projectile/IProjectileMainManager.md)](/docs/api/core/projectile/IProjectileMainManager.md)
