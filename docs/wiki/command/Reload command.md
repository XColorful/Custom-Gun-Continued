[English](#English)

# 重载指令
> wiki 版本：`0.0.1`.1

## 服务端指令

### 重载全部枪包
> _/customgun reload_

重新加载全部枪包数据
- 在单人游戏中，该指令通过服务端线程触发，会同步等待客户端资源包重载完成后再执行服务端数据包重载，保证时序正确

## 客户端指令

### 重载客户端枪包
> _/customgun reload_client_

重新加载客户端资产包
- 该指令直接在渲染线程触发资源包重载（异步，不阻塞），避免渲染线程死锁
- 本质上和原版 `F3`+`T` 行为一致，但更方便

# English
> wiki verison: `0.0.1`.1

## Server command

### Reload all gun packs
> _/customgun reload_

Reload all gun pack data.
- In singleplayer, this command is triggered via the server thread. It synchronously waits for the client resource pack reload to complete before executing the server datapack reload, ensuring correct ordering.

## Client command

### Reload client gun packs
> _/customgun reload_client_

Reload client asset packs.
- This command triggers resource pack reload directly on the render thread (async, non-blocking), avoiding a render-thread deadlock.
- Functionally equivalent to vanilla `F3`+`T` but more convenient.
