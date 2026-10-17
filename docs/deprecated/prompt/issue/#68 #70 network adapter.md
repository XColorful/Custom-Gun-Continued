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

# 

```
现在cherry pick好了，再继续检查config事件监听注册的问题
- ForgeNetworkAdapter/NeoNetworkAdapter预期是一个可让其他mod复用的设计（构造函数内传了信息），如果注册事件需要唯一标识，尽可能只用传入的modid以及注册时保留信息/直接用注册的事件类名等可用的标识
- 同样只完成首个差异版本，相同的后续版本等我cherry pick
```

# 

我先确认各分支里与 `RegisterConfigurationTasksEvent` / 配置阶段任务相关的现有代码，以及适配器实例化与事件总线注册的位置。

先查历史里是否曾有配置阶段任务注册（判断这是“移植丢失”还是“从未实现”）。

发现最近有个 `#68 #70 network adapter.md` 文档提交，先读它。

按 `RegisterConfigurationTasksEvent` 补上握手任务注册；Task 类型用 `modId` + 注册的握手消息类名，避免硬编码。

`config 事件监听` 的检查结果和修复已完成，1.21.1 编译通过。

## 检查结果

**payload 注册事件**（`RegisterPayloadHandlersEvent`）

- 参数语义随版本变化：1.20.4 是 `registrar(String namespace)`，1.21.x 是 `registrar(String version)`。当前统一传 `this.modId` 是刻意的跨版本写法，两个语义下都成立，且满足"复用适配器时只用构造函数传入的 modId"。
- 消息唯一标识已经是 `modId + 注册类名`：`"%s:%s"` → `modId:classsimplelower`。没有硬编码 `customgun`。
- 这部分**不需要改**。

**配置阶段任务注册**（`RegisterConfigurationTasksEvent`）

- 之前适配器只注册了 `RegisterPayloadHandlersEvent`，从未注册 `RegisterConfigurationTasksEvent`。结果是 1.20.2+ 的"握手"消息只有注册、没有发送，完全是死路径；1.20.1 靠 Forge 的 `markAsLoginPacket()` 自动发送，所以只有 1.20.1 能跑。
- 已补上。唯一标识直接用注册信息推导：`ConfigurationTask.Type(packetId)`，即 `modId:<握手消息类名>`，不引入任何硬编码 id。

## 修改内容（1.21.1neoforge）

[NeoNetworkAdapter.java](app://localhost/Custom-Gun-Continued-1.21.1neoforge/neoforge-compat/src/main/java/dev/xcolorful/customgun/neoforge/network/NeoNetworkAdapter.java)

- 实现 `@SubscribeEvent registerConfigurationTasks(RegisterConfigurationTasksEvent)`：
    - 只有同时注册了握手 ack（C2S）和握手消息（S2C）时才注册任务，避免只有一半时把连接卡在配置阶段
    - 任务类型 = `modId:握手消息类名`（与 payload id 一致）
    - `run(...)` 时通过反射创建消息实例并发出（与 1.20.1 `markAsLoginPacket` 的隐式无参构造一致；`S2CMessageSyncedEntityDataMapping` 本来就有无参构造）
    - 注册时校验无参构造；不存在则记录错误并且不注册任务（不让连接卡死）
- ack 的处理通过 `NetworkContext.setHandled()` 接回配置任务：`setHandledFunc = () -> context.enqueueWork(() -> context.finishCurrentTask(taskType))`
    - 只对 `CLIENT_TO_SERVER` 握手包生效；S2C mapping 仍然是空 lambda
    - `finishCurrentTask` 走 `enqueueWork` 提交到主线程，和 NeoForge 自己 `ServerPayloadHandler` 的调用方式一致
- 任务实例用的 `Supplier<CustomPacketPayload>` 每次连接重新创建，避免跨连接共享可变消息对象

现在的流程与 1.20.1 对齐：服务端配置阶段发送 mapping → 客户端解析并更新映射 → 回 ack → 服务端完成配置任务。

## Cherry-pick

|改动|已改版本|复用|说明|
|---|---|---|---|
|Neo 配置任务注册 + 握手发送/完成|**1.21.1neoforge**|1.21.4neoforge、1.21.6neoforge|文件相同，可直接应用|
|同上|—|1.21.10neoforge、1.21.11neoforge|逻辑相同；import 区域需解一次冲突（`ClientPacketDistributor`/`Identifier`）|
|同上|—|26.1.2 / 26.1.x / 26.2 / 26.3|按要求暂不处理|

## 未覆盖的版本

- **Neo 1.20.4**：对应 API 是 `OnGameConfigurationEvent` + `ICustomConfigurationTask` + `ITaskCompletedHandler`，和 1.21.x 不是同一份改动，不能 cherry-pick，需要单独移植。
- **Neo 1.20.2**：NeoForge 20.2.93 没有配置任务 API（源码里连 `ConfigurationTask` 都没有），无法实现。
- **Forge 1.20.2+**：Forge 也没有 mod 侧配置任务事件，且 `markAsLoginPacket` 已删除；握手保持现状（不发送）。

编译验证：`1.21.1neoforge:neoforge-compat:compileJava` BUILD SUCCESSFUL。未运行游戏，未提交。需要我继续补 1.20.4 的 `OnGameConfigurationEvent` 版本吗？

# 

[fork1](#fork1)
[fork2](#fork2)

# fork1

```
握手消息是否NeoForge只支持注册一个/不会有多个注册的需求？目前的实现似乎默认了只注册一个？用bool和遍历registeredPackets来筛选的区别？
```

# 

先核实两件事：vanilla 的 `finishCurrentTask` 语义，以及当前实现在多次注册时会怎样。

NeoForge 本身不限制注册数量，但当前实现确实是"单条"假设。我先确认 vanilla 的任务完成语义，再改成按 `registeredPackets` 推导。

NeoForge 本身没有数量限制，但 CGC 的**握手协议**只支持"N 条 S2C + 1 条 ack"。当前实现确实默认了单条，已改成从 `registeredPackets` 推导。

## 回答

**1. NeoForge 是否只支持一个？**

- payload 注册层面没有限制：`configurationToClient` / `configurationToServer` / 配置任务都可以注册任意多个。
- 但 CGC 的握手协议里 ack 是**无内容、无标识**的 `C2SMessageAcknowledge`，而 vanilla 的 `finishCurrentTask(Type)` 只认当前那一个任务：

    ```java
    if (!taskType.equals(currentTask.type()))    throw new IllegalStateException("Unexpected request for task finish, ...");
    ```

  所以：
    - 多条 S2C 握手消息可以放在**同一个任务**里一起发，客户端处理完回一次 ack；
    - 多条 ack 没有语义：第一个 ack 完成后任务就切换了，第二个再 `finishCurrentTask` 会抛异常（可能导致断连）。Forge 1.20.1 靠 login index 区分多条，payload 路径没有这个信息。

**2. 原实现的问题**  
`handshakeTaskType` / `handshakePayloadFactory` 是单值字段，第二次 `registerHandshakeMessage` 会静默覆盖：所有消息都注册为可接收，但任务只发最后一条。`hasHandshakeAck` 只是"有没有 ack"的存在标记，不是问题根源。

**3. bool vs 遍历 `registeredPackets`**

- bool：O(1)，但是和 `registeredPackets` 重复的状态，只回答"有没有 ack"，不携带任何消息信息。
- 遍历：`registeredPackets` 已经保存了 `isHandshake` / `direction` / `messageType`，是唯一数据源；天然支持多条；`RegisterConfigurationTasksEvent` 每个连接只触发一次，n≈30 的遍历开销可以忽略。
- 因此去掉了 bool，改为在事件触发时筛选。

## 现在的实现

- `RegisteredPacket` 增加 `Supplier<CustomPacketPayload> handshakePayloadFactory`（仅 S2C 握手消息非空）
- `registerHandshakeMessage` 在注册时校验无参构造并生成工厂；`handshakeTaskType` 只取**第一个可发送**握手消息的 id（仍然是 `modId + 注册类名`，不硬编码）
- `registerConfigurationTasks` 遍历：
    - `hasAck` = 存在 `isHandshake && CLIENT_TO_SERVER`
    - `payloadFactories` = 所有 `isHandshake && SERVER_TO_CLIENT` 且工厂非空的项
    - 条件不足（无 S2C / 无 ack）就不注册任务，避免把连接卡在配置阶段
    - 任务 `run` 里依次发送所有工厂
- 重复注册 ack 时记录 error（协议只支持一个）
- `setHandledFunc` 仍只给 C2S 握手包挂 `finishCurrentTask`，S2C mapping 保持空 lambda

注意一个协议层面的约束：如果以后加的第二条 S2C 握手消息也回复 ack，就会变成二次 `finishCurrentTask`。要真正支持"每条消息各自确认"，需要给 ack 加任务/消息标识，这属于协议改动，不在本次范围。

编译验证：`1.21.1neoforge:neoforge-compat:compileJava` BUILD SUCCESSFUL。本次仍是同一份 1.21.x 改动，cherry-pick 目标不变（1.21.4/1.21.6 直接应用，1.21.10/1.21.11 解 import 冲突）。文件里的 `@ApiStatus.AvailableSince("1.21.1")` 是外部编辑加的，我保留了。

# 

```
那么1.20.1目前实现是否支持多条注册？login index是否是forge专有的？
如果只能注册一个，log error改成throw？
```

# 

## 1.20.1 的实现支持多条注册

Forge 1.20.1 的 `SimpleChannel` 两边都支持多条：

**S2C（`markAsLoginPacket`）**

```java
private List<Function<Boolean, ? extends List<? extends Pair<String,?>>>> loginPackets;
...
if (this.loginPacketGenerators != null)
    this.channel.loginPackets.add(this.loginPacketGenerators);
```

`loginPackets` 是列表，`networkLoginGather` 遍历它逐个生成并发送，每条都有独立的 login index。

**C2S（`HandshakeHandler.indexFirst`）**

```java
// 取收到的 login index，和 sentMessages 里记录的服务端已发 index 对账
this.sentMessages.removeIf(i -> i == message.getAsInt());
if (!removed) LOGGER.error("Recieved unexpected index {} in client reply");
```

`NetworkEvent.Context` 在构造时就绑定了 `packet.getIndex()`，`reply`/`getPacketDispatcher()` 会用同一个 index 回包，所以多个 ack 类型可以按 index 各自对应。

结论：1.20.1 是 **N 条 S2C + N 条 ack**；payload + 配置任务路径只能做到 **N 条 S2C + 1 条 ack**。

## login index 是 Forge 专有的

- 它属于 Forge `SimpleChannel` 的 login-packet 机制：`NetworkEvent.getLoginIndex()`、`PacketDispatcher` 的 index、`LoginIndexHolder` 都是这条链路的一部分（`LoginIndexHolder` 来自 MrCrayfish Framework 的实现）。
- NeoForge 20.2.93 的 SimpleChannel 是同一份代码的分支，仍有 `loginIndex(...)`；但从 **NeoForge 20.4 的 payload API** 开始没有这个概念，1.21.x 的配置任务也没有任务内的消息序号。
- Forge **1.20.2+** 删掉了 `markAsLoginPacket` 和 index 对账，只剩 `LOGIN_TO_*` 方向，也没有替代品。
- 所以现代 NeoForge 的配置任务只能用"一个任务 + 一个无标识 ack"来表达握手，这就是当前实现只能支持一个 ack 的根本原因。

## log error 已改成 throw

[NeoNetworkAdapter.java](app://localhost/Custom-Gun-Continued-1.21.1neoforge/neoforge-compat/src/main/java/dev/xcolorful/customgun/neoforge/network/NeoNetworkAdapter.java)：

```java
this.registeredPackets.stream()
        .filter(rp -> rp.isHandshake() && rp.direction() == MessageDirection.CLIENT_TO_SERVER)
        .findFirst()
        .ifPresent(existing -> {
            throw new IllegalStateException(String.format(
                    "Multiple handshake acknowledge messages registered: %s and %s; only one is supported",
                    existing.messageType().getName(), clazz.getName()));
        });
```

理由：重复注册 ack 在运行期会变成 vanilla 的  
`IllegalStateException("Unexpected request for task finish, ...")`（第二次 `finishCurrentTask`），可能直接断连；在注册期抛出能在启动时就暴露，错误信息也更明确。

另外还有一个同类的静默失败点：**握手消息没有无参构造**时目前只是 `LOGGER.error` 并且不发送（握手静默失效）。按同样的 fail-loud 思路也可以改成 throw（Forge 1.20.1 是在 login 时抛 `RuntimeException("Inaccessible no-arg constructor for message ...")`）。要不要一起改？

编译验证：`:neoforge-compat:compileJava` BUILD SUCCESSFUL。文件里外部加的无参构造重载（5 参数便捷构造）我保留了。

# 

```
也改throw，并且按照现在的话，是否1.20.1会发送多条握手导致跟高版本有差异？如果只注册一个，是否不需要LoginIndexHolder？
```

# 

已把无参构造的报错也改成 throw，并移除了不再使用的 `CustomGun` import。编译通过。

## 1.20.1 会不会发多条，跟高版本有没有差异？

**取决于注册数量，而不是版本本身。**

- 1.20.1：`markAsLoginPacket()` 把每条注册的 S2C 消息都加入 `loginPackets`，登录时全部发送，每条带独立 login index，并期望对应 index 的 ack。
- 1.21.x Neo：当前实现把一个任务里所有注册的 S2C 消息依次发出，但**只能有一个无标识 ack** 来完成任务。

所以：

|注册情况|1.20.1|1.21.x Neo|是否等价|
|---|---|---|---|
|1 条 S2C + 1 条 ack（core 现状）|发 1 条，1 个 index ack|发 1 条，1 个 ack|**等价**|
|N 条 S2C + 1 条 ack|发 N 条，期望 N 个 index ack|发 N 条，只能收 1 个 ack|不等价|
|N 条 ack|按 index 可对应|已 throw|—|

core 的 `NetworkHandler` 只注册了一条 `S2CMessageSyncedEntityDataMapping` 和一条 `C2SMessageAcknowledge`，所以**当前实际行为没有差异**。差异只在"有人注册多条"时出现。

如果确认没有多条需求，最省心的做法是把语义统一成"1 条 S2C + 1 条 ack"：在第二条 `registerHandshakeMessage` 时也 throw，同时把任务简化为单条发送。需要的话我可以改；当前实现是多条一起发、单 ack 完成。

## 只注册一个时 LoginIndexHolder 是否还需要？

分平台：

**1.21.x Neo：不需要**  
适配器完全没有使用它：

- 不调用 `getLoginIndex` / `setLoginIndex`
- `NeoPayload` 不写 index
- 配置任务靠 `ConfigurationTask.Type`，不是 login index

它只是 core `INetworkAdapter` 的类型约束和两个消息类的父类。因为 core 跨版本共享且冻结，不能只为 Neo 删掉。

**1.20.1 Forge：仍然需要，即使只有一条**

- `loginIndex` 不是"消息序号"这么简单，它是 login 阶段的**应答关联 id**。
- 登录包由 `GatherLoginPayloadsEvent` 统一收集，index 由 Forge 跨所有 mod 的 login payload 分配，不保证是 0（其他 mod 的登录包可能排在前面）。
- 服务端回包时 `PacketDispatcher` 带着接收到的 index 发回；`HandshakeHandler.handleIndexedMessage` 用这个 index 去 `sentMessages` 对账：

    ```java
    boolean removed = this.sentMessages.removeIf(i -> i == message.getAsInt());if (!removed) LOGGER.error("Recieved unexpected index {} in client reply");
    ```

- 如果去掉 `.loginIndex(...)`，消息里的 index 保持默认值，回包 index 对不上，轻则报错，重则登录握手无法正确完成。

所以 `LoginIndexHolder` 必须留在 core；它的价值只在 1.20.1 的 Forge login-index 机制里，在 1.21.x 只是无害的兼容类型约束。

## 编译

`:neoforge-compat:compileJava` BUILD SUCCESSFUL。当前 1.21.1neoforge 的待提交改动包括：配置任务注册、多 S2C 汇总发送、重复 ack / 无无参构造改为 throw。

# 

```
增加限制，使1.20.1跟高版本统一，1.20.1额外有但高版本没有的需要限制不使用
```

# 

统一为"1 条 S2C + 1 条 ack"，并对所有版本加注册数量限制。先改 1.20.1 Forge。

已把"1 条 S2C + 1 条 ack"的约束加到所有适配器，1.20.1 的多条注册能力被限制掉。所有改动编译通过。

## 改动内容

每个适配器在 `registerHandshakeAcknowledge` / `registerHandshakeMessage` 开头加了注册数量检查，第二次注册直接 `IllegalStateException`（消息里带两个类名）。

|分支/文件|方式|
|---|---|
|1.20.1 Forge|新增 `handshakeAckClass` / `handshakeMessageClass` 字段，第二次注册 throw|
|1.20.2 Forge（1.20.4 相同）|同上|
|1.21.1 Forge（1.21.x forge-compat 相同）|同上|
|Neo 1.20.2|同上（SimpleChannel 没有统一列表，用字段）|
|Neo 1.20.4|用已有 `registeredPackets` 筛选 `isHandshake + direction`，第二次注册 throw|
|Neo 1.21.1|ack 原本已 throw；本次补上 S2C 的同样检查|

这样：

- 1.20.1 的 `markAsLoginPacket()` 只会自动发送一条登录包，不再可能因为多条注册而出现"高版本没有"的多包/多 index 行为。
- 所有版本的 `INetworkAdapter` 握手契约统一为：**最多 1 条 ack、最多 1 条 S2C 消息**。
- 无参构造缺失仍然是 throw（上一轮已改）。

## 验证

```
1.20.1      :forge-compat:compileJava          BUILD SUCCESSFUL
1.20.2      :forge-compat + :neoforge-compat   BUILD SUCCESSFUL
1.20.4      :neoforge-compat:compileJava       BUILD SUCCESSFUL
1.21.1forge :forge-compat:compileJava          BUILD SUCCESSFUL
1.21.1neo   :neoforge-compat:compileJava       BUILD SUCCESSFUL
```

## Cherry-pick

|改动|已改版本|复用|
|---|---|---|
|Forge 握手注册数量限制|**1.20.1**|无（唯一文件）|
|同上|**1.20.2forge**|1.20.4forge|
|同上|**1.21.1forge**|1.21.1neoforge、1.21.4、1.21.6、1.21.10、1.21.11 的 `forge-compat`|
|Neo 握手注册数量限制|**1.20.2neoforge**|无|
|同上|**1.20.4neoforge**|无|
|同上|**1.21.1neoforge**|1.21.4、1.21.6（1.21.10/1.21.11 解 import 冲突）|

## 仍存在的一处能力差异

1.20.1 会通过 `markAsLoginPacket()` **自动发送**那一条登录包；Forge 1.20.2+ 没有等价的公开 API，所以仍然不发送。Neo 1.21.x 已通过配置任务补齐，Neo 1.20.2/1.20.4 没有对应 API。

要不要把 1.20.1 的自动发送也停掉（例如不调用 `markAsLoginPacket()`，让握手在所有版本都变成"注册但不发送"）？这样完全一致，但 1.20.1 的握手会一起失效。目前我保留它，只限制了"多条"。

# fork2

```
现在要排查当前1.21.1neoforge代码状态的问题，客户端无法连上专用服务端，双端的日志在D:\Temp-File\下
```

# 

先看日志目录结构。

崩溃点是 `ClientPayloadContext.player()` 在配置阶段被调用。先确认崩溃时的 jar 是否包含当前源码改动，以及第 276 行对应哪段代码。

已定位直接原因：客户端**刚进入 play、`Minecraft.player` 还没赋值**时收到服务端 play 包，网络线程上 `context.player()` 抛异常。我先确认服务端时序和是否还有第二个问题。

文件在此期间被外部重构过（配置任务改成支持多个握手消息），我的 `sender` 修复已叠加。现在编译验证当前状态。

再确认网络线程上 `getLogicalSide()` 不会触碰客户端状态。

网络线程上 `getLogicalSide()` 依赖 `EffectiveSide.get()`，必须确认 Netty 线程被归到正确的 SidedThreadGroup，否则 C2S 逻辑会被跳过。

已定位并修复。客户端崩溃不是配置阶段的问题，而是**刚进入 play、客户端 player 还没赋值时，网络线程上取了 `context.player()`**。

## 根因

崩溃栈：

```
ClientPayloadContext.player (配置阶段/无 player 时抛 UOE)
  ← NeoNetworkAdapter.lambda$registerPacketInternal$25 (NeoNetworkAdapter.java:276)
  ← NetworkRegistry.handleModdedPayload (:329)
  ← ClientCommonPacketListenerImpl.handleCustomPayload
  ← Netty Client IO #1
```

反编译 jar 后确认第 276 行就是：

```java
rp.isHandshake ? null : context.player()
```

日志时序：

|时间（客户端时钟）|事件|
|---|---|
|14:52:31.869|Netty 线程收到握手 mapping：`Received synced key mappings from server`|
|14:52:32.331|Render 线程正在初始化 `ClientPacketListener`（`handleLogin` 相关 mixin）|
|14:52:32.543|Netty 线程处理一个 **play** 包，`context.player()` 抛异常 → 断线|

服务端时钟对应位置也印证了握手是成功的：

```
14:52:26.903 [Netty Server IO #2] [CustomGun/HANDSHAKE_ACKNOWLEDGE]: Received acknowledgement from client
14:52:27.599 XiaoColorful joined the game
14:52:27.742 XiaoColorful lost connection: Disconnected
```

即：**新的配置任务握手已经正常完成**，问题出在 join 后紧接着的 play 包。

`ClientPayloadContext.player()` 的异常文案不准确——它在 `Minecraft.getInstance().player == null` 时就会抛，不只是配置阶段。登录包由主线程处理并赋值 `Minecraft.player`，而 Netty 线程会并发收到紧随其后的 play 包。

## 为什么现在才出现

之前 `PayloadRegistrar` 默认 `HandlerThread.MAIN`，handler 被 `MainThreadPayloadHandler` 包一层 `enqueueWork`，真正执行时主线程已经把 player 设好了。改成 `executesOn(HandlerThread.NETWORK)` 后，handler 在 Netty 线程立即执行，就踩到了这个时间窗。

## 修复

[NeoNetworkAdapter.java:273](app://localhost/Custom-Gun-Continued-1.21.1neoforge/neoforge-compat/src/main/java/dev/xcolorful/customgun/neoforge/network/NeoNetworkAdapter.java:273)：

```java
Player sender = null;
if (!rp.isHandshake && rp.direction == MessageDirection.CLIENT_TO_SERVER) {
    sender = context.player();
}
```

- 只有 **C2S play 包**（在服务端处理）才取 `context.player()`，此时 `ServerPlayer` 一定存在
- S2C 包和握手包一律传 `null`
- 这正好符合 core 里 `NetworkContext.sender` 的约定：`客户端统一从 Minecraft 获取 LocalPlayer`；core 的所有 S2C handler 都没有使用 `sender`
- 检查了 `D:\Github` 下的 CGC 插件仓库，没有任何地方使用 `.sender()`，无兼容风险

编译验证：`1.21.1neoforge:neoforge-compat:compileJava` BUILD SUCCESSFUL。

## Cherry-pick

|改动|已改版本|复用|
|---|---|---|
|网络线程上只对 C2S play 解析 `player()`|**1.21.1neoforge**|1.21.4neoforge、1.21.6neoforge（文件相同）；1.21.10/1.21.11 需解 import 冲突|

## 日志里的其他噪音（与本次问题无关）

- 服务端 `ClassNotFoundException: org.jetbrains.annotations.ApiStatus$Internal`：Mixin 扫描类元数据时的警告，启动早期就出现，不影响运行
- 客户端 `java.net.ConnectException: Connection timed out`：某个 mod 的外部连接（更新检查之类）
- MTS/Journeymap/ModernFix 的报错均为既有问题

请重新打包后再连一次专用服务端；如果仍有问题，把新的双端日志放到同一目录即可。

# 

```
已修复，检查1.20.1-1.21.1其他分支是否有这个问题
```

# 

逐个分支核对：是否在**网络线程上解析了客户端 player**。先看 1.20.4 Neo 的 `Optional<Player>` 构造和 1.20.2 Neo 的 `getSender()`。

检查完成。结论：**1.20.1–1.21.1 的其他分支都没有这个崩溃**，但 1.21.4 以上（含 26.x）都还带着同样的隐患。

## 各分支核对

|分支|play handler 线程|取 player 的方式|是否有同样问题|
|---|---|---|---|
|1.20.1 forge|网络线程|`context.getSender()`（客户端返回 null）+ `getNetworkManager()`|否|
|1.20.2 forge|网络线程（上轮已改）|`context.getSender()` + `getConnection()`|否|
|1.20.4 forge|网络线程|同上|否|
|1.21.1 forge|网络线程|同上|否|
|1.20.2 neoforge|网络线程（上轮已改）|`context.getSender()`（仅 ServerPlayer，客户端 null）+ `getNetworkManager()`|否|
|1.20.4 neoforge|网络线程（平台默认）|`context.player()` 返回 `Optional<Player>`，join 窗口为 empty，不会抛异常|否|
|1.21.1 neoforge|网络线程（`executesOn`）|已改为只对 C2S play 调用 `context.player()`|**已修复**|

依据：

- Forge 1.20.1/1.20.2/1.20.4/1.21.1 的 `CustomPayloadEvent.Context.getSender()` / `NetworkEvent.Context.getSender()` 实现都只返回 `ServerPlayer`，客户端侧一律 `null`，不存在 `Minecraft.player == null` 抛异常的问题。
- NeoForge 20.2.93 的 `NetworkEvent.Context.getSender()` 同样只返回 `ServerPlayer` 或 `null`。
- NeoForge 20.4.251 的 `PlayPayloadContext.player()` 是 `Optional.ofNullable(playPacketListener.getMinecraft().player)`，join 尚未完成时返回 empty，不抛异常。

## 需要留意的 1.20.4 Neo 差异（不是崩溃）

1.20.4 在 join 窗口内 `context.player()` 为空，导致 adapter 里的 `connection` 为 `null`。这不影响连接，但 `S2CMessageSyncDataPack.handle` 会因此把 `remoteConnection` 判成 `false`，跳过 `_AllDataManager.clearInstance()`。属于既有的小偏差，和这次的崩溃无关；该平台没有 `IPayloadContext.connection()`，要彻底修需要另找拿 `Connection` 的途径。

## 1.21.4 以上仍有同样问题

这些分支已经拿到了 `executesOn(HandlerThread.NETWORK)`，但 `sender` 仍是旧写法：

|分支|位置|
|---|---|
|1.21.4neoforge|`NeoNetworkAdapter.java:204`|
|1.21.6neoforge|`:204`|
|1.21.10neoforge|`:205`|
|1.21.11neoforge|`:205`|
|26.1.2 / 26.1.x / 26.2 / 26.3|`:205`|

它们都会在 join 窗口收到首个 play 包时抛出同样的 `UnsupportedOperationException`。按之前的流程，我没有改这些分支，等你在 1.21.1neoforge 上 cherry-pick；需要我直接改也可以说一声。
