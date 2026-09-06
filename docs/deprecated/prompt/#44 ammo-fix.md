> #44 提示词

# 

```
目前测试生存模式枪械装弹，例如弹匣为30/30的BerylM762，枪内弹匣剩余20多发的时候，装弹会直接将背包一个堆叠30的762子弹直接清空，并且装弹完成后没有补充枪内子弹；创造模式正常装弹且不消耗子弹

辅助快速浏览：
- 从背包抽子弹的逻辑在_DefaultGunInventory#findAndExtractInventoryAmmo

检查并修复该问题
```

# 

I'll investigate this reload/ammo consumption bug. Let me start by locating the relevant code.

I've found the root cause. Let me verify my understanding of the Forge inventory semantics is correct before fixing.

The bug is in `findAndExtractInventoryAmmo`. The key sequence:

1. `slotItemReadOnly = getItemReadOnly(i)` returns the **live** stack reference (via `InvWrapper.getStackInSlot` → `getInv().getItem(slot)`).
2. `extractItem(i, slotItemReadOnly.getCount(), false)` → `Container.removeItem` → `ItemStack.split(amount)`, which **shrinks the original stack in place** (the same object `slotItemReadOnly` points to) and returns a _copy_.
3. Then `existAmmoCount = iAmmo.getAmmoCount(slotItemReadOnly)` reads from the **already-emptied** live reference, so it returns `0` instead of `30`.

With `existAmmoCount = 0`, the code takes the "consume-all" branch: it sets `modifiedItem` (the actual 30-round copy) to `0` and returns `0` extracted — emptying the stack while reporting no consumption, so the gun never gets refilled. This exactly matches the symptom.

The fix is to read the ammo count from `modifiedItem` (the extracted copy) instead of the mutated `slotItemReadOnly`.

The fix compiles cleanly. Let me verify by compiling just this file to be certain.

已定位并修复该问题。

## 根因

问题出在 [_DefaultGunInventory.java:214](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/gun/inventory/_DefaultGunInventory.java:214) 的 `findAndExtractInventoryAmmo`。

`getItemReadOnly(i)` 返回的是背包槽位的**活引用**（`InvWrapper.getStackInSlot` → `Container.getItem`），而不是副本。紧接着的 `extractItem(i, count, false)` 最终走到 `Container.removeItem` → `ItemStack.split`，它会把槽位里的那个原对象 **原地 `shrink` 清空**，再返回一个 `copy`。

原代码在 `extractItem` 之后才读数量：

```java
int existAmmoCount = iAmmo.getAmmoCount(slotItemReadOnly); // 此时已被 shrink 成 0
```

于是 `existAmmoCount` 恒为 `0`，导致：

1. 走进「全部扣除」分支，对抽出的 `modifiedItem`（真正的 30 发）执行 `setAmmoCount(..., 0)` → **30 发堆叠被清空**；
2. 返回值 `extracted` 始终是 `0` → `_defaultReloadFeed` 里 `consumedAmmo` 为 0 → **枪内子弹没有被补充**。

创造模式走的是 `needConsumeAmmo == false` 分支，直接跳过 `consumeAmmoFromPlayer`，所以表现正常——这与现象完全吻合。

## 修复

改成从抽出来的 `modifiedItem`（副本）读取数量：

```java
// slotItemReadOnly 是槽位的活引用，extractItem 已将其 shrink 清空，须从抽出的 modifiedItem 读取数量
int existAmmoCount = iAmmo.getAmmoCount(modifiedItem);
```

这样对普通子弹（`getAmmoCount` = `getCount()`）和弹药盒（`getAmmoCount` 读 NBT）都正确：装弹时只扣 `needAmmoCount`（30 − 剩余），剩余部分正确塞回背包，枪内补满。

## 验证

- IDEA 文件检查无错误，`build_project` 单文件编译通过。
- 逻辑复算：24/30 装弹 → `needAmmoCount=6`，抽 6、塞回 24，枪补到 30；空仓装弹 → 扣满 30、枪补到 30，均正确。

同文件里的 `retrieveAmmoToInventory`（退弹）是在 `extractItem` **之前**读取 `existAmmoCount` 的，不受此问题影响，未改动。

改动仅此一行，未提交；如需我提交请告知。

# 
---

```
模组移植到1.21.1forge，现在要处理IForgeItem没有了int getMaxStackSize(ItemStack ammoItem)，并且Item#getDefaultMaxStackSize又拿不到ItemStack的tag，是否只能mixin注入ItemStack#getMaxStackSize然后特判？
对于neoforge1.21.1及最新版neoforge都保留了neoforge的IItemExtension，所以只需要处理1.21.1forge即可。

当前IDEA打开了1.21.1forge, 1.21.1neoforge和1.20.4的窗口供浏览源码
```

# 

I'll investigate how max stack size for ammo is currently handled across the three versions, then assess the options for 1.21.1forge. Let me start by locating the relevant code.

The 1.21.1forge `forge-compat` module still has the old-style `@Override getMaxStackSize(ItemStack)` mixin that depends on `IForgeItem`. Let me verify exactly what Forge 1.21.1's actual API surface is for per-stack max size, by inspecting the platform source through IDEA.

Big finding: in 1.21.1, `maxStackSize` became a **DataComponent** (`DataComponents.MAX_STACK_SIZE`) rather than an overridable method. Let me confirm the full Forge 1.21.1 API surface for `IForgeItem` and `IForgeItemStack` to see whether any per-stack hook survived.

Confirmed: `IForgeItem` (1.21.1 Forge) has no `getMaxStackSize(ItemStack)`, and `IForgeItemStack` has no `getMaxStackSize()` either. The key 1.21.1 change is that `ItemStack#getMaxStackSize()` now reads `DataComponents.MAX_STACK_SIZE` (a data component), not a method on `Item`. Let me look at how the ammo item is constructed and how ammo location is stored, plus how neoforge's `IItemExtension` still exposes the hook, to give you a grounded recommendation.

Let me confirm two remaining things: what default components `AmmoItem` is registered with (its `CUSTOM_ITEM_PROPERTY`), and exactly how neoforge 1.21.1's `IItemExtension#getMaxStackSize(ItemStack)` is implemented (as the contrast point).

根因查清了。结论：**不是"只能"——还有一个不写 mixin 的办法，但 mixin 注入 `ItemStack#getMaxStackSize()` 才是正确且几乎唯一干净的做法**，而且它本质上是把 Forge 删掉的那层钩子补回来。

## 根因：1.21.1 把 max stack size 从「Item 方法」改成了「数据组件」

对比 1.21.1 三个加载器的源码，差异很明确：

**Forge 1.21.1**（[ItemStack.java](app://localhost/epitaxy/forge-compat/build/forge-1.21.1-52.1.10-sources.jar!/net/minecraft/world/item/ItemStack.java) 反编译）：

```java
public int getMaxStackSize() {
    return this.getOrDefault(DataComponents.MAX_STACK_SIZE, Integer.valueOf(1)); // 直接读组件
}
```

`IForgeItem` 和 `IForgeItemStack` 里**都没有** `getMaxStackSize`，`Item#getDefaultMaxStackSize()` 是无参方法只能拿到 item 原型组件，拿不到 ItemStack 的 tag。所以 Forge 这边「per-stack 动态取值」这层钩子确实被砍掉了。

**NeoForge 1.21.1**（`IItemExtension` / `ItemStack` 反编译）：

```java
// ItemStack
public int getMaxStackSize() { return this.getItem().getMaxStackSize(this); }
// IItemExtension
default int getMaxStackSize(ItemStack stack) { return stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 1); }
```

NeoForge 保留了 `ItemStack → Item#getMaxStackSize(ItemStack)` 这一层间接调用，所以 neoforge 的 `AmmoItemMixin` 不用动。**你的判断完全正确。**

## 方案对比

**方案 A（推荐）：mixin 注入 `ItemStack#getMaxStackSize()` 特判**。这是对 Forge 删除的钩子的直接等价替换，而且延续了你们既有设计意图——[ModItems.java:37-38](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/init/registry/ModItems.java) 里 `.stacksTo(1)` 后面就写着 `// 堆叠数会重载getter来修改`，即「惰性、读时计算」的 pull 模型。所有 `Slot`/`Container` 的 `getMaxStackSize(ItemStack)` 最终都收敛到 `stack.getMaxStackSize()`，这一个注入点就全覆盖，且无论 tag 何时被改都永远正确。

```java
@Mixin(ItemStack.class)
public abstract class AmmoItemStackMixin {
    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void cgc$ammoMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        IAmmo iAmmo = IAmmoGetter.fromItemStack(self);
        if (iAmmo != null) cir.setReturnValue(iAmmo.getAmmoMaxStackSize(self));
    }
}
```

非 ammo 物品只多一次 `instanceof`，开销可忽略。

**方案 B（不推荐）：在 `setAmmoLocation` 等处 eager 写入 `DataComponents.MAX_STACK_SIZE` 组件**。这是唯一的非 mixin 办法，但它是「写时同步」的 push 模型，和你现有的「getter 惰性取值」设计相反：派生状态（maxStackSize）被重复存储，必须在所有修改 ammo location 的地方同步维护；从 NBT/箱子读出来的旧 stack、资源包热重载改了 `AmmoIndex.maxStackSize` 后，组件都会过期。属于自己埋雷。

## 两个必须你拍板的落点

1. **旧的 `getMaxStackSize(ItemStack)` override 现在就是编译错误**（`Item` 上已无此方法）。需要从 [AmmoItemMixin.java:30-36](app://localhost/epitaxy/forge-compat/src/main/java/dev/xcolorful/customgun/forgeclient/mixin/item/AmmoItemMixin.java) 里删掉这段，只保留 BEWLR 部分（`initializeClient`/`cgc$getBEWLR`），再新增上面那个 `@Mixin(ItemStack.class)`。
    
2. **注册在 client 还是 common，是关键决策点。** 现有 `AmmoItemMixin` 注册在 [customgunforgeclient.mixins.json](app://localhost/epitaxy/forge-compat/src/main/resources/customgunforgeclient.mixins.json) 的 `"client"` 里（仅客户端），而 1.20.4 也一直是 client-only。但 `getMaxStackSize()` 会参与**服务端**的背包合并/槽位上限判定（shift-click、拾取堆叠等），仅客户端注入意味着服务端仍按 `stacksTo(1)` 处理。如果子弹基本只待在枪械自己的容器里、不进 vanilla 背包，client-only 可能是有意的；但更稳妥是把它注册进 [customgunforge.mixins.json](app://localhost/epitaxy/forge-compat/src/main/resources/customgunforge.mixins.json) 的 `"mixins"`（common，两端生效）。这一条请你确认是沿用 1.20.4 的有意行为，还是顺带修掉这个潜在 bug。
    

要我直接按方案 A 落地（删掉死代码 + 新增 common 的 ItemStack mixin 并注册）吗？还是你希望我先只改 client 端保持与 1.20.4 一致？

# 

```
添加到forge注入，mixin类叫ItemStackMixin，forgeclient里的mixin类我已经改好了
```

# 

I'll create the common-side mixin and register it. Let me write `ItemStackMixin` under the common `forge.mixin` package (following the `entity.GunProjectileMixin` subpackage convention), then register it in `customgunforge.mixins.json`.

Let me verify the new mixin compiles cleanly via IDEA's inspection.

The build succeeded, but the warnings reveal something important I need to reconcile: `AmmoItemMixin.java:34` still shows `public int getMaxStackSize(ItemStack ammoItem)` compiling with only a `[dep-ann]` warning — not an error. That means Forge 1.21.1's `Item` may still carry a _deprecated_ `getMaxStackSize(ItemStack)`. Let me verify directly.

`Item.java` has no `getMaxStackSize(ItemStack)` around `getDefaultMaxStackSize()`. But the `[dep-ann]` warning proves `AmmoItemMixin.getMaxStackSize(ItemStack)` is overriding _something_ deprecated that still exists. Let me scan the whole `Item.java` for it.

I've been relying on a stale read of `AmmoItemMixin`. The `[dep-ann]` warning (not error) means `getMaxStackSize(ItemStack)` still compiles — which contradicts my conclusion. Let me re-read the user's edited file and definitively grep the Forge source jar for any `getMaxStackSize(ItemStack)`.

完成了。`ItemStackMixin` 已创建并注册到 common 注入，构建通过。

## 我做的两处改动

1. 新建 [ItemStackMixin.java](app://localhost/epitaxy/forge-compat/src/main/java/dev/xcolorful/customgun/forge/mixin/item/ItemStackMixin.java)（`dev.xcolorful.customgun.forge.mixin.item` 包，对齐 `entity.GunProjectileMixin` 的子包惯例），`@Inject` 到 `ItemStack#getMaxStackSize()` 的 `HEAD`、`cancellable`，命中 `IAmmo` 时用 `getAmmoMaxStackSize` 覆盖返回值，非 ammo 物品直接走原版组件读取。
    
2. 在 [customgunforge.mixins.json](app://localhost/epitaxy/forge-compat/src/main/resources/customgunforge.mixins.json) 的 `"mixins"`（common，客户端+服务端都生效）里加了 `"item.ItemStackMixin"`。
    

`build_project` 返回 `isSuccess: true`，新 mixin 无任何 error，只有项目里既有的 `[removal]`/`[dep-ann]` 警告。

## 需要你知道的一点（你 forgeclient 的改动）

你改过的 [AmmoItemMixin.java:28-39](app://localhost/epitaxy/forge-compat/src/main/java/dev/xcolorful/customgun/forgeclient/mixin/item/AmmoItemMixin.java) 把 `@Override` 注释掉了、把 `super.getMaxStackSize(ammoItem)` 改成了 `return 1`。功能上没问题——因为 `@Override` 已注释，这个方法不再覆盖任何东西，是纯死代码，`ItemStack#getMaxStackSize()` 永远不会调到它。但有两处小瑕疵：

- Javadoc 里写了 `@deprecated` 却没有配 `@Deprecated` 注解，所以编译冒出一条 `[dep-ann] deprecated item is not annotated with @Deprecated` 警告（就是构建输出里那条）。
- 这是死代码，还留着 `ItemStack`/`IAmmo`/`IAmmoGetter`/`Nullable` 四个 import 只为它服务，将来有人误把 `@Override` 取消注释会直接编译报错（`Item` 上已无此方法）。

如果你只是拿它当「指向新位置的注释」，建议要么加个 `@Deprecated` 注解消掉警告，要么直接整段删掉更干净。要我把这段死方法删掉吗？还是保持现状？
