> #101 提示词

# 

```
测试枪械：tacz:ak47（实际任意枪械都触发）
开启跑动时射击、跑动时装弹等配置时，装弹、瞄准、射击都没有直接覆盖第一人称跑动动画，而是叠加
第一人称跑动+瞄准时装弹则会取消跑动动画的叠加（正常）
第一人称跑动+检视时则没有跑动动画的叠加（正常）

需要研究这个状态输入，当有射击、装弹、瞄准、检视时应该最多只有移动时动画叠加，没有跑动移动时动画叠加
```

# 

I'll investigate the first-person animation state input. Starting with the architecture docs and the animation layer code.

Found `animation-state-machine.md` — that's the core doc.

`_LocalAnimHandler` posts `idle`/`walk`/`run` each tick — that's the state input to inspect.

The base state machine (`default_state_machine_v2.lua`) holds `movement_track_states`. Reading it.

The test pack's `default_state_machine_v2.lua` already contains an `allow_run_overlay` mechanism that explicitly permits reload/draw/bolt to overlay run. Checking whether that's in the repo or only in the test game dir.

Now I have the full picture. Applying the fix in the client state input.

Compile is running. While it works, let me confirm how the aiming state and reload state line up with what the script expects.

Build succeeded. Finalizing the change.

## 调查结论

**状态输入在哪里**：`_LocalAnimHandler._tickAnimState`（[_LocalAnimHandler.java:89](core/src/main/java/dev/xcolorful/customgun/client/entity/shooter/player/_LocalAnimHandler.java:89)）每 tick 只按玩家移动状态投递 `idle` / `walk` / `run`，完全不看枪在做什么。上游 TaCZ 的 `TickAnimationEvent` 也是同样的写法——它之所以不出问题，是因为上游默认在换弹/瞄准时直接禁止疾跑。

**为什么会叠加**：
- **换弹**：`default_state_machine_v2.lua` 里的 `allow_run_overlay`（掏枪/换弹/拉栓置 true）会让 movement 状态停在 run，run 动画因此叠在换弹上。旧版 `default_state_machine.lua` 没有这个机制，靠"主轨道被占用就降级到 walk"，所以只有 v2 脚本的枪（默认包里的大部分枪）才暴露这个问题。
- **瞄准/射击**：两者都不占主轨道（瞄准在 `ADS_TRACK`，射击在 `GUN_KICK_TRACK_LINE`），run 判定只看主轨道是否空闲，于是 run 一直播。
- **检视**：检视占主轨道且 `allow_run_overlay` 为 false，脚本自动降级到 walk——所以你说它是正常的，这条路径本来就是对的。
- **跑动+瞄准+换弹**：瞄准默认会取消疾跑（`ENABLE_SPRINT_WHILE_AIMING` 默认关闭，见 [LivingShooterSprint.java:52](core/src/main/java/dev/xcolorful/customgun/core/entity/shooter/LivingShooterSprint.java:52)），根本没进 run，所以看起来也正常。

**改动**：在状态输入这一层拦截，进入移动分支时若处于瞄准（`cgc$isAim`）、射击冷却中（`cgc$getShootCooldown() > 0`）或换弹中（同步的 `ReloadState.isReloading()`），就只投递 `walk`。这样对所有枪包和脚本都生效，不需要改 Lua；检视走原来的脚本降级，没有动。同时在 [animation-state-machine.md:83](docs/architecture/client/render/animation-state-machine.md:83) 补了一句说明。

`./gradlew :core:compileJava` 通过（只有既有的 deprecation 警告）。**没有在游戏内验证**，也没有提交。

一个已知的边缘表现：半自动连点时，冷却期和非冷却期会把移动叠加在 walk/idle 和 run 之间来回切，每次切换会播 `run_start`/`run_end`；全自动和换弹/瞄准是稳定的。如果实测觉得连点抖动明显，可以再把"射击"的判断改成"最近一次开火后延迟一小段"，你测完告诉我。
