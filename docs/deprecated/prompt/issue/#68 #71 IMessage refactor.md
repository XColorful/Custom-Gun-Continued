> #71 提示词

# 

```
读Issue #68 https://github.com/XColorful/Custom-Gun-Continued/issues/68 里对于IMessage的提问
- 现在IDEA打开了1.20.1-1.21.11各分支的窗口，供浏览源码；26.1.x到26.3的NeoNetworkAdapter跟1.21.11一样，当前不处理26.1+版本
- 如果不同版本之间需要修改的代码相同，则只修改第一个版本，并说明后续要cherry pick到哪几个版本（假设1.20.2和1.20.4修改相同，那么只要说明1.20.2修改了，1.20.4复用1.20.2的修改即可）
- 可分配多Agent执行，先分析哪些版本的修改是一致的（在任务完成后给出cherry pick表格），再开始修改
- 可先浏览上一个处理的变更"#68 #70 network adapter.md"

当前IMessage接口为目前设计的过渡方式，CGC本体先都效仿C2SMessageShooterAim用var message = this;来移植，forge/neoforge层改用无T message的版本，已有扩展mod仍然由default转发为带T message的版本，且不要求CGC本体再实现带T message的版本
- 需要分析这个过渡方案是否可行，例如CGC Tactical Addon作为一个Forge/NeoNetworkAdapter的用户需要保证临时的向后兼容
- 目前先处理core1.20.1，1.20.1-1.21.1forge直接由主分支core1.20.1merge时处理差异
- 后续处理neoforge侧才检查cherry pick规则
```

# 

I'll read Issue #68 and the related design document first, then inspect the current code.

现在检查各版本的消息类与适配器调用点，确认过渡方案的编译与二进制兼容性关键点。

方案的核心是二进制兼容性，我先在 JVM 层面验证泛型擦除/桥方法的行为，再决定接口写法。

JVM 实验确认：`T extends IMessage<T>` 会改变擦除签名，老 addon 的桥方法不再覆盖接口方法，默认实现被触发（若是空实现则静默丢失收发）。因此过渡接口必须保持 `T` 无界。现在开始改代码。

编译通过。现在核对各版本源码是否一致，生成 cherry-pick 关系。

## 结论

方案可行，但**工作区里那版 `IMessage<T extends IMessage<T>>` 必须撤掉**。这是唯一会破坏向后兼容的改动，其余设计成立。

### 为什么这个上界不能加

类型参数的上界会改变方法擦除签名：

- `IMessage<T>`（无界）：`encode(T, FriendlyByteBuf)` 擦除为 `encode(Object, FriendlyByteBuf)`
- `IMessage<T extends IMessage<T>>`：擦除为 `encode(IMessage, FriendlyByteBuf)`

旧扩展 mod 编译时会为 `encode(MyMsg, buf)` 生成一个 `encode(Object, buf)` 的桥方法。一旦上界改变，这个桥方法不再覆盖接口方法，调用会落到接口默认实现——如果默认是空实现，就是**静默不收发**。

我用 JDK 21 做了最小复现（旧接口编译 addon → 运行时换新接口）：

```
--- 保持 T 无界
ADDON-ENCODE
ADDON-HANDLE
--- 加上 T extends IMessage<T>
ENCODE-FAIL: UnsupportedOperationException
HANDLE-FAIL: UnsupportedOperationException
```

所以 `IMessage<T>` 的类型参数必须保持无界。这个约束已写入 [IMessage.java](core/src/main/java/dev/xcolorful/customgun/core/api/network/message/IMessage.java:12) 的类注释。

### 兼容矩阵

| 实现方式 | 运行时行为 |
|---|---|
| 已发布 addon 的旧 class（只实现带 T 方法） | 正常：无 T 默认方法转发到桥方法 → addon 实现 |
| 旧 addon 重新源码编译 | 正常：带 T 方法仍是可覆盖的 default |
| 新 addon 只实现无 T 方法 | 正常 |
| 两者都不实现 | 抛 `UnsupportedOperationException`，不会静默发包 |
| addon 自建适配器、直接调用 CGC 消息的带 T 方法 | 会抛异常（唯一不兼容面；CGC 消息不再实现旧签名） |

CGC Tactical Addon 当前没有任何 `IMessage` 实现，只依赖 CGC 的 `INetworkAdapter` 发包，因此**不受影响**；后续若加自定义消息，两种签名都可以实现。

我做的另一个判断是把旧签名默认实现设为**抛异常**而不是空实现（你工作区里原是 `{}`）。理由是漏实现时能立刻暴露；如果你更希望保持空实现，我可以改回。

## 本次改动（core1.20.1）

- [IMessage.java](core/src/main/java/dev/xcolorful/customgun/core/api/network/message/IMessage.java)：`T` 恢复无界；新增无 T 默认方法，单向转发到旧签名；旧签名标记 `@Deprecated(forRemoval = true)`。
- core 下 **34/34** 个消息类改为 `encode(FriendlyByteBuf)` / `handle(Consumer, NetworkContext)`，实现体按 `var message = this;` 移植。
- [ForgeNetworkAdapter.java](forge-compat/src/main/java/dev/xcolorful/customgun/forge/network/ForgeNetworkAdapter.java)：6 处调用改为无 T 版本。

`./gradlew :core:compileJava :forge-compat:compileJava` **BUILD SUCCESSFUL**。未提交。

## Cherry-pick 表（已核对各 worktree 的 HEAD 内容）

**core 补丁**：1.20.2、1.20.4、1.21.1forge、1.21.1neoforge、1.21.4neoforge、1.21.6neoforge、1.21.10neoforge 的消息文件与 1.20.1 HEAD 完全一致，直接复用。1.21.11neoforge 有 6 个文件仅 `ResourceLocation` → `Identifier` 改名，方法签名一致，同一补丁可用（上下文冲突只需接受改名）。

**适配器补丁**只需两行模式：`x.encode(x, buffer)` → `x.encode(buffer)`；`x.handle(x, handler, ctx)` → `x.handle(handler, ctx)`。

| 分支 | core | ForgeNetworkAdapter | NeoNetworkAdapter |
|---|---|---|---|
| core1.20.1（本次） | 已改 | 已改 | — |
| 1.20.2 | 复用 1.20.1 | 复用 1.20.1 的两行改法（ChannelBuilder 写法不同） | 6 处（3 encode + 3 handle，`messageInstance`） |
| 1.20.4 | 复用 1.20.2 | 复用 1.20.2 | 2 处（`this.message` + `Runnable::run`），与 1.20.2 不同 |
| 1.21.1forge | 复用 1.20.1 | 复用 1.20.2 的改法 | — |
| 1.21.1neoforge | 复用 1.20.1 | — | 2 处 |
| 1.21.4neoforge | 复用 1.21.1neoforge | — | 复用 1.21.1neoforge |
| 1.21.6neoforge | 复用 1.21.1neoforge | — | 复用 1.21.1neoforge |
| 1.21.10neoforge | 复用 1.21.1neoforge | — | 2 处（调用行与 1.21.11 相同，文件其余部分有差异） |
| 1.21.11neoforge | core 补丁可用（6 文件改名上下文） | — | 复用 1.21.10 |
| 26.1–26.3 | 按你说明同 1.21.11 | — | 同 1.21.11 |

按你的要求，1.20.2–1.21.1forge 的差异留给主分支 merge；neoforge 侧尚未改动。

# 

[fork1](#fork1)
[fork2](#fork2)
[fork3](https://github.com/XColorful/BattleRoyale/blob/HEAD/docs/deprecated/prompt/issue/%2362%20IMessage%20refactor.md#fork3)

# fork1

```
1.20.2、1.20.4、1.21.1forge已经在合并中，处理合并冲突但不要提交
- neoforge侧先不要处理，防止改动跟Merge混在一起
- Tactical Addon已切换分支，现在可检查IMessage兼容问题
```

# 

先查看三个 worktree 的合并状态与冲突文件。

三个 worktree 的合并都只卡在 `ForgeNetworkAdapter.java`。先读取冲突内容与合并双方版本。

三个分支的冲突已解决并编译通过。现在检查 Tactical Addon 的 `Network-skeleton` 分支。

## 合并冲突处理（未提交）

三个 worktree 的冲突都只有 [ForgeNetworkAdapter.java]，已全部解决并 `git add` 标记为已解决；`.git/MERGE_HEAD` 仍在，未创建任何提交。

解决原则：保留各分支自己的 ChannelBuilder 结构、握手方向和 `loginIndex` 取舍，只合入无 T 调用：

- `.encoder((m, buffer) -> m.encode(buffer))`
- `.handle(context::enqueueWork, netContext)`

|Worktree|冲突块|编译|
|---|---|---|
|1.20.2|5 处（3 builder + 2 handle）|`BUILD SUCCESSFUL`|
|1.20.4|同上|`BUILD SUCCESSFUL`|
|1.21.1forge|4 处（`message` 命名、`decoder::apply`）|`BUILD SUCCESSFUL`|

三个分支的 `IMessage` 与 34 个消息类都已正确合入（34/34 无 T 方法），无残留冲突标记。neoforge 侧未触碰。

## Tactical Addon 兼容检查

`Network-skeleton` 分支现有 12 个消息类，全部使用旧签名：

```java
public void encode(Xxx message, FriendlyByteBuf buffer)
public void handle(Xxx message, Consumer<Runnable> handler, NetworkContext context)
```

它直接使用 CGC 的 `ForgeNetworkAdapter` 注册和发包，没有自建适配器，也没有直接调用 `encode`/`handle`。这正是过渡方案要覆盖的场景，**兼容成立**：

- 旧 jar 的接口描述符为 `encode(Object, FriendlyByteBuf)` / `handle(Object, Consumer, NetworkContext)`
- 新接口保留了完全相同的描述符（只是从 abstract 变成 default）
- addon 编译出的桥方法仍然覆盖它们，适配器调用无 T 默认方法 → 转发到 addon 的旧签名实现

新旧描述符已用 `javap -s` 逐个核对，转发链用 JVM 实验验证过。

### 一个与 IMessage 无关的阻塞项

用当前 `libs/customgun-26.9.21-forge-1.20.1.jar` 编译 addon 分支会失败：

```
NetworkHandler(INetworkAdapter) has private access
adapter / ID_COUNT has private access
```

原因是 addon 的 `NetworkHandler` 继承 CGC 的 `NetworkHandler`，而 26.9.21 发布 jar 里这些成员是 `private`；core1.20.1 在 `26.10.24` 已把它们改成 `protected`。所以 addon 需要换用包含该改动的 CGC jar 才能编译，这与 IMessage 过渡无关。addon 仓库未做任何修改。

# fork2

```
现在forge侧已经处理好，开始neoforge侧的修改，按cherry pick规则只修改必要的分支并重新给表格
```

# 

先核对各 neoforge worktree 的当前状态：core 是否已经带上过渡改法、适配器调用点是否仍为旧签名。

三个分支的适配器已改完，逐个编译验证。

NeoForge 侧完成。所有 neoforge worktree 的 core 已带着与 core1.20.1 完全一致的过渡改动（`IMessage<T>` 无界、34/34 消息已迁移），因此本次只改适配器调用点。

## 修改的分支

|分支|修改内容|编译|
|---|---|---|
|1.20.2|6 处：3× `messageInstance.encode(buffer)`、3× `messageInstance.handle(context::enqueueWork, netContext)`|BUILD SUCCESSFUL|
|1.20.4|2 处：`this.message.encode(buffer)`、`message.handle(Runnable::run, netContext)`|BUILD SUCCESSFUL|
|1.21.1neoforge|2 处：`this.message.encode(buffer)`、`message.handle(context::enqueueWork, netContext)`|BUILD SUCCESSFUL|

## Cherry-pick 表

|分支|是否需要改|复用来源|
|---|---|---|
|1.20.2|已改|独立（6 处调用，文件结构与后续版本不同）|
|1.20.4|已改|独立（`Runnable::run` 与 1.20.2 不同）|
|1.21.1neoforge|已改|该组首个版本|
|1.21.4neoforge|不改|复用 1.21.1neoforge（文件逐字节相同）|
|1.21.6neoforge|不改|复用 1.21.1neoforge（文件逐字节相同）|
|1.21.10neoforge|不改|复用 1.21.1neoforge（`git apply --check` 通过）|
|1.21.11neoforge|不改|复用 1.21.1neoforge（`ResourceLocation`→`Identifier` 在调用点之外，补丁可干净应用）|
|26.1–26.3|暂不处理|与 1.21.11 相同，按前述约定跳过|

1.21.1neoforge 的补丁已用 `git apply --check` 在 1.21.4/1.21.6/1.21.10/1.21.11 四个 worktree 上验证，均可干净应用。

三个分支均为未提交状态，等你 review。
