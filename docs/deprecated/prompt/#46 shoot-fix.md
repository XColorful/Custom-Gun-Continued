> #46 提示词

# 

```
目前会在从暂停画面点击回到游戏时，如果当前手持枪械，则ShootKey会误触开火

- 可以通过IDEA的mcp访问tacz源码
- tacz类到cgc的迁移映射在./docs-tacz/TaCZ Migration Mapping.md

辅助快速浏览：
- CGC本地开火是从dev.xcolorful.customgun.client.input.shooter.ShootKey开始调用dev.xcolorful.customgun.client.entity.shooter.LocalShooterShoot
- TaCZ是com.tacz.guns.client.input.ShootKey调用com.tacz.guns.client.gameplay.LocalPlayerShoot
- TaCZ的本地事件监听在com.tacz.guns.client.event下的若干类，其中部分在CGC对应dev.xcolorful.customgun.client.entity.shooter.player下的若干类

现在需要调查TaCZ是否做了些修改来避免这个问题，或者是否是CGC哪里写的有问题
```

# 

I'll investigate this. Let me start by reading the migration mapping and the CGC-side firing code, then compare with TaCZ's approach.

调查完成。结论先行：**这不是 TaCZ 做了特殊规避，而是 CGC 的一处 bug——`ShootKey` 是唯一一个没有在「输入时刻」做焦点检查的按键。**

## 根因

CGC 的 `ShootKey` 用了一个输入队列来缓存按压/释放事件（做边沿检测，用于区分半自动点射和全自动连发）：

[ShootKey.java:110](core/src/main/java/dev/xcolorful/customgun/client/input/shooter/ShootKey.java:110)
```java
private void onShootKeyInput(int action) {
    switch (action) {
        case GLFW.GLFW_PRESS -> this.inputQueue.offer(ShootInput.PRESS);
        case GLFW.GLFW_RELEASE -> this.inputQueue.offer(ShootInput.RELEASE);
    }
}
```

这里**无条件入队**，没有任何焦点判断。焦点检查被推迟到了下一 tick 的 `_checkAndDoShoot` 里（[ShootKey.java:159](core/src/main/java/dev/xcolorful/customgun/client/input/shooter/ShootKey.java:159) `if (!ClientInputUtils.isGameplayFocused()) return false;`）——但那时队列里已经存下了暂停画面那一次点击的 `PRESS`。

具体时序：

1. 暂停画面打开，玩家点击「回到游戏」按钮。
2. GLFW 触发鼠标左键 `PRESS`，进入 `MouseHandler.onPress`。其中 `ForgeHooksClient.onMouseButtonPre`（[MouseHandler.java:76](core/build/tmp/.cache/expanded/zip_360104c6fc04675d4421fcf7cf7ba91a/net/minecraft/client/MouseHandler.java:76)）**在任何 screen 判断之前无条件触发**，于是 Forge 的 `InputEvent.MouseButton.Pre` 发出，CGC 的 `ShootKey.onMouseInput` 把 `PRESS` 入队。
3. 随后暂停画面才消费这次点击、关闭界面、重新 grab 鼠标。
4. 下一 tick，`onShootPreInput` 消费队列 → `hasPressedShoot = true`；此时 `isGameplayFocused()` 已变回 `true`，`_checkAndDoShoot` 看到 `shootPressed = true` → 误触开火。

## 为什么 TaCZ 没有这个问题

TaCZ 的 [ShootKey.java:58](D:\Github\TACZ\src\main\java\com\tacz\guns\client\input\ShootKey.java:58) 用的是 `SHOOT_KEY.isDown()`，在 tick 时**直接读当前物理按键状态**：

```java
boolean isShootDown = SHOOT_KEY.isDown() || controllerShootDown;
```

「回到游戏」的点击是一次完整的按下+抬起，到 tick 执行时鼠标已经松开，`isDown()` 为 `false`，所以不会误触。它没有缓存边沿事件，也就不存在「过期按压被延迟消费」的问题。焦点判断 `isInGame()`（[InputExtraCheck.java:9](D:\Github\TACZ\src\main\java\com\tacz\guns\util\InputExtraCheck.java:9)）和 CGC 的 `isGameplayFocused()` 逻辑等价，并非差异所在。

## 关键证据：兄弟按键都做了输入时刻的焦点检查

CGC 其它所有按键在 `onKeyInput`/`onMouseInput` 里**第一行**就检查焦点：

- [ReloadKey.java:101](core/src/main/java/dev/xcolorful/customgun/client/input/shooter/ReloadKey.java:101) `if (!ClientInputUtils.isGameplayFocused()) return;`
- `MeleeKey.java:77`、`AimKey.java:98`、`InspectKey.java:76`、`ProneKey.java:94`、`ZoomKey.java:80`、`SwitchFireModeKey.java:77`、`InteractKey.java:102`、`ConfigKey.java:79` 全是同一写法。

只有 `ShootKey` 把这个检查放到了 tick 阶段，导致暂停画面点击产生的 `PRESS` 被先入队、后消费。这就是它「独一份」误触的原因。

## 修复方向（未改动）

最小修法是在 `onShootKeyInput` 里对齐其它按键，入队前先做焦点检查（或在非焦点时清空 `inputQueue`）。需要注意一点：`ShootKey` 的队列本意是捕获本 tick 内的按压/释放边沿，加上焦点门槛后，非焦点期间（含暂停画面）的按压/释放会被整体丢弃，这正是期望行为，不会影响正常开火/连发。

需要我直接落地这个修复吗？还是你先确认方案？

# 
---

```
经测试：
- CGC：手持SLR（单发）时按住开火并切枪，表现为SLR开一枪，切到Beryl切枪动画结束后，一直播放开枪动画而没减少子弹
- TaCZ：手持SLR（单发）时按住开火并切枪，表现为SLR开一枪，刚切Beryl时播放一次开枪动画（没减少子弹），Beryl切枪结束时正常扫射

- 可以通过IDEA的mcp访问tacz源码
- tacz类到cgc的迁移映射在./docs-tacz/TaCZ Migration Mapping.md

现在需要调查TaCZ是否做了些修改来避免这个问题，或者是否是CGC哪里写的有问题

当前代码状态下的测试日志如下
[10Sep2026 14:04:36.749] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: LocalShooterProperty: operation LocalShooterShoot#shoot failed, current state: LocalShooterDraw#draw
[10Sep2026 14:04:36.749] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: ShootKey: local shoot failed (PRE_STATE_CHECK)
[10Sep2026 14:04:36.798] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: LocalShooterProperty: operation LocalShooterShoot#shoot failed, current state: LocalShooterDraw#draw
[10Sep2026 14:04:36.798] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: ShootKey: local shoot failed (PRE_STATE_CHECK)
[10Sep2026 14:04:36.847] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: LocalShooterProperty: operation LocalShooterShoot#shoot failed, current state: LocalShooterDraw#draw
[10Sep2026 14:04:36.847] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: ShootKey: local shoot failed (PRE_STATE_CHECK)
[10Sep2026 14:04:36.986] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: LivingShooterShoot: XiaoColorful shoot in shoot cooldown (cooldown: 9)
[10Sep2026 14:04:36.986] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: C2SMessageShooterShoot: XiaoColorful shoot failed (PRE_STATE_CHECK)
[10Sep2026 14:04:37.000] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: ShootKey: local shoot failed (STATE_LOCK)
[10Sep2026 14:04:37.098] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: ShootKey: local shoot failed (STATE_LOCK)
[10Sep2026 14:04:37.160] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: LivingShooterShoot: XiaoColorful shoot in shoot cooldown (cooldown: 7)
[10Sep2026 14:04:37.160] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: C2SMessageShooterShoot: XiaoColorful shoot failed (PRE_STATE_CHECK)
[10Sep2026 14:04:37.250] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: ShootKey: local shoot failed (STATE_LOCK)
[10Sep2026 14:04:37.334] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: LivingShooterShoot: XiaoColorful shoot in shoot cooldown (cooldown: 8)
[10Sep2026 14:04:37.334] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: C2SMessageShooterShoot: XiaoColorful shoot failed (PRE_STATE_CHECK)
[10Sep2026 14:04:37.353] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: ShootKey: local shoot failed (STATE_LOCK)
[10Sep2026 14:04:37.452] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: ShootKey: local shoot failed (STATE_LOCK)
[10Sep2026 14:04:37.509] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: LivingShooterShoot: XiaoColorful shoot in shoot cooldown (cooldown: 9)
[10Sep2026 14:04:37.509] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: C2SMessageShooterShoot: XiaoColorful shoot failed (PRE_STATE_CHECK)
[10Sep2026 14:04:37.601] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: ShootKey: local shoot failed (STATE_LOCK)
[10Sep2026 14:04:37.680] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: LivingShooterShoot: XiaoColorful shoot in shoot cooldown (cooldown: 8)
[10Sep2026 14:04:37.681] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: C2SMessageShooterShoot: XiaoColorful shoot failed (PRE_STATE_CHECK)
[10Sep2026 14:04:37.742] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: LocalShooterProperty: operation LocalShooterReload#reload failed, current state: LocalShooterShoot#shoot
[10Sep2026 14:04:40.506] [Server thread/INFO] [net.minecraft.client.server.IntegratedServer/]: Saving and pausing game...
```

# 

I'll investigate this. Let me start by reading the relevant documentation and the current state of the modified files.

Let me read the state machine classes and the TaCZ originals in parallel.

The comparison is revealing. Let me read the TaCZ data holder and draw logic, plus find where `tickStateLock` is called in both projects.

Let me look at the git diff to understand what's been changed on this branch, and compare with TaCZ's ShootKey.

The diff shows the user's changes are only debug logging — the underlying logic predates this branch. Let me compare the server-side draw handling and how shoot cooldown is synced, which is where the client-server desync likely originates.

Let me look at how the synced shoot cooldown is computed on the server and the charge logic, since `tickStateLock` depends on it.

Let me verify the server draw path and check the prior `#42` doc about a stuck state lock, which is closely related.

Let me examine the remaining server-side pieces — the shoot interval/Lua backcompat writes and the default attack, which are the last places `shootTimestamp` could be mutated.

Let me do a final targeted check for any other writers to `shootTimestamp` and how the server fire path interacts with it.

I've completed a thorough side-by-side comparison of the shoot/draw/cooldown flow between TaCZ and CGC. Here's what I found.

## 结论

TaCZ **没有**针对这个问题做任何特殊规避——射击、切枪、冷却这条链路的代码在两个项目里**几乎逐行一致**。我对照了客户端 `ShootKey` / `LocalPlayerShoot` vs `ShootKey` / `LocalShooterShoot`、切枪 `LocalPlayerDraw` vs `LocalShooterDraw`、服务端 `LivingEntityShoot` / `LivingEntityDrawGun` vs `LivingShooterShoot` / `LivingShooterDraw`、以及状态锁 `LocalPlayerDataHolder.tickStateLock` vs `LocalShooterProperty.tickStateLock`，核心逻辑完全相同。所以这是 CGC 自身的 bug（或一个两边共有、但在 CGC 里被触发的竞态）。

## 日志暴露的机制

服务端持续打出 `shoot in shoot cooldown (cooldown: 9/7/8/9/8)`，这来自 `LivingShooterShoot.isInServerShootCooldown` 的**射击冷却**分支，而不是切枪冷却分支。也就是说，切枪动画结束后，服务端的 `shootTimestamp` 仍是一个"很近"的值（≈ `shootInterval - 13ms`），而不是切枪时应该被 `resetProperty()` 重置成的 `-1`。

关键点：`LivingShooterDraw.draw()` 里确实调用了 `this.shooterProperty.resetProperty()`，它会把 `shootTimestamp = -1`（[ShooterProperty.java:131](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/entity/ShooterProperty.java)）。一旦是 `-1`，第一发 Beryl 的 `interval = clientTimestamp - (-1)` 会是一个大正数，冷却应为 0，会被接受。日志里冷却为 7-9ms 且**振荡**，说明 `shootTimestamp` 不是 `-1`，而是被"切枪之后才到达的一发"改写了。

## 找到的差异（都很小，但这是仅有的差异）

1. **客户端 `shoot()` 检查顺序**：[LocalShooterShoot.java:155](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/entity/shooter/LocalShooterShoot.java) 把 `!isShootRecorded || cooldown >= 50` 合并成 `STATE_LOCK` 并放在状态锁（IS_DRAWING）检查**之前**；TaCZ 是 `!isShootRecorded` → 状态锁 → `cooldown >= 50`。我推演下来它不影响结果（切枪期间 `cooldown` 恒为 0，两处都会落到状态锁/preCheck）。
    
2. **服务端 `shoot()` 检查顺序**：[LivingShooterShoot.java:88](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/entity/shooter/LivingShooterShoot.java) 把 `_shouldForceDisableShoot()`（含切枪冷却）放在射击冷却检查**之前**；TaCZ 是先查射击冷却、再查切枪。区别在于 CGC 在切枪期间会**短路跳过** `isInServerShootCooldown`（连带它的 alpha/base 重同步），但 bug 场景里切枪已结束、冷却检查是唯一在拒绝的那一环，所以这条也不是直接原因。
    
3. **切枪冷却的 5ms 窗口**：CGC `getDrawCooldown()` 是 `coolDown < 5 ? 0 : coolDown`，TaCZ 是 `max(coolDown - 5, 0)`，CGC 少了 `-5`，切枪冷却比 TaCZ 长 5ms——量级太小，且这是切枪冷却不是射击冷却。
    

## 最可能的根因

是一个**竞态**：SLR 那发的 C2S 是 `_doShoot` 里 `SCHEDULED_EXECUTOR_SERVICE` 异步线程发的（[LocalShooterShoot.java:310](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/entity/shooter/LocalShooterShoot.java)），而切枪的 `C2SMessageShooterDraw` 是渲染线程发的。当"按住开火"和"切枪"几乎同时发生时（tick N 的 END 与 tick N+1 的 HEAD 在时间上相邻），SLR 的 C2S 和切枪 C2S 到达服务端的顺序是不确定的。一旦 SLR 的 C2S 落在切枪之后，它会被当作 Beryl 的射击处理并**改写 `shootTimestamp`**，导致后续 Beryl 全自动射击全部落在"刚开过一枪"的冷却窗口里被拒，子弹不减少但动画照播。

我没有通过纯静态阅读把"为什么它会**持续**拒绝而不是一两发后自愈"完全坐实——按代码推演 `delay = cooldown` 的补偿会让时间戳精确地间隔一个 `shootInterval`，第二发本该冷却归零。这需要运行时数据才能钉死。

## 建议的下一步（最小改动）

在服务端 `isInServerShootCooldown` 拒绝时，把 `shooterProperty.shootTimestamp` 和收到的 `clientFromBaseToCurrentTimeMs` 一起打出来，并在 `LivingShooterDraw.draw()` / `resetProperty()` 打点确认切枪是否到达并重置：

```java
CustomGun.LOGGER.debug("... cooldown: {}, shootTimestamp: {}, clientTs: {}",
        cooldown, this.shooterProperty.shootTimestamp, clientFromBaseToCurrentTimeMs);
```

如果确认是 `shootTimestamp` 在切枪后被 SLR 那发改写，修法方向是**在切枪开始时同步取消/丢弃尚未发出的异步射击任务**（`LocalShooterProperty.drawFuture` 已有先例），或让 SLR 那发 C2S 带上"切枪前的枪械 id"让服务端按当时的枪校验。

需要我直接加这套定位日志，或者按"切枪时取消 pending 的异步射击"这个方向先做一版修复吗？

# 

```
我之前添加的日志已经commit了；你现在加上额外的定位日志，把你刚才研究到的可能的情况都要排查出来
```

# 

I've reviewed the committed state. Now I'll add targeted `[CGC-DEBUG]` logging across the server and client shoot/draw paths to pin down which of the hypotheses is actually happening. Let me start with the server-side shoot logic.

Now let me verify the changes compile. Let me check the edited files for problems.

构建通过（`isSuccess: true`，只有与本次改动无关的既有弃用警告，没有 error）。

我在射击/切枪链路上补了一套统一前缀 `[CGC-DEBUG]` 的日志，覆盖了我研究到的每一种可能。下面是新增的埋点和你该 grep 什么。

## 新增日志一览

|标签|位置|关键信息|
|---|---|---|
|`shoot.server.enter`|`LivingShooterShoot.shoot()` 入口|收到的 `clientTs`、当前 `shootTimestamp`、枪械 id|
|`shoot.server.disable`|服务端 `_shouldForceDisableShoot()`|拒绝原因（reloading/draw/bolting/sprint）|
|`shoot.server.cooldown`|服务端冷却拒绝|**`cooldown`、`clientTs`、`shootTimestamp`、`interval`、枪械 id**|
|`shoot.server.alpha`|服务端 alpha 窗口拒绝|`alpha`、`clientTs`、`shootTimestamp`|
|`shoot.server.accept`|服务端接受射击|`shootTimestamp` 旧值→新值|
|`shoot.client.enter`|客户端 `shoot()` 入口|`clientShootTimestamp`、`cooldown`、`isShootRecorded`|
|`shoot.client.result`|客户端 `shoot()` 返回|STATE_LOCK / IS_DRAWING / PRE_STATE_CHECK / SUCCESS|
|`shoot.client.disable`|客户端 `_shouldForceDisableShoot()`|拒绝原因|
|`shoot.client.doshoot`|客户端异步任务|`blocked by stateLock` / `send C2S`（含 `clientShootTimestamp` 旧→新）|
|`draw.server`|服务端切枪|`shootTimestamp` 重置前/后、新枪 id|
|`draw.client`|客户端切枪|`clientShootTimestamp` 重置前/后、旧枪/新枪 id|
|`draw.packet`|`C2SMessageShooterDraw` 收包|确认切枪包到达服务端|
|`stateLock.lock/wait/block/release`|客户端状态锁|上锁/等待/被哪个条件卡住/释放|

## 怎么读日志定案

**核心矛盾是我之前卡住的点**：按代码，切枪时服务端 `resetProperty()` 会把 `shootTimestamp` 置为 `-1`，那 Beryl 第一发 `interval` 就是大正数、冷却应为 0；但日志却显示冷却恒为 7-9ms 且振荡，说明 `shootTimestamp` 不是 `-1`。现在这一矛盾可以用一条日志直接判定：

看 `shoot.server.cooldown` 里的 `shootTimestamp` 值——

1. **如果是 `-1`**：说明切枪确实重置了，那 `interval = clientTs - (-1)` 就该是巨大正数、`cooldown` 应为 0，却出现了正数——这指向 `clientTs`（= `clientShootTimestamp - clientBaseTimestamp`）本身异常（比如 `clientBaseTimestamp` 没对齐）。这时看 `draw.client` 里的 `clientShootTimestamp` 重置、以及 `shoot.client.doshoot` 里的 `baseTs` 值。
    
2. **如果是一个很近的正数**（≈ `shootInterval` 之前）：说明切枪之后又有一发把 `shootTimestamp` 改写了。对比 `shoot.server.accept`（哪一发、什么时候被接受）和 `draw.server: enter` 的先后顺序，就能确认是不是 **SLR 那发异步 C2S 落在切枪包之后** 的竞态。配合 `shoot.client.doshoot: send C2S` 的时间戳，可以对齐客户端"发包时刻"与服务端"处理时刻"。
    
3. **如果根本没有 `draw.server` / `draw.packet` 日志**：说明切枪包没到服务端或没触发 `draw()`，那就是切枪链路断在客户端或网络层，需看 `draw.client` 是否触发。
    
4. 状态锁那条支线：如果 `stateLock.block: reason=shootCooldown=...` 持续出现且数值异常大，就是 #42 那类"锁被冷却卡死"的问题；如果 `stateLock.release` 正常出现，就能排除状态锁。
    

建议你复现后 `grep "CGC-DEBUG"` 全量抓日志（按时间戳排序即可，`draw.server` 和 `shoot.server.*` 是关键）。

按项目规则，我没有提交或推送。日志加完你进游戏复现一次，把带 `[CGC-DEBUG]` 的日志贴回来，我就能从上面 1-4 里锁定是哪种情况，再给最小修复。

# 

```
进游戏时手持SLR，在聊天栏输入start后，按住左键（SLR开一枪），然后左键不放，切到Beryl，Beryl不消耗子弹但是一直开火，日志一共有5W字符，我先取前几行你看看是否够排查问题？

[10Sep2026 15:13:39.962] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Not Secure] <XiaoColorful> start
[10Sep2026 15:13:39.967] [Render thread/INFO] [net.minecraft.client.gui.components.ChatComponent/]: [CHAT] <XiaoColorful> start
[10Sep2026 15:13:40.000] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.057] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.111] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.157] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.202] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.253] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.312] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.363] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.409] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.452] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.507] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.553] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.620] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.659] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.712] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.756] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.808] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.854] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.900] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:40.953] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:41.006] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:41.062] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:41.107] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:41.153] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:41.204] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:41.255] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:41.313] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:41.315] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.enter: gun=cbrg:slr, clientShootTimestamp=-1, cooldown=0, isShootRecorded=true
[10Sep2026 15:13:41.315] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.lock: state=LocalShooterShoot#shoot, hasCondition=true
[10Sep2026 15:13:41.315] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.result: SUCCESS, gun=cbrg:slr
[10Sep2026 15:13:41.315] [pool-7-thread-1/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.doshoot: send C2S, gun=cbrg:slr, clientShootTimestamp=-1->1789024421315, baseTs=1789024417661, sentTs=3654
[10Sep2026 15:13:41.316] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.server.enter: gun=cbrg:slr, clientTs=3654, charge=0.0, shootTimestamp=-1
[10Sep2026 15:13:41.327] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.server.accept: gun=cbrg:slr, shootTimestamp=-1->3654
[10Sep2026 15:13:41.361] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.wait: state=LocalShooterShoot#shoot, lockTime=46ms
[10Sep2026 15:13:41.408] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.wait: state=LocalShooterShoot#shoot, lockTime=93ms
[10Sep2026 15:13:41.455] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.block: state=LocalShooterShoot#shoot, reason=shootCooldown=26
[10Sep2026 15:13:41.512] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=LocalShooterShoot#shoot
[10Sep2026 15:13:41.553] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:41.614] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:41.654] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:41.715] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.release: state=null
[10Sep2026 15:13:41.715] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] draw.client: enter, clientShootTimestamp_before=1789024421315
[10Sep2026 15:13:41.715] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] draw.client: after reset, clientShootTimestamp=-1
[10Sep2026 15:13:41.715] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.lock: state=LocalShooterDraw#draw, hasCondition=true
[10Sep2026 15:13:41.715] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] draw.client: lastGun=cbrg:slr, currentGun=cbrg:beryl_m762
[10Sep2026 15:13:41.717] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.enter: gun=cbrg:beryl_m762, clientShootTimestamp=-1, cooldown=0, isShootRecorded=true
[10Sep2026 15:13:41.718] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: LocalShooterProperty: operation LocalShooterShoot#shoot failed, current state: LocalShooterDraw#draw
[10Sep2026 15:13:41.718] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.result: IS_DRAWING, gun=cbrg:beryl_m762
[10Sep2026 15:13:41.718] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: ShootKey: local shoot failed (IS_DRAWING)
[10Sep2026 15:13:41.732] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] draw.packet: received, player=XiaoColorful, selectedSlot=1
[10Sep2026 15:13:41.733] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] draw.server: enter, newGun=cbrg:slr, shootTimestamp_before=3654
[10Sep2026 15:13:41.733] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] draw.server: after reset, shootTimestamp=-1
[10Sep2026 15:13:41.752] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.wait: state=LocalShooterDraw#draw, lockTime=37ms
[10Sep2026 15:13:41.753] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.enter: gun=cbrg:beryl_m762, clientShootTimestamp=-1, cooldown=0, isShootRecorded=true
[10Sep2026 15:13:41.753] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: LocalShooterProperty: operation LocalShooterShoot#shoot failed, current state: LocalShooterDraw#draw
[10Sep2026 15:13:41.754] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.result: IS_DRAWING, gun=cbrg:beryl_m762
[10Sep2026 15:13:41.754] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: ShootKey: local shoot failed (IS_DRAWING)
[10Sep2026 15:13:41.815] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.block: state=LocalShooterDraw#draw, reason=drawCooldown=945
[10Sep2026 15:13:41.817] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.enter: gun=cbrg:beryl_m762, clientShootTimestamp=-1, cooldown=0, isShootRecorded=true
[10Sep2026 15:13:41.817] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: LocalShooterProperty: operation LocalShooterShoot#shoot failed, current state: LocalShooterDraw#draw
[10Sep2026 15:13:41.817] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.disable: reason=draw, drawCooldown=945
[10Sep2026 15:13:41.817] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.result: PRE_STATE_CHECK, gun=cbrg:beryl_m762
[10Sep2026 15:13:41.817] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: ShootKey: local shoot failed (PRE_STATE_CHECK)
[10Sep2026 15:13:41.856] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.block: state=LocalShooterDraw#draw, reason=drawCooldown=891
[10Sep2026 15:13:41.857] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.enter: gun=cbrg:beryl_m762, clientShootTimestamp=-1, cooldown=0, isShootRecorded=true
[10Sep2026 15:13:41.857] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: LocalShooterProperty: operation LocalShooterShoot#shoot failed, current state: LocalShooterDraw#draw
[10Sep2026 15:13:41.857] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.disable: reason=draw, drawCooldown=891
[10Sep2026 15:13:41.857] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.result: PRE_STATE_CHECK, gun=cbrg:beryl_m762
[10Sep2026 15:13:41.857] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: ShootKey: local shoot failed (PRE_STATE_CHECK)
[10Sep2026 15:13:41.913] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.block: state=LocalShooterDraw#draw, reason=drawCooldown=848
[10Sep2026 15:13:41.915] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.enter: gun=cbrg:beryl_m762, clientShootTimestamp=-1, cooldown=0, isShootRecorded=true
[10Sep2026 15:13:41.915] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: LocalShooterProperty: operation LocalShooterShoot#shoot failed, current state: LocalShooterDraw#draw
[10Sep2026 15:13:41.915] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.disable: reason=draw, drawCooldown=848
[10Sep2026 15:13:41.915] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.result: PRE_STATE_CHECK, gun=cbrg:beryl_m762
[10Sep2026 15:13:41.915] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: ShootKey: local shoot failed (PRE_STATE_CHECK)
[10Sep2026 15:13:41.951] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] stateLock.block: state=LocalShooterDraw#draw, reason=drawCooldown=802
[10Sep2026 15:13:41.953] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.enter: gun=cbrg:beryl_m762, clientShootTimestamp=-1, cooldown=0, isShootRecorded=true
[10Sep2026 15:13:41.953] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: LocalShooterProperty: operation LocalShooterShoot#shoot failed, current state: LocalShooterDraw#draw
[10Sep2026 15:13:41.953] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.disable: reason=draw, drawCooldown=802
[10Sep2026 15:13:41.953] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG] shoot.client.result: PRE_STATE_CHECK, gun=cbrg:beryl_m762
[10Sep2026 15:13:41.953] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: ShootKey: local shoot failed (PRE_STATE_CHECK)
```

# 

This log is very revealing. Let me analyze the key finding and check how TaCZ's draw packet handles this.

This log pinpoints the problem. The server drew the wrong gun. Let me confirm how TaCZ triggers the draw client-side, since the timing differs.

This is a clear finding. The server drew the **wrong gun** (`newGun=cbrg:slr`) while the client drew `beryl_m762`. Let me confirm how Minecraft syncs the hotbar slot to understand the timing race.

The first few lines are enough — they already reveal the root cause. Let me confirm the fix by checking how CGC accesses `gameMode`.

够了，前几行就锁定了根因——而且和我之前猜的（`shootTimestamp` 没重置）不一样，实际是**服务端把枪认错了**。

## 关键证据（前几行就出现）

```
draw.client: ... currentGun=cbrg:beryl_m762      ← 客户端正确切到 Beryl
draw.packet: received, ... selectedSlot=1
draw.server: enter, newGun=cbrg:slr              ← 服务端却把新枪当成 SLR
draw.server: after reset, shootTimestamp=-1       ← shootTimestamp 其实重置正确了
```

客户端和服务端在"当前是哪把枪"上出现了分歧：客户端是 Beryl，服务端是 SLR。

## 根因：迁移时丢掉了 `ensureHasSentCarriedItem()`

TaCZ 的 [LocalPlayerDraw.draw()](app://localhost/epitaxy/local_c1e34fe5-ed90-42d6-b40d-d8d0f4c43efa) 在发包前有这一步：

```java
// 发包通知服务器
if (Minecraft.getInstance().gameMode != null) {
    Minecraft.getInstance().gameMode.ensureHasSentCarriedItem();
}
NetworkHandler.CHANNEL.sendToServer(new ClientMessagePlayerDrawGun());
```

`ensureHasSentCarriedItem()` 会主动把玩家当前选中的快捷栏槽位（`ServerboundSetCarriedItemPacket`）同步给服务端。而 CGC 的 [LocalShooterDraw.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/entity/shooter/LocalShooterDraw.java) 迁移时**把这一行丢了**，直接 `SendUtils.sendMessageToServer(new C2SMessageShooterDraw())`。

后果链条：

1. 滚轮切枪只改了**客户端** `Inventory.selected`（`Inventory.swapPaint` 不发任何包）。
2. CGC 在下一个 tick 就发 `C2SMessageShooterDraw`。
3. 服务端处理时读 `inventory.getItem(selected)`，但服务端的 `selected` 还是**旧槽位**（SLR 所在槽），因为还没收到 `ServerboundSetCarriedItemPacket`。
4. 于是服务端 `currentGunItem` 被设成 SLR，而不是 Beryl。

之后你按住左键，客户端按 Beryl 全自动发 `C2SMessageShooterShoot`，服务端却用 SLR 的 `currentGunItem` 处理——扣的是 SLR 的子弹、走 SLR 的射击逻辑，所以你看 Beryl 一直有开火动画但 Beryl 子弹不减少。这正好也解释了之前日志里那串很小的 `shoot cooldown`（用的是 SLR 的射速间隔，而客户端按 Beryl 的节奏发包）。

## 修复方向

要恢复 TaCZ 的行为，需要两步：

1. **补回访问转换器**（TaCZ 有、CGC 删了）——`ensureHasSentCarriedItem()` 是 `MultiPlayerGameMode` 的 private 方法，TaCZ 靠 AT 打开权限。需在 [accesstransformer.cfg](app://localhost/epitaxy/forge-compat/src/main/resources/META-INF/accesstransformer.cfg) 加：
    
    ```
    public net.minecraft.client.multiplayer.MultiPlayerGameMode m_105297_()V # ensureHasSentCarriedItem
    ```
    
2. **在 [LocalShooterDraw.draw()](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/entity/shooter/LocalShooterDraw.java) 发 `C2SMessageShooterDraw` 之前**补上：
    
    ```java
    if (Minecraft.getInstance().gameMode != null) {
        Minecraft.getInstance().gameMode.ensureHasSentCarriedItem();
    }
    ```
    

这里涉及新增平台 API（`MultiPlayerGameMode#ensureHasSentCarriedItem`，需要 AT 打开），按项目规则我先报给你定夺是否照做，而不是直接改。

要我现在就按这个方向把修复落地吗？（AT 那条我加在 `forge-compat` 的 accesstransformer.cfg，调用加在 `LocalShooterDraw`，然后再进游戏验证一次。）

后台那条超时的 `find` 命令报失败（退出码 1），但无关紧要——它只是我之前搜 AT 时的一个冗余命令，我随后用更精准的 `grep` 已经拿到了答案（`ensureHasSentCarriedItem` 的 AT 与调用点都确认了）。

当前状态不变：根因已定位（`LocalShooterDraw.draw()` 漏了 `ensureHasSentCarriedItem()` 及其 AT），我已把修复方案报给你，**仍在等你确认是否落地**，没有擅自改动或提交。
