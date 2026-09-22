# 

```
现在要排查bug

经测试：
- 1.20.1-1.21.6生存模式装弹能正常扣背包子弹
- 测试枪械为cbrg:beryl_m762，子弹为cbrg:ammo_762，非背包直读，非虚拟备弹
- 1.21.10到26.3生存模式装弹都不会扣子弹（bug）；背包没有子弹时不能装弹（正常）
- 不同版本的枪械HUD对背包子弹数量的显示都正常

- 当前IDEA打开了多个窗口供浏览不同版本源码
- 需要排查是否有原版机制变化，以及是否有露merge/露cherry pick的问题
- 任一方式找到原因并修复即可，默认这个问题是1.21.10开始引入的，不需要逐个检查高版本
- 如果是1.21.10的问题，改动优先写在1.21.10，我视情况决定是否back port

修复完成后我视情况决定统一cherry pick
```

# 

I'll start by understanding the reload/ammo consumption flow across versions. Let me check memory first, then explore.

Let me explore the reload flow. First, the docs and the ammo consumption code.

Ammo consumption will be inside `tickReload`. Let me find the IGun implementation.

Found a strong lead: `getItemReadOnly` returns a copy (`resource.toStack(amount)`), not a live reference. Let me compare with the working 1.21.6 version.

Confirmed: 1.21.6's `getStackInSlot` returns the live stack; 1.21.10's transfer API returns a copy. Let me check the interface contract.

The unload path (line 100-110) uses extract→insert; the reload path (line 195+) mutates a live reference. Let me check git history to see when that live-reference approach appeared.

There's a prior "Fix ammo comsuption issue" commit — let me inspect it.

Let me see which commit replaced the extract/insert approach with live mutation, and why.

Let me read the relevant memory about where cross-version drift should be fixed.

Now let me verify the 1.21.10 NeoForge transfer API semantics for slot-targeted insert/extract, since my fix depends on them.

Key question for the minimal fix: does NeoForge 21.10 still expose the legacy `IItemHandler` API?

`IItemHandler` survives (deprecated) with an `IItemHandler.of(...)` adapter. Let me read that adapter to see what mutation it supports.

Let me confirm definitively that the legacy adapter also can't provide live stacks (ruling out a compat-only fix).

The adapter also returns a copy — so a compat-only fix is impossible. Let me check whether `IAmmo` is per-item (stateless) or bound to a stack.

`IAmmo` is the item singleton (stateless), so it's safe to reuse. Let me check the call context to be sure the feed runs once, not per tick.

Now let me verify the IAmmoBox path and `NBTUtils.getCustomData` null-safety before editing.

Root cause confirmed. Now let me apply the fix in core.

Now let me verify it compiles.

IDEA build timed out. Let me compile via Gradle instead.

While that compiles, let me confirm no other call site mutates a `getItemReadOnly` result.

Compile is still running. Let me wait for it.

Compile passed. Let me confirm the class was regenerated and check the diff.

Compile verified (class regenerated 21:35). Let me record the cross-version constraint as a memory.

根因已定位并修复，不是 merge/cherry-pick 漏了东西。

## 根因

`core` 的装弹消耗依赖了 `IInventoryCapability#getItemReadOnly` 返回**槽位活引用**这一从未写进契约的行为 —— 该方法的注释就写着"只读 getter"。

两个各自看着没问题的改动撞在一起：

1. **1.21.10 移植**把 `NeoInventoryCapability` 换成了 NeoForge 新的 transfer API（`ResourceHandler<ItemResource>`），[NeoInventoryCapability.java:43](neoforge-compat/src/main/java/dev/xcolorful/customgun/neoforge/minecraft/capability/NeoInventoryCapability.java:43) 只能 `resource.toStack(amount)` 造一个新栈，改它不可能写回背包。
2. **两前天（2026-09-20）的 `6899c2390 "Unify ammo consumption"`** 把 core 的历史实现（extract → 改 → insert 回）简化成了 [_DefaultGunInventory.java:233](core/src/main/java/dev/xcolorful/customgun/core/gun/inventory/_DefaultGunInventory.java:233) 的 `iAmmo.consumeAmmo(slotItemReadOnly, n)`，直接改那个栈。

在 1.20.1–1.21.6，旧的 `IItemHandler.getStackInSlot` → `Container.getItem` 确实返回活引用，所以简化是"能用"的；1.21.9 起 NeoForge 删掉了 `IItemHandler`（连 `Capabilities.ItemHandler` 都没了），活引用语义一并消失。核心 commit 是 9/20，1.21.10 的兼容层改写在 8 月，所以是**旧 core 假设遇上新平台**，不是漏 merge。

这正好解释全部现象：`consumeAmmo` 在副本上算出 `consumed > 0`，枪照常补满，背包却一点没扣（白送弹药）；只读调用点 `hasInventoryAmmo` / `getInventoryAmmoCount` 不受影响，所以 HUD 数量和"没子弹不能装弹"都正常。

## 修复

只改 [_DefaultGunInventory.java](core/src/main/java/dev/xcolorful/customgun/core/gun/inventory/_DefaultGunInventory.java)，把写回改成用接口已有的、真实存在的操作（同文件 `retrieveAmmoToInventory` 退弹就是这个模式）：

```java
ItemStack ammoItem = inventoryCapability.extractItem(i, slotItemReadOnly.getCount(), false);
int consumedAmmo = iAmmo.consumeAmmo(ammoItem, requiredAmmoCount);
if (!ammoItem.isEmpty()) { // 没抽成 EMPTY 就放回去，适用于 IAmmoBox
    ItemStack remain = inventoryCapability.insertItem(i, ammoItem, false);
    ...
}
```

保留了你 `6899c2390` 的 `consumeAmmo` 统一（无限供弹、IAmmoBox 的组件计数语义都没变），只换了持久化方式；顺手删掉那段 `// @Deprecated` 的注释死代码（它正是被替代掉的旧实现，且注释里"活引用"的前提已不成立）。

**兼容层没有单点修法**：`IItemHandler.of(ResourceHandler)` 适配器的 `getStackInSlot` 走的也是 `ItemUtil.getStack` 造副本，所以只能落到 core。好消息是这段代码在 1.20.1–1.21.6 上行为完全不变，cherry-pick 到 core1.20.1 后不需要给低版本补任何东西。

## 验证状态

- `./gradlew :core:compileJava` 通过（exit 0，class 已重新生成）。
- **未做游戏内实测** —— 我没法启动客户端跑装弹。请你在 1.21.10 生存下确认：弹匣 24/30 装弹应扣 6 发、枪补到 30；空仓装弹应扣满一个弹匣量。

改动未提交（1 file，+10/−35），diff 已开在旁边；要我提交的话说一声。26.3 那份是同一段代码，check 完 1.21.10 后可以按你的节奏一起 cherry-pick。

# 

```
1.21.10的getter是否方便改回同一引用而不是复制？如果不行，在core1.20.1的窗口统一改成readonly检测后抽出来，用IAmmo的api扣子弹，然后不检查isEmpty直接强制塞回
```

# 

Good questions — let me check both before touching anything.

Both questions answered — let me apply the unified change in core1.20.1.

Mirroring the identical content into the 1.21.10 worktree so it stays testable and merges cleanly.

Both copies now differ only by a pre-existing line. Waiting on the core1.20.1 compile.

core1.20.1 compiles. Now re-verifying the 1.21.10 copy.

Both compile. Let me update the memory with the definitive findings.

## Q1：1.21.10 的 getter 改不回活引用

不行，这不是实现偷懒，是新 API 里根本不存在可别名的对象：

- `ResourceHandler<ItemResource>` 的槽位只有两样东西：不可变的 `ItemResource`（item + 组件补丁）和一个 `int` 数量。`getResource(int)` / `getAmountAsInt(int)` 都问不出 ItemStack 来，**没有"同一个 ItemStack"可返回**。
- 具体实现也救不了：`StacksResourceHandler` 构造时 `this.stacks = mutableCopyOf(stacks)`，留的是它自己的 `NonNullList` 副本，不是玩家的 `Inventory`；所以就算在 compat 层 `instanceof` 强转成 `ItemStacksResourceHandler` 拿到的也是 NeoForge 内部副本，不是真槽位。
- 而且那样做是**有害**的：`StacksResourceHandler` 继承 `SnapshotJournal`，每个 index 一个 `StackJournal` 做事务回滚；绕过 `insert/extract/set` 直接改栈会让日志和实际内容脱节。这正是 07c587bb6 那个"活引用被原地清空"陷阱的 1.21.9 版本，只是这次坏在事务记账上。
- 后路也堵死了：`Capabilities.ItemHandler` 已删除；`IItemHandler.of(ResourceHandler)` 适配器的 `getStackInSlot` 走 `ItemUtil.getStack`，一样是副本，而且 `IItemHandler` 自己的 javadoc 写着返回栈 "MUST NOT be modified"。
- 另外这个 capability 要服务任意 `LivingEntity`（不只是玩家），绕开它去摸 `PlayerInventory` 只能覆盖玩家，还得自己保证 index 对齐。

真要一个写回原语，NeoForge 有 `StacksResourceHandler#set(index, resource, amount)`（就是给 `ResourceHandlerSlot` 用的 `IndexModifier`），但它在具体类上、不在 `ResourceHandler` 接口上，core 够不着。

所以按你说的走第二种：**统一改在 core1.20.1**，只读检测 → 抽出来 → 用 IAmmo 扣 → 强制塞回。

## 改动

[_DefaultGunInventory.java](core/src/main/java/dev/xcolorful/customgun/core/gun/inventory/_DefaultGunInventory.java) 的核心三行：

```java
ItemStack ammoItem = inventoryCapability.extractItem(i, slotItemReadOnly.getCount(), false);
int consumedAmmo = iAmmo.consumeAmmo(ammoItem, requiredAmmoCount);
ItemStack remain = inventoryCapability.insertItem(i, ammoItem, false);
```

关于"不检查 isEmpty 直接塞回"——**验证过是安全的**，我照做了：Forge 1.20.1 `InvWrapper.insertItem` 开头就是 `if (stack.isEmpty()) return ItemStack.EMPTY;`，本项目 1.21.10 的 `NeoInventoryCapability.insertItem` 同样先判空。所以整堆扣光（栈变 EMPTY）时塞回是 no-op、槽位自然留空，`extractItem` 万一失败也不会把槽位抹掉。

**只保留了一处非输入侧检查**：`insertItem` 的*返回值* remain 若不为空就 warn（原来的警告文案）。这是防"塞不回去 → 子弹凭空消失"的唯一提示，属于项目 Rule 10 fail loud；如果你觉得多余，删掉那一行就行。

两个副本现在**逐字节相同**（`diff` 只剩 line 171 那处既有差异）：

- `D:\Github\Custom-Gun-Continued`（core1.20.1，正本）
- `D:\Github\Custom-Gun-Continued-1.21.10neoforge`（镜像一份，这样你现在就能直接进游戏验，将来 merge/cherry-pick 是干净的）

两处都编译通过：`:core:compileJava` exit 0，class 已重新生成。

## 顺带一个发现（与"露 merge"有关）

同一个文件 line 171，两个分支的 core 源码其实不一样：

```java
// 1.21.10:  itemEntity.setThrower(livingShooter);
// core1.20.1: itemEntity.setThrower(livingShooter.getUUID());
```

这是 1.21.9 改签名留下的平台差异，直接写在 core 里 —— 也就是说 `core1.20.1 → 1.21.10` 每次 merge 都会在这一行上撞一次。不是我这次任务的范围，我没动它；要不要我单独查一下它是已知例外还是漏处理的，你说。

## 状态

- 未提交。要我提交（core1.20.1 一个 commit）就说一声。
- **仍未做游戏内实测** —— 请你在 1.21.10 生存验：24/30 装弹扣 6 发、枪补到 30；空仓装弹扣满一个弹匣；弹药盒那套也顺便过一下（退弹到 0 发的盒子要能留在包里）。
