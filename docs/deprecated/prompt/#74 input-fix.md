> #74 提示词

# 

```
现在需要研究下dev.xcolorful.customgun.client.input的InputKeyManager和InputKey的配合，是否可以实现设置按键的时候同时检测按键和modifier，从而避免将modifierkey的判定交给InputKey判定（如CGC Config Addon里的dev.xcolorful.cgcconfig.client.input.config.ConfigKey
- 需要在一般按键（无modifier）时也能通过
- 设置modifier键的时候需要包含modifier key按下才调用子InputKey
- 估计改动不会很大，应该只在InputKeyManager；已有的CGC Config Addon里的实现应该也会兼容（调用时一定按下了modifier key）
```

# 

我先看下这两个类的实现和 CGC Config Addon 里的调用方。

让我确认下按键子类是否依赖按下/松开事件配对，避免 modifier 过滤把 release 事件也吞掉。

逻辑已经清楚了：目前分发只看键码，modifier 完全由子类自己判断。我将在分发层统一加上 modifier 判定。

可行，已经实现。改动只在 `InputKeyManager` 的分发辅助类里（`_InputEventHandler` 仅被 `InputKeyManager` 调用）。

**改法**：[core/.../client/input/_InputEventHandler.java](core/src/main/java/dev/xcolorful/customgun/client/input/_InputEventHandler.java) 在键盘和鼠标两条分发路径上，键码匹配之后再要求 `keyMapping.getKeyModifier().isActive(keyMapping.getKeyConflictContext())` 为真，才调用子 `InputKey`。

**为什么满足三点要求**：
- **无 modifier 的按键照常通过**：Forge 的 `KeyModifier.NONE.isActive(IN_GAME)` 直接返回 `true`，CGC 现有按键全部是 `NONE + IN_GAME`，行为不变。
- **设置了 modifier 的按键**：`ALT.isActive(...)` 走 `Screen.hasAltDown()`（实时查询 GLFW 键状态，游戏中同样有效），未按住 Alt 时事件根本不会到达子 `InputKey`。
- **CGC Config Addon 兼容**：分发时 modifier 必然处于按下状态，`ConfigKey` 里那句 `getKeyModifier().isActive(...)` 依然为真，无需改动它；核心里的旧 `ConfigKey`（原先没检查 modifier）顺带被修正为必须 Alt+T。

**一个需要知道的语义边界**：modifier 检查对所有 action 生效，所以若先按 Alt+T、再松开 Alt、最后松开 T，T 的 RELEASE 不会送达。当前带 modifier 的映射只处理 PRESS（`ConfigKey`），而依赖 RELEASE 的 `AimKey`/`ShootKey` 都是 `NONE`，因此没有回归；但以后若有人给需要按住状态的键加 modifier，要留意这一点。

验证：`:core:compileJava` 编译通过（仅原有 deprecation 警告），未做游戏内运行验证。

# 

```
需要检查26.3改了scancode后该改动是否仍然生效，以及对于依赖release的键，正确使用方式是否是先松开key再松开modifier key？MC原版按键绑定的处理逻辑是怎样的？是否所有按键都支持键鼠/modifier？
```

# 

我先看 26.3 worktree 里对应实现与输入事件链。

## 1. 26.3 换成 scancode 后，补丁还能生效吗？

**判定方式仍然成立**，但 26.3 worktree 里还没有这份改动（`core/.../client/input/_InputEventHandler.java` 仍是旧版），所以要在那个分支上同步后才谈得上生效。补丁本身不需要为 scancode 改动任何逻辑，依据如下：

- 26.3 的 `InputConstants.Type` 只剩 `KEYBOARD` / `MOUSE`，`KEYBOARD` 的 value 就是 **SDL scancode**（`KEY_A=4`、`KEY_T=23`）；`InputConstants.isKeyDown(int)` 直接读 `SDL_GetKeyboardState()[scancode]`。
- 26.3 `KeyMapping.matches(KeyEvent)` = `key.getType()==KEYBOARD && key.getValue()==event.key()`，`matchesMouse` 走 MOUSE。也就是说键匹配已经由 26.3 的 `IKeyMapping.matches` 封装好，补丁里这一句不用动。
- 补丁新增的 `getKeyModifier().isActive(...)` 在 26.3 是 `NeoKeyModifier` → NeoForge `KeyModifier.isActive` → `Minecraft.hasAltDown()` → `isKeyDown(226/230)`（SDL scancode，实时读键盘状态）。与 scancode 切换无关。
- `NONE.isActive(IN_GAME)` 在 26.3 依旧是直接 `true`，普通按键（无 modifier）照常通过。
- `KeyEvent.key()` 是 scancode、`keycode()` 是虚拟键码；我们的分发只用 `matches(KeyEvent)`，不自己解析键码，正好绕开了这个差异。

## 2. 依赖 release 的键：正确的松手顺序

要分两层看：

- **我们自己的事件分发层（补丁后）**：RELEASE 也要求 modifier 仍按下，所以依赖 release 的子 `InputKey` 必须**先松 key、再松 modifier**。先松 modifier 的话，key 的 release 事件会被过滤掉。
- **原版 `KeyMapping` 层**：
    - `isDown()` = 原始按下状态 && `isConflictContextAndModifierActive()`，所以轮询式读取即使先松 modifier 也会得到 false，顺序无所谓。
    - 26.3 NeoForge 还专门补了「先松 modifier」的处理：`KeyMappingLookup.getAll(key, releasing=true)` 在释放的键本身是 modifier 时，会把**所有注册在该 modifier 下的绑定**一并 `setDown(false)`。1.20.1 Forge 没有这段，只是靠 `isDown()` 的屏蔽让症状不显现。

CGC 现有键里只有 `ConfigKey` 带 modifier 且只关心 PRESS，所以目前没有实际影响；但以后给 `ShootKey` 之类加 modifier 就会踩到。如果要让事件层和 26.3 原版对齐，可以在 `_InputEventHandler` 里补一条：RELEASE 且被释放的键等于该 mapping 的 modifier 时，也向该 mapping 派发 RELEASE（用 `ClientInputUtils.KeyType.keyboard().getOrCreate(keyEvent.key())` + `getKeyModifier().matches(...)`，两个版本都成立）。这个是否要做由你决定。

## 3. MC 原版按键绑定的处理逻辑

- **键盘**：`KeyboardHandler.keyPress` → 先交给打开中的 Screen；未被消费且无 GUI 时 `KeyMapping.set(key, action)`，PRESS 再 `KeyMapping.click(key)`。REPEAT 也走 press 分支。
- **筛选**：`KeyMapping.set/click` → `KeyMappingLookup.getAll(key[, releasing])` → 每条绑定用 `isActiveAndMatches(key)` 过滤：键相等 && conflict context 激活 && **modifier 激活**。modifier 的判定不在 `matches` 里，而在查找阶段。
- **状态**：`isDown()` 再与一次 context+modifier；`consumeClick()` 消费点击计数；失焦 `releaseAll()`，重获焦点 `setAll()`。
- **鼠标**：`MouseHandler.onButton` → `InputEvent.MouseButton.Pre` → Screen → 无 screen/overlay 时 `KeyMapping.set/click(Type.MOUSE.getOrCreate(button))`。
- 26.3 的 modifier 释放清理见上一节。

我们改的 `InputKeyManager` 是绕过 `KeyMappingLookup` 自己分发，所以需要把原版查找阶段的 modifier 判定补回来——这也正是这次改动的本质。

## 4. 键鼠/modifier 的支持范围

- **一个绑定只能是键盘或鼠标之一**：26.3 `InputConstants.Type` 只有 KEYBOARD/MOUSE（1.20.1 的 SCANCODE 类型已移除），`matches(KeyEvent)` 只匹配键盘，`matchesMouse` 只匹配鼠标。CGC 的 `InputKey` 两种都能传。
- **modifier 与输入类型正交**：键盘绑定和鼠标绑定都可以带 SHIFT/CONTROL/ALT。NeoForge 26.3 另有 `CONTROL_OR_COMMAND`（macOS 用 Command），CGC 的 `IKeyModifier.Type` 没有暴露它。
- **约束**：如果绑定的键本身就是该 modifier，`KeyMapping` 构造函数会把 modifier 自动重置为 NONE（1.20.1 与 26.3 都有 `if (keyModifier.matches(keyCode)) keyModifier = NONE`），所以不能绑定「Alt + Alt」这类组合。
- `KeyMapping.setAll()` 的焦点恢复只处理 KEYBOARD 类型。
- 补丁对键盘、鼠标两条分发路径都加了 modifier 判定，所以鼠标绑定带 modifier 也能正确过滤。

需要我把 `_InputEventHandler` 的这处改动同步到 26.3 worktree 吗？另外如果需要，我可以顺手把「先松 modifier 也派发 RELEASE」的兼容逻辑加上。

# 

```
那在_InputEventHandler补一条来跨版本统一
```

# 

我先确认下 `InputAction` 和 26.3 的事件类型签名，保证这条逻辑两边都能直接编译。

已在 `_InputEventHandler` 补上「先松 modifier 也派发 RELEASE」。`:core:compileJava` 通过。

**规则**（[core/.../client/input/_InputEventHandler.java](core/src/main/java/dev/xcolorful/customgun/client/input/_InputEventHandler.java)）：

- 键盘事件先按键码匹配；匹配后仍要求 modifier 处于按下状态才分发。
- 若键码不匹配、但当前是 RELEASE 且**被释放的键就是该 mapping 的 modifier**，也向它分发一次 RELEASE。
- 鼠标路径不变（鼠标键不可能是 modifier），但带 modifier 的鼠标绑定同样会被上面这条键盘扇出覆盖——与原版「释放 modifier 时把该 modifier 下所有绑定一起置为未按下」一致。

**跨版本成立的点**：

- `InputAction.of(event.getAction())` 屏蔽了 1.20.1(0/1/2) 与 26.3(0/1/-1) 的 action 常量差异。
- `ClientInputUtils.KeyType.keyboard().getOrCreate(keyEvent.key())` 在 1.20.1 得到 `KEYSYM`（GLFW 键码 342/346），在 26.3 得到 `KEYBOARD`（SDL scancode 226/230），恰好都能被对应版本的 `KeyModifier.matches` 识别；两边的 `getKeyEvent()` 返回值虽然类型不同，但都有 `key()`。

**行为矩阵**：

| 操作顺序 | 结果 |
|---|---|
| 按住 modifier → 按/放主键 | 正常收到 PRESS / RELEASE |
| 先放主键，再放 modifier | RELEASE 收两次（主键释放一次 + modifier 释放扇出一次），与原版重复 `setDown(false)` 等价 |
| 先放 modifier，再放主键 | modifier 释放时扇出 RELEASE；主键释放被过滤，不会重复 |
| 只点按 modifier（主键从未按下） | 会收到一次 RELEASE |

后两种情况下，子 `InputKey` 的 RELEASE 处理需要是幂等的；CGC 现有的 `AimKey`/`ShootKey` 都满足，带 modifier 的 `ConfigKey` 只处理 PRESS。

26.3 worktree 我没动（按分支规则）。这段代码在那边可以直接原样编译，需要的话我把整个 `_InputEventHandler` 同步过去。

# 

```
ClientInputUtils.KeyType.keyboard().getOrCreate(keyEvent.key()是否可以简化?
```

# 

不能真正简化，这已经是最小且跨版本成立的形式。理由：

1. **没有可用的公共 `InputConstants.getKey(...)` 重载**
    - 1.20.1：`InputConstants.getKey(int keyCode, int scanCode)` 存在；
    - 26.3：只剩 `getKey(KeyEvent)` 和 `getKey(String)`，没有 `(int, int)` 版本。
    - 而且 `event.getKeyEvent()` 在两个分支返回的类型不同（1.20.1 是 core 的 `IInputKeyEvent.KeyEvent` record，26.3 是 `net.minecraft.client.input.KeyEvent`），所以不能用 `InputConstants.getKey(event.getKeyEvent())` 统一调用。

2. **`ClientInputUtils.KeyType.keyboard()` 正是为这个差异存在的**
    - 1.20.1 返回 `Type.KEYSYM`，26.3 返回 `Type.KEYBOARD`，再配合 `getOrCreate(keyEvent.key())`，就能在两边都得到 `KeyModifier.matches` 期望的 `InputConstants.Key`。`IKeyModifier.matches` 只接受 `InputConstants.Key`，这个对象绕不过去。

3. **改用事件里的 modifier 位掩码会更糟**
    - 1.20.1 的事件 modifiers 是 GLFW 位掩码（ALT=4），且常量只定义了 `MOD_CONTROL`；
    - 26.3 是 SDL 位掩码（ALT=768，且 SHIFT/CONTROL/ALT 都有）。两套值完全不同，需要再引入一层版本常量，反而更不统一。

唯一可选的小调整是把表达式提到循环外（每次事件构造一次而不是每个 subManager 一次），但 `getOrCreate` 背后有缓存、返回同一实例，开销可忽略；现在写在 `else if` 里靠短路只在 RELEASE 时求值，也是合理的。为这一个调用点再包一层 `keyboardKey(int)` 辅助方法则不划算。

另外确认：当前磁盘上的版本 IDE 检查无错误。

# 

```
目前这个设计，是否方便mod随时热替换按键的实现，避免冲突？如果改用原版KeyMappingLookup的逻辑是否按键就绑定CGC本体类的写法而不方便改？
```

# 

我先确认一下 `InputKeyManager` 的获取方式和注册路径，再回答可替换性。

**结论先说**：现在这套设计的**分发层**是可热替换的，但「替换某个内置按键」受启动期注册和静态单例两处限制；改成原版 `KeyMappingLookup` 不会更好改，反而会把行为绑定到具体的 `KeyMapping` 实例（也就是 CGC 本体构造的那个对象）上。

## 现在设计的可替换性

**能热替换的部分：**

- 整个管理器可以换：`CustomGunClient.setInputKeyManager(...)`（已标 `@Deprecated`，但机制完整：先注销旧管理器的平台监听，再注册新的）。
- 单个按键可以随时增删：`registerSubManager` / `unregisterSubManager` 操作的是运行时活列表，分发只依赖 `IInputKeySubManager` 接口，不要求继承 `InputKey`。一个 addon 完全可以自己实现 `IInputKeySubManager` 注册进来。

**影响「替换内置键」的摩擦点：**

1. **自注册 + 静态单例 + 启动期实例化**：`InputKey` 构造函数里直接 `_registerToManager()`（[InputKey.java:26](core/src/main/java/dev/xcolorful/customgun/client/input/InputKey.java:26)），而内置键都是 holder 单例，由 [ClientKeyMappingRegistry.java:22](core/src/main/java/dev/xcolorful/customgun/client/init/ClientKeyMappingRegistry.java:22) 在 `RegisterKeyMappingsEvent` 里首次 `get()` 触发构造。addon 如果在 CGC 初始化**之前**注销，之后这次构造会重新注册回来；必须在初始化之后执行替换。
2. **原版按键项是硬编码的**：`ClientKeyMappingRegistry.registerKeyMappings` 写死了 10 个 `KeyMapping`，且注册事件只触发一次。addon 自己的 `KeyMapping` 不会出现在原版控制页里（除非同样在 init 期注册），CGC 原有的条目也删不掉。替换行为容易，替换「控制页条目」很难。

**冲突避免方面：**

- 分发是「所有匹配的 subManager 都调用」，没有 override、优先级或 consume 语义——[_InputEventHandler](core/src/main/java/dev/xcolorful/customgun/client/input/_InputEventHandler.java) 的循环不会因为某个 handler 取消事件而提前结束，`ArraySet` 也只按 `equals` 去重（`InputKey` 没重写 equals，即按实例）。
- 所以同一物理键注册两个 handler 会**双触发**，要靠 addon 主动先注销原实现，属于协作式约定，框架不强制。
- 原版的冲突检测（`KeyMapping.same()` / 控制页红线）只对注册进 `Options` 的 `KeyMapping` 生效，和运行时分发是两套东西。

## 改用原版 KeyMappingLookup 会怎样

- 索引单位会变成 **`KeyMapping` 实例**：`KeyMappingLookup` 是 `InputConstants.Key → KeyMapping` 的静态全局表，分发只能拿到实例，再由实例反查 handler。这不一定是「绑定 CGC 类」，但确实绑定到 **CGC 构造出来的那个对象**；替换行为要么改这个对象的 `setKey`/`setKeyModifierAndCode`，要么另建一张 `KeyMapping → handler` 的表——比现在的接口列表更绕。
- **丢失 action 流**：原版查询只暴露 `isDown()` / `consumeClick()`，没有 PRESS/RELEASE/REPEAT 事件。`AimKey`、`ShootKey` 依赖 RELEASE（瞄准释放、扳机释放判定），改用轮询需要重做逻辑。
- **跨版本行为又分叉**：1.20.1 的 `KeyMappingLookup.getAll` 没有 `releasing` 分支，26.3 才加了「先松 modifier 释放该 modifier 下所有绑定」——我们刚在分发层统一掉的东西，用原版 lookup 就得按版本各写一套。
- **冲突同样不强制**：`getAll` 返回所有匹配绑定，没有覆盖语义。
- 表的填充仍发生在构造期（`KeyMapping` 构造函数 + `resetMapping`），并没有比现在更「随时」。

所以：现在这层的接口化 subManager 列表恰恰是更方便替换的部分；真正阻碍热替换的是 `KeyMapping` 的启动期注册和控制页硬编码，换不换 lookup 都一样。如果想把替换做扎实，值得动的是另外三处（需要的话我可以做）：按 `CustomInputKey` 做 replace 语义、给分发加 consume/优先级、把 `ClientKeyMappingRegistry` 变成可扩展注册表。
