> #80 提示词

# 

```
看游戏目录./logs/debug.log第2796-2840行，排查下core\src\main\resources\assets\customguntab\cgc_scripts\gun_icon.lua还缺什么
```

# 

## 结论

报错根因很明确：**脚本没有 `return` 一个 table**。

日志 2797 行：

```
LuaError: bad argument: table expected, got nil
  at DataScript.executeAndCache(DataScript.java:100)
```

`DataScript.java:100` 和客户端对应的 `AssetsScript.java:104` 都是这样取值：

```java
this.resultTable = this.chunk.call().checktable(1);
```

Lua chunk 如果不 `return`，执行结果是 `nil`，`checktable(1)` 就抛 "table expected, got nil"，脚本被判无效，后续连锁出现：

- [debug.log:2846] `ScriptManager: File validation failed ... customguntab:gun_icon`
- [debug.log:2847] `GunIndexInstance: DataScript customguntab:gun_icon not found`
- [debug.log:2856] `GunDisplayInstance: AssetsScript customguntab:gun_icon not found` → `pistol_icon validation failed`

## `gun_icon.lua` 缺的东西

1. **顶层导出表**（就是上面的直接原因）。而且两个副本都要：
    - [assets/customguntab/cgc_scripts/gun_icon.lua](core/src/main/resources/assets/customguntab/cgc_scripts/gun_icon.lua) 只定义了两个 `local` 函数，没有返回；
    - [data/customguntab/cgc_scripts/gun_icon.lua](core/src/main/resources/data/customguntab/cgc_scripts/gun_icon.lua) 目前是 **0 字节**，空脚本同样返回 nil；它被 [pistol_icon.json](core/src/main/resources/data/customguntab/cgc_data/guns/pistol_icon.json) 的 `script_location` 引用。
2. **钩子函数必须是导出表的字段**。`local function` 不写进返回的表，等于没导出（`ScriptMethodType` 只从表里按名字取：`anim_init`/`initialize`、`anim_exit`/`exit`、`anim_states`/`states`）。
3. **函数签名要用冒号形式**。`LuaAnimStateMachine.java:65` 是 `initializeFunc.call(table, context)`，第一个参数是脚本表本身，所以应写 `function M:anim_init(context)`；现在的 `local function anim_init(context)` 即使导出，`context` 收到的也是脚本表。参照 TaCZ 的 `default_state_machine.lua` 写法。
4. **`anim_states()` 必须返回状态表数组**。没有它状态机照样能 build，但一个状态都没有；当前 [gun_icon.animation.json](core/src/main/resources/assets/customguntab/cgc_animation/gun_icon.animation.json) 的 `animations` 是空的，所以返回空数组即可（静态图标），以后要动效再加状态和 `anim_context_*` 钩子。

最小可用版本：

```lua
-- assets 侧：状态机
local M = {}

function M:anim_init(context)
end

function M:anim_exit(context)
end

function M:anim_states()
    return {}
end

return M
```

```lua
-- data 侧：枪械逻辑，图标不需要逻辑，空表即可
return {}
```

需要我直接把这两个文件改成上面的版本吗？（data 侧如果以后要写逻辑，同样用 `local M = {} ... return M`。）

# 

```
改成最小可用版本
```

# 

已改为最小可用版本：

- [assets/.../gun_icon.lua](core/src/main/resources/assets/customguntab/cgc_scripts/gun_icon.lua) — 导出 `M`，含 `M:anim_init`、`M:anim_exit`、`M:anim_states()`（返回空状态列表），末尾 `return M`。
- [data/.../gun_icon.lua](core/src/main/resources/data/customguntab/cgc_scripts/gun_icon.lua) — `return {}`，满足 `DataScript` 的表校验。

两个文件现在都能通过 `chunk.call().checktable(1)`。需要重新构建 jar 后进游戏 `/reload` 验证日志不再出现 `table expected, got nil` 和 `DataScript/AssetsScript ... not found`。
