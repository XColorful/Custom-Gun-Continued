[English](#English)

# 模组开发说明
> wiki 版本：`0.0.8`.0

## 开发原理

### 跨版本架构

#### 平台抽象层
> 常见于绝大多数支持多版本/多平台的模组，较为通用

建立平台抽象层必要条件：
1. 隔离平台环境：使用多个 Gradle 子项目
> 特征：_./src/_ 不直接位于源码仓库根目录，而是采用如 _./core/src/_、_./common/src/_、_./forge/src/_ 等目录结构
2. 隔离平台接口：通过 Java interface 或 Mixin 注入等方式实现

#### 跨版本统一
> 常见的模组开发策略通常只需构建平台抽象层即可；但若需要兼顾旧版本，则有必要**在此基础上更进一步**实现跨版本统一，从而**大幅降低同步更新的开发成本**

针对外部差异接口，建立统一的内部接口：
- 将“不同版本接口差异”转化为“针对该环境下的一次性适配”
- 对“具体适配实现”进行内部封装，或下沉至[平台抽象层](#平台抽象层)（外部封装）实现
> 本模组的内部封装的经典手法为：由 _./utils_ 下的类对内部其他模块提供**跨版本统一的接口**，并在工具类内部针对不同版本完成具体适配

### 开发流程
> 后 ---> 1.20.1 ---> 1.21.1 ---> 新版本 ---> 前

以下为本模组 1.20.1 主分支向前（新版本）实现跨版本同步的流程：
1. 基于主分支新建功能分支
2. 在该分支完成内容开发
3. 将更新内容合并至主分支
4. 将主分支逐个合并到高版本分支：
	- 当出现**新的版本差异**时，进入跨版本同步阶段：
		- 在该高版本分支上完成适配封装，暂不提交（不 commit）
		- 在主分支构造对应的版本差异，使其向后同步至主分支
		- 将主分支重新合并至高版本分支，使其向前同步至高版本
			- 此时完成对首个出现差异版本的适配
			- 将该适配通过拣选（Cherry-pick）或合并方式向前**同步/复用至更高版本分支**
5. 解决完所有版本差异后，完成跨版本同步更新

该流程的价值在于以下事实：
- 很多版本差异是可以通过一次性封装来永久解决的，其他模组也可以直接复用
- 熟悉 Minecraft 不同版本的差异，使得能够针对经常变动的地方，进行提前预测和防御
- 当一个版本差异必须每次都解决时，可能留有解决方式或提示，从而不需要每次重新学习
> 如“查找替换” _import `ResourceLocation`_ 为 _import `Identifier`_、使用 _var resourceLocation_ 而不是 _ResourceLocation resourceLocation_、提前查询高版本写法并附上同行注释
- 当向后构造版本差异时，能够自觉使用
> 如本模组 1.20.1 分支在读取 NBT 时手动模拟`CustomData`

目前跳过新建分支（前两步）的情况：
- 当前正处于新内容分支的跨版本同步阶段，直接提交至主分支并附带`#PR号`
- 仅需进行轻量修复（如文档勘误、代码格式与规范调整）

### 开发路线
> 以下规范仅适用于本模组，亦可供其他模组开发参考

#### 一般原则

本模组名“自定义枪械永续”既是模组目标，也是对该原则的概括：
- 自定义：设计时应考虑或留有自定义扩展性
- 枪械专精：提供跟枪械模组相关的接口，便于直接使用
- 可持续性：跨版本统一的模组不应该依赖一个不跨版本的模组，否则将导致无法延续

#### 并行开发

有利于并行开发的方式：
- 解耦程序与美术：两者并行开发，更新进度互不干扰
- 拆分可选内容：将非核心内容外置，分摊至多模组并行维护
- 关注“可并行性”：“有没有并行开发”是执行层面的问题，而架构上“能不能并行开发”才是设计层面的关键
> 若本应解耦的内容拖累了其他模块的开发，应视为设计上的缺陷

将功能拆分为多模组并行开发的优势：
- 更好地契合不同开发者的更新节奏
- 避免因主模组更新缓慢导致关联功能迟迟无法上线
- 开发内容不集中在主模组，保持结构精简的同时兼顾扩展性：
	- 避免制造“精英准入”的心理门槛
	- 便于更多人借助 AI Agent 参与生态的并行构建

#### 扩展模组

本模组将原本内置的一些功能外置（如[自定义枪械永续配置扩展](https://github.com/XColorful/CGC-Config-Addon)）：
- 如果某个功能本身就需安装额外模组，那么它就不应被视为由主模组直接提供的核心功能
- 主模组不直接集成的功能，即意味着它是一个默认不包含的**可选功能**

关于选择“内置联动”还是“拆为扩展”，两者各有优劣：
- 内置模组联动：
	- 社区集中贡献代码，便于进行“官方认证”与质量把控
	- 开发者近距离实时掌握模组动态，在熟悉的地方添加代码
	- 维护者（Maintainer）开发路径短，操作直观、简单
- 拆成扩展模组：
	- 满足多样化的扩展需求，不强制所有玩家安装冗余功能
	- 各扩展可按各自节奏独立更新，无需经由主模组集中审核
	- [并行开发](#并行开发)，避免增加主模组维护压力

#### 兼容模组

内置模组兼容的好处：
- 帮玩家提前规避潜在的冲突与兼容性问题
- 开箱即用，无需安装兼容补丁

但本模组默认不内置对其他模组的兼容适配，原因如下：
- 更彻底地[并行开发](#并行开发)
- 主模组不因此增加维护负担，保持简洁

设计意图与示例见[模组兼容框架](https://github.com/XColorful/Custom-Gun-Continued/wiki/Mod-compat-framework)

# English
> wiki version: `0.0.8`.0

## Development fundamentals

### Cross-version architecture

#### Platform abstraction layer
> Common in the vast majority of multi-version and multi-platform mods, serving as a general approach

Prerequisites for establishing a platform abstraction layer:
1. Isolate platform environments: Use multiple Gradle subprojects
> Characteristic: _./src/_ is not located directly at the root of the source repository; instead, directory structures such as _./core/src/_, _./common/src/_, and _./forge/src/_ are used
2. Isolate platform interfaces: Implement via Java interfaces, Mixin injections, or other mechanisms

#### Cross-version unification
> Standard mod development strategies usually only require building a platform abstraction layer. However, to support legacy versions, it is necessary to **take it a step further** on this basis to achieve cross-version unification, thereby **significantly reducing the development cost of synchronized updates**

Establish unified internal interfaces for external API differences:
- Transform "API differences across versions" into "a one-time adaptation for that specific environment"
- Encapsulate the "concrete adaptation implementations" internally, or push them down to the [Platform abstraction layer](Platform-abstraction-layer) (external encapsulation)
> A classic approach to internal encapsulation in this mod is to provide **cross-version unified interfaces** to other internal modules via classes under _./utils_, while handling specific adaptations for different versions inside those utility classes

### Development workflow
> Older ---> 1.20.1 ---> 1.21.1 ---> Newer ---> Latest

The following is the workflow for forward (newer versions) cross-version synchronization from the main 1.20.1 branch of this mod:
1. Create a feature branch based on the main branch
2. Complete feature development on that branch
3. Merge the updated content into the main branch
4. Merge the main branch into higher-version branches sequentially:
	- When a **new version difference** occurs, enter the cross-version synchronization phase:
		- Complete the adaptation encapsulation on that higher-version branch without committing
		- Construct the corresponding version difference on the main branch to sync it backward to the main branch
		- Re-merge the main branch into the higher-version branch to sync it forward to the higher version
			- At this point, adaptation for the first version with the difference is complete
			- Sync or reuse this adaptation **to even higher version branches** via cherry-picking or merging
5. Resolve all version differences to complete the cross-version sync update

The value of this workflow lies in the following facts:
- Many version differences can be permanently resolved through one-time encapsulation, and other mods can reuse them directly
- Familiarity with differences across Minecraft versions allows predicting and defensively coding for frequently changing areas in advance
- When a version difference must be resolved every time, solution notes or hints can be left to avoid re-learning each time
> Examples include "Replace in files..." _import `ResourceLocation`_ with _import `Identifier`_, using _var resourceLocation_ instead of _ResourceLocation resourceLocation_, and querying higher-version syntax in advance with inline comments
- Consciously apply these patterns when constructing backward version differences
> For example, manually simulating `CustomData` when reading NBT on the 1.20.1 branch of this mod

Cases where creating a new branch (the first two steps) is currently skipped:
- Currently in the cross-version sync phase of a feature branch; commit directly to the main branch with the `#PR number`
- Only lightweight fixes are required (such as documentation errata, code formatting, and style adjustments)

### Development roadmap
> The following guidelines apply specifically to this mod, but can also serve as a reference for other mod development

#### General principles

The mod name "Custom Gun Continued" is both the goal of the mod and a summary of these principles:
- Customization: Designs should consider or leave space for custom extensibility
- Gun Specialization: Provide gun-mod-related interfaces for ease of direct use
- Sustainability: A cross-version unified mod should not depend on a non-cross-version mod, otherwise it will fail to continue

#### Parallel development

Practices beneficial to parallel development:
- Decouple code and assets: Develop both in parallel so update progress does not interfere with each other
- Split optional content: Externalize non-core content and distribute it across multiple mods for parallel maintenance
- Focus on "parallelizability": Whether parallel development is actually happening is an execution issue, whereas whether the architecture allows parallel development is the key design consideration
> If content that should be decoupled drags down the development of other modules, it should be considered a design flaw

Advantages of splitting features into multiple mods for parallel development:
- Better aligns with the update cadences of different developers
- Avoids delays in launching related features due to slow updates of the main mod
- Distributes development away from the main mod, keeping the structure concise while ensuring extensibility:
    - Avoids creating a psychological barrier of "elite-only access"
    - Facilitates broader community participation in building the ecosystem using AI Agents in parallel

#### Addon mods

This mod externalizes some previously built-in features (e.g., [CGC Config Addon](https://github.com/XColorful/CGC-Config-Addon)):
- If a feature requires installing an extra mod in the first place, it should not be considered a core feature directly provided by the main mod
- Features not directly integrated into the main mod are treated as **optional features** that are excluded by default

Both "built-in integration" and "splitting into addons" have pros and cons:
- Built-in Mod Integration:
    - Centralizes community code contributions, facilitating "official certification" and quality control
	- Allows developers to track mod dynamics closely and add code in familiar places
	- Shortens the development path for maintainers, making operations intuitive and simple
- Splitting into Addon Mods:
	- Satisfies diverse extension needs without forcing all players to install redundant features
    - Allows each addon to update independently at its own pace without centralized review by the main mod
    - Enables [Parallel development](#Parallel-development), avoiding additional maintenance pressure on the main mod

#### Compatible mods

Benefits of built-in mod compatibility:
- Helps players avoid potential conflicts and compatibility issues in advance
- Works out of the box without installing compatibility patches

However, this mod does not include built-in compatibility adaptations for other mods by default for the following reasons:
- Achieves more thorough [Parallel development](#Parallel-development)
- Prevents the main mod from increasing its maintenance burden, keeping it concise

See the [Mod compatibility framework](https://github.com/XColorful/Custom-Gun-Continued/wiki/Mod-compat-framework#English) for design intent and examples.