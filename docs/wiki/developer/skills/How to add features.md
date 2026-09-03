[English](#English)

# 如何添加功能
> wiki 版本：`0.0.8`.0

了解需求如何确定并最终落地；从首次添加功能，到照葫芦画瓢。

## 🆕首次添加功能

### 🗂️目录结构

添加功能首先要考虑的是：**代码写在哪，Java 类起什么名字，这背后包含了对这个功能的理解**
- 🤔这个功能属于哪一类？可参考[常见目录结构](https://github.com/XColorful/Custom-Gun-Continued/wiki/Directory-structure#%EF%B8%8F常见目录结构)
- 🤔是否仅客户端需要？放在 _./client/_ 下隔离[客户端与服务端](https://github.com/XColorful/Custom-Gun-Continued/wiki/Common-mechanism#%E2%80%8D客户端与服务端%EF%B8%8F)逻辑
- 🤔是否需要注册或初始化？（注册项补充在 _./init/_）

对常见的类别，如添加物品/实体/方块：
- 原版内容类别 -> [常见目录结构](https://github.com/XColorful/Custom-Gun-Continued/wiki/Directory-structure#%EF%B8%8F常见目录结构) -> _./item/_、_./entity/_、_./block/_
- 🤔需要注册/初始化？-> 添加注册项（_./init/_）
- 需要实现的功能 -> 直接属于物品/实体/方块本身 -> 写在类里

而有些复杂内容的类别不清晰，例如“添加酷炫效果”：
- 🤔什么效果？-> 粒子效果 -> 起名为`xxxParticle.java`（xxx粒子） -> 添加粒子类别（_./particle/_）
- 🤔需要注册/初始化？-> 添加注册项（_./init/_）
- 🤔还缺什么？-> 专门的逻辑来添加效果 -> 起名为`xxxSpawner.java`（xxx生成器）
- 🤔需要注册/初始化？-> 添加初始化逻辑（_./init/_）

> 强烈建议**先拆解并明确需求**🤯，若思路不清晰，后续将一团糟😵‍💫

### 🚪寻找调用入口

确定包目录，明确其功能后接着思考：**这个功能怎么触发**？

常见的入口有：
- 原版注册项（如物品/实体/方块）：通常为其父类方法，如`onUseXXX`、`tick`
- 游戏行为：通常寻找对应的事件并监听，以实现在特定逻辑下触发
- 手动判定触发：在满足某种状态的时候触发，通常需要挂载在服务端 tick 事件来更新状态
- 游戏启动时的一次性注册：模组主类的初始化逻辑

> 如果调用链路复杂，可以**拆分触发处和逻辑实现**，分别放在不同目录下

### 📦确定可用上下文

确定功能触发的入口之后，就能清晰地看到当前可用内容👀
- 直接上下文：函数参数列表，可能包含当前玩家/物品/方块/维度等
- Minecraft 全局变量（如客户端有`Minecraft.getInstance()`）
- 模组自行维护的可用内容

同时注意每次调用时的差异：
- 每次调用时都能获取到以上内容吗？
- 不同时刻下获取到的值都相同吗？
- 是否要区分[客户端与服务端](https://github.com/XColorful/Custom-Gun-Continued/wiki/Common-mechanism#%E2%80%8D客户端与服务端%EF%B8%8F)逻辑？

在充分了解具体情况后，就能**对“能实现什么”做一个较为现实的设想**✅，避免脱离实际🙅

### ✍️留下说明

完成功能后，**请留下注释说明**，养成写文档的习惯✍️✍️✍️：
- 这段代码用于什么功能？为什么这么设计？
- 已经完成代码并测试正常？
- 以后还要添加功能？写个 _TODO_ 注释
- 是否遇到了奇怪的问题没能解决？

请试想以下情况😅：
- 🤨这段代码是做什么用的？我当时为什么这么写？
- 😨崩溃日志提示在这里，当时测试过吗？
- 😥这段代码目前算是完成了吗？我记得哪里还有没做完的？
- 🫩过了几周，还记得那个奇怪的问题吗？当时怎么触发的？

## 🔄后续更新

### 📄查阅文档定位

继续添加某个功能时：
- 😏项目的“经验老人”熟悉项目，他们知道在哪写、怎么写👌
- 🥺而后来者就不那么幸运了……

**先查阅项目已有的文档**，可以从中获得一些帮助：
- 如果没有文档，还得再研究一遍😫
- 🥰还是不写文档，那么双倍留给下一位😈

> 😡为什么不写文档？😡为什么不写文档？😡为什么不写文档？

> 😘写文档！☺️写文档！🥰写文档！

### 😋模仿已有实现

如果项目内已经有实现可参考，那么就不再是白手起家，这提示我们：
- 尽可能在第一次实现的时候就弄清楚，逻辑清晰
- 既让之后能快速了解，也减少了开发者共同的心智负担

当已有实现能**写得足够简单、甚至高度模板化**时：
- 对于后续更新更有把握
- 不容易偏离预期的方向
- 不需再次研究相同问题

> 如果某个内容已经被你研究到能模板复制，享受`Ctrl + C` `Ctrl + V`带来的便利吧😇

# English
> wiki version: `0.0.8`.0

Learn how to clarify requirements and implement them; from adding a feature for the first time, to reusing existing patterns.

## 🆕Adding features for the first time

### 🗂️Directory structure

The first thing to consider when adding a feature is: **where to put the code and what to name the Java class, which reflects your understanding of the feature**.
- 🤔What category does this feature belong to? Refer to [Common directory structure](https://github.com/XColorful/Custom-Gun-Continued/wiki/Directory-structure#%EF%B8%8Fcommon-directory-structure)
- 🤔Is it client-side only? Place it under _./client/_ to separate [Client and server](https://github.com/XColorful/Custom-Gun-Continued/wiki/Common-mechanism#%E2%80%8Dclient-and-server%EF%B8%8F) logic.
- 🤔 Does it need registration or initialization? (Add registration entries in _./init/_.)

For common categories, such as adding items/entities/blocks:
- Vanilla content category -> [Common directory structure](https://github.com/XColorful/Custom-Gun-Continued/wiki/Directory-structure#%EF%B8%8Fcommon-directory-structure) -> _./item/_, _./entity/_, _./block/_
- 🤔Needs registration/initialization? -> Add registration entries (_./init/_)
- The feature to be implemented -> Belongs directly to the item/entity/block -> Write inside its class

For complex features without clear categories, such as "adding a cool effect":
- 🤔What kind of effect? -> Particle effect -> Name it `xxxParticle.java` -> Add a particle type (_./particle/_)
- 🤔Needs registration/initialization? -> Add registration entries (_./init/_)
- 🤔 What else is missing? -> Dedicated logic to add the effect -> Name it `xxxSpawner.java`
- 🤔 Needs registration/initialization? -> Add initialization logic (_./init/_)

> Highly recommend to **break down and clarify requirements first** 🤯. Unclear logic will lead to a complete mess later 😵‍💫.

### 🚪Finding the entry point

After deciding on the package directory and clarifying the functionality, think next: **how is this feature triggered**?

Common entry points include:
- Vanilla registered items (e.g., items/entities/blocks): Usually methods in superclasses, like `onUseXXX` or `tick`.
- Game behaviors: Usually find the corresponding event and listen to it, thus trigger logic at specific moments.
- Manual condition checks: Trigger when certain states are met (typically attached to server tick events to update states).
- One-time registration on startup: Initialization logic in the mod main class.

> If the call chain is complex, **separate the trigger entry point from the logic implementation** and place them in separate directories.

### 📦Determining available context

Once the entry point is identified, you can clearly see what is currently available👀:
- Direct context: Method arguments, which may include the current player, item, block, level, etc.
- Minecraft global variables (e.g., `Minecraft.getInstance()` on the client side).
- Contents maintained by the mod itself.

Also pay attention to differences between calls:
- Is all of the above available every time it is called?
- Are the values the same at different times?
- Do you need to distinguish between [Client and server](https://github.com/XColorful/Custom-Gun-Continued/wiki/Common-mechanism#%E2%80%8Dclient-and-server%EF%B8%8F) logic?

With a full understanding of the context, you can **make a more realistic expectations of what can be achieved** ✅, avoiding impractical designs 🙅.

### ✍️Leaving notes

After completing a feature, **please leave comments explaining it** and build the habit of writing documentation✍️✍️✍️:
- What is this code for? Why was it designed this way?
- Is the code complete and tested successfully?
- Planning to add more features later? Write a _TODO_ comment.
- Encountered unresolved or weird issues?

Imagine the following scenarios 😅:
- 🤨What does this code do? Why did I write it this way?
- 😨Crash log points right here... was it ever tested?
- 😥Is this feature actually finished? Wasn't there something left undone?
- 🫩Weeks later, do you still remember that weird bug and how to reproduce it?

## 🔄Subsequent updates

### 📄Check the documentation

When continuing to add a feature:
- 😏"Veterans of the project" know where and how to write the code👌.
- 🥺But newcomers aren't as fortunate...

**Check existing project documentation first**. It may provide some useful information:
- If there's nothing, you will have to research it again😫.
- 🥰Still do not write documentation, and double the pain for the next one😈.

> 😡Why not write docs? 😡Why not write docs? 😡Why not write docs?

> 😘Write docs! ☺️Write docs! 🥰Write docs!

### 😋Following existing implementations

When reference implementations already exist in the project, you don't have to start from scratch. This reminds us:
- Make things clear and clean during the initial implementation.
- Not only make it easier to understand later, but also reduce the shared mental burden for developers

When an existing implementation is **simple enough or well-templated**:
- Later updates become much more predictable.
- It is less likely to drift from intended design patterns.
- No need to re-investigate the same problem again.

> Once a pattern is refined into a reusable template, enjoy the convenience of `Ctrl + C` and `Ctrl + V`😇