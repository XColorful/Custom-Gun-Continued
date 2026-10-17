> #70 提示词

# 

```
读Issue #68 https://github.com/XColorful/Custom-Gun-Continued/issues/68 里对于1.21.11NeoNetworkAdapter的提问
- 现在IDEA打开了1.20.1-1.21.11各分支的窗口，供浏览源码；26.1.x到26.3的NeoNetworkAdapter跟1.21.11一样，当前不处理26.1+版本
- 检查1.20.1起的ForgeNetworkAdapter和NeoNetworkAdapter，如果有差异则检查下是否是等价实现/移植，已有代码可能有遗留的移植/未研究NeoForge API的问题
- 如果不同版本之间需要修改的代码相同，则只修改第一个版本，并说明后续要cherry pick到哪几个版本（假设1.20.2和1.20.4修改相同，那么只要说明1.20.2修改了，1.20.4复用1.20.2的修改即可）
- 检查不同版本是否都默认在主线程处理、在主线程处理时调用enqueueWork是否会延迟1tick执行
- 已知高版本握手包注册可能存在问题但不影响游戏，需要检查注册方式
- 可分配多Agent执行，先分析哪些版本的修改是一致的（在任务完成后给出cherry pick表格），再开始修改
```

# 

我先读 Issue #68 的内容，再定位各版本的 NetworkAdapter 实现。

`gh` 不可用，改用 WebFetch 读 Issue。

现在对比各版本的实现差异。

现在核实各版本平台的 `enqueueWork`/`execute` 语义。先找源码。

我现在把修改应用到 1.21.1neoforge（作为该改动的首个版本），随后编译验证。

修改已通过 1.21.1 编译。现在编译 1.20.2 / 1.20.4 的 neoforge-compat，确认这些移植实现本身能对应各自的 NeoForge API。

已完成检查与修改，编译验证通过，未提交。

## 1. Issue #68 四点逐条核实

**① 手动构造 `connection`（原 167–232 行）**  
`context.connection()` 在 NeoForge 1.21.1+ 存在，服务端 play、客户端 play、配置阶段都返回真实 `Connection`，与手动从 `ServerPlayer` / `_LocalPlayer` 取的是同一个对象。手动写法是从 **NeoForge 1.20.4** 遗留下来的——20.4 的 `IPayloadContext` 只有 `Optional<Player>()`，没有 `connection()`。

**② `rp.isHandshake ? () -> {} : null`**  
这个空 lambda 是**正确的**：新 payload API 由 NeoForge 自动标记已处理，没有 `setPacketHandled`。保留它只是为了 `IMessage.NetworkContext.setHandled()` 跨版本统一（core 只在这两个握手消息里调 `setHandled()`）。Issue 说"没有作用"属实，但不是缺陷。

**③ `context.enqueueWork(() -> message.handle(...))`**  
确实多余。`PayloadRegistrar` 的默认值是 `HandlerThread.MAIN`，注册时会把 handler 包进 `MainThreadPayloadHandler`，handler 本来就在主线程。不需要 `.executesOn(HandlerThread.NETWORK)`——这里没有网络线程语义。

**④ 握手包注册方式**  
方向注册是对的（ack → `configurationToServer`，mapping → `configurationToClient`），但有两个真问题：

- **握手从来没被发送过**。TaCZ 的参考实现额外注册了 `RegisterConfigurationTasksEvent` + `ICustomConfigurationTask` 在配置阶段发 mapping，CGC 只移植了注册、没移植发送。1.20.2+ 全是死代码（这也是"不用也没事"的原因）。
- 1.21.x 的 handler 无条件调 `context.player()`，而**配置阶段的 `player()` 会抛 `UnsupportedOperationException`**（`ServerPayloadContext`/`ClientPayloadContext` 里显式抛）。一旦握手真的发出去，接收端会直接崩。同时旧代码在配置阶段 `connection == null`，`S2CMessageSyncedEntityDataMapping` 失败时 `connection().disconnect()` 会 NPE。

1.20.1 是唯一真正会跑握手的版本：Forge 1.20.1 的 `markAsLoginPacket()` 会在 login 阶段自动发送消息（`GatherLoginPayloadsEvent`），所以它有效。

## 2. 各版本实现差异与等价性

**NeoNetworkAdapter**

|版本|机制|差异|
|---|---|---|
|1.20.2|旧 SimpleChannel（20.2.93 无 payload API）|独立通道 `handshake`/`network`，`consumerMainThread` / `consumerNetworkThread`|
|1.20.4|第一代 payload API（`IPayloadRegistrar`）|handler 在**网络线程**，必须 `workHandler().execute`；play 消息用 `common(...)`，会**同时注册到配置阶段**（1.21.x 只注册 play）|
|1.21.1 = 1.21.4 = 1.21.6|`PayloadRegistrar`|文件完全相同|
|1.21.10|同上|`ClientPacketDistributor.sendToServer`、`FMLLoader.getCurrent()`|
|1.21.11|同上|`ResourceLocation` → `Identifier`|

**ForgeNetworkAdapter**

|版本|机制|差异|
|---|---|---|
|1.20.1|`NetworkRegistry.newSimpleChannel` + `registerMessage(..., Optional<NetworkDirection>)`|有 `HandshakeHandler.indexFirst` + `markAsLoginPacket`（login 自动发送）|
|1.20.2 = 1.20.4|`ChannelBuilder` + `messageBuilder`|`LOGIN_TO_*` 方向；`markAsLoginPacket` API 已删除，握手不会再自动发送|
|1.21.1…1.21.11|同上|仅 `NetworkDirection<?>` 泛型 + `context.isClientSide()` 取代 `convert(getDirection())`|

结论：Forge 1.20.2+ 与 1.21.x 是等价移植；Neo 1.20.2 / 1.20.4 分别对应各自平台的 API，没有发现错误的移植逻辑。唯一非等价的是 1.20.4 的 `common` 注册面比 1.21.x 宽（无害）。

## 3. 线程模型与 enqueueWork

|分支|play handler|握手 handler|主线程上调 enqueueWork|
|---|---|---|---|
|1.20.1 Forge|**网络线程**|网络线程|立即执行（有 `isSameThread()` 检查）|
|1.20.2 Forge|主线程（`consumerMainThread`）|网络线程|立即执行|
|1.20.4 / 1.21.1 Forge|主线程|网络线程|立即执行|
|1.20.2 Neo|主线程|网络线程|立即执行|
|1.20.4 Neo|**网络线程**（文档明确）|网络线程|需要，排队到主线程|
|1.21.1–1.21.11 Neo|主线程（默认 `HandlerThread.MAIN`）|主线程|立即执行|

`enqueueWork` 在两个平台上都是先判 `isSameThread()`，同线程直接 `run()`，**不会延迟 1 tick**。跨线程提交才会进 `BlockableEventLoop` 队列，在下一次主线程队列排空时执行（可能同 tick 末，也可能下一 tick），但这不是固定 +1 tick。

## 4. 已修改（只改第一个版本）

[NeoNetworkAdapter.java](app://localhost/Custom-Gun-Continued-1.21.1neoforge/neoforge-compat/src/main/java/dev/xcolorful/customgun/neoforge/network/NeoNetworkAdapter.java)：

- `context.connection()` 取代手动 `ServerPlayer` / `_LocalPlayer` 查找，删除整段和 `_LocalPlayer`
- 握手包 `sender` 传 `null`，避免配置阶段 `context.player()` 抛异常；普通 play 包仍取 `context.player()`
- 去掉外层 `context.enqueueWork`，直接 `message.handle(message, context::enqueueWork, netContext)`（与 Forge 适配器结构一致，同线程内联）
- 删除 `Connection` / `Player` / `FMLLoader` 导入

编译验证：1.21.1neoforge、1.20.2neoforge、1.20.4neoforge 的 `:neoforge-compat:compileJava` 全部通过。未运行游戏。

### Cherry-pick 表

|改动|已改版本|复用该改动的版本|说明|
|---|---|---|---|
|Neo 适配器线程/连接/握手修复|**1.21.1neoforge**|1.21.4neoforge、1.21.6neoforge|文件与 1.21.1 完全相同，`git apply --check` 已通过|
|同上|—|1.21.10neoforge、1.21.11neoforge|逻辑相同；import 上下文不同（1.21.10 多 `ClientPacketDistributor`、1.21.11 用 `Identifier`），cherry-pick 需解一次 import 冲突|
|同上|—|26.1.2 / 26.1.x / 26.2 / 26.3|按你的要求本轮不处理，补版本时同样适用|

1.20.2 / 1.20.4 的 Neo 适配器**不适用**这个补丁（20.4 没有 `IPayloadContext.connection()`，且 handler 在网络线程，`workHandler().execute` 必须保留）。

## 5. 未改动、需要你决策的点

- **1.20.4 的 `registrar.common(...)`**：会把所有 play 包也注册到配置阶段；改成 `registrar.play(...)` 才能和 1.21.x 一致，但当前无害。
- **握手功能本身仍是死的**：要在 1.20.2+ 真正生效，需要补 `RegisterConfigurationTasksEvent`（Neo）/ 登录事件发送（Forge）。这属于功能移植，且会改变连接行为，未擅自加。
- **neoforge-compat 里的 `MessageDirectionHelper`** 在所有 Neo 分支都未被引用（只有 Forge 的在用），可以后续清理。

需要我把 1.21.10/1.21.11 也直接改掉，或补上握手配置任务吗？

# 

```
1.20.1是否默认注册时在网络线程？对于1.20.2起跟1.20.1不一样的也是如此，保留默认注册在网络线程，然后复用enqueueWork
1.20.4common改成play，首要跟1.20.1一致
```

# 

明白，目标是统一成 1.20.1 的语义：**handler 保留在网络线程，游戏逻辑由 core 传入的 `enqueueWork` 再跳到主线程**。我先核实各版本平台默认线程与 `setPacketHandled` 语义，再改。

切换前先审计所有 `IMessage.handle`：确认跳线程前的代码不触碰游戏状态。

开始修改。1.20.2 Forge 与 1.20.4 Forge 文件完全相同，先改 1.20.2。

文件在我编辑期间被外部改动过（`_LocalPlayer` 被重新加回并加了注释）。我先读取当前实际内容再决定。

已按"网络线程注册 + 复用 `enqueueWork`"统一，所有改动编译通过。

## 先回答：1.20.1 确实默认在网络线程

1.20.1 的 vanilla `handleCustomPayload` 是：

```java
// ServerGamePacketListenerImpl
public void handleCustomPayload(ServerboundCustomPayloadPacket p) {
    NetworkHooks.onCustomPayload(p, this.connection);   // 没有 ensureRunningOnSameThread
}
// ClientPacketListener
public void handleCustomPayload(ClientboundCustomPayloadPacket p) {
    if (NetworkHooks.onCustomPayload(p, this.connection)) return;  // 同样在网络线程
    PacketUtils.ensureRunningOnSameThread(...);
}
```

所以 `SimpleChannel` 的 consumer 在 Netty 网络线程执行，再由 adapter 把 `context::enqueueWork` 交给 core，core 里的游戏逻辑才跳到主线程。这正是 `IMessage.handle(..., Consumer<Runnable> handler, ...)` 这个参数存在的原因。1.20.2 起各版本改成了"主线程 consumer"，反而绕过了 core 的跳线程设计。

## 本次改动

**Forge 1.20.2（1.20.4 复用）**  
[ForgeNetworkAdapter.java](app://localhost/Custom-Gun-Continued-1.20.2/forge-compat/src/main/java/dev/xcolorful/customgun/forge/network/ForgeNetworkAdapter.java)

- `consumerMainThread` → `consumerNetworkThread`
- 补 `context.setPacketHandled(true)`：Forge 的 `consumerNetworkThread` 不像 `consumerMainThread` 会自动标记已处理，1.20.1 适配器里也是显式设置的

**Forge 1.21.1（其余 Forge 分支复用）**  
[ForgeNetworkAdapter.java](app://localhost/Custom-Gun-Continued-1.21.1forge/forge-compat/src/main/java/dev/xcolorful/customgun/forge/network/ForgeNetworkAdapter.java)

- 同样处理；`consumerNetworkThread` 在 Forge 1.21.1 仍保留为 `consumer(...)` 的别名

**Neo 1.20.2**  
[NeoNetworkAdapter.java](app://localhost/Custom-Gun-Continued-1.20.2/neoforge-compat/src/main/java/dev/xcolorful/customgun/neoforge/network/NeoNetworkAdapter.java)

- `consumerMainThread` → `consumerNetworkThread`（20.2.93 的 `add()` 会自动 `setPacketHandled(true)`，无需手写）

**Neo 1.20.4**  
[NeoNetworkAdapter.java](app://localhost/Custom-Gun-Continued-1.20.4/neoforge-compat/src/main/java/dev/xcolorful/customgun/neoforge/network/NeoNetworkAdapter.java)

- `registrar.common(...)` → `registrar.play(...)`，play 消息不再额外注册到配置阶段，与 1.20.1 的"只走 play 通道"一致
- 该版本 handler 本来就在网络线程，`context.workHandler().execute(...)` 保留

**Neo 1.21.1（后续版本复用）**  
[NeoNetworkAdapter.java](app://localhost/Custom-Gun-Continued-1.21.1neoforge/neoforge-compat/src/main/java/dev/xcolorful/customgun/neoforge/network/NeoNetworkAdapter.java)

- `event.registrar(modId).executesOn(HandlerThread.NETWORK)`，恢复网络线程语义
- `context.connection()` 取代手动 `ServerPlayer` / `_LocalPlayer` 查找
- 握手包 `sender` 传 `null`，避免配置阶段 `context.player()` 抛 `UnsupportedOperationException`
- `message.handle(message, context::enqueueWork, netContext)`：去掉多余的外层 `enqueueWork`，跳线程统一交给 core 的 `handler`
- 删除 `Connection` / `FMLLoader` 导入

> 注意：编辑过程中该文件被外部改动过——`_LocalPlayer` 被重新加回并标了 `@Deprecated(since = "1.21.1")` 与注释。我保留了这个改动，没有覆盖。它现在是未使用的私有类，需要的话可以再删。

## Cherry-pick 表

|改动|已改版本|复用/回传到|验证|
|---|---|---|---|
|Forge play handler 改网络线程 + `setPacketHandled`|**1.20.2forge**|1.20.4forge|`git apply --check` OK|
|同上|**1.21.1forge**|1.21.1neoforge、1.21.4、1.21.6、1.21.10、1.21.11 的 `forge-compat`|5 个目标 `git apply --check` 全部 OK|
|Neo 1.20.2 改网络线程|**1.20.2neoforge**|无（该分支唯一）|编译通过|
|Neo 1.20.4 `common` → `play`|**1.20.4neoforge**|无（该分支唯一）|编译通过|
|Neo 1.21.x 改网络线程 + 连接/握手修复|**1.21.1neoforge**|1.21.4、1.21.6 可直接应用；1.21.10、1.21.11 需解一次 import 冲突（`ClientPacketDistributor` / `Identifier`）|1.21.4/1.21.6 apply OK|
|26.1.2 / 26.1.x / 26.2 / 26.3|按要求未处理|后续与 1.21.11 相同|—|

## 验证

- `1.20.2neoforge` + `1.20.2forge` `compileJava`：BUILD SUCCESSFUL
- `1.20.4neoforge`：BUILD SUCCESSFUL
- `1.21.1forge`：BUILD SUCCESSFUL
- `1.21.1neoforge`：BUILD SUCCESSFUL

审计过所有 `IMessage.handle`：`handler.accept(...)` 之外只有 `getLogicalSide()`、日志、时间戳、`context.connection()/reply()/setHandled()` 这些线程安全的操作；`S2CMessageSyncDataPack.doSync` 本来就是设计成网络线程解析。未运行游戏，未提交。
