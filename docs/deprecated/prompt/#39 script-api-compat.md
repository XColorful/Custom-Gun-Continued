> #39 提示词

# 

```
现在要解决资源包脚本的兼容性问题

目前有default_state_machine.lua第555行崩溃问题：
org.luaj.vm2.LuaError: @tacz/default_state_machine:555 attempt to call a nil value
请检查IGunScriptBackCompat、IClientGunScriptBackCompat是否足够支持tacz的两个脚本api，并且检查tacz数据包和资源包的脚本里是否存在者用调用的变量继续调用，从而导致script api本身还不够，需要额外检查其他类也需要做back-compat接口
tacz到cgc类的迁移映射在./docs-tacz/TaCZ Migration Mapping.md

当前IDEA打开了TaCZ的窗口，可以浏览源码

资源包状态机脚本在游戏目录下resourcepacks\tacz_default_gun\assets\tacz\scripts

数据包脚本在游戏目录下
saves\新的世界\datapacks\tacz_default_gun\data\tacz\scripts

需要列出所有没兼容上的脚本接口，以及分析555行call nil崩溃的原因
```

# 

我先理解项目结构和迁移映射，然后定位 back-compat 接口和崩溃点。

Now let me understand how CGC loads and executes these state machine scripts, and check for other copies of the script that might differ.

I have the core picture now. Let me systematically extract every script API call from all scripts, then compare against the original TaCZ API and CGC's back-compat interfaces.

我已完成全面排查。以下是结论。

## 结论先行

`IGunScriptBackCompat` + `IClientGunScriptBackCompat` **基本够用，但有 2 处方法缺口 + 1 处全局常量缺口 + 1 处行为 bug**。555 行崩溃的直接原因是**方法名大小写不匹配**。

---

## 一、555 行崩溃原因（已定位到根因）

崩溃行是 `default_state_machine.lua:555`：

```lua
function over_heat_states.normal.update(this, context)
    if (isOverHeat(context)) then   -- line 555
```

`isOverHeat` 是脚本第 102 行定义的本地包装函数，内部调用 `context:isOverHeat()`（**大写 H**）：

```lua
local function isOverHeat(context)
    return context:isOverHeat()     -- line 103
end
```

而 CGC 的 [IClientGunScriptBackCompat.java:114](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/api/script/context/IClientGunScriptBackCompat.java) 提供的方法是：

```java
default boolean isOverheat() {   // ← 小写 h
    return this.isOverheatLocked();
}
```

原版 TaCZ 的 `GunAnimationStateContext` 是 `isOverHeat()`（大写 H，[TaCZ 源码第 88 行](app://localhost/epitaxy/local_98e81890-3cbd-4c99-9663-cbe316eb1150)）。

**LuaJ 的 Java 方法查找是大小写敏感的**。我核对了 LuaJ 3.0.8-figura 的源码：`JavaClass.getMethod()` 用 `HashMap` 以 `LuaString.valueOf(mi.getName())` 精确作 key，`methods.get(key)` 精确匹配（`LuaString.equals` 区分大小写）。所以 `context.isOverHeat`（大写 H）查不到 → 返回 nil → `nil(...)` 抛出 `attempt to call a nil value`。

（报错定位在 555 而非 103，是因为本地包装函数 `isOverHeat` 无 `pcall`，错误沿闭包链上抛到外层调用点，LuaJ 的 `fileline` 归属受 debug 栈帧影响落在 555；根因仍是 103 行那个方法名查不到。）

**修复**：在 `IClientGunScriptBackCompat` 增加 `isOverHeat()`（大写 H），或把 `isOverheat` 改名/加别名。

---

## 二、所有没兼容上的脚本接口清单

我把资源包 33 个状态机脚本（1900 处 `context:` 调用）和数据包 10 个逻辑脚本（188 处 `api:` 调用）逐条比对了两套 back-compat 接口 + 原版 TaCZ 源码，结果如下：

### 1. 方法缺口（会崩/会静默失效）

|脚本调用|CGC 现状|影响|
|---|---|---|
|`context:isOverHeat()`|只有 `isOverheat()`（小写 h）|**555 行崩溃**（default / hk_mk23 / minigun 三个脚本用）|
|`context:isAiming()`|完全缺失（应委托 `ILocalShooter.cgc$isAim()`）|default:513/529、hk_mk23:531/547 的滑铲状态，`isAiming()` 返回 nil → 同样 `attempt to call a nil value`|

### 2. 全局常量缺口

|脚本用到的常量|CGC 现状|影响|
|---|---|---|
|`INPUT_FIRE_SELECT`|被重命名为 `INPUT_SWITCH_FIRE_MODE`（[GunAnimationState.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/api/animation/statemachine/GunAnimationState.java)，值也从 `"fire_select"` 改成 `"switch_fire_mode"`）|14 个脚本（ak47/b93r/fn_fal/g36k/hk416d/hk_g3/hk_mp5a5/m4a1/qbz_191/scar_h/scar_l/spas_12/spr15hb/vector45）的 `input == INPUT_FIRE_SELECT` 恒为 false，**换火模式切换静默失效**（不崩，但功能坏）|

`LuaGunAnimationLib` 只按 `state.name()` 装常量，装的是 `INPUT_SWITCH_FIRE_MODE`，没装旧名 `INPUT_FIRE_SELECT`，也没装旧值 `"fire_select"`。其余 `INPUT_*`（BOLT/DRAW/PUT_AWAY/INSPECT/BAYONET_*/RELOAD/CANCEL_RELOAD/SHOOT/WALK/RUN/IDLE）与 `PLAY_ONCE_*`/`LOOP`/`BURST`/`SEMI`/`AUTO` 均匹配。

### 3. 行为 bug（接口在，实现错）

[IGunScriptBackCompat.java:480-489](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/script/context/IGunScriptBackCompat.java) 的 `getAttachment`：

```java
if (ResourceTag.NULL_LOCATION.equals(attachmentLocation)) return EMPTY_ATTACHMENT_ID;
else return attachmentCategory.toString();   // ← 应返回 attachmentLocation.toString()
```

返回的是**分类名**（如 `"SCOPE"`）而不是**配件 ID**（如 `"tacz:scope_acog_ta31"`）。导致 `m16a4_state_machine.lua:31` 的 `getAttachment("SCOPE") == "tacz:scope_acog_ta31"` 永远为 false。

---

## 三、关于「用调用的变量继续调用」（链式调用）的结论

**tacz_default_gun 的脚本没有链式调用**。我扫了所有脚本的方法调用接收者，只有 `context`（1900 处）和 `api`（188 处）两种，全部是直接 `context:xxx()` / `api:xxx()`。`getNbt` / `getNbtAccessor` / `getEntityUtil` / `getAbstractGunItem` / `getItemStack` / `getShooter` / `getDataHolder` 等返回对象再调方法的模式**在这些脚本里一次都没出现**（grep 为空）。

因此：

- 对这些脚本而言，`IGunScriptBackCompat` / `IClientGunScriptBackCompat` 本身够用，**不需要再给 `_LuaNbtAccessor`、`_LuaEntityAccessor`、`_AbstractGunItem` 等辅助类补 back-compat**（它们已存在并接好，只是这些脚本没用到）。
- 但要注意：`getAttachment` 返回 String 后脚本直接拿它比较（属于「返回值继续被用」，但不属于「再调方法」），这个比较语义已被第 3 点的 bug 破坏。

服务端（数据包）`api:` 的 34 个方法（`shootOnce`/`getScriptParams`/`setAmmoInBarrel`/`consumeAmmoFromPlayer`/`getReloadStateType`/全套 heat 方法等）**全部已在 `GunScriptApi` + `IGunScriptBackCompat` 中覆盖**，无缺口。

---

## 四、建议的最小修复（供参考，尚未改动代码）

1. `IClientGunScriptBackCompat` 加 `default boolean isOverHeat() { return this.isOverheatLocked(); }`（或直接把 `isOverheat` 改名并保留旧名）。
2. `IClientGunScriptBackCompat` 加 `default boolean isAiming() { ILocalShooter s = getILocalShooter(); return s != null && s.cgc$isAim(); }`。
3. `LuaGunAnimationLib` 补映射 `INPUT_FIRE_SELECT → "fire_select"`（与 `INPUT_SWITCH_FIRE_MODE` 并存）。
4. `IGunScriptBackCompat.getAttachment` 的 `else` 分支改为返回 `attachmentLocation.toString()`。

需要我按上述修复动手改代码吗？还是你先确认这份接口缺口清单？

# 

```
帮我完成修改

并且在LocalShooterSwitchFireMode里trigger INPUT_SWITCH_FIRE_MODE的情况下，看下如果连续trigger新旧两个enum，对tacz状态机脚本是否影响结果/有额外执行一遍default case？
```

# 

我先读取需要修改的文件和触发机制相关代码，理清 `LocalShooterSwitchFireMode` 的 trigger 逻辑。

确认了关键类型。现在开始做修改。先改 `IClientGunScriptBackCompat`（补 `isOverHeat` 和 `isAiming`）。

修改已完成。现在验证编译是否通过，检查这 4 个文件的问题。

4 处修改全部完成，IDE 检查无编译错误。以下是总结。

## 已完成的修改

**1. [IClientGunScriptBackCompat.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/api/script/context/IClientGunScriptBackCompat.java)** — 补两个方法：

- `isOverHeat()`（大写 H）别名，委托 `isOverheat()` → 修复 555 行崩溃。
- `isAiming()`，委托 `ILocalShooter.cgc$isAim()` → 修复滑铲状态（default:513/529、hk_mk23:531/547）。

**2. [LuaGunAnimationLib.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/api/script/LuaGunAnimationLib.java)** — 补常量映射：

- `INPUT_FIRE_SELECT → "fire_select"`（取 `GunAnimationState.INPUT_SWITCH_FIRE_MODE.typeNameOld`）。

**3. [IGunScriptBackCompat.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/script/context/IGunScriptBackCompat.java)** — 修 `getAttachment` 返回值：

- `return attachmentCategory.toString()` → `return attachmentLocation.toString()`（现在返回真实配件 ID 而非分类名）。

**4. [LocalShooterSwitchFireMode.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/entity/shooter/LocalShooterSwitchFireMode.java)** — 连续触发新旧两个输入。

## 关于「连续 trigger 新旧两个 enum」的分析

**结论：安全，不会造成错误结果，也不会「多执行一遍有副作用的 default case」。**

关键事实：tacz 的 33 个脚本里 `INPUT_FIRE_SELECT` 只出现在 `inspect_state.transition`，用于「检视时切火 → 打断检视返回 idle」。其余所有状态（idle/movement/ADS/slide/fire_mode_state）的 `transition` 既不认 `INPUT_FIRE_SELECT` 也不认 `"switch_fire_mode"`。脚本里没有任何 `"switch_fire_mode"`/`"fire_select"` 字符串字面量（已 grep 确认，只有 14 处全局 `INPUT_FIRE_SELECT` 引用）。

所以连续 `trigger("fire_select")` + `trigger("switch_fire_mode")` 时：

- `"fire_select"` 命中 inspect 的打断逻辑 → 正确转移 `inspect → idle`。
- `"switch_fire_mode"` 对 tacz 脚本是未知输入，会走 `transition` 的 fallthrough 链（`inspect_state.transition → main_track_states.inspect.transition → main_track_states.idle.transition`，即你说的「default case」），**但这条链对 `"switch_fire_mode"` 全程返回 nil，无状态转移、无动画、无副作用**。

也就是说，「default case」确实会被**多求值一次**，但它是纯空转（返回 nil），不产生任何副作用，最终结果与只 trigger 一次完全一致。两者顺序也无关（`fire_select` 命中转移后，`switch_fire_mode` 落在新的 idle 态上仍是 nil）。

唯一代价是每次切火多一次廉价的 transition 链求值，可忽略。之所以选「都 trigger」而不是只 trigger 旧的：`GunAnimationState.fromString` 已把 `"fire_select"` 和 `"switch_fire_mode"` 当作同一逻辑输入的别名，这样旧 TaCZ 脚本和未来新脚本都能命中，互不干扰（没有任何脚本同时认两个值，不会双转移）。

## 验证情况

4 个文件经 IntelliJ 检查（`get_file_problems`）均无 error；改动是纯增方法 + 一处返回值修正 + 一次行为修正，未触碰现有调用方。未跑全量 gradle build（改动简单且 IDE 增量编译已通过）。

需要我继续跑一次完整构建，或把这几个改动整理成 commit 吗？
