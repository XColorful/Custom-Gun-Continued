[English](#English)

## 枪械管理器

- 外部只需要获取全局静态API`IGunManager`即可，`IGunManager`具体类再往下层层委派功能
- 外部调用视为拥有所有权限

[![IGunManager](/docs/api/core/gun/IGunManager.md)](/docs/api/core/gun/IGunManager.md)

### 枪械主管理器

- 主管理器调度各子管理器
- 提供其下所有子管理器的获取接口

[![IGunMainManager](/docs/api/core/gun/IGunMainManager.md)](/docs/api/core/gun/IGunMainManager.md)

# English

## Gun Manager

- Externally, only the static API `IGunManager` needs to be accessed. The concrete `IGunManager` class then delegates functionality layer by layer.
- External calls are treated as having full permissions.

[![IGunManager](/docs/api/core/gun/IGunManager.md)](/docs/api/core/gun/IGunManager.md)

### Gun Main Manager

- The Main Manager orchestrates all Sub-Managers.
- Provides getter interfaces for all its Sub-Managers.

[![IGunMainManager](/docs/api/core/gun/IGunMainManager.md)](/docs/api/core/gun/IGunMainManager.md)
