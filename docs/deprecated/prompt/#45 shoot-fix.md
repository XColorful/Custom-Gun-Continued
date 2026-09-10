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
