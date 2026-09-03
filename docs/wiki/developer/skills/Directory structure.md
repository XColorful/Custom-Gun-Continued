[English](#English)

# 目录结构
> wiki 版本：`0.0.8`.1

模组中常见的目录结构。

### ❓为什么要了解目录结构？

第一次开发模组时，不知道怎么组织代码文件，不妨使用一个简单的标准开始。

除此之外还有以下好处：
- 当浏览其他模组源码时，能够**快速定位代码所在位置**
- 常见的模组内容就这么些类别，自然形成了一种通用语言
- 养成给代码分类，整理文件的习惯，有助于厘清思路，更好地管理模组👍
> ⚠️ 注意：若不对代码进行分类，将难以查找某个代码所在位置，逻辑乱成一团💩

### 🗂️常见目录结构

不用担心目录结构太长🥲🙅，实际上**一个模组可能都用不到几个**😮‍💨😘
- 例如只添加建筑方块的模组，可能就没有 _./command/_（指令）、_./entity/_ （实体）
- 如果模组没有添加 GUI（图形界面）、可能就不会用 _./gui/_、_./menu/_、_./inventory/_ 等目录
- 有些目录名称**可能是重复的，只是开发者喜欢不同的名称**，例如 _./gui/_、_./menu/_、_./inventory/_ 都可能用来放置 GUI 相关代码
> 几乎很少有模组会涵盖全方面的内容
```
└───src/main/java/**/
    ├───advancements // 进度
    ├───api          // API 接口
    ├───block        // 方块
    │   └───entity   // 方块实体
    ├───client       // 仅客户端逻辑
    │   ├───renderer // 特殊渲染效果
    │   └───screen   // GUI 界面
    ├───command      // 指令
    ├───common
    ├───compat       // 内置模组联动
    ├───config       // 配置项
    ├───enchantment  // 附魔
    ├───entity       // 实体
    ├───event        // 模组事件
    ├───gui
    ├───init         // 注册项/初始化
    ├───inventory
    ├───item         // 物品
    ├───loot         // 战利品
    ├───menu
    ├───mixin        // Mixin 注入
    ├───network      // 网络通信
    ├───particle     // 粒子效果
    ├───potion       // 药水
    ├───recipe       // 原版配方
    ├───resource
    ├───server       // 仅服务端逻辑
    ├───sound
    ├───util
    │
    └───ModMainClass.java // 模组主类
```

- 这只是一个常见的结构，并不代表必须遵守
- 具体开发时可以根据需要添加额外的目录结构

当浏览模组的源码时，通常关注以下几个目录：

#### 玩家能直接感受到的

- _./block/_：方块
	- _./block/entity/_：方块实体
- _./client/_：客户端逻辑
	- _./client/renderer/_：特殊渲染效果
- _./entity/_：实体
- _./item/_：物品
- _./gui/_、_./menu/_、_./inventory/_：背包界面/图形界面

#### 面向服主的

- _./command/_：指令
- _./loot/_：战利品

#### 只有开发者会查看的

- _./api/_：查阅模组提供的 API 接口
- _./client/_：客户端逻辑
- _./compat/_：查询有哪些内置的模组联动
- _./config/_：模组的配置项
- _./init/_：模组的注册项/初始化逻辑，可以以此了解某类东西的注册方式
- _./mixin/_：模组使用 Mixin 的地方，通常会引发模组间冲突
- _./network/_：模组的网络通信协议
- _./util/_：工具类，可用于重复利用一些经常使用的代码

# English
> wiki version: `0.0.8`.1

Common directory structures in mods.

### ❓Why learn about directory structure?

When developing a mod for the first time, you may not know how to organize your code files. It's a good idea to start with a simple standard.

There are also a few other benefits:
- When looking through other mods' source code, you can **quickly find where the code is**
- There are only so many common types of mod content, so they naturally form a kind of common language
- Getting into the habit of organizing your code and sorting files into categories can help clear your mind and make your mod easier to manage👍
> ⚠️ Note: If you don't organize your code into categories, it can become hard to find where something is, and the whole project can turn into a mess💩

### 🗂️Common directory structure

Don't worry about how long this directory structure looks🥲🙅. In reality, **a mod may only need a few of these**😮‍💨😘
- For example, a mod that only adds building blocks may not have _./command/_ (commands) or _./entity/_ (entities)
- If a mod doesn't add a GUI, it may not use directories like _./gui/_, _./menu/_, or _./inventory/_
- Some directory names **may overlap simply because developers prefer different names**. For example, _./gui/_, _./menu/_, and _./inventory/_ may all be used for GUI-related code
> Very few mods cover every area
```
└───src/main/java/**/
    ├───advancements // Advancements
    ├───api          // API interfaces
    ├───block        // Blocks
    │   └───entity   // Block entities
    ├───client       // Client-only logic
    │   ├───renderer // Special rendering effects
    │   └───screen   // GUI screens
    ├───command      // Commands
    ├───common
    ├───compat       // Built-in mod compatibility
    ├───config       // Configuration
    ├───enchantment  // Enchantments
    ├───entity       // Entities
    ├───event        // Mod events
    ├───gui
    ├───init         // Registration/initialization
    ├───inventory
    ├───item         // Items
    ├───loot         // Loot
    ├───menu
    ├───mixin        // Mixin injection
    ├───network      // Network communication
    ├───particle     // Particle effects
    ├───potion       // Potions
    ├───recipe       // Vanilla recipes
    ├───resource
    ├───server       // Server-only logic
    ├───sound
    ├───util
    │
    └───ModMainClass.java // Main mod class
```

- This is just a common structure and is not something you have to follow
- You can add extra directories based on what you need when developing your mod

When looking through a mod's source code, you will usually want to pay attention to these directories:

#### Things players can directly experience

- _./block/_：Blocks
    - _./block/entity/_：Block entities
- _./client/_：Client logic
    - _./client/renderer/_：Special rendering effects
- _./entity/_：Entities
- _./item/_：Items
- _./gui/_, _./menu/_, _./inventory/_：Inventory screens / GUIs

#### Things for server owners

- _./command/_：Commands
- _./loot/_：Loot

#### Things only developers look at

- _./api/_：Look at the API provided by the mod
- _./client/_：Client logic
- _./compat/_：Check for built-in mod compatibility
- _./config/_：Mod configuration
- _./init/_：Mod registration / initialization logic. You can use it to learn how a certain type of thing is registered
- _./mixin/_：Where the mod uses Mixin, which may cause conflicts between mods
- _./network/_：The mod's network communication protocol
- _./util/_：Utility classes for reusing commonly used code