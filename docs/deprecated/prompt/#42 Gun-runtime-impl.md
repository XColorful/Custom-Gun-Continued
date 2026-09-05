> #42 提示词

# 

```
ak47用的xmag_reload_logic.lua会在枪管1发子弹，弹匣29/30的时候无法在进行装弹

- 可以通过IDEA的mcp访问tacz源码
- tacz所有脚本位于游戏目录下.\saves\新的世界\datapacks\tacz_default_gun\data\tacz\scripts
- tacz类到cgc的迁移映射在./docs-tacz/TaCZ Migration Mapping.md

辅助快速查找：
- 枪械脚本调用的api方法，在cgc里对应IGunScriptBackCompat的方法

检查问题所在并尝试修复
```

# 

I'll investigate this reload bug. Let me start by reading the key files in parallel.

I've done extensive analysis. Let me verify the two clear bugs I found and apply fixes. First, the `adjustReloadTime` bug (modifies the wrong field):

Let me verify my edits are valid by checking for compile problems:

Let me verify the changes compile cleanly with a targeted build:

I've completed my investigation and applied two fixes. Here's what I found.

## What I fixed

**1. `adjustReloadTime` wrote to the wrong field** — [IGunScriptBackCompat.java:165](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/script/context/IGunScriptBackCompat.java:165)

```java
shooterProperty.shootTimestamp -= alpha;   // before — wrong field
shooterProperty.reloadTimestamp -= alpha;  // after
```

This is the `IGunScriptBackCompat` method that backs the Lua `adjustReloadTime` API. It was shifting the shoot timestamp instead of the reload timestamp, so any script that used it would corrupt the fire-cooldown clock instead of the reload clock. `adjustBoltTime` and `adjustShootInterval` next to it were correct, so this was an isolated copy/paste slip.

**2. `isReloadFeeding()` misclassified tactical-finishing as feeding** — [ReloadState.java:135](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/entity/ReloadState.java:135)

```java
return this == StateType.EMPTY_RELOAD_FEEDING || this.category == StateCategory.TACTICAL_RELOAD;  // before
return this == StateType.EMPTY_RELOAD_FEEDING || this == StateType.TACTICAL_RELOAD_FEEDING;       // after
```

The original checked `category == TACTICAL_RELOAD`, which is true for _both_ `TACTICAL_RELOAD_FEEDING` and `TACTICAL_RELOAD_FINISHING`. So the default reload path could run its "feed ammo" step during the finishing phase too. (The empty-bolt branch was already written as a concrete `==` comparison; the tactical branch was the odd one out.)

Both edits compile cleanly — the targeted build returned `isSuccess: true` with only pre-existing deprecation warnings.

## What I traced (and did not find a bug in)

I walked the full AK reload path against the TACZ reference: client `ReloadKey` → `LocalShooterReload` → `canReload` (`_DefaultGunAction.canReload`, since the Lua script defines no `can_reload`) → server `LivingShooterReload.reload` → `start_reload` → `tick_reload` → the Lua `finishReload`. For "barrel = 1, magazine 29/30", this classifies as a _tactical_ reload (`boltType.useBarrelAmmo() && hasBarrelAmmo()`), and `finishReload(api, true)` computes `getNeededAmmoAmount() = 30 - 29 = 1`, consumes 1 from inventory, and tops the magazine to 30 — leaving the chamber round alone, which is correct. The ammo-count math (`getMagAmmoLimit` = 30 from `ammo_amount`, `getMagAmmoCount` reading only `MAG_AMMO`, not the barrel) and the reload-state ordinals (`getIndex()` vs the `LuaGunLogicLib` constants) all line up with TACZ.

## Honest caveat

The two bugs above are real and are in the reload code the hint pointed at, but neither is exercised by `xmag_reload_logic.lua`'s tactical reload specifically — `adjustReloadTime` isn't called by that script, and `isReloadFeeding` is only used by the default (non-Lua) reload path. I did not find a third defect that would _unconditionally block_ reloading at exactly "chamber 1 / mag 29/30"; the logic reads correct end-to-end. If the repro is still present after these fixes, the next thing I'd want is a live log capture (specifically the `GunActionManager: Failed to tick reload` error path) or the exact ammo values via the new `/customgun debug showMagAmmo` command, since that would tell us whether the reload is being refused, silently no-op'd, or throwing during the Lua call.

Per the project rules, I've stopped here for your review — I haven't committed or pushed.

# 

```
刚才的修改是否会修复某个潜在的问题？给我做描述

之后，在调用链路上都加新的log来排查问题
- log都使用相同前缀[CGC-DEBUG]以便我搜集
- 从客户端输入到最终装弹判定的链路都要加上，以便排查在哪一步断了
```

# 

I'll describe the previous fixes, then instrument the reload call chain with `[CGC-DEBUG]` logs. First, let me locate the server-side handler for the reload packet and re-read the files I need to edit.

Now let me instrument the full call chain. I'll add `[CGC-DEBUG]` logs from client input through to the ammo-transfer API methods.

Let me verify all instrumented files compile cleanly:

构建通过（`isSuccess: true`，只有与本次改动无关的既有弃用告警）。

## 关于之前两处修改：它们修的是什么，会不会修掉你说的 bug

**1. `adjustReloadTime` 写错字段** — [IGunScriptBackCompat.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/script/context/IGunScriptBackCompat.java)

之前是 `shootTimestamp -= alpha`，现在改为 `reloadTimestamp -= alpha`。这是一个**真实但潜伏**的 bug：只有当某个 Lua 脚本调用 `adjustReloadTime`（比如"快速换弹"类脚本）时才会触发。`xmag_reload_logic.lua` 并没有调用这个 API，所以**这处修改不会直接修复你报的 AK 复现**，它修的是其它会用到该 API 的脚本的潜在问题。

**2. `isReloadFeeding()` 判定错误** — [ReloadState.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/entity/ReloadState.java)

之前是 `category == TACTICAL_RELOAD`（对 `TACTICAL_RELOAD_FINISHING` 也返回 true），现在改为 `== TACTICAL_RELOAD_FEEDING`。这**只影响默认（非 Lua 脚本）换弹路径**，而且即使触发，第二次 `feed` 因为 `needAmmoCount` 已为 0 也是空操作。AK 走的是 Lua 脚本换弹，所以这处**同样不会直接修复 AK 复现**。

结论：这两处是顺手修掉的真实 bug，但都不是你这条 AK 报错的根本原因——所以我按你的要求把整条链路打上日志，用日志来定位断点。

## 新增的 `[CGC-DEBUG]` 日志链路

所有日志统一前缀 `[CGC-DEBUG]`，用 `grep "CGC-DEBUG"` 即可搜集。从客户端按键到最终装弹判定，每步都已埋点：

|阶段|位置|日志标记|关键信息|
|---|---|---|---|
|客户端按键|`ReloadKey.onReloadKeyInput`|`reload.key`|按下 R 时的 magAmmo/limit/barrel|
|客户端换弹入口|`LocalShooterReload.reload`|`reload.client`|stateLock、magAmmo/limit/barrel|
|客户端 canReload|同上|`reload.client`|`canReload=` 结果|
|客户端发包|同上|`reload.client`|"sending packet"|
|客户端动画|`LocalShooterReload._doReload`|`reload.client`|bolt、magAmmoWithBarrel|
|服务端收包|`ClientMessagePlayerReloadGun.handle`|`reload.packet`|玩家名|
|服务端换弹入口|`LivingShooterReload.reload`|`reload.server`|isReloading/isBolting/冷却/magAmmo/barrel|
|服务端 startReload|同上|`reload.server`|`startReload=` 结果|
|服务端状态分类|`LivingShooterReload._doReload`|`reload.server`|bolt、barrelAmmo、tactical|
|服务端逐 tick|`LivingShooterReload.tickReloadState`|`reload.server`|每 tick 的 state/countdown/elapsedMs|
|管理器 canReload|`GunActionManager.canReload`|`reload.manager`|scriptResult、result|
|管理器 startReload|`GunActionManager.startReload`|`reload.manager`|canReload、事件取消、scriptResult|
|管理器 tickReload|`GunActionManager.tickReload`|`reload.manager`|hasLuaFunction、lua state/countdown|
|默认 canReload 判定|`_DefaultGunAction.canReload`|`reload.check`|freeAmmoFeed/needCheckAmmo/magAmmo/barrel + 每个返回分支|
|Lua API 判定|`IGunScriptBackCompat` 系列|`reload.api`|见下|

`reload.api` 下覆盖了 Lua 脚本调用的全部方法：`getReloadTime`、`getReloadStateType`、`getMagExtentLevel`、`getNeededAmmoAmount`、`isReloadingNeedConsumeAmmo`、`consumeAmmoFromPlayer`、`putAmmoInMagazine`、`removeAmmoFromMagazine`、`setAmmoInBarrel`（int 与 boolean 两个重载）。

**怎么读这条链路定位断点**（按日志出现顺序）：

1. 有 `reload.key` 但没有 `reload.client: enter` → 卡在客户端按键判定（旁观/无枪/背包直读）。
2. 有 `reload.client: canReload=false` → 卡在客户端 canReload（这里会同时打印 magAmmo/limit/barrel）。
3. 有 `reload.client: sending packet` 但没有 `reload.packet` → 网络/发包问题。
4. 有 `reload.packet` 但没有 `reload.server: startReload=true` → 服务端 startReload 被拒，看 `reload.manager: startReload canReload=` / `reload.check: canReload` 的具体分支。
5. 有 `reload.server: _doReload` 但 tactical 判定不对 → 状态分类问题。
6. tick 阶段 `reload.manager: tickReload hasLuaFunction=false` → Lua 脚本没加载，走了默认路径。
7. 到了 tick 阶段却始终不见 `reload.api: getNeededAmmoAmount` / `putAmmoInMagazine` → Lua 的 `finishReload` 一直没触发（喂弹时机未到或状态卡住，结合 `getReloadTime`/`getReloadStateType` 看）。
8. 出现了 `putAmmoInMagazine` 但 `magAmmo X->Y` 没变化或 `consumed=0` → 弹药消耗/装填环节断了。

按项目规则，我停在改动完成后等待你审阅，没有提交或推送。

