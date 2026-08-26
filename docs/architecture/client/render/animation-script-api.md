# 动画状态机脚本 API

> 枪械的动画状态机脚本是一段 Lua，随资源包提供。它定义了「有哪些状态、每个状态播放什么动画、收到什么输入时转移到哪个状态」。本文说明这段脚本通过哪些能力影响动画与渲染，以及这些能力在整个动画系统里的位置。脚本本身的结构细节见 [动画状态机与轨道](./animation-state-machine.md)。

## 脚本与状态机的关系

`GunDisplayInstance` 构建状态机时，把 `GunDisplay` 里指定的 Lua 脚本编译成 `LuaTable`，交给 `LuaAnimStateMachine.Builder`。脚本表按约定分成两层：

- 顶层脚本表：提供 `anim_init`、`anim_exit`、`anim_states` 三个函数（旧脚本用 `initialize` / `exit` / `states`，通过 back-compat 兼容）。
- 状态表：`anim_states` 返回的每个状态是一个表，提供 `anim_context_update`、`anim_context_entry_action`、`anim_context_exit`、`anim_context_transition`（旧脚本用 `update` / `entry` / `exit` / `transition`）。

`LuaAnimStateMachine` 在初始化 / 退出 / 更新 / 转移时回调这些函数，并把 `GunAnimStateContext` 作为参数传入。

## 脚本的生命周期钩子

顶层脚本表的两个函数决定状态机的进出：

- `anim_init(context)`：状态机初始化时调用，通常在这里建立初始状态或做一次性准备。
- `anim_exit(context)`：状态机退出时调用，用于收尾。

`anim_states()`：返回初始状态列表。每个状态的 `entry` 在进入时调用、`exit` 在离开时调用、`update` 每帧调用、`transition(context, condition)` 在状态机收到输入时被调用并决定是否转移。

## 脚本对动画的操控能力

脚本最核心的能力是通过传入的 `context` 控制「在哪个轨道播放哪个动画」。这一组能力定义在 `AnimStateContext` 上，脚本调用它们即等同于操作动画控制器：

- 轨道管理：`addTrackLine` / `ensureTrackLineSize` / `assignNewTrack` / `findIdleTrack` / `getAsSingletonTrack` 决定动画落在哪条轨道上。
- 播放控制：`runAnimation`（播放并可选过渡）、`stopAnimation`、`holdAnimation`（拖到末尾定格）、`pauseAnimation`、`resumeAnimation`。
- 进度控制：`setAnimationProgress`、`adjustAnimationProgress` 直接拖动或微调动画进度。
- 状态查询：`isHolding` / `isStopped` / `isPause` / `hasAnimationPrototype` 用于脚本自己判断动画状态。
- 主动转移：`trigger` 让脚本在动画内部再触发一次状态机输入。

这些能力让脚本既能在「状态转移」粒度上编排动画（用 `transition`），也能在「单条动画」粒度上精细控制播放（用 `runAnimation` / `holdAnimation` 等）。

## 脚本能拿到的常量

两类 Lua 库在脚本加载时注入常量，避免脚本里写魔法数字：

- `LuaAnimationLib`：注入 `AnimationPlayType` 的枚举名（`PLAY_ONCE_HOLD` / `PLAY_ONCE_STOP` / `LOOP`），对应它们的 ordinal 值，供 `runAnimation` 的 playType 参数使用。
- `LuaGunAnimationLib`：注入 `GunAnimationState` 的常量（如 `INPUT_SHOOT`、`INPUT_RELOAD`）、`ReloadState.StateType`、`FireModeType` 的枚举值，供脚本写 `transition` 条件与读取枪械状态。

## 脚本能读取的枪械与射手状态

`GunAnimStateContext` 实现了 `IClientGunScriptApi`，把渲染链路里现成的数据暴露给脚本，脚本据此判断「现在应该播什么」：

- 枪械对象与物品：`getIGun` / `getGunItem`。
- 资源实例：`getGunIndexInstance`（枪械数据）、`getGunDisplayInstance`（显示配置与模型）。
- 射手与摄像机：`getLocalShooter` / `getILocalShooter` / `getCameraShooter`。
- 帧信息：`getPartialTicks`、`getPutAwayTime`。
- 状态机参数：`getStateMachineParams` 返回 display 里配置的脚本参数表。

这一层之上还有一叠 back-compat 辅助方法（`IClientGunScriptBackCompat`），把常见的组合逻辑封装成单调用，例如：

- 射击相关：`getAimingProgress`、`getChargeProgress`、`hasBulletInBarrel`、`isOverheat`、`getShootCooldown`、`adjustClientShootInterval`（客户端侧微调射击间隔）。
- 移动输入：`isInputUp` / `isInputDown` / `isInputLeft` / `isInputRight` / `isInputJumping` / `isInputProne` / `isOnGround` / `isCrouching`。
- 姿态谓词：`shouldTilting`（蹲下侧倾，见 [枪械附加变换模块](./gun-render-addons.md)）。
- 移动距离：`anchorWalkDist` / `getWalkDist`，用于「走了多少路」驱动走路 / 跑步动画。
- 视觉附加：`popShellFrom(index)` 触发指定抛壳口的抛壳渲染（见 [功能性渲染器](./functional-renderers.md)）、`setShouldHideCrossHair` 控制准心显隐。

## 脚本影响渲染的路径

脚本自身不直接写模型。它所有的影响都收敛到两条路径：

1. 通过 `runAnimation` 等让动画控制器在轨道上运行动画，动画关键帧经监听器写入模型节点。
2. 通过读取枪械 / 射手状态做条件判断，决定状态机朝哪个状态转移。

脚本里的 `popShellFrom`、`adjustClientShootInterval`、`setShouldHideCrossHair` 等属于对渲染 / 表现的「旁路」操作，直接调用对应的渲染模块或射手状态，而不经过动画关键帧。
