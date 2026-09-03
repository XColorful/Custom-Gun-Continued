# 

```
目前遇到以下问题：
- 在连接到多人游戏时，服务端会因为以下问题而断开与客户端的连接：
日志内容为：
[03Sep2026 17:50:21.346] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: XiaoColorful joined the game
[03Sep2026 17:50:21.382] [Server thread/INFO] [net.minecraft.server.network.ServerGamePacketListenerImpl/]: XiaoColorful lost connection: Internal Exception: io.netty.handler.codec.EncoderException: java.lang.NullPointerException: Cannot invoke "net.minecraft.world.item.ItemStack.m_41619_()" because "p_130056_" is null
[03Sep2026 17:50:21.383] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: XiaoColorful left the game

经测试，1.20.1forge、1.20.2forge/neoforge稳定触发，1.20.4forge/neoforge、1.21.1neoforge、26.2neoforge均不触发

经查询：
- ItemStack.m_41619_()为ItemStack.isEmpty()
- 没在ServerGamePacketListenerImpl里查到p_130056_的位置，估计是在哪个网络包的解析里出问题的

目前初步怀疑是1.20.1在dev.xcolorful.customgun.core.util.NetworkUtils#writeItem引入的参数问题：
- 修改成buffer.writeItemStack(item, true);后，测试仍然稳定触发bug
- 但是1.20.4仍然使用该接口却没问题
- 因此排除该位置

目前打开了4个IDEA窗口：1.20.1、1.20.2、1.20.4、26.2，供对照浏览不同版本源码的实现差异

请排查并修复该问题，修改完后让我测试检查，不要commit
```

# 

I'll investigate this server-side NullPointerException. Let me start by understanding the project structure and the network code involved.

I've found the root cause. Let me verify my understanding of the exact null-flow before making the fix.

I've confirmed the root cause. Now let me apply the fix to `TableResult.java`.

排查完成。问题根因已定位并修复。

## 结论

NPE 不在 `NetworkUtils.writeItem`，而是**配方同步（recipe sync）时序列化了一个 `null` 的 `ItemStack`**。

## 根因

错误信息里的 `p_130056_` 正是 `FriendlyByteBuf.writeItemStack(ItemStack p_130056_, boolean limitedTag)` 的形参（我已通过 IDEA 反编译确认 1.20.1 源码）。服务端在玩家加入时发送 `ClientboundUpdateRecipesPacket`，逐条调用配方的 `toNetwork`，其中模组的 [TableRecipeSerializer.toNetwork](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/recipe/TableRecipeSerializer.java:75) 执行：

```java
NetworkUtils.writeItem(buffer, tableResult.getResultItem());  // getResultItem() 返回 null → NPE
```

`getResultItem()` 为 null 的链路：

1. [TableRecipeSerializer._fromJson](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/recipe/TableRecipeSerializer.java:43) → `TableRecipe.fromPojo` → [TableResult.fromPojo](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/recipe/TableResult.java:42) 执行 `new TableResult(pojo, null, null, null)`，把 `resultItem` 和 `tabGroupLocation` 都设成了 `null`。
2. 本应由 [TableResult.prepare](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/recipe/TableResult.java:50) 通过 `_TableResultRaw` 懒解析出结果物品，但 `raw` 字段从未被创建（`_TableResultRaw` 是全项目死代码，`fromPojo` 里传的是 `null`），所以 `prepare()` 是空操作，`resultItem` 一直是 `null`。

于是配方同步时 `writeItemStack(null)` 触发 NPE。（顺带一提，`getTabGroupLocation()` 同样是 null，会在下一行 `writeResourceLocation(null)` 触发第二个 NPE，只是被第一个遮住了。）

你之前把 `writeItemStack(item, false)` 改成 `true` 仍复现，正是因为 `false/true` 只影响 NBT 详情的 `limitedTag` 参数，**NPE 发生在读取该参数之前的 `p_130056_.isEmpty()`**，与它无关。

## 修复

在 [TableResult.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/recipe/TableResult.java) 让两个 getter 对 null 兜底：

```java
public ItemStack getResultItem() {
    return this.resultItem != null ? this.resultItem : ItemStack.EMPTY;
}
public ResourceLocation getTabGroupLocation() {
    return this.tabGroupLocation != null ? this.tabGroupLocation : TabGroup.GUN_CUSTOM.registryLocation;
}
```

`TabGroup.GUN_CUSTOM` 兜底与 [_TableResultRaw.getEmpty()](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/recipe/_TableResultRaw.java:64) 现有逻辑一致。

## 关于版本差异（1.20.4 不触发）

我把 1.20.1/1.20.2/1.20.4 的整个 recipe 包做了逐文件 diff，`TableResult`、`TableRecipe`、`TableRecipeSerializer`、`_TableResultRaw`、`ModRecipe` 在 1.20.2 与 1.20.4 之间**完全相同**，vanilla 的 `ClientboundUpdateRecipesPacket` 也都调用 `serializer.toNetwork`。也就是说「resultItem 为 null」这个缺陷在所有版本代码里都存在，差异只可能出在**配方是否真正被加载进 `RecipeManager`**（1.20.4 的 `fromJson` 不再 `@Override`，改走 `codec()` 路径，实际是否加载成功我无法仅靠静态代码 100% 确定）。

这层版本差异不影响修复方向：只要配方被加载并同步，这个 null 就会把 `writeItemStack(null)` 喂进去；我的修复把 null 变成 `ItemStack.EMPTY`，从根源上消除 NPE。

## 需要你注意的更深层问题

`fromPojo` 没有真正构建配方结果物品（`_TableResultRaw` 未被接线），所以当前配方产物实际是「空」的。我这个修复只让产物「非 null（空）」以避免崩溃，**并没有实现完整的配方产物解析**（那属于 `fromPojo` → `_TableResultRaw` 的接线，是另一个功能点）。如果配方合成需要产出真实枪械/配件/子弹物品，需要另行实现。

## 状态

- 只改了 [TableResult.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/recipe/TableResult.java) 一个文件，未 commit。
- 请在 1.20.1 / 1.20.2 连接多人游戏测试确认是否还复现。如果仍复现，请把完整堆栈（`latest.log`/`debug.log` 里 `EncoderException` 上方的 `at ...` 调用链）发我，我可以据此进一步定位是否还有第二个 null 来源。
