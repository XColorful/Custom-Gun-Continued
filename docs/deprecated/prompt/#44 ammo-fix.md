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
